/*
 * Copyright 2022 Centre for Computational Geography.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leeds.ccg.r3d.io.d;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.r3d.d.entities.TriangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_TriangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_VectorDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.light.V3D_VDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.light.V3D_VLineDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.light.V3D_VTriangleDouble;

/**
 * For reading STL Files. An STL file is effectively a file of triangles. The
 * triangles can have a short attribute and a normal vector. They may not have
 * either. The normal can be computed by the right hand rule when it is not
 * supplied.
 *
 * If the points of the triangles representing a closed surface are not given in
 * the right order, then the computed normal direction can be opposite to what
 * is likely to be wanted. In order to correct for this and to produce
 * statistics that may be of interest, topology can be considered. This comes at
 * a computational expense.
 *
 * @author Andy Turner
 */
public class STL_ReaderDouble {

    /**
     * The set of triangles.
     */
    public ArrayList<TriangleDouble> triangles;

    /**
     * A switch set to true if topology is to be assessed.
     */
    public boolean assessTopology;

    /**
     * Topology. (What triangles have shared points)
     */
    //public HashMap<V3D_V, Set<Triangle>> points;
    /**
     * Count of triangles with shared points.
     */
    public HashMap<V3D_VDouble, Integer> pointCounts;

    /**
     * Topology. (What triangles have shared edges). For simple closed surfaces,
     * ones that bound volumes, each triangle edge of the surface must be
     * matched with exactly one other triangle edge. If there are triangles with
     * an edge not shared with any other triangles, then this triangle is at the
     * edge of an unclosed surface. If more than two triangles share an edge,
     * then either a surface is self intersecting or is folded. Surfaces may
     * meet along several edges and indeed along (parts of) faces of triangles.
     * Where surfaces are folded and meet along faces there may not be any
     * shared points or edges.
     */
    //public HashMap<V3D_VLine, Set<Triangle>> edges;
    /**
     * Count of triangles with shared edges.
     */
    public HashMap<V3D_VLineDouble, Integer> edgeCounts;

    /**
     * Stats
     */
    public Stats stats;

    /**
     * Create a new instance.
     *
     * @param assessTopology If this is set to true, then {@link #points},
     * {@link #pointCounts}, {@link #edge} and {@link #edgeCounts} are
     * initialised and topology will be assessed.
     */
    public STL_ReaderDouble(boolean assessTopology) {
        triangles = new ArrayList<>();
        this.assessTopology = assessTopology;
        if (assessTopology) {
            //points = new HashMap<>();
            pointCounts = new HashMap<>();
            //edges = new HashMap<>();
            edgeCounts = new HashMap<>();
        }
    }

