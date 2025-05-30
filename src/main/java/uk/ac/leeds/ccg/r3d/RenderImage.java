/*
 * Copyright 2021 Centre for Computational Geography.
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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.MemoryImageSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.leeds.ccg.generic.io.Generic_IO;
import uk.ac.leeds.ccg.math.arithmetic.Math_BigDecimal;
import uk.ac.leeds.ccg.r3d.io.IO;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_AABB;
import uk.ac.leeds.ccg.v3d.geometry.V3D_LineSegment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Plane;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_PolygonNoInternalHoles;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Ray;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class RenderImage {

    /**
     * Universe.
     */
    public Universe universe;

    /**
     * Path to output file.
     */
    Path output;

    /**
     * Create a new instance.
     *
     * @param universe The universe.
     * @param offset The offset.
     * @param focus The camera focal point.
     * @param dim The width and height in pixels.
     * @param screen The rect of the frustum.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @throws Exception
     */
    public RenderImage(Universe universe, V3D_Vector offset,
            V3D_Point focus, Dimension dim, V3D_Rectangle screen,
            int oom, RoundingMode rm) throws Exception {
        this.universe = universe;
        universe.setCamera(new Camera(universe.env, offset, focus, screen, dim, oom, rm));
        //CameraOld c = new CameraOld(pt, universe.aabb, size.width, size.height, oom, rm);
        //Camera c = new Camera(pt, universe.aabb, size, zoomFactor, oom, rm);
        //Camera1 c = new Camera1(pt, universe.aabb, size.width, size.height, oom, rm);
        //this.universe.setCamera(c);
    }

    public static void main(String[] args) {
        int oom = -6;
        RoundingMode rm = RoundingMode.HALF_UP;
        V3D_Environment env = new V3D_Environment(oom, rm);
        V3D_Vector offset = V3D_Vector.ZERO;
        Path inDataDir = Paths.get("data", "input");
        Path outDataDir = Paths.get("data", "output");
        //run00(args, oom, rm, env, offset, inDataDir, outDataDir);
        //run0(args, oom, rm, env, offset, inDataDir, outDataDir);
        //run1(args, oom, rm, env, offset, inDataDir, outDataDir);
        //run2(args, oom, rm, env, offset, inDataDir, outDataDir);
        runUtah(args, oom, rm, env, offset, inDataDir, outDataDir);
        //runGeographos(args, oom, rm, env, offset, inDataDir, outDataDir);
        //runKatrina(args, oom, rm, env, offset, inDataDir, outDataDir);
    }

    public static void run00(String[] args, int oom, RoundingMode rm, V3D_Environment env,
            V3D_Vector offset, Path inDataDir, Path outDataDir) {
        try {
            // Visualise Axes in 3D
            int nrows = 200;
            int ncols = 200;
            // Init universe
            Universe universe = new Universe(env, offset, oom, rm);
            //addPoints0(universe, oom, rm);
            //addTriangle0(universe, oom, rm);
            //addRectangles0(universe, oom, rm);
            addLines0(universe, oom, rm);
            addPolygons00(universe, oom, rm);
            addAxes(universe, oom, rm);

            // Detail the camera
            Dimension dim = new Dimension(ncols, nrows);
            V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
            BigRational radius = universe.aabb.getPointsArray()[0].getDistance(centroid, oom, rm);
            String name = "axes";
            //boolean addGraticules = true;
            boolean addGraticules = false;
            int i = 0;
            int j = 0;
            int k = -1;
            System.out.println("Camera i=" + i + ", j=" + j + ", k=" + k);
            V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
            V3D_Point focus = getCameraPt(centroid, direction, radius.multiply(2), oom, rm);
            // Render the image
//                double zoomFactor = 2d;
//                V3D_Rectangle rect = universe.aabb.getViewport3(focus, V3D_Vector.I, zoomFactor, oom, rm);
            BigRational P100 = BigRational.valueOf(100);
            BigRational N100 = P100.negate();
            V3D_Rectangle rect = new V3D_Rectangle(
                    new V3D_Point(env, N100, N100, radius.negate()),
                    new V3D_Point(env, N100, P100, radius.negate()),
                    new V3D_Point(env, P100, P100, radius.negate()),
                    new V3D_Point(env, P100, N100, radius.negate()), oom, rm);
            RenderImage r = new RenderImage(universe, offset, focus, dim, rect, oom, rm);
            Path dir = Paths.get(outDataDir.toString(), "test", name);
            r.output = Paths.get(dir.toString(), "test.png");
            /**
             * AmbientLight makes dark surfaces lighter even if they are
             * orientated opposite to the lighting vector.
             */
            BigRational ambientLight = BigRational.valueOf(1, 20);
            V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
            r.run(dim, lighting, ambientLight, false, addGraticules, oom, rm);

//                i = -1;
//                j = 0;
//                k = -1;
//                System.out.println("i=" + i + ", j=" + j + ", k=" + k);
//                direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
//                focus = getCameraPt(centroid, direction, radius.multiply(2), oom, rm);
            V3D_Ray xRay = new V3D_Ray(new V3D_Point(universe.env, 0, 0, 0),
                    new V3D_Point(universe.env, 1, 0, 0), oom, rm);
            V3D_Vector xuv = xRay.l.v.getUnitVector(oom, rm);
            V3D_Ray yRay = new V3D_Ray(new V3D_Point(universe.env, 0, 0, 0),
                    new V3D_Point(universe.env, 0, 1, 0), oom, rm);
            V3D_Vector yuv = yRay.l.v.getUnitVector(oom, rm);
            V3D_Ray zRay = new V3D_Ray(
                    new V3D_Point(universe.env, 0, 0, 0),
                    new V3D_Point(universe.env, 0, 0, 1), oom, rm);
            V3D_Vector zuv = zRay.l.v.getUnitVector(oom, rm);
            Math_BigDecimal bd = new Math_BigDecimal();
            BigRational pi = BigRational.valueOf(bd.getPi(oom - 2, rm));
            //int anglei = 4;
            int anglei = 16; // 2; 4; 8; 16;
            int anglei2p1 = (anglei * 2) + 1;
            BigRational angle = pi.divide(anglei);

            // 
            //Path p = Paths.get(outDataDir.toString(), "test", name, "ffmpeg.txt");
            //BufferedWriter br = Generic_IO.getBufferedWriter(p, false);
            int a3 = anglei2p1 * anglei2p1 * anglei2p1;
            String a3s = Integer.toString(a3);
            int a3sl = a3s.length();

            V3D_Rectangle rectr;
            V3D_Point focusr;

            i = Integer.valueOf(args[0]);
            //i = 0;
            //for (i = 0; i < anglei2p1; i++) {
            //rect = rect.rotate(xRay, xuv, angle, epsilon);
            //focus = focus.rotate(xRay, xuv, angle, epsilon);
            //j = 1;
            for (j = 0; j < anglei2p1; j++) {
                //rect = rect.rotate(yRay, yuv, angle, epsilon);
                //focus = focus.rotate(yRay, yuv, angle, epsilon);
                //k = 8;
                for (k = 0; k < anglei2p1; k++) {
                    //rect = rect.rotate(zRay, zuv, angle, epsilon);
                    //focus = focus.rotate(zRay, zuv, angle, epsilon);
                    rectr = rect.rotate(zRay, zuv, bd, angle.multiply(k), oom, rm);
                    focusr = focus.rotate(zRay, zuv, bd, angle.multiply(k), oom, rm);
                    rectr = rectr.rotate(xRay, xuv, bd, angle.multiply(i), oom, rm);
                    focusr = focusr.rotate(xRay, xuv, bd, angle.multiply(i), oom, rm);
                    rectr = rectr.rotate(yRay, yuv, bd, angle.multiply(j), oom, rm);
                    focusr = focusr.rotate(yRay, yuv, bd, angle.multiply(j), oom, rm);
                    r = new RenderImage(universe, offset, focusr, dim, rectr, oom, rm);
                    dir = Paths.get(outDataDir.toString(), "test", name + "r");

                    StringBuilder sb = new StringBuilder();
                    int id = (i * anglei2p1 * anglei2p1) + (j * anglei2p1) + k;
                    String ids = Integer.toString(id);
                    int idsl = ids.length();
                    int ld = a3sl - idsl;
                    for (int it = 0; it < ld; it++) {
                        sb.append("0");
                    }
                    sb.append(ids);
                    r.output = Paths.get(dir.toString(),
                            "test_" + sb.toString() + "_i" + i + "_j" + j + "_k" + k + ".png");
                    System.out.println(r.output.toString());
                    r.run(dim, lighting, ambientLight, false, addGraticules, oom, rm);
                }
            }
            // ffmpeg -y -pattern_type glob -framerate 4 -i "*.png" -vcodec mpeg4 -pix_fmt yuv420p -r 25 ../video4.mp4
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void run0(String[] args, int oom, RoundingMode rm, V3D_Environment env,
            V3D_Vector offset, Path inDataDir, Path outDataDir) {
        try {
            // Visualise Axes in 3D
            int nrows = 200;
            int ncols = 200;
            // Init universe
            Universe universe = new Universe(env, offset, oom, rm);
            //addPoints0(universe, oom, rm);
            //addTriangle0(universe, oom, rm);
            //addRectangles0(universe, oom, rm);
            //addLines0(universe, oom, rm);
            addPolygons0(universe, oom, rm);
            addAxes(universe, oom, rm);

            // Detail the camera
            Dimension dim = new Dimension(ncols, nrows);
            V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
            BigRational radius = universe.aabb.getPointsArray()[0].getDistance(centroid, oom, rm);
            String name = "triangles";
            //boolean addGraticules = true;
            boolean addGraticules = false;
            int i = 0;
            int j = 0;
            int k = -1;
            System.out.println("i=" + i + ", j=" + j + ", k=" + k);
            V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
            V3D_Point focus = getCameraPt(centroid, direction, radius.multiply(2), oom, rm);
            // Render the image
//                double zoomFactor = 2d;
//                V3D_Rectangle rect = universe.aabb.getViewport3(focus, V3D_Vector.I, zoomFactor, oom, rm);
            BigRational P100 = BigRational.valueOf(100);
            BigRational N100 = P100.negate();
            V3D_Rectangle rect = new V3D_Rectangle(
                    new V3D_Point(env, N100, N100, radius.negate()),
                    new V3D_Point(env, N100, P100, radius.negate()),
                    new V3D_Point(env, P100, P100, radius.negate()),
                    new V3D_Point(env, P100, N100, radius.negate()), oom, rm);
            RenderImage r = new RenderImage(universe, offset, focus, dim, rect, oom, rm);
            Path dir = Paths.get(outDataDir.toString(), "test", name);
            r.output = Paths.get(dir.toString(),
                    "test.png");
            /**
             * AmbientLight makes dark surfaces lighter even if they are
             * orientated opposite to the lighting vector.
             */
            BigRational ambientLight = BigRational.valueOf(1, 20);
            V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
            r.run(dim, lighting, ambientLight, false, addGraticules, oom, rm);

//                i = -1;
//                j = 0;
//                k = -1;
//                System.out.println("i=" + i + ", j=" + j + ", k=" + k);
//                direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
//                focus = getCameraPt(centroid, direction, radius.multiply(2), oom, rm);
            V3D_Ray xRay = new V3D_Ray(new V3D_Point(universe.env, 0, 0, 0),
                    new V3D_Point(universe.env, 1, 0, 0), oom, rm);
            V3D_Vector xuv = xRay.l.v.getUnitVector(oom, rm);
            V3D_Ray yRay = new V3D_Ray(new V3D_Point(universe.env, 0, 0, 0),
                    new V3D_Point(universe.env, 0, 1, 0), oom, rm);
            V3D_Vector yuv = yRay.l.v.getUnitVector(oom, rm);
            V3D_Ray zRay = new V3D_Ray(
                    new V3D_Point(universe.env, 0, 0, 0),
                    new V3D_Point(universe.env, 0, 0, 1), oom, rm);
            V3D_Vector zuv = zRay.l.v.getUnitVector(oom, rm);
            Math_BigDecimal bd = new Math_BigDecimal();
            BigRational pi = BigRational.valueOf(bd.getPi(oom - 2, rm));
            //int anglei = 4;
            int anglei = 8;
            //int anglei = 16;
            int anglei2 = anglei * 2;
            BigRational angle = pi.divide(anglei);

            V3D_Rectangle rectr;
            V3D_Point focusr;

            i = Integer.valueOf(args[0]);
            //i = 0;
            //for (i = 0; i < anglei2; i++) {
            //rect = rect.rotate(xRay, xuv, angle, epsilon);
            //focus = focus.rotate(xRay, xuv, angle, epsilon);
            //j = 1;
            for (j = 0; j < anglei2; j++) {
                //rect = rect.rotate(yRay, yuv, angle, epsilon);
                //focus = focus.rotate(yRay, yuv, angle, epsilon);
                //k = 8;
                for (k = 0; k < anglei2; k++) {
                    //rect = rect.rotate(zRay, zuv, angle, epsilon);
                    //focus = focus.rotate(zRay, zuv, angle, epsilon);
                    rectr = rect.rotate(zRay, zuv, bd, angle.multiply(k), oom, rm);
                    focusr = focus.rotate(zRay, zuv, bd, angle.multiply(k), oom, rm);
                    rectr = rectr.rotate(xRay, xuv, bd, angle.multiply(i), oom, rm);
                    focusr = focusr.rotate(xRay, xuv, bd, angle.multiply(i), oom, rm);
                    rectr = rectr.rotate(yRay, yuv, bd, angle.multiply(j), oom, rm);
                    focusr = focusr.rotate(yRay, yuv, bd, angle.multiply(j), oom, rm);
                    r = new RenderImage(universe, offset, focusr, dim, rectr, oom, rm);
                    dir = Paths.get(outDataDir.toString(), "test", name + "r");
                    r.output = Paths.get(dir.toString(),
                            "test_i" + i + "_j" + j + "_k" + k + ".png");
                    System.out.println(r.output.toString());
                    r.run(dim, lighting, ambientLight, false, addGraticules, oom, rm);
                }
            }
            //}
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void run1(String[] args, int oom, RoundingMode rm, V3D_Environment env,
            V3D_Vector offset, Path inDataDir, Path outDataDir) {
        try {
            //int oom = -5;
            //V3D_Environment env = new V3D_Environment(oom, rm);
            int n = 1;
            //n = 5;
            int w = 100 * n;
            int h = 100 * n;
            //int w = 100 * n;
            //int h = 75 * n;
            boolean assessTopology = false;
            boolean castShadow = false;
            BigRational zoomFactor = BigRational.ONE;
            /**
             * AmbientLight makes non black surfaces non black even if they are
             * orientated opposite to the lighting vector.
             */
            BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
            //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
            String name = "3361664_Platonic_Solid_Collection";
            String filename = "Icosahedron";
            Path input = Paths.get(inDataDir.toString(), name, "files", filename + ".stl");
            Color color = Color.YELLOW;
            // Init universe
            Universe universe = new Universe(input, V3D_Vector.ZERO, color,
                    oom, rm, env);
            // Detail the camera
            Dimension size = new Dimension(w, h);
            V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
            BigRational radius = universe.aabb.getPointsArray()[0]
                    .getDistance(centroid, oom, rm);
            V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        if (!(i == 0 && j == 0 && k == 0)) {
                            V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
                            V3D_Point pt = getCameraPt(centroid, direction,
                                    radius.multiply(2), oom, rm);
//                                // Render the image
//                                RenderImage r = new RenderImage(universe, pt, size, zoomFactor, oom, rm);
//                                String ls = "lighting(i=" + Math_BigRational.round(lighting.getDX(oom, rm), -4, rm).toString()
//                                        + "_j=" + Math_BigRational.round(lighting.getDY(oom, rm), -4, rm).toString()
//                                        + "_k=" + Math_BigRational.round(lighting.getDZ(oom, rm), -4, rm).toString()
//                                        + ")_ambientLight(" + Math_BigRational.round(ambientLight, -4, rm) + ")";
//                                Path dir = Paths.get(outDataDir.toString(), name, "files", "oom=" + oom, ls);
//                                if (castShadow) {
//                                    dir = Paths.get(dir.toString(), "shadow");
//                                }
//
//                                r.output = Paths.get(dir.toString(),
//                                        filename
//                                        + "_" + size.width + "x" + size.height
//                                        + "_pt(i=" + Math_BigRational.round(pt.getX(oom, rm), -4, rm).toString()
//                                        + "_j=" + Math_BigRational.round(pt.getY(oom, rm), -4, rm).toString()
//                                        + "_k=" + Math_BigRational.round(pt.getZ(oom, rm), -4, rm).toString()
//                                        + ")_" + ls + "_oom=" + oom + ".png");
//                                r.run(size, lighting, ambientLight, castShadow, oom, rm);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void run2(String[] args, int oom, RoundingMode rm, V3D_Environment env,
            V3D_Vector offset, Path inDataDir, Path outDataDir) {
        try {
            String name = "testPoly";
            String filename = "testPolygon";
            //int oom = -5;
            //V3D_Environment env = new V3D_Environment(oom, rm);
            int n = 1;
            //n = 5;
            int w = 100 * n;
            int h = 100 * n;
            //int w = 100 * n;
            //int h = 75 * n;
            boolean assessTopology = false;
            boolean castShadow = false;
            BigRational zoomFactor = BigRational.ONE;
            /**
             * AmbientLight makes non black surfaces non black even if they are
             * orientated opposite to the lighting vector.
             */
            BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
            //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
            Color color = Color.YELLOW;
            // Init universe
            Universe universe = new Universe(env, V3D_Vector.ZERO, oom, rm);
            // Detail the camera
            Dimension size = new Dimension(w, h);
            V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
            BigRational radius = universe.aabb.getPointsArray()[0]
                    .getDistance(centroid, oom, rm);
            V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
            int i = 1;
            int j = 0;
            int k = 0;

            V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
            V3D_Point pt = getCameraPt(centroid, direction,
                    radius.multiply(2), oom, rm);
//                                // Render the image
//                                RenderImage r = new RenderImage(universe, pt, size, zoomFactor, oom, rm);
//                                String ls = "lighting(i=" + Math_BigRational.round(lighting.getDX(oom, rm), -4, rm).toString()
//                                        + "_j=" + Math_BigRational.round(lighting.getDY(oom, rm), -4, rm).toString()
//                                        + "_k=" + Math_BigRational.round(lighting.getDZ(oom, rm), -4, rm).toString()
//                                        + ")_ambientLight(" + Math_BigRational.round(ambientLight, -4, rm) + ")";
//                                Path dir = Paths.get(outDataDir.toString(), name, "files", "oom=" + oom, ls);
//                                if (castShadow) {
//                                    dir = Paths.get(dir.toString(), "shadow");
//                                }
//
//                                r.output = Paths.get(dir.toString(),
//                                        filename
//                                        + "_" + size.width + "x" + size.height
//                                        + "_pt(i=" + Math_BigRational.round(pt.getX(oom, rm), -4, rm).toString()
//                                        + "_j=" + Math_BigRational.round(pt.getY(oom, rm), -4, rm).toString()
//                                        + "_k=" + Math_BigRational.round(pt.getZ(oom, rm), -4, rm).toString()
//                                        + ")_" + ls + "_oom=" + oom + ".png");
//                                r.run(size, lighting, ambientLight, castShadow, oom, rm);
//                            }
//                        }
//                    }
//                }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void runUtah(String[] args, int oom, RoundingMode rm, V3D_Environment env,
            V3D_Vector offset, Path inDataDir, Path outDataDir) {
        try {
            //int oom = -3;
            oom = -6;
            int n = 1;
            n = 5;
            int w = 200 * n;
            int h = 200 * n;
            boolean assessTopology = false;
            boolean castShadow = false;
            /**
             * AmbientLight makes non black surfaces non black even if they are
             * orientated opposite to the lighting vector.
             */
            BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
            //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
            String name = "Utah_teapot_(solid)";
            Color color = Color.YELLOW;
            Path input = Paths.get(inDataDir.toString(), name, name + ".stl");
            // Init universe
            Universe universe = new Universe(input, V3D_Vector.ZERO, color,
                    oom, rm, env);
            // Detail the camera
            Dimension size = new Dimension(w, h);
            V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
            //BigRational radius = universe.aabb.getPointsArray(oom, rm)[0]
            //        .getDistance(centroid, oom, rm);
            //                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
            V3D_Point focus = new V3D_Point(env, 200d, 0d, 0d);

            double x = 100d;
            V3D_Rectangle rect = new V3D_Rectangle(
                    new V3D_Point(env, x, -x, -x),
                    new V3D_Point(env, x, -x, x),
                    new V3D_Point(env, x, x, x),
                    new V3D_Point(env, x, x, -x), oom, rm);
            // Render the image
            RenderImage r = new RenderImage(universe, offset, focus, size, rect, oom, rm);
            //V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
            V3D_Vector lighting = new V3D_Vector(1, 2, 3).getUnitVector(oom, rm);
            Path dir = Paths.get(outDataDir.toString(), name);
            r.output = Paths.get(dir.toString(), "test.png");
            r.run(size, lighting, ambientLight, castShadow, oom, rm);
//                            }
//                        }
//                    }
//                }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void runGeographos(String[] args, int oom, RoundingMode rm, V3D_Environment env,
            V3D_Vector offset, Path inDataDir, Path outDataDir) {
        try {
            //int oom = -2;
            //int oom = -4;
            //int oom = -7;
            //int oom = -8;
            //V3D_Environment env = new V3D_Environment(oom, rm);
            int n = 1;
            n = 5;
            int w = 100 * n;
            int h = 75 * n;
            String name = "geographos";
            String filename = "1620geographos";
            boolean assessTopology = false;
            boolean castShadow = false;
            BigRational zoomFactor = BigRational.ONE;
            /**
             * AmbientLight makes non black surfaces non black even if they are
             * orientated opposite to the lighting vector.
             */
            BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
            //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
            Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
            Color color = Color.YELLOW;
            // Init universe
            Universe universe = new Universe(input, V3D_Vector.ZERO, color,
                    oom, rm, env);
            // Detail the camera
            Dimension size = new Dimension(w, h);
            V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
            BigRational radius = universe.aabb.getPointsArray()[0]
                    .getDistance(centroid, oom, rm);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        if (!(i == 0 && j == 0 && k == 0)) {
                            if (!(i == -1 && j == 1 && k == 1)) {
//                int i = -1;
//                int j = 1;
//                int k = 1;
                                V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
                                V3D_Point pt = getCameraPt(centroid, direction,
                                        radius.multiply(2), oom, rm);
//                                    // Render the image
//                                    RenderImage r = new RenderImage(universe, pt, size, zoomFactor, oom, rm);
//                                    V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
//
//                                    String ls = "lighting(i=" + Math_BigRational.round(lighting.getDX(oom, rm), -4, rm).toString()
//                                            + "_j=" + Math_BigRational.round(lighting.getDY(oom, rm), -4, rm).toString()
//                                            + "_k=" + Math_BigRational.round(lighting.getDZ(oom, rm), -4, rm).toString()
//                                            + ")_ambientLight(" + Math_BigRational.round(ambientLight, -4, rm).toString()
//                                            + ")";
//                                    Path dir = Paths.get(outDataDir.toString(), name, "files", "oom=" + oom, ls, "nset");
//                                    if (castShadow) {
//                                        dir = Paths.get(dir.toString(), "shadow");
//                                    }
//                                    r.output = Paths.get(dir.toString(),
//                                            filename
//                                            + "_" + size.width + "x" + size.height
//                                            + "_pt(i=" + Math_BigRational.round(pt.getX(oom, rm), -4, rm).toString()
//                                            + "_j=" + Math_BigRational.round(pt.getY(oom, rm), -4, rm).toString()
//                                            + "_k=" + Math_BigRational.round(pt.getZ(oom, rm), -4, rm).toString()
//                                            + ")_" + ls + "_oom=" + oom + ".png");
//                                    r.run(size, lighting, ambientLight, castShadow, oom, rm);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void runKatrina(String[] args, int oom, RoundingMode rm, V3D_Environment env,
            V3D_Vector offset, Path inDataDir, Path outDataDir) {
        try {
//                int oom = -8; //int oom = -8; //int n = 1;
//                int n = 10;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "Hurricane_Katrina";
//                String filename = "Katrina";
//                boolean assessTopology = false;
//                boolean castShadow = false;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
//                //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
//                Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
//                Color color = Color.YELLOW;
//                // Init universe
//                Universe universe = new Universe(input, V3D_Vector.ZERO, color,
//                        assessTopology, oom, rm);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
//                BigRational radius = universe.aabb.getPointsArray(oom, rm)[0]
//                        .getDistance(centroid, oom, rm);
         ////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
////                                V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
//                V3D_Vector direction = new V3D_Vector(1, 1, 1).getUnitVector(oom, rm);
//                V3D_Point pt = getCameraPt(centroid, direction,
//                        radius.multiply(2), oom, rm);
//                // Render the image
//                RenderImage r = new RenderImage(universe, pt, size, oom, rm);
//                V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
//                String ls = "lighting(i=" + lighting.getDX(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_j=" + lighting.getDY(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_k=" + lighting.getDZ(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + ")_ambientLight(" + ambientLight.round(-4, rm) + ")";
//                Path dir = Paths.get(outDataDir.toString(), name, "files", "oom=" + oom, ls);
//                if (castShadow) {
//                    dir = Paths.get(dir.toString(), "shadow");
//                }
//                r.output = Paths.get(dir.toString(),
//                        filename
//                        + "_" + r.size.width + "x" + r.size.height
//                        + "pt(i=" + pt.getX(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_j=" + pt.getY(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_k=" + pt.getZ(oom, rm).round(-4, rm).getStringValue().trim()
//                        + ")_" + ls + "_oom=" + oom + ".png");
//                r.run(lighting, ambientLight, castShadow);
////                            }
////                        }
////                    }
////                }
//            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * The process for rendering and image.
     *
     * @throws Exception
     */
    public void run(Dimension size, V3D_Vector lighting, BigRational ambientLight,
            boolean castShadow, int oom, RoundingMode rm) throws Exception {
        run(size, lighting, ambientLight, castShadow, false, oom, rm);
    }

    /**
     * The process for rendering and image.
     *
     * @throws Exception
     */
    public void run(Dimension size, V3D_Vector lighting, BigRational ambientLight,
            boolean castShadow, boolean addGraticules, int oom, RoundingMode rm) throws Exception {
        int[] pix = universe.camera.render(this.universe, lighting, ambientLight, castShadow, addGraticules, oom, rm);
        /**
         * Convert pix to an image and write to a file.
         */
        MemoryImageSource m = new MemoryImageSource(size.width, size.height, pix, 0, size.width);
        Panel panel = new Panel();
        Image image = panel.createImage(m);
        IO.imageToFile(image, "png", this.output);
        System.out.println("Rendered");
    }

    /**
     * Get the focal point for a camera.
     *
     * @param pt The point the camera is pointing towards.
     * @param v The vector from pt in the direction of the camera screen.
     * @param distance The distance from the camera screen that the focus is.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The focal point for the camera.
     */
    public static V3D_Point getCameraPt(V3D_Point pt, V3D_Vector v,
            BigRational distance, int oom, RoundingMode rm) {
        V3D_Point r = new V3D_Point(pt);
        r.translate(v.multiply(distance, oom, rm), oom, rm);
        return r;
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public static void addPoints0(Universe universe, int oom, RoundingMode rm) {
        double scale = 100d;
        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j += 2) {
                for (int k = -1; k < 2; k += 2) {
                    universe.addPoint(new V3D_Point(
                            universe.env,
                            i * scale,
                            j * scale,
                            k * scale), oom, rm);
                }
            }
        }
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public static void addLines0(Universe universe, int oom, RoundingMode rm) {
        double scale = 100d;
//        V3D_Point pv = new V3D_Point(universe.env, -1d * scale, -1d * scale, 0d);
//        V3D_Point q = new V3D_Point(universe.env, -1d * scale, 1d * scale, 0d);
//        V3D_Point r = new V3D_Point(universe.env, 1d * scale, 1d * scale, 0d);
//        V3D_Point s = new V3D_Point(universe.env, 1d * scale, -1d * scale, 0d);
//        V3D_LineSegment pq = new V3D_LineSegment(pv, q, oom, rm);
//        V3D_LineSegment qr = new V3D_LineSegment(q, r, oom, rm);
//        V3D_LineSegment rs = new V3D_LineSegment(r, s, oom, rm);
//        V3D_LineSegment sp = new V3D_LineSegment(s, pv, oom, rm);
//        V3D_LineSegment pr = new V3D_LineSegment(pv, r, oom, rm);
//        V3D_LineSegment qs = new V3D_LineSegment(q, s, oom, rm);
//        universe.addLine(pq, Color.WHITE, oom, rm);
//        universe.addLine(qr, Color.WHITE, oom, rm);
//        universe.addLine(rs, Color.WHITE, oom, rm);
//        universe.addLine(sp, Color.WHITE, oom, rm);
//        universe.addLine(pr, Color.WHITE, oom, rm);
//        universe.addLine(qs, Color.WHITE, oom, rm);
        // Cube
        // Points
        V3D_Point lbf = new V3D_Point(universe.env, new V3D_Vector(-1 * scale, -1 * scale, -1 * scale));
        V3D_Point lba = new V3D_Point(universe.env, new V3D_Vector(-1 * scale, -1 * scale, 1 * scale));
        V3D_Point ltf = new V3D_Point(universe.env, new V3D_Vector(-1 * scale, 1 * scale, -1 * scale));
        V3D_Point lta = new V3D_Point(universe.env, new V3D_Vector(-1 * scale, 1 * scale, 1 * scale));
        V3D_Point rbf = new V3D_Point(universe.env, new V3D_Vector(1 * scale, -1 * scale, -1 * scale));
        V3D_Point rba = new V3D_Point(universe.env, new V3D_Vector(1 * scale, -1 * scale, 1 * scale));
        V3D_Point rtf = new V3D_Point(universe.env, new V3D_Vector(1 * scale, 1 * scale, -1 * scale));
        V3D_Point rta = new V3D_Point(universe.env, new V3D_Vector(1 * scale, 1 * scale, 1 * scale));
        // Lines
        universe.addLine(new V3D_LineSegment(lbf, lba, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(lbf, ltf, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(lbf, rbf, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(lba, lta, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(lba, rba, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(ltf, lta, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(ltf, rtf, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(lta, rta, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(rbf, rba, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(rbf, rtf, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(rba, rta, oom, rm), Color.WHITE, oom, rm);
        universe.addLine(new V3D_LineSegment(rtf, rta, oom, rm), Color.WHITE, oom, rm);
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public static void addAxes(Universe universe, int oom, RoundingMode rm) {
        BigRational xmin = universe.aabb.getXMin(oom, rm);
        BigRational xmax = universe.aabb.getXMax(oom, rm);
        BigRational ymin = universe.aabb.getYMin(oom, rm);
        BigRational ymax = universe.aabb.getYMax(oom, rm);
        BigRational zmin = universe.aabb.getZMin(oom, rm);
        BigRational zmax = universe.aabb.getZMax(oom, rm);
        V3D_LineSegment xaxis = new V3D_LineSegment(
                new V3D_Point(universe.env, xmin, BigRational.ZERO, BigRational.ZERO),
                new V3D_Point(universe.env, xmax, BigRational.ZERO, BigRational.ZERO), oom, rm);
        universe.addLine(xaxis, Color.BLUE, oom, rm);
        V3D_LineSegment yaxis = new V3D_LineSegment(
                new V3D_Point(universe.env, BigRational.ZERO, ymin, BigRational.ZERO),
                new V3D_Point(universe.env, BigRational.ZERO, ymax, BigRational.ZERO), oom, rm);
        universe.addLine(yaxis, Color.RED, oom, rm);
        V3D_LineSegment zaxis = new V3D_LineSegment(
                new V3D_Point(universe.env, BigRational.ZERO, BigRational.ZERO, zmin),
                new V3D_Point(universe.env, BigRational.ZERO, BigRational.ZERO, zmax), oom, rm);
        universe.addLine(zaxis, Color.GREEN, oom, rm);
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public static void addRectangles0(Universe universe, int oom, RoundingMode rm) {
        V3D_Rectangle r = new V3D_Rectangle(
                new V3D_Point(universe.env, -100d, -100d, 0d),
                new V3D_Point(universe.env, -100d, 100d, 0d),
                new V3D_Point(universe.env, 100d, 100d, 0d),
                new V3D_Point(universe.env, 100d, -100d, 0d), oom, rm);
        universe.addArea(r, Color.GRAY, oom, rm);
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public static void addPolygons00(Universe universe, int oom, RoundingMode rm) {
        double scale = 10d;
        V3D_Point[] pts = new V3D_Point[8];
        pts[0] = new V3D_Point(universe.env, -8d * scale, -8d * scale, 0d * scale);
        pts[1] = new V3D_Point(universe.env, -4d * scale, 0d * scale, 0d * scale);
        pts[2] = new V3D_Point(universe.env, -8d * scale, 8d * scale, 0d * scale);
        pts[3] = new V3D_Point(universe.env, 0d * scale, 4d * scale, 0d * scale);
        pts[4] = new V3D_Point(universe.env, 8d * scale, 8d * scale, 0d * scale);
        pts[5] = new V3D_Point(universe.env, 4d * scale, 0d * scale, 0d * scale);
        pts[6] = new V3D_Point(universe.env, 8d * scale, -8d * scale, 0d * scale);
        pts[7] = new V3D_Point(universe.env, 0d * scale, -4d * scale, 0d * scale);
        V3D_PolygonNoInternalHoles polygon = new V3D_PolygonNoInternalHoles(
                pts, V3D_Plane.Z0.getN(), oom, rm);
        //Color c = Color.GRAY;
        Color c = Color.WHITE;
        universe.addArea(polygon, c, oom, rm);
        // Edges
        //Color ec = Color.DARK_GRAY;
        Color ec = Color.PINK;
        polygon.getEdges(oom, rm).values().forEach(x
                -> universe.addLine(x, ec, oom, rm)
        );
        //System.out.println("Polygon");
        //System.out.println(polygon.toString());
        //System.out.println("External holes");
        //polygon.externalHoles.values().forEach(x -> 
        //    System.out.println(x.toString())
        //);
        //System.out.println("External holes");
    }

    /**
     * Cube surface comprised of triangles.
     *
     * @param universe
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public static void addPolygons0(Universe universe, int oom, RoundingMode rm) {
        double scale = 50d;
        V3D_Vector offset = new V3D_Vector(0d, 0d, 0d);
        V3D_Point l = new V3D_Point(universe.env, offset, new V3D_Vector(-2 * scale, 0d, 0d));
        V3D_Point r = new V3D_Point(universe.env, offset, new V3D_Vector(2 * scale, 0d, 0d));
        V3D_Point b = new V3D_Point(universe.env, offset, new V3D_Vector(0d, -2 * scale, 0d));
        V3D_Point t = new V3D_Point(universe.env, offset, new V3D_Vector(0d, 2 * scale, 0d));
        V3D_Point f = new V3D_Point(universe.env, offset, new V3D_Vector(0d, 0d, 2 * scale));
        V3D_Point a = new V3D_Point(universe.env, offset, new V3D_Vector(0d, 0d, -2 * scale));
        V3D_Point lbf = new V3D_Point(universe.env, offset, new V3D_Vector(-scale, -scale, -scale));
        V3D_Point lba = new V3D_Point(universe.env, offset, new V3D_Vector(-scale, -scale, scale));
        V3D_Point ltf = new V3D_Point(universe.env, offset, new V3D_Vector(-scale, scale, -scale));
        V3D_Point lta = new V3D_Point(universe.env, offset, new V3D_Vector(-scale, scale, scale));
        V3D_Point rbf = new V3D_Point(universe.env, offset, new V3D_Vector(scale, -scale, -scale));
        V3D_Point rba = new V3D_Point(universe.env, offset, new V3D_Vector(scale, -scale, scale));
        V3D_Point rtf = new V3D_Point(universe.env, offset, new V3D_Vector(scale, scale, -scale));
        V3D_Point rta = new V3D_Point(universe.env, offset, new V3D_Vector(scale, scale, scale));
        universe.addArea(new V3D_Triangle(f, lbf, ltf, rtf, oom, rm), Color.BLUE, oom, rm);
        universe.addArea(new V3D_Triangle(f, lbf, rbf, rtf, oom, rm), Color.BLUE, oom, rm);
        universe.addArea(new V3D_Triangle(l, lbf, ltf, lta, oom, rm), Color.RED, oom, rm);
        universe.addArea(new V3D_Triangle(l, lbf, lba, lta, oom, rm), Color.RED, oom, rm);
        universe.addArea(new V3D_Triangle(a, lba, lta, rta, oom, rm), Color.YELLOW, oom, rm);
        universe.addArea(new V3D_Triangle(a, lba, rba, rta, oom, rm), Color.YELLOW, oom, rm);
        universe.addArea(new V3D_Triangle(r, rbf, rtf, rta, oom, rm), Color.GREEN, oom, rm);
        universe.addArea(new V3D_Triangle(r, rbf, rta, rba, oom, rm), Color.GREEN, oom, rm);
        universe.addArea(new V3D_Triangle(t, ltf, lta, rta, oom, rm), Color.ORANGE, oom, rm);
        universe.addArea(new V3D_Triangle(t, rtf, ltf, rta, oom, rm), Color.ORANGE, oom, rm);
        universe.addArea(new V3D_Triangle(b, lbf, rbf, rba, oom, rm), Color.PINK, oom, rm);
        universe.addArea(new V3D_Triangle(b, lbf, lba, rba, oom, rm), Color.PINK, oom, rm);
    }

    /**
     *
     * @param focus
     * @param aabb
     * @param zoomFactor
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return
     */
    private static V3D_Rectangle getRect(V3D_Point focus,
            V3D_AABB aabb, double zoomFactor, int oom, RoundingMode rm) {
        // Need something orthoganol to pt and ve centroid
        V3D_Plane pl = new V3D_Plane(focus,
                new V3D_Vector(focus, aabb.getCentroid(oom, rm), oom, rm));
        V3D_Vector pv = pl.getPV(oom, rm);
        //return = ve.getViewport2(pt, pv);
        return aabb.getViewport3(focus, pv, oom, rm);
    }
}
