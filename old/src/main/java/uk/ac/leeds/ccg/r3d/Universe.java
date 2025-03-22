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
package uk.ac.leeds.ccg.r3d;

import java.awt.Color;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.entities.Tetrahedron;
import uk.ac.leeds.ccg.r3d.entities.Triangle;
import uk.ac.leeds.ccg.r3d.io.STL_Reader;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Envelope;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * A class that holds reference to visible and invisible objects.
 *
 * @author Andy Turner
 */
public class Universe {

    /**
     * Envelope
     */
    V3D_Envelope envelope;

    /**
     * The triangles to render.
     */
    public ArrayList<Triangle> triangles;

    /**
     * The tetrahedra to render.
     */
    public ArrayList<Tetrahedron> tetrahedra;

    /**
     * A single camera.
     */
    public Camera camera;

    /**
     * Create a new instance.
     *
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public Universe(V3D_Vector offset, int oom, RoundingMode rm) {
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        /**
         * Create a AABox centred at 0,0,0
         */
        V3D_Point[] points = new V3D_Point[8];
        // multiplication factor
        long m = 100;
        V3D_Point lbf = new V3D_Point(offset, new V3D_Vector(-1 * m, -1 * m, -1 * m));
        V3D_Point lba = new V3D_Point(offset, new V3D_Vector(-1 * m, -1 * m, 1 * m));
        V3D_Point ltf = new V3D_Point(offset, new V3D_Vector(-1 * m, 1 * m, -1 * m));
        V3D_Point lta = new V3D_Point(offset, new V3D_Vector(-1 * m, 1 * m, 1 * m));
        V3D_Point rbf = new V3D_Point(offset, new V3D_Vector(1 * m, -1 * m, -1 * m));
        V3D_Point rba = new V3D_Point(offset, new V3D_Vector(1 * m, -1 * m, 1 * m));
        V3D_Point rtf = new V3D_Point(offset, new V3D_Vector(1 * m, 1 * m, -1 * m));
        V3D_Point rta = new V3D_Point(offset, new V3D_Vector(1 * m, 1 * m, 1 * m));
        //createCubeFrom5Tetrahedra(oom, rm, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        //createCubeFrom6Tetrahedra(oom, rm, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        createCubeSurfaceFromTriangles(oom, rm, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        points[0] = lbf;
        points[1] = lba;
        points[2] = ltf;
        points[3] = lta;
        points[4] = rbf;
        points[5] = rba;
        points[6] = rtf;
        points[7] = rta;
        this.envelope = new V3D_Envelope(oom, rm, points);
    }

    /**
     * The central Tetrahedra is regular and different to the others.
     */
    public void createCubeFrom5Tetrahedra(int oom, RoundingMode rm,
            V3D_Point lbf, V3D_Point lba, V3D_Point ltf, V3D_Point lta,
            V3D_Point rbf, V3D_Point rba, V3D_Point rtf, V3D_Point rta) {
        V3D_Tetrahedron t1 = new V3D_Tetrahedron(lbf, ltf, lta, rtf, oom, rm);
        tetrahedra.add(new Tetrahedron(t1, Color.BLUE, oom, rm));
        V3D_Tetrahedron t2 = new V3D_Tetrahedron(lta, lba, lbf, rba, oom, rm);
        tetrahedra.add(new Tetrahedron(t2, Color.RED, oom, rm));
        V3D_Tetrahedron t3 = new V3D_Tetrahedron(lta, rta, rba, rtf, oom, rm);
        tetrahedra.add(new Tetrahedron(t3, Color.GREEN, oom, rm));
        V3D_Tetrahedron t4 = new V3D_Tetrahedron(lbf, rbf, rtf, rba, oom, rm);
        tetrahedra.add(new Tetrahedron(t4, Color.YELLOW, oom, rm));
        V3D_Tetrahedron t5 = new V3D_Tetrahedron(lbf, rba, rtf, lta, oom, rm);
        tetrahedra.add(new Tetrahedron(t5, Color.CYAN, oom, rm));
        // Volume check
        System.out.println("t1 volume=" + t1.getVolume(oom, rm));
        System.out.println("t2 volume=" + t2.getVolume(oom, rm));
        System.out.println("t3 volume=" + t3.getVolume(oom, rm));
        System.out.println("t4 volume=" + t4.getVolume(oom, rm));
        System.out.println("t5 volume=" + t5.getVolume(oom, rm));
    }

    public void createCubeFrom6Tetrahedra(int oom, RoundingMode rm,
            V3D_Point lbf, V3D_Point lba, V3D_Point ltf, V3D_Point lta,
            V3D_Point rbf, V3D_Point rba, V3D_Point rtf, V3D_Point rta) {
        // Half - a triangular prism
        V3D_Tetrahedron t1 = new V3D_Tetrahedron(ltf, rtf, rta, rba, oom, rm);
        tetrahedra.add(new Tetrahedron(t1, Color.BLUE, oom, rm));
        V3D_Tetrahedron t2 = new V3D_Tetrahedron(rtf, rbf, lbf, rba, oom, rm);
        tetrahedra.add(new Tetrahedron(t2, Color.RED, oom, rm));
        V3D_Tetrahedron t3 = new V3D_Tetrahedron(lbf, rtf, rba, ltf, oom, rm);
        tetrahedra.add(new Tetrahedron(t3, Color.GREEN, oom, rm));
        // Another Half - a triangular prism
        V3D_Tetrahedron t4 = new V3D_Tetrahedron(ltf, lbf, lba, rta, oom, rm);
        tetrahedra.add(new Tetrahedron(t4, Color.YELLOW, oom, rm));
        V3D_Tetrahedron t5 = new V3D_Tetrahedron(ltf, lta, rta, lba, oom, rm);
        tetrahedra.add(new Tetrahedron(t5, Color.CYAN, oom, rm));
        V3D_Tetrahedron t6 = new V3D_Tetrahedron(lbf, lba, rba, rta, oom, rm);
        tetrahedra.add(new Tetrahedron(t6, Color.MAGENTA, oom, rm));
        // Volume check
        System.out.println("t1 volume=" + t1.getVolume(oom, rm));
        System.out.println("t2 volume=" + t2.getVolume(oom, rm));
        System.out.println("t3 volume=" + t3.getVolume(oom, rm));
        System.out.println("t4 volume=" + t4.getVolume(oom, rm));
        System.out.println("t5 volume=" + t5.getVolume(oom, rm));
        System.out.println("t6 volume=" + t6.getVolume(oom, rm));
    }

    public void createCubeSurfaceFromTriangles(int oom, RoundingMode rm,
            V3D_Point lbf, V3D_Point lba, V3D_Point ltf, V3D_Point lta,
            V3D_Point rbf, V3D_Point rba, V3D_Point rtf, V3D_Point rta) {
        V3D_Point centroid = V3D_Point.ORIGIN;
//        triangles.add(new Triangle(new V3D_Triangle(lbf, ltf, rtf, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(lbf, rbf, rtf, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(lbf, ltf, lta, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(lbf, lba, lta, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(lba, lta, rta, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(lba, rba, rta, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(rbf, rtf, rta, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(rbf, rta, rba, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(ltf, lta, rta, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(rtf, ltf, rta, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(lbf, rbf, rba, oom, rm), Color.WHITE));
//        triangles.add(new Triangle(new V3D_Triangle(lbf, lba, rba, oom, rm), Color.WHITE));
        // Coloured triangles
        triangles.add(new Triangle(new V3D_Triangle(centroid, lbf, ltf, rtf, oom, rm), Color.BLUE));
        triangles.add(new Triangle(new V3D_Triangle(centroid, lbf, rbf, rtf, oom, rm), Color.BLUE));
        triangles.add(new Triangle(new V3D_Triangle(centroid, lbf, ltf, lta, oom, rm), Color.RED));
        triangles.add(new Triangle(new V3D_Triangle(centroid, lbf, lba, lta, oom, rm), Color.RED));
        triangles.add(new Triangle(new V3D_Triangle(centroid, lba, lta, rta, oom, rm), Color.YELLOW));
        triangles.add(new Triangle(new V3D_Triangle(centroid, lba, rba, rta, oom, rm), Color.YELLOW));
        triangles.add(new Triangle(new V3D_Triangle(centroid, rbf, rtf, rta, oom, rm), Color.GREEN));
        triangles.add(new Triangle(new V3D_Triangle(centroid, rbf, rta, rba, oom, rm), Color.GREEN));
        triangles.add(new Triangle(new V3D_Triangle(centroid, ltf, lta, rta, oom, rm), Color.ORANGE));
        triangles.add(new Triangle(new V3D_Triangle(centroid, rtf, ltf, rta, oom, rm), Color.ORANGE));
        triangles.add(new Triangle(new V3D_Triangle(centroid, lbf, rbf, rba, oom, rm), Color.PINK));
        triangles.add(new Triangle(new V3D_Triangle(centroid, lbf, lba, rba, oom, rm), Color.PINK));

    }

    /**
     * Create a new instance.
     *
     * @param path The path of the STL binary file to be read.
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public Universe(Path path, V3D_Vector offset, Color color,
            boolean assessTopology, int oom, RoundingMode rm) throws IOException {
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        STL_Reader data = new STL_Reader(assessTopology);
        data.readBinary(path, offset, oom, rm);
        V3D_Point p = data.triangles.get(0).triangle.getPl(oom, rm).getP();
        Math_BigRational xmin = p.getX(oom, rm);
        Math_BigRational xmax = p.getX(oom, rm);
        Math_BigRational ymin = p.getY(oom, rm);
        Math_BigRational ymax = p.getY(oom, rm);
        Math_BigRational zmin = p.getZ(oom, rm);
        Math_BigRational zmax = p.getZ(oom, rm);
        for (Triangle t : data.triangles) {
            t.baseColor = color;
            t.lightingColor = color;
            triangles.add(t);
            for (var pt : t.triangle.getPoints(oom, rm)) {
                Math_BigRational x = pt.getX(oom, rm);
                Math_BigRational y = pt.getY(oom, rm);
                Math_BigRational z = pt.getZ(oom, rm);
                xmin = xmin.min(x);
                xmax = xmax.max(x);
                ymin = ymin.min(y);
                ymax = ymax.max(y);
                zmin = zmin.min(z);
                zmax = zmax.max(z);
            }
        }
        envelope = new V3D_Envelope(oom, rm, xmin, xmax, ymin, ymax, zmin, zmax);
    }

    /**
     * Set the camera.
     *
     * @param camera What {@link #camera} is set to.
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * This will be for updating the universe from one time to the next.
     */
    public void update() {
    }
}