    public static void main(String[] args) {
        Path p = Paths.get("data", "Utah_teapot_(solid).stl");
        try {
            STL_ReaderDouble s = new STL_ReaderDouble(true);
            s.readBinary(p, V3D_VectorDouble.ZERO, false);
            //System.out.println(ts.get(0));
            System.out.println("minx=" + s.stats.minx);
            System.out.println("maxx=" + s.stats.maxx);
            System.out.println("miny=" + s.stats.miny);
            System.out.println("maxy=" + s.stats.maxy);
            System.out.println("minz=" + s.stats.minz);
            System.out.println("maxz=" + s.stats.maxz);
        } catch (IOException ex) {
            Logger.getLogger(STL_ReaderDouble.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Read the binary STL file at the given Path following the available file
     * specification: https://en.wikipedia.org/wiki/STL_(file_format)#Binary_STL
     * Everything is assumed to be little endian. Report the min and max of the
     * x, y, and z values.
     *
     * @param p The file to read.
     * @param offset The common offset.
     * @param initNormal If this is true, then the normal read from the file is 
     * discounted and the normal is calculated from the triangle corner point
     * vectors. 
     * @return An ArrayList of triangles read from the file.
     * @throws IOException
     */
    public void readBinary(Path p, V3D_VectorDouble offset, boolean initNormal)
            throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(
                p.toFile()));
        // Skip 80 bytes
        dis.read(new byte[80]);
        int nTriangles = Integer.reverseBytes(dis.readInt());
        System.out.println("Reading " + nTriangles + " triangles.");
        // read triangles
        float x, y, z;
        // Read the first triangle
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        V3D_VDouble n = new V3D_VDouble(x, y, z);
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        stats = new Stats(x, y, z);
        V3D_VDouble pv = new V3D_VDouble(x, y, z);
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        stats.update(x, y, z);
        V3D_VDouble qv = new V3D_VDouble(x, y, z);
        x = readFloat(dis);
        y = readFloat(dis);
        z = readFloat(dis);
        stats.update(x, y, z);
        V3D_VDouble rv = new V3D_VDouble(x, y, z);
        V3D_VLineDouble pq = new V3D_VLineDouble(pv, qv);
        V3D_VLineDouble qr = new V3D_VLineDouble(qv, rv);
        V3D_VLineDouble rp = new V3D_VLineDouble(rv, pv);
        short attribute = Short.reverseBytes(dis.readShort());
        process(offset, pv, qv, rv, pq, qr, rp, n, attribute, initNormal);
        int i = 1;
        int nTriangles1PC = nTriangles / 100;
        if (nTriangles1PC < 1) {
            nTriangles1PC = 1;
        }
        while (dis.available() > 0) {
            try {
            if (i % nTriangles1PC == 0) {
                System.out.println("Reading " + i + " out of " + nTriangles + " triangles.");
            }
            n = new V3D_VDouble(readFloat(dis), readFloat(dis), readFloat(dis));
            // Turn n into a unit normal.
            n = n.getUnitVector();
            x = readFloat(dis);
            y = readFloat(dis);
            z = readFloat(dis);
            stats.update(x, y, z);
            pv = new V3D_VDouble(x, y, z);
            x = readFloat(dis);
            y = readFloat(dis);
            z = readFloat(dis);
            stats.update(x, y, z);
            qv = new V3D_VDouble(x, y, z);
            x = readFloat(dis);
            y = readFloat(dis);
            z = readFloat(dis);
            rv = new V3D_VDouble(x, y, z);
            stats.update(x, y, z);
            attribute = Short.reverseBytes(dis.readShort());
            pq = new V3D_VLineDouble(pv, qv);
            qr = new V3D_VLineDouble(qv, rv);
            rp = new V3D_VLineDouble(rv, pv);
            process(offset, pv, qv, rv, pq, qr, rp, n, attribute, initNormal);
            i++;
            } catch (EOFException ex) {
                break;
            }
        }
        if (assessTopology) {
            // Topology checks and reporting.
            System.out.println(pointCounts.size() + " unique points.");
            System.out.println(edgeCounts.size() + " unique edges.");
            int pmax = 0;
            int pmin = pointCounts.entrySet().iterator().next().getValue();
            for (var pc : pointCounts.keySet()) {
                pmax = Math.max(pmax, pointCounts.get(pc));
                pmin = Math.min(pmin, pointCounts.get(pc));
            }
            System.out.println(pmax + " = maximum number of triangles containing any unique point.");
            System.out.println(pmin + " = minimum number of triangles containing any unique point.");
            int emax = 0;
            int emin = edgeCounts.entrySet().iterator().next().getValue();
            for (var ec : edgeCounts.keySet()) {
                emax = Math.max(emax, edgeCounts.get(ec));
                emin = Math.min(emin, edgeCounts.get(ec));
            }
            System.out.println(emax + " = maximum number of triangles sharing any edge.");
            System.out.println(emin + " = minimum number of triangles sharing any edge.");
            if (emax == 2 && emin == 2) {
                System.out.println("Each edge is only shared between two triangles.");
                if (edgeCounts.size() == pointCounts.size() * 2 + 2) {
                    System.out.println("There is a single unfolded closed surface.");
                }
            }
        }
    }

    private void process(V3D_VectorDouble offset, V3D_VDouble pv, 
            V3D_VDouble qv, V3D_VDouble rv, V3D_VLineDouble pq, 
            V3D_VLineDouble qr, V3D_VLineDouble rp, V3D_VDouble n, 
            short attribute, boolean initNormal) {
        V3D_VTriangleDouble vt = new V3D_VTriangleDouble(pq, qr, rp);
        if (n.isZero() || initNormal) {
            n = vt.getNormal().getUnitVector();
        }
        TriangleDouble t = new TriangleDouble(
                new V3D_TriangleDouble(offset, vt), n, attribute);
        triangles.add(t);
        if (assessTopology) {
//        Generic_Collections.addToMap(points, pv, t);
//        Generic_Collections.addToMap(points, qv, t);
//        Generic_Collections.addToMap(points, rv, t);
            Generic_Collections.addToCount(pointCounts, pv, 1);
            Generic_Collections.addToCount(pointCounts, qv, 1);
            Generic_Collections.addToCount(pointCounts, rv, 1);
//        Generic_Collections.addToMap(edges, pq, t);
//        Generic_Collections.addToMap(edges, qr, t);
//        Generic_Collections.addToMap(edges, rp, t);
            Generic_Collections.addToCount(edgeCounts, pq, 1);
            Generic_Collections.addToCount(edgeCounts, qr, 1);
            Generic_Collections.addToCount(edgeCounts, rp, 1);
        }
    }

    float readFloat(DataInputStream dis) throws IOException {
        return Float.intBitsToFloat(Integer.reverseBytes(dis.readInt()));
    }

    public class Stats {

        float minx, maxx, miny, maxy, minz, maxz;

        Stats(float x, float y, float z) {
            minx = maxx = x;
            miny = maxy = y;
            minz = maxz = z;
        }

        void update(float x, float y, float z) {
            minx = Math.min(x, minx);
            maxx = Math.max(x, maxx);
            miny = Math.min(y, miny);
            maxy = Math.max(y, maxy);
            minz = Math.min(z, minz);
            maxz = Math.max(z, maxz);
        }
    }
}
