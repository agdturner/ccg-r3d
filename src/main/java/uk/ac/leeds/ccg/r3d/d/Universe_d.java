/*
 * Copyright 2022-2025 Centre for Computational Geography.
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
import uk.ac.leeds.ccg.data.id.Data_ID_long;
import uk.ac.leeds.ccg.r3d.d.entities.Line_d;
import uk.ac.leeds.ccg.r3d.d.entities.Point_d;
import uk.ac.leeds.ccg.r3d.d.entities.PolygonNoInternalHoles_d;
import uk.ac.leeds.ccg.r3d.d.entities.Tetrahedron_d;
import uk.ac.leeds.ccg.r3d.d.entities.Triangle_d;
import uk.ac.leeds.ccg.r3d.io.d.STL_Reader_d;
import uk.ac.leeds.ccg.v3d.core.d.V3D_Environment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_AABB_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineSegment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Point_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PolygonNoInternalHoles_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Tetrahedron_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Triangle_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Vector_d;

/**
 * A class referring to all objects in the universe.
 *
 * @author Andy Turner
 */
public class Universe_d {
    
    /**
     * Environment
     */
    public final V3D_Environment_d env;
    
    /**
     * Envelope
     */
    V3D_AABB_d aabb;

    /**
     * The triangles to render.
     */
    public ArrayList<Point_d> points;

    /**
     * The triangles to render.
     */
    public ArrayList<Line_d> lines;

    /**
     * The triangles to render.
     */
    public ArrayList<Triangle_d> triangles;

    /**
     * The polygons with no internal holes.
     */
    public ArrayList<PolygonNoInternalHoles_d> pnih;
    
    /**
     * The tetrahedra to render.
     */
    public ArrayList<Tetrahedron_d> tetrahedra;

    /**
     * A single camera.
     */
    public Camera_d camera;
        
    /**
     * long
     */
    long nextID;

    /**
     * Create a new instance.
     *
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public Universe_d(V3D_Environment_d env, V3D_Vector_d offset, double epsilon) {
        this.env = env;
        points = new ArrayList<>();
        lines = new ArrayList<>();
        triangles = new ArrayList<>();
        pnih = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        /**
         * Create a AABox centred at 0,0,0
         */
        V3D_Point_d[] points = new V3D_Point_d[8];
        // multiplication factor
        long m = 3;
        V3D_Point_d lbf = new V3D_Point_d(env, offset, new V3D_Vector_d(-1 * m, -1 * m, -1 * m));
        V3D_Point_d lba = new V3D_Point_d(env, offset, new V3D_Vector_d(-1 * m, -1 * m, 1 * m));
        V3D_Point_d ltf = new V3D_Point_d(env, offset, new V3D_Vector_d(-1 * m, 1 * m, -1 * m));
        V3D_Point_d lta = new V3D_Point_d(env, offset, new V3D_Vector_d(-1 * m, 1 * m, 1 * m));
        V3D_Point_d rbf = new V3D_Point_d(env, offset, new V3D_Vector_d(1 * m, -1 * m, -1 * m));
        V3D_Point_d rba = new V3D_Point_d(env, offset, new V3D_Vector_d(1 * m, -1 * m, 1 * m));
        V3D_Point_d rtf = new V3D_Point_d(env, offset, new V3D_Vector_d(1 * m, 1 * m, -1 * m));
        V3D_Point_d rta = new V3D_Point_d(env, offset, new V3D_Vector_d(1 * m, 1 * m, 1 * m));
        //createCubeFrom5Tetrahedra(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        //createCubeFrom6Tetrahedra(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        //createCubeSurfaceFromTriangles(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        //createAxes(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        createTriangle(epsilon, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        points[0] = lbf;
        points[1] = lba;
        points[2] = ltf;
        points[3] = lta;
        points[4] = rbf;
        points[5] = rba;
        points[6] = rtf;
        points[7] = rta;
        this.aabb = new V3D_AABB_d(points);
    }
