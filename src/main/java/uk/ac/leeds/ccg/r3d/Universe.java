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

import ch.obermuhlner.math.big.BigRational;
import java.awt.Color;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import uk.ac.leeds.ccg.r3d.entities.Volume;
import uk.ac.leeds.ccg.r3d.entities.Area;
import uk.ac.leeds.ccg.r3d.entities.Line;
import uk.ac.leeds.ccg.r3d.entities.Point;
import uk.ac.leeds.ccg.r3d.io.STL_Reader;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_AABB;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Area;
import uk.ac.leeds.ccg.v3d.geometry.V3D_LineSegment;
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
     * Environment
     */
    public final V3D_Environment env;
    
    /**
     * The aabb of all finite geometries.
     */
    public V3D_AABB aabb;

    /**
     * The points.
     */
    public ArrayList<Point> points;

    /**
     * The lines.
     */
    public ArrayList<Line> lines;

    /**
     * The areas.
     */
    public ArrayList<Area> areas;

    /**
     * The volumes to render.
     */
//    public ArrayList<Volume> volumes;
/**
     * A single camera.
     */
    public Camera camera;

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
    public Universe(V3D_Environment env, V3D_Vector offset, 
            int oom, RoundingMode rm) {
        this.env = env;
        points = new ArrayList<>();
        lines = new ArrayList<>();
        areas = new ArrayList<>();
        //volumes = new ArrayList<>();
        /**
         * Create a AABox centred at 0,0,0
         */
        V3D_Point[] points = new V3D_Point[8];
        // multiplication factor
        long m = 100;
        V3D_Point lbf = new V3D_Point(env, offset, new V3D_Vector(-1 * m, -1 * m, -1 * m));
        V3D_Point lba = new V3D_Point(env, offset, new V3D_Vector(-1 * m, -1 * m, 1 * m));
        V3D_Point ltf = new V3D_Point(env, offset, new V3D_Vector(-1 * m, 1 * m, -1 * m));
        V3D_Point lta = new V3D_Point(env, offset, new V3D_Vector(-1 * m, 1 * m, 1 * m));
        V3D_Point rbf = new V3D_Point(env, offset, new V3D_Vector(1 * m, -1 * m, -1 * m));
        V3D_Point rba = new V3D_Point(env, offset, new V3D_Vector(1 * m, -1 * m, 1 * m));
        V3D_Point rtf = new V3D_Point(env, offset, new V3D_Vector(1 * m, 1 * m, -1 * m));
        V3D_Point rta = new V3D_Point(env, offset, new V3D_Vector(1 * m, 1 * m, 1 * m));
        //createCubeFrom5Tetrahedra(oom, rm, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        //createCubeFrom6Tetrahedra(oom, rm, lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        //createCubeSurfaceFromTriangles(oom, rm, env,  lbf, lba, ltf, lta, rbf, rba, rtf, rta);
        points[0] = lbf;
        points[1] = lba;
        points[2] = ltf;
        points[3] = lta;
        points[4] = rbf;
        points[5] = rba;
        points[6] = rtf;
        points[7] = rta;
        this.aabb = new V3D_AABB(oom, points);
    }

