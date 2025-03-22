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
package uk.ac.leeds.ccg.r3d.d;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import uk.ac.leeds.ccg.r3d.d.entities.TetrahedronDouble;
import uk.ac.leeds.ccg.r3d.d.entities.TriangleDouble;
import uk.ac.leeds.ccg.r3d.io.d.STL_ReaderDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_EnvelopeDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_FiniteGeometryDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PointDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_RayDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_TetrahedronDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_TriangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_VectorDouble;

/**
 * A class that holds reference to visible and invisible objects.
 *
 * @author Andy Turner
 */
public class UniverseDouble {

    /**
     * Envelope
     */
    V3D_EnvelopeDouble envelope;

    /**
     * The triangles to render.
     */
    public ArrayList<TriangleDouble> triangles;

    /**
     * The tetrahedra to render.
     */
    public ArrayList<TetrahedronDouble> tetrahedra;

    /**
     * A single camera.
     */
    public CameraDouble camera;

    /**
     * Create a new instance.
     *
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public UniverseDouble(V3D_VectorDouble offset, double epsilon) {
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        /**
         * Create a AABox centred at 0,0,0
         */
        V3D_PointDouble[] points = new V3D_PointDouble[8];
        // multiplication factor
        long m = 100;
        V3D_PointDouble lbf = new V3D_PointDouble(offset, new V3D_VectorDouble(-1 * m, -1 * m, -1 * m));
        V3D_PointDouble lba = new V3D_PointDouble(offset, new V3D_VectorDouble(-1 * m, -1 * m, 1 * m));
        V3D_PointDouble ltf = new V3D_PointDouble(offset, new V3D_VectorDouble(-1 * m, 1 * m, -1 * m));
        V3D_PointDouble lta = new V3D_PointDouble(offset, new V3D_VectorDouble(-1 * m, 1 * m, 1 * m));
        V3D_PointDouble rbf = new V3D_PointDouble(offset, new V3D_VectorDouble(1 * m, -1 * m, -1 * m));
        V3D_PointDouble rba = new V3D_PointDouble(offset, new V3D_VectorDouble(1 * m, -1 * m, 1 * m));
        V3D_PointDouble rtf = new V3D_PointDouble(offset, new V3D_VectorDouble(1 * m, 1 * m, -1 * m));
        V3D_PointDouble rta = new V3D_PointDouble(offset, new V3D_VectorDouble(1 * m, 1 * m, 1 * m));
        //createCubeFrom5Tetrahedra(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        //createCubeFrom6Tetrahedra(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        createCubeSurfaceFromTriangles(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        points[0] = lbf;
        points[1] = lba;
        points[2] = ltf;
        points[3] = lta;
        points[4] = rbf;
        points[5] = rba;
        points[6] = rtf;
        points[7] = rta;
        this.envelope = new V3D_EnvelopeDouble(points);
    }

    /**
     * The central Tetrahedra is regular and different to the others.
     */
    public void createCubeFrom5Tetrahedra(double epsilon,
            V3D_PointDouble lbf, V3D_PointDouble lba, V3D_PointDouble ltf, V3D_PointDouble lta,
            V3D_PointDouble rbf, V3D_PointDouble rba, V3D_PointDouble rtf, V3D_PointDouble rta) {
        V3D_TetrahedronDouble t1 = new V3D_TetrahedronDouble(lbf, ltf, lta, rtf);
        tetrahedra.add(new TetrahedronDouble(t1, Color.BLUE));
        V3D_TetrahedronDouble t2 = new V3D_TetrahedronDouble(lta, lba, lbf, rba);
        tetrahedra.add(new TetrahedronDouble(t2, Color.RED));
        V3D_TetrahedronDouble t3 = new V3D_TetrahedronDouble(lta, rta, rba, rtf);
        tetrahedra.add(new TetrahedronDouble(t3, Color.GREEN));
        V3D_TetrahedronDouble t4 = new V3D_TetrahedronDouble(lbf, rbf, rtf, rba);
        tetrahedra.add(new TetrahedronDouble(t4, Color.YELLOW));
        V3D_TetrahedronDouble t5 = new V3D_TetrahedronDouble(lbf, rba, rtf, lta);
        tetrahedra.add(new TetrahedronDouble(t5, Color.CYAN));
        // Volume check
        System.out.println("t1 volume=" + t1.getVolume(epsilon));
        System.out.println("t2 volume=" + t2.getVolume(epsilon));
        System.out.println("t3 volume=" + t3.getVolume(epsilon));
        System.out.println("t4 volume=" + t4.getVolume(epsilon));
        System.out.println("t5 volume=" + t5.getVolume(epsilon));
    }

    public void createCubeFrom6Tetrahedra(double epsilon,
            V3D_PointDouble lbf, V3D_PointDouble lba, V3D_PointDouble ltf, V3D_PointDouble lta,
            V3D_PointDouble rbf, V3D_PointDouble rba, V3D_PointDouble rtf, V3D_PointDouble rta) {
        // Half - a triangular prism
        V3D_TetrahedronDouble t1 = new V3D_TetrahedronDouble(ltf, rtf, rta, rba);
        tetrahedra.add(new TetrahedronDouble(t1, Color.BLUE));
        V3D_TetrahedronDouble t2 = new V3D_TetrahedronDouble(rtf, rbf, lbf, rba);
        tetrahedra.add(new TetrahedronDouble(t2, Color.RED));
        V3D_TetrahedronDouble t3 = new V3D_TetrahedronDouble(lbf, rtf, rba, ltf);
        tetrahedra.add(new TetrahedronDouble(t3, Color.GREEN));
        // Another Half - a triangular prism
        V3D_TetrahedronDouble t4 = new V3D_TetrahedronDouble(ltf, lbf, lba, rta);
        tetrahedra.add(new TetrahedronDouble(t4, Color.YELLOW));
        V3D_TetrahedronDouble t5 = new V3D_TetrahedronDouble(ltf, lta, rta, lba);
        tetrahedra.add(new TetrahedronDouble(t5, Color.CYAN));
        V3D_TetrahedronDouble t6 = new V3D_TetrahedronDouble(lbf, lba, rba, rta);
        tetrahedra.add(new TetrahedronDouble(t6, Color.MAGENTA));
        // Volume check
        System.out.println("t1 volume=" + t1.getVolume(epsilon));
        System.out.println("t2 volume=" + t2.getVolume(epsilon));
        System.out.println("t3 volume=" + t3.getVolume(epsilon));
        System.out.println("t4 volume=" + t4.getVolume(epsilon));
        System.out.println("t5 volume=" + t5.getVolume(epsilon));
        System.out.println("t6 volume=" + t6.getVolume(epsilon));
    }

    public void createCubeSurfaceFromTriangles(double epsilon,
            V3D_PointDouble lbf, V3D_PointDouble lba, V3D_PointDouble ltf, V3D_PointDouble lta,
            V3D_PointDouble rbf, V3D_PointDouble rba, V3D_PointDouble rtf, V3D_PointDouble rta) {
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, ltf, rtf), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, rbf, rtf), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, ltf, lta), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, lba, lta), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lba, lta, rta), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lba, rba, rta), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(rbf, rtf, rta), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(rbf, rta, rba), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(ltf, lta, rta), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(rtf, ltf, rta), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, rbf, rba), Color.WHITE));
//        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, lba, rba), Color.WHITE));
        // Coloured triangles
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, ltf, rtf), Color.BLUE));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, rbf, rtf), Color.BLUE));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, ltf, lta), Color.RED));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, lba, lta), Color.RED));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lba, lta, rta), Color.YELLOW));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lba, rba, rta), Color.YELLOW));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(rbf, rtf, rta), Color.GREEN));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(rbf, rta, rba), Color.GREEN));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(ltf, lta, rta), Color.ORANGE));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(rtf, ltf, rta), Color.ORANGE));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, rbf, rba), Color.PINK));
        triangles.add(new TriangleDouble(new V3D_TriangleDouble(lbf, lba, rba), Color.PINK));

    }

    /**
     * Create a new instance.
     *
     * @param path The path of the STL binary file to be read.
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public UniverseDouble(Path path, V3D_VectorDouble offset, Color color,
            boolean assessTopology, double epsilon, boolean initNormal) throws IOException {
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        STL_ReaderDouble data = new STL_ReaderDouble(assessTopology);
        data.readBinary(path, offset, initNormal);
        V3D_PointDouble p = data.triangles.get(0).triangle.getPl().getP();
        double xmin = p.getX();
        double xmax = p.getX();
        double ymin = p.getY();
        double ymax = p.getY();
        double zmin = p.getZ();
        double zmax = p.getZ();
        for (TriangleDouble t : data.triangles) {
            t.baseColor = color;
            t.lightingColor = color;
            triangles.add(t);
            for (var pt : t.triangle.getPoints()) {
                double x = pt.getX();
                double y = pt.getY();
                double z = pt.getZ();
                xmin = Math.min(xmin, x);
                xmax = Math.max(xmax, x);
                ymin = Math.min(ymin, y);
                ymax = Math.max(ymax, y);
                zmin = Math.min(zmin, z);
                zmax = Math.max(zmax, z);
            }
        }
        envelope = new V3D_EnvelopeDouble(xmin, xmax, ymin, ymax, zmin, zmax);
        System.out.println(envelope.toString());
    }

    /**
     * Set the camera.
     *
     * @param camera What {@link #camera} is set to.
     */
    public void setCamera(CameraDouble camera) {
        this.camera = camera;
    }

    /**
     * This will be for updating the universe from one time to the next.
     */
    public void update() {
    }
    
    /**
     * 
     * @param x The x value at which the maximum z value is returned.
     * @param y The y value at which the maximum z value is returned.
     * @return The maximum z value for all triangles intersecting with a ray 
     * from V3D_PointDouble(x, y, envelope.getZMin()) through 
     * V3D_PointDouble(x, y, envelope.getZMax()) 
     */
    public Double getMaxZ(double x, double y, double epsilon) {
        V3D_RayDouble ray = new V3D_RayDouble(
                new V3D_PointDouble(x, y, envelope.getZMin()),
                new V3D_PointDouble(x, y, envelope.getZMax()));
        Double r = null;
        for (var t: this.triangles) {
            V3D_FiniteGeometryDouble d = t.triangle.getIntersection(ray, epsilon);
            if (d != null) {
                double zMax = d.getEnvelope().getZMax();
                if (r == null) {
                    r = zMax;
                } else {
                    r = Math.max(r, zMax);
                }
            }
        }
        return r;
    }
}