/**
     * Create a new instance.
     *
     * @param path The path of the STL binary file to be read.
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public Universe_d(V3D_Environment_d env, Path path, V3D_Vector_d offset, Color color,
            boolean assessTopology, double epsilon, boolean initNormal) throws IOException {
        this.env = env;
        triangles = new ArrayList<>();
        pnih = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        STL_Reader_d data = new STL_Reader_d(env, assessTopology);
        data.readBinary(path, offset, initNormal);
        V3D_Point_d p = data.triangles.get(0).triangle.pl.getP();
        double xmin = p.getX();
        double xmax = p.getX();
        double ymin = p.getY();
        double ymax = p.getY();
        double zmin = p.getZ();
        double zmax = p.getZ();
        for (Triangle_d t : data.triangles) {
            t.baseColor = color;
            t.lightingColor = color;
            triangles.add(t);
            for (var pt : t.triangle.getPoints().values()) {
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
        aabb = new V3D_AABB_d(env, xmin, xmax, ymin, ymax, zmin, zmax);
        System.out.println(aabb.toString());
    }
    

    /**
     * @return The next id. 
     */
    private Data_ID_long getNextID(){
        Data_ID_long id = new Data_ID_long(nextID);
        nextID ++;
        return id;
    }
    
    /**
     *
     */
    public void createTriangle(double epsilon,
            V3D_Point_d lbf, V3D_Point_d lba, V3D_Point_d ltf, V3D_Point_d lta,
            V3D_Point_d rbf, V3D_Point_d rba, V3D_Point_d rtf, V3D_Point_d rta) {
        V3D_AABB_d e = new V3D_AABB_d(lbf, lba, ltf, lta,
                rbf, rba, rtf, rta);
        // Create x axis
        //V3D_Point_d p = new V3D_Point_d(e.getXMin(), 0d, -1d);
        V3D_Point_d p = new V3D_Point_d(env, e.getXMin(), 0d, e.getZMin());
        //V3D_Point_d q = new V3D_Point_d(e.getXMax(), e.getYMax(), -1d);
        V3D_Point_d q = new V3D_Point_d(env, e.getXMax(), e.getYMax(),  e.getZMin());
        //V3D_Point_d r = new V3D_Point_d(e.getXMax(), 0d, -1d);
        V3D_Point_d r = new V3D_Point_d(env, e.getXMax(), 0d, e.getZMin());
        triangles.add(new Triangle_d(new V3D_Triangle_d(p, q, r), Color.PINK));
    }
    
    /**
     *
     */
    public void createAxes(double epsilon,
            V3D_Point_d lbf, V3D_Point_d lba, V3D_Point_d ltf, V3D_Point_d lta,
            V3D_Point_d rbf, V3D_Point_d rba, V3D_Point_d rtf, V3D_Point_d rta) {
        V3D_AABB_d e = new V3D_AABB_d(lbf, lba, ltf, lta,
                rbf, rba, rtf, rta);
        // Create x axis
        V3D_Point_d xMin = new V3D_Point_d(env, e.getXMin(), 0d, 0d);
        V3D_Point_d xMax = new V3D_Point_d(env, e.getXMax(), 0d, 0d);
        lines.add(new Line_d(new V3D_LineSegment_d(xMin, xMax), Color.RED));
        // Create y axis
        V3D_Point_d yMin = new V3D_Point_d(env, 0d, e.getYMin(), 0d);
        V3D_Point_d yMax = new V3D_Point_d(env, 0d, e.getYMax(), 0d);
        lines.add(new Line_d(new V3D_LineSegment_d(yMin, yMax), Color.GREEN));
        // Create z axis
        V3D_Point_d zMin = new V3D_Point_d(env, 0d, 0d, e.getZMin());
        V3D_Point_d zMax = new V3D_Point_d(env, 0d, 0d, e.getZMax());
        lines.add(new Line_d(new V3D_LineSegment_d(zMin, zMax), Color.BLUE));


    }

    /**
     * The central Tetrahedra is regular and different to the others.
     */
    public void createCubeFrom5Tetrahedra(double epsilon,
            V3D_Point_d lbf, V3D_Point_d lba, V3D_Point_d ltf, V3D_Point_d lta,
            V3D_Point_d rbf, V3D_Point_d rba, V3D_Point_d rtf, V3D_Point_d rta) {
        V3D_Tetrahedron_d t1 = new V3D_Tetrahedron_d(lbf, ltf, lta, rtf, epsilon);
        tetrahedra.add(new Tetrahedron_d(t1, Color.BLUE));
        V3D_Tetrahedron_d t2 = new V3D_Tetrahedron_d(lta, lba, lbf, rba, epsilon);
        tetrahedra.add(new Tetrahedron_d(t2, Color.RED));
        V3D_Tetrahedron_d t3 = new V3D_Tetrahedron_d(lta, rta, rba, rtf, epsilon);
        tetrahedra.add(new Tetrahedron_d(t3, Color.GREEN));
        V3D_Tetrahedron_d t4 = new V3D_Tetrahedron_d(lbf, rbf, rtf, rba, epsilon);
        tetrahedra.add(new Tetrahedron_d(t4, Color.YELLOW));
        V3D_Tetrahedron_d t5 = new V3D_Tetrahedron_d(lbf, rba, rtf, lta, epsilon);
        tetrahedra.add(new Tetrahedron_d(t5, Color.CYAN));
        // Volume check
        System.out.println("t1 volume=" + t1.getVolume());
        System.out.println("t2 volume=" + t2.getVolume());
        System.out.println("t3 volume=" + t3.getVolume());
        System.out.println("t4 volume=" + t4.getVolume());
        System.out.println("t5 volume=" + t5.getVolume());
    }

    public void createCubeFrom6Tetrahedra(double epsilon,
            V3D_Point_d lbf, V3D_Point_d lba, V3D_Point_d ltf, V3D_Point_d lta,
            V3D_Point_d rbf, V3D_Point_d rba, V3D_Point_d rtf, V3D_Point_d rta) {
        // Half - a triangular prism
        V3D_Tetrahedron_d t1 = new V3D_Tetrahedron_d(ltf, rtf, rta, rba, epsilon);
        tetrahedra.add(new Tetrahedron_d(t1, Color.BLUE));
        V3D_Tetrahedron_d t2 = new V3D_Tetrahedron_d(rtf, rbf, lbf, rba, epsilon);
        tetrahedra.add(new Tetrahedron_d(t2, Color.RED));
        V3D_Tetrahedron_d t3 = new V3D_Tetrahedron_d(lbf, rtf, rba, ltf, epsilon);
        tetrahedra.add(new Tetrahedron_d(t3, Color.GREEN));
        // Another Half - a triangular prism
        V3D_Tetrahedron_d t4 = new V3D_Tetrahedron_d(ltf, lbf, lba, rta, epsilon);
        tetrahedra.add(new Tetrahedron_d(t4, Color.YELLOW));
        V3D_Tetrahedron_d t5 = new V3D_Tetrahedron_d(ltf, lta, rta, lba, epsilon);
        tetrahedra.add(new Tetrahedron_d(t5, Color.CYAN));
        V3D_Tetrahedron_d t6 = new V3D_Tetrahedron_d(lbf, lba, rba, rta, epsilon);
        tetrahedra.add(new Tetrahedron_d(t6, Color.MAGENTA));
        // Volume check
        System.out.println("t1 volume=" + t1.getVolume());
        System.out.println("t2 volume=" + t2.getVolume());
        System.out.println("t3 volume=" + t3.getVolume());
        System.out.println("t4 volume=" + t4.getVolume());
        System.out.println("t5 volume=" + t5.getVolume());
        System.out.println("t6 volume=" + t6.getVolume());
    }

    public void createCubeSurfaceFromTriangles(double epsilon,
            V3D_Point_d lbf, V3D_Point_d lba, V3D_Point_d ltf, V3D_Point_d lta,
            V3D_Point_d rbf, V3D_Point_d rba, V3D_Point_d rtf, V3D_Point_d rta) {
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, ltf, rtf), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, rbf, rtf), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, ltf, lta), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, lba, lta), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lba, lta, rta), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lba, rba, rta), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(rbf, rtf, rta), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(rbf, rta, rba), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(ltf, lta, rta), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(rtf, ltf, rta), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, rbf, rba), Color.WHITE));
//        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, lba, rba), Color.WHITE));
        // Coloured triangles
        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, ltf, rtf), Color.BLUE));
        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, rbf, rtf), Color.BLUE));
        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, ltf, lta), Color.RED));
        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, lba, lta), Color.RED));
        triangles.add(new Triangle_d(new V3D_Triangle_d(lba, lta, rta), Color.YELLOW));
        triangles.add(new Triangle_d(new V3D_Triangle_d(lba, rba, rta), Color.YELLOW));
        triangles.add(new Triangle_d(new V3D_Triangle_d(rbf, rtf, rta), Color.GREEN));
        triangles.add(new Triangle_d(new V3D_Triangle_d(rbf, rta, rba), Color.GREEN));
        triangles.add(new Triangle_d(new V3D_Triangle_d(ltf, lta, rta), Color.ORANGE));
        triangles.add(new Triangle_d(new V3D_Triangle_d(rtf, ltf, rta), Color.ORANGE));
        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, rbf, rba), Color.PINK));
        triangles.add(new Triangle_d(new V3D_Triangle_d(lbf, lba, rba), Color.PINK));

    }

    

    /**
     * Set the camera.
     *
     * @param camera What {@link #camera} is set to.
     */
    public void setCamera(Camera_d camera) {
        this.camera = camera;
    }

    /**
     * This will be for updating the universe from one time to the next.
     */
    public void update() {
    }
    
    /**
     * Adds the polygon and returns entity.
     * @param polygon The polygon to add.
     * @return The Triangle.
     */
    public PolygonNoInternalHoles_d addPolygonNoInternalHoles(
            V3D_PolygonNoInternalHoles_d polygon){
        PolygonNoInternalHoles_d p = new PolygonNoInternalHoles_d(polygon,
                getNextID());
        pnih.add(p);
        aabb = aabb.union(polygon.getAABB());
        return p;
    }
}