//    /**
//     * The central Tetrahedra is regular and different to the others.
//     */
//    public void createCubeFrom5Tetrahedra(int oom, RoundingMode rm,
//            V3D_Point lbf, V3D_Point lba, V3D_Point ltf, V3D_Point lta,
//            V3D_Point rbf, V3D_Point rba, V3D_Point rtf, V3D_Point rta) {
//        V3D_Tetrahedron t1 = new V3D_Tetrahedron(lbf, ltf, lta, rtf, oom, rm);
//        volumes.add(new Volume(t1, Color.BLUE, oom, rm));
//        V3D_Tetrahedron t2 = new V3D_Tetrahedron(lta, lba, lbf, rba, oom, rm);
//        volumes.add(new Volume(t2, Color.RED, oom, rm));
//        V3D_Tetrahedron t3 = new V3D_Tetrahedron(lta, rta, rba, rtf, oom, rm);
//        volumes.add(new Volume(t3, Color.GREEN, oom, rm));
//        V3D_Tetrahedron t4 = new V3D_Tetrahedron(lbf, rbf, rtf, rba, oom, rm);
//        volumes.add(new Volume(t4, Color.YELLOW, oom, rm));
//        V3D_Tetrahedron t5 = new V3D_Tetrahedron(lbf, rba, rtf, lta, oom, rm);
//        volumes.add(new Volume(t5, Color.CYAN, oom, rm));
//        // Volume check
//        System.out.println("t1 volume=" + t1.getVolume(oom, rm));
//        System.out.println("t2 volume=" + t2.getVolume(oom, rm));
//        System.out.println("t3 volume=" + t3.getVolume(oom, rm));
//        System.out.println("t4 volume=" + t4.getVolume(oom, rm));
//        System.out.println("t5 volume=" + t5.getVolume(oom, rm));
//    }
//
//    public void createCubeFrom6Tetrahedra(int oom, RoundingMode rm,
//            V3D_Point lbf, V3D_Point lba, V3D_Point ltf, V3D_Point lta,
//            V3D_Point rbf, V3D_Point rba, V3D_Point rtf, V3D_Point rta) {
//        // Half - a triangular prism
//        V3D_Tetrahedron t1 = new V3D_Tetrahedron(ltf, rtf, rta, rba, oom, rm);
//        volumes.add(new Volume(t1, Color.BLUE, oom, rm));
//        V3D_Tetrahedron t2 = new V3D_Tetrahedron(rtf, rbf, lbf, rba, oom, rm);
//        volumes.add(new Volume(t2, Color.RED, oom, rm));
//        V3D_Tetrahedron t3 = new V3D_Tetrahedron(lbf, rtf, rba, ltf, oom, rm);
//        volumes.add(new Volume(t3, Color.GREEN, oom, rm));
//        // Another Half - a triangular prism
//        V3D_Tetrahedron t4 = new V3D_Tetrahedron(ltf, lbf, lba, rta, oom, rm);
//        volumes.add(new Volume(t4, Color.YELLOW, oom, rm));
//        V3D_Tetrahedron t5 = new V3D_Tetrahedron(ltf, lta, rta, lba, oom, rm);
//        volumes.add(new Volume(t5, Color.CYAN, oom, rm));
//        V3D_Tetrahedron t6 = new V3D_Tetrahedron(lbf, lba, rba, rta, oom, rm);
//        volumes.add(new Volume(t6, Color.MAGENTA, oom, rm));
//        // Volume check
//        System.out.println("t1 volume=" + t1.getVolume(oom, rm));
//        System.out.println("t2 volume=" + t2.getVolume(oom, rm));
//        System.out.println("t3 volume=" + t3.getVolume(oom, rm));
//        System.out.println("t4 volume=" + t4.getVolume(oom, rm));
//        System.out.println("t5 volume=" + t5.getVolume(oom, rm));
//        System.out.println("t6 volume=" + t6.getVolume(oom, rm));
//    }

    public void createCubeSurfaceFromTriangles(int oom, RoundingMode rm,
            V3D_Environment env,
            V3D_Point lbf, V3D_Point lba, V3D_Point ltf, V3D_Point lta,
            V3D_Point rbf, V3D_Point rba, V3D_Point rtf, V3D_Point rta) {
        V3D_Point centroid = V3D_Point.ORIGIN;
        centroid.env = env;
//        areas.add(new Area(new V3D_Triangle(lbf, ltf, rtf, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(lbf, rbf, rtf, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(lbf, ltf, lta, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(lbf, lba, lta, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(lba, lta, rta, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(lba, rba, rta, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(rbf, rtf, rta, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(rbf, rta, rba, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(ltf, lta, rta, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(rtf, ltf, rta, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(lbf, rbf, rba, oom, rm), Color.WHITE));
//        areas.add(new Area(new V3D_Triangle(lbf, lba, rba, oom, rm), Color.WHITE));
        // Coloured areas
        areas.add(new Area(new V3D_Triangle(centroid, lbf, ltf, rtf, oom, rm), Color.BLUE));
        areas.add(new Area(new V3D_Triangle(centroid, lbf, rbf, rtf, oom, rm), Color.BLUE));
        areas.add(new Area(new V3D_Triangle(centroid, lbf, ltf, lta, oom, rm), Color.RED));
        areas.add(new Area(new V3D_Triangle(centroid, lbf, lba, lta, oom, rm), Color.RED));
        areas.add(new Area(new V3D_Triangle(centroid, lba, lta, rta, oom, rm), Color.YELLOW));
        areas.add(new Area(new V3D_Triangle(centroid, lba, rba, rta, oom, rm), Color.YELLOW));
        areas.add(new Area(new V3D_Triangle(centroid, rbf, rtf, rta, oom, rm), Color.GREEN));
        areas.add(new Area(new V3D_Triangle(centroid, rbf, rta, rba, oom, rm), Color.GREEN));
        areas.add(new Area(new V3D_Triangle(centroid, ltf, lta, rta, oom, rm), Color.ORANGE));
        areas.add(new Area(new V3D_Triangle(centroid, rtf, ltf, rta, oom, rm), Color.ORANGE));
        areas.add(new Area(new V3D_Triangle(centroid, lbf, rbf, rba, oom, rm), Color.PINK));
        areas.add(new Area(new V3D_Triangle(centroid, lbf, lba, rba, oom, rm), Color.PINK));

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
            boolean assessTopology, int oom, RoundingMode rm, 
        V3D_Environment env) throws IOException {
        this.env = env;
        areas = new ArrayList<>();
        //volumes = new ArrayList<>();
        STL_Reader data = new STL_Reader(assessTopology);
        data.readBinary(path, offset, oom, rm, env);
        V3D_Point p = data.triangles.get(0).area.getPl(oom, rm).getP();
        BigRational xmin = p.getX(oom, rm);
        BigRational xmax = p.getX(oom, rm);
        BigRational ymin = p.getY(oom, rm);
        BigRational ymax = p.getY(oom, rm);
        BigRational zmin = p.getZ(oom, rm);
        BigRational zmax = p.getZ(oom, rm);
        for (Area t : data.triangles) {
            t.color = color;
            t.lightingColor = color;
            areas.add(t);
            for (var pt : t.area.getPoints(oom, rm).values()) {
                BigRational x = pt.getX(oom, rm);
                BigRational y = pt.getY(oom, rm);
                BigRational z = pt.getZ(oom, rm);
                xmin = BigRational.min(xmin, x);
                xmax = BigRational.max(xmax, x);
                ymin = BigRational.min(ymin, y);
                ymax = BigRational.max(ymax, y);
                zmin = BigRational.min(zmin, z);
                zmax = BigRational.max(zmax, z);
            }
        }
        aabb = new V3D_AABB(env, oom, xmin, xmax, ymin, ymax, zmin, zmax);
    }

    /**
     * Set the camera.
     *
     * @param camera What {@link #camera} is set to.
     */
    //public void setCamera(CameraOld camera) {
    //public void setCamera(Camera1 camera) {
    public void setCamera(Camera camera) {
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
    public Point addPoint(V3D_Point p, int oom, RoundingMode rm) {
        Point e = new Point(p);
        points.add(e);
        aabb = aabb.union(p.getAABB(oom, rm), oom);
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
    public Line addLine(V3D_LineSegment l, Color baseColor, int oom, RoundingMode rm) {
        Line e = new Line(l, baseColor);
        lines.add(e);
        aabb = aabb.union(l.getAABB(oom, rm), oom);
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
    public Area addArea(V3D_Area a, Color baseColor, int oom, RoundingMode rm) {
        Area e = new Area(a, baseColor);
        areas.add(e);
        aabb = aabb.union(a.getAABB(oom, rm), oom);
        return e;
    }
}
