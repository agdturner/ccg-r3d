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
import uk.ac.leeds.ccg.r3d.d.entities.Area_d;
import uk.ac.leeds.ccg.r3d.d.entities.Tetrahedron_d;
import uk.ac.leeds.ccg.r3d.d.entities.Triangle_d;
import uk.ac.leeds.ccg.r3d.io.d.STL_Reader_d;
import uk.ac.leeds.ccg.v3d.core.d.V3D_Environment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_AABB_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Area_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineSegment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Line_d;
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
     * The aabb of all finite geometries.
     */
    public V3D_AABB_d aabb;

    /**
     * The points.
     */
    public ArrayList<Point_d> points;

    /**
     * The lines.
     */
    public ArrayList<Line_d> lines;

    /**
     * The areas.
     */
    public ArrayList<Area_d> areas;

    /**
     * The volumes.
     */
    //public ArrayList<Volume_d> volume;
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
        areas = new ArrayList<>();
        aabb = new V3D_AABB_d(env, 0d, 0d, 0d);
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
        points = new ArrayList<>();
        lines = new ArrayList<>();
        areas = new ArrayList<>();
        STL_Reader_d data = new STL_Reader_d(env, assessTopology);
        data.readBinary(path, offset, initNormal);
        V3D_Point_d p = data.triangles.get(0).area.pl.getP();
        double xmin = p.getX();
        double xmax = p.getX();
        double ymin = p.getY();
        double ymax = p.getY();
        double zmin = p.getZ();
        double zmax = p.getZ();
        for (Area_d t : data.triangles) {
            t.color = color;
            t.lightingColor = color;
            areas.add(t);
            for (var pt : t.area.getPoints().values()) {
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
            break;
        }
        aabb = new V3D_AABB_d(env, xmin, xmax, ymin, ymax, zmin, zmax);
        System.out.println(aabb.toString());
    }

    /**
     * @return The next id.
     */
    private Data_ID_long getNextID() {
        Data_ID_long id = new Data_ID_long(nextID);
        nextID++;
        return id;
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

//    /**
//     * The central Tetrahedra is regular and different to the others.
//     */
//    public void createCubeFrom5Tetrahedra(double epsilon,
//            V3D_Point_d lbf, V3D_Point_d lba, V3D_Point_d ltf, V3D_Point_d lta,
//            V3D_Point_d rbf, V3D_Point_d rba, V3D_Point_d rtf, V3D_Point_d rta) {
//        V3D_Tetrahedron_d t1 = new V3D_Tetrahedron_d(lbf, ltf, lta, rtf, epsilon);
//        volumes.add(new Tetrahedron_d(t1, Color.BLUE));
//        V3D_Tetrahedron_d t2 = new V3D_Tetrahedron_d(lta, lba, lbf, rba, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t2, Color.RED));
//        V3D_Tetrahedron_d t3 = new V3D_Tetrahedron_d(lta, rta, rba, rtf, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t3, Color.GREEN));
//        V3D_Tetrahedron_d t4 = new V3D_Tetrahedron_d(lbf, rbf, rtf, rba, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t4, Color.YELLOW));
//        V3D_Tetrahedron_d t5 = new V3D_Tetrahedron_d(lbf, rba, rtf, lta, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t5, Color.CYAN));
//        // Volume check
//        System.out.println("t1 volume=" + t1.getVolume());
//        System.out.println("t2 volume=" + t2.getVolume());
//        System.out.println("t3 volume=" + t3.getVolume());
//        System.out.println("t4 volume=" + t4.getVolume());
//        System.out.println("t5 volume=" + t5.getVolume());
//    }
//
//    public void createCubeFrom6Tetrahedra(double epsilon,
//            V3D_Point_d lbf, V3D_Point_d lba, V3D_Point_d ltf, V3D_Point_d lta,
//            V3D_Point_d rbf, V3D_Point_d rba, V3D_Point_d rtf, V3D_Point_d rta) {
//        // Half - a triangular prism
//        V3D_Tetrahedron_d t1 = new V3D_Tetrahedron_d(ltf, rtf, rta, rba, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t1, Color.BLUE));
//        V3D_Tetrahedron_d t2 = new V3D_Tetrahedron_d(rtf, rbf, lbf, rba, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t2, Color.RED));
//        V3D_Tetrahedron_d t3 = new V3D_Tetrahedron_d(lbf, rtf, rba, ltf, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t3, Color.GREEN));
//        // Another Half - a triangular prism
//        V3D_Tetrahedron_d t4 = new V3D_Tetrahedron_d(ltf, lbf, lba, rta, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t4, Color.YELLOW));
//        V3D_Tetrahedron_d t5 = new V3D_Tetrahedron_d(ltf, lta, rta, lba, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t5, Color.CYAN));
//        V3D_Tetrahedron_d t6 = new V3D_Tetrahedron_d(lbf, lba, rba, rta, epsilon);
//        tetrahedra.add(new Tetrahedron_d(t6, Color.MAGENTA));
//        // Volume check
//        System.out.println("t1 volume=" + t1.getVolume());
//        System.out.println("t2 volume=" + t2.getVolume());
//        System.out.println("t3 volume=" + t3.getVolume());
//        System.out.println("t4 volume=" + t4.getVolume());
//        System.out.println("t5 volume=" + t5.getVolume());
//        System.out.println("t6 volume=" + t6.getVolume());
//    }
//
//    
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
     * Adds the point and returns a point entity. Adding may involve expanding
     * the universe Axis Aligned BoundingBox so it includes all entities.
     *
     * @param p The point to add.
     * @return The point entity.
     */
    public Point_d addPoint(V3D_Point_d p) {
        Point_d e = new Point_d(p);
        points.add(e);
        aabb = aabb.union(p.getAABB());
        return e;
    }

    /**
     * Adds the line and returns a line entity. Adding may involve expanding
     * the universe Axis Aligned BoundingBox so it includes all entities.
     *
     * @param l The line to add.
     * @param baseColor The line colour.
     * @return The line entity.
     */
    public Line_d addLine(V3D_LineSegment_d l, Color baseColor) {
        Line_d e = new Line_d(l, baseColor);
        lines.add(e);
        aabb = aabb.union(l.getAABB());
        return e;
    }

    /**
     * Adds an area and returns an area entity. Adding may involve expanding the
     * universe Axis Aligned BoundingBox so it includes all entities.
     *
     * @param a The area to add.
     * @param baseColor The line colour.
     * @return The area entity.
     */
    public Area_d addArea(V3D_Area_d a, Color baseColor) {
        Area_d e = new Area_d(a, baseColor);
        areas.add(e);
        aabb = aabb.union(a.getAABB());
        return e;
    }
}
