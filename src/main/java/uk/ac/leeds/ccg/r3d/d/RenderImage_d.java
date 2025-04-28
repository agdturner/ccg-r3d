/*
 * Copyright 2021-2025 Andy Turner, University of Leeds.
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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.MemoryImageSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.leeds.ccg.r3d.io.IO;
import uk.ac.leeds.ccg.v3d.core.d.V3D_Environment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_AABB_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineSegment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Plane_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Point_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PolygonNoInternalHoles_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Ray_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Rectangle_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Triangle_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Vector_d;

public class RenderImage_d {

    /**
     * Universe.
     */
    public Universe_d universe;

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
     * @param epsilon The tolerance within which two vector coordinates are
     * considered equal.
     * @throws Exception
     */
    public RenderImage_d(Universe_d universe, V3D_Vector_d offset,
            V3D_Point_d focus, Dimension dim, V3D_Rectangle_d screen,
            double epsilon) throws Exception {
        this.universe = universe;
        universe.setCamera(new Camera_d(universe.env, offset, focus, screen, dim));
    }

    public static void main(String[] args) {
        try {
            //boolean run00 = true;
            boolean run00 = false;
            //boolean run0 = true;
            boolean run0 = false;
            //boolean run1 = true;
            boolean run1 = false;
            boolean runUtah = true;
            //boolean runUtah = false;
            //boolean runGeographos = true;
            boolean runGeographos = false;
            //boolean runKatrina = true;
            boolean runKatrina = false;
            //boolean runCuriosity = true;
            boolean runCuriosity = false;
            //boolean runApollo17 = true;
            boolean runApollo17 = false;
            //boolean run3dcityloader = true;
            boolean run3dcityloader = false;
            //boolean runGlacier = true;
            boolean runGlacier = false;
            boolean runGSHHS = true;
            // boolean runGSHHS = false;

            V3D_Environment_d env = new V3D_Environment_d();
            V3D_Vector_d offset = V3D_Vector_d.ZERO;

            Path inDataDir = Paths.get("data", "input");
            Path outDataDir = Paths.get("data", "output", "d");

            if (run00) {
                /**
                 * Renders axes and a flat shape in the edges of a cube aligned with the axes.
                 */
                double epsilon = 1d / 1000000d;
                int nrows = 200;
                int ncols = 200;
                //int nrows = 20;
                //int ncols = 20;
                // Init universe
                Universe_d universe = new Universe_d(env, offset, epsilon);
                //addPoints0(universe, epsilon);
                //addTriangle0(universe, epsilon);
                //addRectangles0(universe, epsilon);
                addLines0(universe, epsilon);
                addPolygons00(universe, epsilon);
                addAxes(universe, epsilon);

                // Detail the camera
                Dimension dim = new Dimension(ncols, nrows);
                V3D_Point_d centroid = universe.aabb.getCentroid();
                double radius = universe.aabb.getPointsArray()[0].getDistance(centroid);
                String name = "axes";
                //boolean addGraticules = true;
                boolean addGraticules = false;
                int i = 0;
                int j = 0;
                int k = -1;
                System.out.println("i=" + i + ", j=" + j + ", k=" + k);
                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
                V3D_Point_d focus = getCameraPt(centroid, direction, radius * 2d);
                // Render the image
//                double zoomFactor = 2d;
//                V3D_Rectangle_d rect = universe.aabb.getViewport3(focus, V3D_Vector_d.I, zoomFactor, epsilon);
                V3D_Rectangle_d rect = new V3D_Rectangle_d(
                        new V3D_Point_d(env, -100d, -100d, -radius),
                        new V3D_Point_d(env, -100d, 100d, -radius),
                        new V3D_Point_d(env, 100d, 100d, -radius),
                        new V3D_Point_d(env, 100d, -100d, -radius));
                RenderImage_d r = new RenderImage_d(universe, offset, focus, dim, rect, epsilon);
                Path dir = Paths.get(outDataDir.toString(), "test", name);
                r.output = Paths.get(dir.toString(),
                        "test.png");
                /**
                 * AmbientLight makes dark surfaces lighter even if they are
                 * orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
                r.run(dim, lighting, ambientLight, false, addGraticules, epsilon);

                //i = -1;
                //j = 0;
                //k = -1;
                //System.out.println("i=" + i + ", j=" + j + ", k=" + k);
                //direction = new V3D_Vector_d(i, j, k).getUnitVector();
                //focus = getCameraPt(centroid, direction, radius * 2d);
                V3D_Ray_d xRay = new V3D_Ray_d(new V3D_Point_d(universe.env, 0, 0, 0),
                        new V3D_Point_d(universe.env, 1, 0, 0));
                V3D_Vector_d xuv = xRay.l.v.getUnitVector();
                V3D_Ray_d yRay = new V3D_Ray_d(new V3D_Point_d(universe.env, 0, 0, 0),
                        new V3D_Point_d(universe.env, 0, 1, 0));
                V3D_Vector_d yuv = yRay.l.v.getUnitVector();
                V3D_Ray_d zRay = new V3D_Ray_d(new V3D_Point_d(universe.env, 0, 0, 0),
                        new V3D_Point_d(universe.env, 0, 0, 1));
                V3D_Vector_d zuv = zRay.l.v.getUnitVector();
//                double anglei = 1d;
//                double anglei = 2d;
//                double anglei = 4d;
                double anglei = 8d;
//                double anglei = 16d;
                double anglei2 = anglei * 2d;
                //double angle = Math.PI;
                //double angle = Math.PI/4d;
                double angle = Math.PI / anglei;

                V3D_Rectangle_d rectr;
                V3D_Point_d focusr;

                for (i = 0; i < anglei2; i++) {
                    //rect = rect.rotate(xRay, xuv, angle, epsilon);
                    //focus = focus.rotate(xRay, xuv, angle, epsilon);
                    for (j = 0; j < anglei2; j++) {
                        //rect = rect.rotate(yRay, yuv, angle, epsilon);
                        //focus = focus.rotate(yRay, yuv, angle, epsilon);
                        for (k = 0; k < anglei2; k++) {
                            //rect = rect.rotate(zRay, zuv, angle, epsilon);
                            //focus = focus.rotate(zRay, zuv, angle, epsilon);
                            rectr = rect.rotate(zRay, zuv, angle * k, epsilon);
                            focusr = focus.rotate(zRay, zuv, angle * k, epsilon);
                            rectr = rectr.rotate(xRay, xuv, angle * i, epsilon);
                            focusr = focusr.rotate(xRay, xuv, angle * i, epsilon);
                            rectr = rectr.rotate(yRay, yuv, angle * j, epsilon);
                            focusr = focusr.rotate(yRay, yuv, angle * j, epsilon);
                            r = new RenderImage_d(universe, offset, focusr, dim, rectr, epsilon);
                            dir = Paths.get(outDataDir.toString(), "test", name + "r");
                            r.output = Paths.get(dir.toString(),
                                    "test_i" + i + "_j" + j + "_k" + k + ".png");
                            r.run(dim, lighting, ambientLight, false, addGraticules, epsilon);
                        }
                    }
                }
            }

            if (run0) {
                //double epsilon = 1d / 10000000d;
                //double epsilon = 1d / 100000000000d;
                double epsilon = 1d / 10000d;
                //double epsilon = 0d;
                int nrows = 200;
                int ncols = 200;
                // Init universe
                Universe_d universe = new Universe_d(env, offset, epsilon);
                //addPoints0(universe, epsilon);
                //addTriangle0(universe, epsilon);
                //addRectangles0(universe, epsilon);
                //addLines0(universe, epsilon);
                addPolygons0(universe, epsilon);
                addAxes(universe, epsilon);

                // Detail the camera
                Dimension dim = new Dimension(ncols, nrows);
                V3D_Point_d centroid = universe.aabb.getCentroid();
                double radius = universe.aabb.getPointsArray()[0].getDistance(centroid);
                String name = "triangles";
                //boolean addGraticules = true;
                boolean addGraticules = false;
                int i = 0;
                int j = 0;
                int k = -1;
                System.out.println("i=" + i + ", j=" + j + ", k=" + k);
                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
                V3D_Point_d focus = getCameraPt(centroid, direction, radius * 2d);
                // Render the image
//                double zoomFactor = 2d;
//                V3D_Rectangle_d rect = universe.aabb.getViewport3(focus, V3D_Vector_d.I, zoomFactor, epsilon);
                V3D_Rectangle_d rect = new V3D_Rectangle_d(
                        new V3D_Point_d(env, -100d, -100d, -radius),
                        new V3D_Point_d(env, -100d, 100d, -radius),
                        new V3D_Point_d(env, 100d, 100d, -radius),
                        new V3D_Point_d(env, 100d, -100d, -radius));
                RenderImage_d r = new RenderImage_d(universe, offset, focus, dim, rect, epsilon);
                Path dir = Paths.get(outDataDir.toString(), "test", name);
                r.output = Paths.get(dir.toString(),
                        "test.png");
                /**
                 * AmbientLight makes dark surfaces lighter even if they are
                 * orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
                r.run(dim, lighting, ambientLight, false, addGraticules, epsilon);

                //i = -1;
                //j = 0;
                //k = -1;
                //System.out.println("i=" + i + ", j=" + j + ", k=" + k);
                //direction = new V3D_Vector_d(i, j, k).getUnitVector();
                //focus = getCameraPt(centroid, direction, radius * 2d);
                V3D_Ray_d xRay = new V3D_Ray_d(new V3D_Point_d(universe.env, 0, 0, 0),
                        new V3D_Point_d(universe.env, 1, 0, 0));
                V3D_Vector_d xuv = xRay.l.v.getUnitVector();
                V3D_Ray_d yRay = new V3D_Ray_d(new V3D_Point_d(universe.env, 0, 0, 0),
                        new V3D_Point_d(universe.env, 0, 1, 0));
                V3D_Vector_d yuv = yRay.l.v.getUnitVector();
                V3D_Ray_d zRay = new V3D_Ray_d(new V3D_Point_d(universe.env, 0, 0, 0),
                        new V3D_Point_d(universe.env, 0, 0, 1));
                V3D_Vector_d zuv = zRay.l.v.getUnitVector();
//                double anglei = 1d;
//                double anglei = 2d;
//                double anglei = 4d;
                double anglei = 8d;
//                double anglei = 16d;
                double anglei2 = anglei * 2d;
                //double angle = Math.PI;
                //double angle = Math.PI/4d;
                double angle = Math.PI / anglei;

                V3D_Rectangle_d rectr;
                V3D_Point_d focusr;
                
                //i = 1;
                for (i = 0; i < anglei2; i++) {
                    //rect = rect.rotate(xRay, xuv, angle, epsilon);
                    //focus = focus.rotate(xRay, xuv, angle, epsilon);
                    //j = 1;
                    for (j = 0; j < anglei2; j++) {
                        //rect = rect.rotate(yRay, yuv, angle, epsilon);
                        //focus = focus.rotate(yRay, yuv, angle, epsilon);
                        //k = 10;
                        for (k = 0; k < anglei2; k++) {
                            //rect = rect.rotate(zRay, zuv, angle, epsilon);
                            //focus = focus.rotate(zRay, zuv, angle, epsilon);
                            rectr = rect.rotate(zRay, zuv, angle * k, epsilon);
                            focusr = focus.rotate(zRay, zuv, angle * k, epsilon);
                            rectr = rectr.rotate(xRay, xuv, angle * i, epsilon);
                            focusr = focusr.rotate(xRay, xuv, angle * i, epsilon);
                            rectr = rectr.rotate(yRay, yuv, angle * j, epsilon);
                            focusr = focusr.rotate(yRay, yuv, angle * j, epsilon);
                            r = new RenderImage_d(universe, offset, focusr, dim, rectr, epsilon);
                            dir = Paths.get(outDataDir.toString(), "test", name + "r");
                            r.output = Paths.get(dir.toString(),
                                    "test_i" + i + "_j" + j + "_k" + k + ".png");
                            r.run(dim, lighting, ambientLight, false, addGraticules, epsilon);
                        }
                    }
                }
            }

            if (runGSHHS) {
//                //double epsilon = 1d / 10000000d;
//                //double epsilon = 1d / 100000000000d;
//                double epsilon = 1d / 10000d;
//                //double epsilon = 0d;
//                int scale = 10;
//                int nrows;
//                int ncols;
//                // Init universe
//                String gshhs_name = "gshhs_l";
//                Path dir = Paths.get("data", "input", "gshhg-bin-2.3.7");
//        Path filepath = Paths.get(dir.toString(), gshhs_name + ".b");
//        V3D_Point_d[] points = null;
//        GSHHG_d gshhg = new GSHHG_d(filepath, env, scale, epsilon);
//        HashMap<Integer, V3D_Polygon_d> polygons = gshhg.polygons;
//        for (V3D_Polygon_d p : polygons.values()) {
//            universe.addPolygon(p);
//        }
//                nrows = 15 * scale;
//                ncols = 14 * scale;
//                // GB
//                  double  ymin = 47 * scale;
//                  double  ymax = 62 * scale;
//                  double  xmin = -10 * scale;
//                  double  xmax = 4 * scale;
//                V3D_Point_d lb = new V3D_Point_d(env, offset, new V3D_Vector_d(xmin, ymin));
//        V3D_Point_d lt = new V3D_Point_d(env, offset, new V3D_Vector_d(xmin, ymax));
//        V3D_Point_d rt = new V3D_Point_d(env, offset, new V3D_Vector_d(xmax, ymax));
//        V3D_Point_d rb = new V3D_Point_d(env, offset, new V3D_Vector_d(xmax, ymin));
//        V3D_Rectangle_d window = new V3D_Rectangle_d(lb, lt, rt, rb);
//        Universe_d universe = new Universe_d(window.getAABB());
//        
//                Universe_d universe = new Universe_d(env, V3D_Vector_d.ZERO, epsilon);
//                // Detail the camera
//                Dimension size = new Dimension(ncols, nrows);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
//                //String name = "tetras6";
//                //String name = "tetras5";
//                String name = "triangles";
//                boolean castShadow = false;
//                double zoomFactor = 1.0d;
//                double distance = 2.0d;
//                //boolean addGraticules = true;
//                boolean addGraticules = false;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                double ambientLight = 1d / 20d;

            
            ////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
//                                int i = 0;
//                                int j = 0;
//                                int k = 1;
//                                System.out.println("i=" + i + ", j=" + j + ", k=" + k);
//                                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                                V3D_Point_d pt = getCameraPt(centroid, direction, radius * distance);
//                                // Render the image
//                                RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, null, epsilon);
//                                V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
//                                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                                        + "_j=" + String.format("%,.2f", lighting.dy)
//                                        + "_k=" + String.format("%,.2f", lighting.dz)
//                                        + ")_ambientLight(" + ambientLight + ")";
//                                Path dir = Paths.get(outDataDir.toString(), "test", name, "epsilon=" + epsilon, ls,
//                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
//                                if (castShadow) {
//                                    dir = Paths.get(dir.toString(), "shadow");
//                                }
//                                r.output = Paths.get(dir.toString(),
//                                        "test_" + r.universe.camera.nrows + "x"
//                                        + "_" + size.width + "x" + size.height
//                                        + "_pt(i=" + String.format("%,.2f", pt.getX())
//                                        + "_j=" + String.format("%,.2f", pt.getY())
//                                        + "_k=" + String.format("%,.2f", pt.getZ())
//                                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                                r.run(size, lighting, ambientLight, castShadow, addGraticules, epsilon);
////                            }
////                        }
////                    }
////                }
            }
            
            
//            if (run1) {
//                double epsilon = 1d / 10000000d;
//                int n = 1;
//                //n = 5;
//                int w = 100 * n;
//                int h = 100 * n;
//                //int w = 100 * n;
//                //int h = 75 * n;
//                boolean addGraticules = true;
//                boolean assessTopology = false;
//                boolean castShadow = true;
//                // Zoom factor should be at least as big as distance?
//                double zoomFactor = 1d;
//                //double zoomFactor = 2d;
//                //double zoomFactor = 1.5d;
//                //double distance = 3d;
//                double distance = 2d;
//                //double distance = 0.5d;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                //double ambientLight = 0d;
//                double ambientLight = 1d / 20d;
//                //double ambientLight = 1d / 15d;
//                //double ambientLight = 1d / 2d;
//                //double ambientLight = 1d;
//                String name = "3361664_Platonic_Solid_Collection";
//                String filename = "Icosahedron";
//                Path input = Paths.get(inDataDir.toString(), name, "files", filename + ".stl");
//                Color color = Color.YELLOW;
//                // Init universe
//                Universe_d universe = new Universe_d(env, input,
//                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
//                        false);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
//                V3D_Vector_d lighting = new V3D_Vector_d(-1, -5, -3).getUnitVector();
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                                V3D_Point_d pt = getCameraPt(centroid, direction,
//                                        radius * distance);
//                                // Render the image
//                                RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, null, epsilon);
//                                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                                        + "_j=" + String.format("%,.2f", lighting.dy)
//                                        + "_k=" + String.format("%,.2f", lighting.dz)
//                                        + ")_ambientLight(" + ambientLight + ")";
//                                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
//                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
//                                if (castShadow) {
//                                    dir = Paths.get(dir.toString(), "shadow");
//                                }
//                                r.output = Paths.get(dir.toString(),
//                                        filename
//                                        + "_" + size.width + "x" + size.height
//                                        + "_pt(i=" + String.format("%,.2f", pt.getX())
//                                        + "_j=" + String.format("%,.2f", pt.getY())
//                                        + "_k=" + String.format("%,.2f", pt.getZ())
//                                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                                r.run(size, lighting, ambientLight, castShadow, addGraticules, epsilon);
//                            }
//                        }
//                    }
//                }
//            }

            if (runUtah) {
                double epsilon = 1d / 10000000d;
                int n = 1;
                //n = 2;
                int w = 200 * n; //500
                int h = 200 * n;  //375 187.5
                boolean assessTopology = false;
                boolean castShadow = false;
                double zoomFactor = 1.0d;
                double distance = 2.0d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                String name = "Utah_teapot_(solid)";
                Color color = Color.YELLOW;
                Path input = Paths.get(inDataDir.toString(), name, name + ".stl");
                // Init universe
                Universe_d universe = new Universe_d(env, input,
                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension dim = new Dimension(w, h);
                V3D_Point_d centroid = universe.aabb.getCentroid();
                double radius = universe.aabb.getPointsArray()[0].getDistance(centroid);
                V3D_Point_d focus = new V3D_Point_d(env, 0d, 0d, -20);
                double x = 10;
                V3D_Rectangle_d rect = new V3D_Rectangle_d(
                        new V3D_Point_d(env, -x, -x, -x),
                        new V3D_Point_d(env, -x, x, -x),
                        new V3D_Point_d(env, x, x, -x),
                        new V3D_Point_d(env, x, -x, -x));
                //for (int i = -1; i <= 1; i++) {
                //    for (int j = -1; j <= 1; j++) {
                //        for (int k = -1; k <= 1; k++) {
                //            if (!(i == 0 && j == 0 && k == 0)) {
                //                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
                //V3D_Vector_d direction = new V3D_Vector_d(1, 0, 0).getUnitVector();
                //V3D_Point_d pt = getCameraPt(centroid, direction, radius * distance);
                //V3D_Rectangle_d rect = universe.aabb.getViewport3(pt, V3D_Vector_d.I, zoomFactor, epsilon);
                //V3D_Rectangle_d rect = new V3D_Rectangle_d(
                //    new V3D_Point_d(env, -100d, -100d, -radius),
                //    new V3D_Point_d(env, -100d, 100d, -radius),
                //    new V3D_Point_d(env, 100d, 100d, -radius),
                //    new V3D_Point_d(env, 100d, -100d, -radius));
                // Render the image
                RenderImage_d r = new RenderImage_d(universe, offset, focus, dim, rect, epsilon);

                //V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
                V3D_Vector_d lighting = new V3D_Vector_d(1, 2, 3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "epsilon=" + epsilon, ls,
                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                if (castShadow) {
                    dir = Paths.get(dir.toString(), "shadow");
                }
                r.output = Paths.get(dir.toString(), "test.png");
                //name
                //+ "_" + size.width + "x" + size.height
                //+ "_pt(i=" + String.format("%,.2f", pt.getX())
                //+ "_j=" + String.format("%,.2f", pt.getY())
                //+ "_k=" + String.format("%,.2f", pt.getZ())
                //+ ")_" + ls + "_epsilon=" + epsilon + ".png");
                r.run(dim, lighting, ambientLight, castShadow, epsilon);
                //r.run(dim, lighting, ambientLight, castShadow, epsilon);
                //            }
                //        }
                //    }
                //}
            }
//
//            if (runGeographos) {
//                double epsilon = 1d / 10000000d;
//                int n = 1;
//                n = 5;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "geographos";
//                String filename = "1620geographos";
//                boolean assessTopology = false;
//                boolean castShadow = true;
//                double zoomFactor = 1.0d;
//                double distance = 2.0d;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                double ambientLight = 1d / 20d;
//                Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
//                Color color = Color.YELLOW;
//                // Init universe
//                Universe_d universe = new Universe_d(env, input,
//                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
//                        false);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                if (!(i == -1 && j == 1 && k == 1)) {
         ////                int i = -1;
////                int j = 1;
////                int k = 1;
//                                    V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                                    V3D_Point_d pt = getCameraPt(centroid, direction,
//                                            radius * distance);
//                                    // Render the image
//                                    RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, null, epsilon);
//                                    V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
//                                    String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                                            + "_j=" + String.format("%,.2f", lighting.dy)
//                                            + "_k=" + String.format("%,.2f", lighting.dz)
//                                            + ")_ambientLight(" + ambientLight + ")";
//                                    Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls, "nset",
//                                            "zoomFactor=" + zoomFactor, "distance=" + distance);
//                                    if (castShadow) {
//                                        dir = Paths.get(dir.toString(), "shadow");
//                                    }
//                                    r.output = Paths.get(dir.toString(),
//                                            filename
//                                            + "_" + size.width + "x" + size.height
//                                            + "_pt(i=" + String.format("%,.2f", pt.getX())
//                                            + "_j=" + String.format("%,.2f", pt.getY())
//                                            + "_k=" + String.format("%,.2f", pt.getZ())
//                                            + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                                    r.run(size, lighting, ambientLight, castShadow, epsilon);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (runKatrina) {
//                double epsilon = 1d / 10000000d;
//                int n = 10;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "Hurricane_Katrina";
//                String filename = "Katrina";
//                boolean assessTopology = false;
//                boolean castShadow = false;
//                double zoomFactor = 1.0d;
//                double distance = 2.0d;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                double ambientLight = 1d / 20d;
//                Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
//                Color color = Color.YELLOW;
//                // Init universe
//                Universe_d universe = new Universe_d(env, input,
//                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
//                        false);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
////                                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                V3D_Vector_d direction = new V3D_Vector_d(1, 1, 1).getUnitVector();
//                V3D_Point_d pt = getCameraPt(centroid, direction,
//                        radius * distance);
//                // Render the image
//                RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, null, epsilon);
//                V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
//                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                        + "_j=" + String.format("%,.2f", lighting.dy)
//                        + "_k=" + String.format("%,.2f", lighting.dz)
//                        + ")_ambientLight(" + ambientLight + ")";
//                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
//                        "zoomFactor=" + zoomFactor, "distance=" + distance);
//                if (castShadow) {
//                    dir = Paths.get(dir.toString(), "shadow");
//                }
//                r.output = Paths.get(dir.toString(),
//                        filename
//                        + "_" + size.width + "x" + size.height
//                        + "pt(i=" + String.format("%,.2f", pt.getX())
//                        + "_j=" + String.format("%,.2f", pt.getY())
//                        + "_k=" + String.format("%,.2f", pt.getZ())
//                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                r.run(size, lighting, ambientLight, castShadow, epsilon);
////                            }
////                        }
////                    }
////                }
//            }
//
//            if (runCuriosity) {
//                double epsilon = 1d / 10000000d;
//                int n = 10;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "Curiosity Rover 3D Printed Model";
//                String filename = "Curiosity SM (Complete Print 200uM)";
//                boolean assessTopology = false;
//                boolean castShadow = false;
//                double zoomFactor = 1.0d;
//                double distance = 2.0d;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                double ambientLight = 1d / 20d;
//                Path input = Paths.get(inDataDir.toString(), name, name,
//                        "Simplified Curiosity Model (Small)", filename + ".stl");
//                Color color = Color.YELLOW;
//                // Init universe
//                Universe_d universe = new Universe_d(env, input,
//                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
//                        false);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
////                                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                V3D_Vector_d direction = new V3D_Vector_d(1, 1, 1).getUnitVector();
//                V3D_Point_d pt = getCameraPt(centroid, direction,
//                        radius * distance);
//                // Render the image
//                RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, null, epsilon);
//                V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
//                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                        + "_j=" + String.format("%,.2f", lighting.dy)
//                        + "_k=" + String.format("%,.2f", lighting.dz)
//                        + ")_ambientLight(" + ambientLight + ")";
//                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
//                        "zoomFactor=" + zoomFactor, "distance=" + distance);
//                if (castShadow) {
//                    dir = Paths.get(dir.toString(), "shadow");
//                }
//                r.output = Paths.get(dir.toString(),
//                        filename
//                        + "_" + size.width + "x" + size.height
//                        + "pt(i=" + String.format("%,.2f", pt.getX())
//                        + "_j=" + String.format("%,.2f", pt.getY())
//                        + "_k=" + String.format("%,.2f", pt.getZ())
//                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                r.run(size, lighting, ambientLight, castShadow, epsilon);
////                            }
////                        }
////                    }
////                }
//            }
//
//            if (runApollo17) {
//                double epsilon = 1d / 10000000d;
//                int n = 10;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "Apollo_17";
//                String filename = "Apollo_17";
//                boolean assessTopology = false;
//                boolean castShadow = false;
//                double zoomFactor = 1.0d;
//                double distance = 2.0d;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                double ambientLight = 1d / 20d;
//                Path input = Paths.get(inDataDir.toString(), name,
//                        filename + ".stl");
//                Color color = Color.WHITE;
//                // Init universe
//                Universe_d universe = new Universe_d(env, input,
//                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
//                        false);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
////                                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                V3D_Vector_d direction = new V3D_Vector_d(1, 1, 1).getUnitVector();
//                V3D_Point_d pt = getCameraPt(centroid, direction,
//                        radius * distance);
//                // Render the image
//                RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, null, epsilon);
//                V3D_Vector_d lighting = new V3D_Vector_d(-1, -2, -3).getUnitVector();
//                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                        + "_j=" + String.format("%,.2f", lighting.dy)
//                        + "_k=" + String.format("%,.2f", lighting.dz)
//                        + ")_ambientLight(" + ambientLight + ")";
//                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
//                        "zoomFactor=" + zoomFactor, "distance=" + distance);
//                if (castShadow) {
//                    dir = Paths.get(dir.toString(), "shadow");
//                }
//                r.output = Paths.get(dir.toString(),
//                        filename
//                        + "_" + size.width + "x" + size.height
//                        + "pt(i=" + String.format("%,.2f", pt.getX())
//                        + "_j=" + String.format("%,.2f", pt.getY())
//                        + "_k=" + String.format("%,.2f", pt.getZ())
//                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                r.run(size, lighting, ambientLight, castShadow, epsilon);
////                            }
////                        }
////                    }
////                }
//            }
//
//            if (run3dcityloader) {
//                //https://3dcityloader.com/en/city/worldwide
//                double epsilon = 1d / 10000000d;
//                int n = 10;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "3dcityloader";
//                String filename = "76d5e2d2-7410-4ab3-bd07-9faeeb08d4c4";
//                boolean assessTopology = false;
//                boolean castShadow = false;
//                double zoomFactor = 1.0d;
//                double distance = 2.0d;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                double ambientLight = 1d / 20d;
//                Path input = Paths.get(inDataDir.toString(), name, filename,
//                        filename + ".stl");
//                Color color = Color.WHITE;
//                // Init universe
//                Universe_d universe = new Universe_d(env, input,
//                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
//                        false);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
////                                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                V3D_Vector_d direction = new V3D_Vector_d(1, 1, 1).getUnitVector();
//                V3D_Point_d pt = getCameraPt(centroid, direction,
//                        radius * distance);
//                // Render the image
//                RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, null, epsilon);
//                V3D_Vector_d lighting = new V3D_Vector_d(1, -2, 3).getUnitVector();
//                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                        + "_j=" + String.format("%,.2f", lighting.dy)
//                        + "_k=" + String.format("%,.2f", lighting.dz)
//                        + ")_ambientLight(" + ambientLight + ")";
//                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
//                        "zoomFactor=" + zoomFactor, "distance=" + distance);
//                if (castShadow) {
//                    dir = Paths.get(dir.toString(), "shadow");
//                }
//                r.output = Paths.get(dir.toString(),
//                        filename
//                        + "_" + size.width + "x" + size.height
//                        + "pt(i=" + String.format("%,.2f", pt.getX())
//                        + "_j=" + String.format("%,.2f", pt.getY())
//                        + "_k=" + String.format("%,.2f", pt.getZ())
//                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                r.run(size, lighting, ambientLight, castShadow, epsilon);
////                            }
////                        }
////                    }
////                }
//            }
//
//            if (runGlacier) {
//                double epsilon = 1d / 10000000d;
//                int n = 10;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "Mosaics";
//                String filename = "contemp_mosaic_model";
//                //String filename = "future_mosaic_model";
//
//                boolean assessTopology = false;
//                boolean castShadow = false;
////                double zoomFactor = 3.0d;
//                double zoomFactor = 2.0d;
//                double distance = 2.0d;
//                //double distance = 1.0d;
//                //double distance = 0.75d;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                double ambientLight = 1d / 20d;
//                Path input = Paths.get(inDataDir.toString(), name, name,
//                        filename + ".stl");
//                Color color = Color.WHITE;
//                // Init universe
//                Universe_d universe = new Universe_d(env, input,
//                        V3D_Vector_d.ZERO, color, assessTopology, epsilon,
//                        false);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point_d centroid = universe.aabb.getCentroid();
//                double radius = universe.aabb.getPointsArray()[0]
//                        .getDistance(centroid);
////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
////                                V3D_Vector_d direction = new V3D_Vector_d(i, j, k).getUnitVector();
//                V3D_Vector_d direction = new V3D_Vector_d(1, 1, 1).getUnitVector();
//                // (xMin=0.0, xMax=104.31999969482422, yMin=0.0, yMax=140.0, zMin=0.0, zMax=94.50718688964844
//                // Render the image
////                V3D_Point_d pt = getCameraPt(centroid, direction,
////                        radius * distance);
////                V3D_Rectangle_d screen = null;
//                double x = 50d;
//                double y = 70d;
//                //double z = 90d;
//                double z = 60d;
//                V3D_Point_d pt = new V3D_Point_d(env, x, y, z);
//                distance = 5.0d;
//                double apertureWidthd2 = 5d;
//                double apertureHeightd2 = 5d;
//                zoomFactor = 5.0d;
//                V3D_Rectangle_d screen = new V3D_Rectangle_d(
//                        new V3D_Point_d(env, x - apertureWidthd2, 75d, z - apertureHeightd2),
//                        new V3D_Point_d(env, x - apertureWidthd2, 75d, z + apertureHeightd2),
//                        new V3D_Point_d(env, x + apertureWidthd2, 75d, z + apertureHeightd2),
//                        new V3D_Point_d(env, x + apertureWidthd2, 75d, z - apertureHeightd2));
//                for (int angles = 1; angles < 6; angles++) {
//                    double angle = (Math.PI / 6) * angles;
//                    V3D_Ray_d zAxis = new V3D_Ray_d(new V3D_Point_d(env, 0d, 0d, 0d), new V3D_Point_d(env, 0d, 0d, 1d));
//                    screen = screen.rotate(zAxis, zAxis.l.v, angle, epsilon);
//                    RenderImage_d r = new RenderImage_d(universe, pt, size, zoomFactor, screen, epsilon);
//                    V3D_Vector_d lighting = new V3D_Vector_d(1, -2, 3).getUnitVector();
//                    String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
//                            + "_j=" + String.format("%,.2f", lighting.dy)
//                            + "_k=" + String.format("%,.2f", lighting.dz)
//                            + ")_ambientLight(" + ambientLight + ")";
//                    Path dir;
//                    if (screen == null) {
//                        dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
//                                "zoomFactor=" + zoomFactor, "distance=" + distance);
//                    } else {
//                        dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
//                                "_distance=" + distance);
//                    }
//                    if (castShadow) {
//                        dir = Paths.get(dir.toString(), "shadow");
//                    }
//                    r.output = Paths.get(dir.toString(),
//                            filename
//                            + "_" + size.width + "x" + size.height
//                            + "pt(i=" + String.format("%,.2f", pt.getX())
//                            + "_j=" + String.format("%,.2f", pt.getY())
//                            + "_k=" + String.format("%,.2f", pt.getZ())
//                            + ")_angle=" + String.format("%,.2f", angle)
//                            + "_aperture=" + apertureWidthd2 + "x" + apertureHeightd2
//                            + "_" + ls + "_epsilon=" + epsilon + ".png");
//                    r.run(size, lighting, ambientLight, castShadow, epsilon);
//                }
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
    public void run(Dimension size, V3D_Vector_d lighting, double ambientLight,
            boolean castShadow, double epsilon) throws Exception {
        run(size, lighting, ambientLight, castShadow, false, epsilon);
    }

    /**
     * The process for rendering an image.
     *
     * @throws Exception
     */
    public void run(Dimension size, V3D_Vector_d lighting, double ambientLight,
            boolean castShadow, boolean addGraticules, double epsilon)
            throws Exception {
        int[] pix = universe.camera.render(this.universe, lighting,
                ambientLight, castShadow, addGraticules, epsilon);
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
     * @return The focal point for the camera.
     */
    public static V3D_Point_d getCameraPt(V3D_Point_d pt,
            V3D_Vector_d v, double distance) {
        V3D_Point_d r = new V3D_Point_d(pt);
        r.translate(v.multiply(distance));
        return r;
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param epsilon The tolerance within which two vectors are regarded as
     * equal.
     * @return The ids of the original triangles that are intersected.
     */
    public static void addPoints0(Universe_d universe, double epsilon) {
        double scale = 100d;
        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j += 2) {
                for (int k = -1; k < 2; k += 2) {
                    universe.addPoint(new V3D_Point_d(
                            universe.env,
                            i * scale,
                            j * scale,
                            k * scale));
                }
            }
        }
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param epsilon The tolerance within which two vectors are regarded as
     * equal.
     * @return The ids of the original triangles that are intersected.
     */
    public static void addLines0(Universe_d universe, double epsilon) {
        double scale = 100d;
//        V3D_Point_d p = new V3D_Point_d(universe.env, -1d * scale, -1d * scale, 0d);
//        V3D_Point_d q = new V3D_Point_d(universe.env, -1d * scale, 1d * scale, 0d);
//        V3D_Point_d r = new V3D_Point_d(universe.env, 1d * scale, 1d * scale, 0d);
//        V3D_Point_d s = new V3D_Point_d(universe.env, 1d * scale, -1d * scale, 0d);
//        V3D_LineSegment_d pq = new V3D_LineSegment_d(p, q);
//        V3D_LineSegment_d qr = new V3D_LineSegment_d(q, r);
//        V3D_LineSegment_d rs = new V3D_LineSegment_d(r, s);
//        V3D_LineSegment_d sp = new V3D_LineSegment_d(s, p);
//        V3D_LineSegment_d pr = new V3D_LineSegment_d(p, r);
//        V3D_LineSegment_d qs = new V3D_LineSegment_d(q, s);
//        universe.addLine(pq, Color.WHITE);
//        universe.addLine(qr, Color.WHITE);
//        universe.addLine(rs, Color.WHITE);
//        universe.addLine(sp, Color.WHITE);
//        universe.addLine(pr, Color.WHITE);
//        universe.addLine(qs, Color.WHITE);
        // Cube
        // Points
        V3D_Point_d lbf = new V3D_Point_d(universe.env, new V3D_Vector_d(-1 * scale, -1 * scale, -1 * scale));
        V3D_Point_d lba = new V3D_Point_d(universe.env, new V3D_Vector_d(-1 * scale, -1 * scale, 1 * scale));
        V3D_Point_d ltf = new V3D_Point_d(universe.env, new V3D_Vector_d(-1 * scale, 1 * scale, -1 * scale));
        V3D_Point_d lta = new V3D_Point_d(universe.env, new V3D_Vector_d(-1 * scale, 1 * scale, 1 * scale));
        V3D_Point_d rbf = new V3D_Point_d(universe.env, new V3D_Vector_d(1 * scale, -1 * scale, -1 * scale));
        V3D_Point_d rba = new V3D_Point_d(universe.env, new V3D_Vector_d(1 * scale, -1 * scale, 1 * scale));
        V3D_Point_d rtf = new V3D_Point_d(universe.env, new V3D_Vector_d(1 * scale, 1 * scale, -1 * scale));
        V3D_Point_d rta = new V3D_Point_d(universe.env, new V3D_Vector_d(1 * scale, 1 * scale, 1 * scale));
        // Lines
        universe.addLine(new V3D_LineSegment_d(lbf, lba), Color.WHITE);
        //universe.addLine(new V3D_LineSegment_d(lbf, lba), Color.PINK);
        universe.addLine(new V3D_LineSegment_d(lbf, ltf), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(lbf, rbf), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(lba, lta), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(lba, rba), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(ltf, lta), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(ltf, rtf), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(lta, rta), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(rbf, rba), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(rbf, rtf), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(rba, rta), Color.WHITE);
        universe.addLine(new V3D_LineSegment_d(rtf, rta), Color.WHITE);
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param epsilon The tolerance within which two vectors are regarded as
     * equal.
     * @return The ids of the original triangles that are intersected.
     */
    public static void addAxes(Universe_d universe, double epsilon) {
        double xmin = universe.aabb.getXMin();
        double xmax = universe.aabb.getXMax();
        double ymin = universe.aabb.getYMin();
        double ymax = universe.aabb.getYMax();
        double zmin = universe.aabb.getZMin();
        double zmax = universe.aabb.getZMax();
        V3D_LineSegment_d xaxis = new V3D_LineSegment_d(
                new V3D_Point_d(universe.env, xmin, 0d, 0d),
                new V3D_Point_d(universe.env, xmax, 0d, 0d));
        universe.addLine(xaxis, Color.BLUE);
        V3D_LineSegment_d yaxis = new V3D_LineSegment_d(
                new V3D_Point_d(universe.env, 0d, ymin, 0d),
                new V3D_Point_d(universe.env, 0d, ymax, 0d));
        universe.addLine(yaxis, Color.RED);
        V3D_LineSegment_d zaxis = new V3D_LineSegment_d(
                new V3D_Point_d(universe.env, 0d, 0d, zmin),
                new V3D_Point_d(universe.env, 0d, 0d, zmax));
        universe.addLine(zaxis, Color.GREEN);
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param epsilon The tolerance within which two vectors are regarded as
     * equal.
     * @return The ids of the original triangles that are intersected.
     */
    public static void addRectangles0(Universe_d universe, double epsilon) {
        V3D_Rectangle_d r = new V3D_Rectangle_d(
                new V3D_Point_d(universe.env, -100d, -100d, 0d),
                new V3D_Point_d(universe.env, -100d, 100d, 0d),
                new V3D_Point_d(universe.env, 100d, 100d, 0d),
                new V3D_Point_d(universe.env, 100d, -100d, 0d));
        universe.addArea(r, Color.GRAY);
    }

    /**
     * One polygon that is not a convex hull.
     *
     * @param universe
     * @param epsilon The tolerance within which two vectors are regarded as
     * equal.
     * @return The ids of the original triangles that are intersected.
     */
    public static void addPolygons00(Universe_d universe, double epsilon) {
        double scale = 10d;
        V3D_Point_d[] pts = new V3D_Point_d[8];
        pts[0] = new V3D_Point_d(universe.env, -8d * scale, -8d * scale, 0d * scale);
        pts[1] = new V3D_Point_d(universe.env, -4d * scale, 0d * scale, 0d * scale);
        pts[2] = new V3D_Point_d(universe.env, -8d * scale, 8d * scale, 0d * scale);
        pts[3] = new V3D_Point_d(universe.env, 0d * scale, 4d * scale, 0d * scale);
        pts[4] = new V3D_Point_d(universe.env, 8d * scale, 8d * scale, 0d * scale);
        pts[5] = new V3D_Point_d(universe.env, 4d * scale, 0d * scale, 0d * scale);
        pts[6] = new V3D_Point_d(universe.env, 8d * scale, -8d * scale, 0d * scale);
        pts[7] = new V3D_Point_d(universe.env, 0d * scale, -4d * scale, 0d * scale);
        V3D_PolygonNoInternalHoles_d polygon = new V3D_PolygonNoInternalHoles_d(
                pts, V3D_Plane_d.Z0.getN(), epsilon);
        //Color c = Color.GRAY;
        Color c = Color.WHITE;
        universe.addArea(polygon, c);
        // Edges
        //Color ec = Color.DARK_GRAY;
        Color ec = Color.PINK;
        polygon.getEdges().values().forEach(x
                -> universe.addLine(x, ec)
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
     * @param epsilon The tolerance within which two vectors are regarded as
     * equal.
     * @return The ids of the original triangles that are intersected.
     */
    public static void addPolygons0(Universe_d universe, double epsilon) {
        double scale = 50d;
        V3D_Vector_d offset = new V3D_Vector_d(0d, 0d, 0d);
        V3D_Point_d l = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(-2 * scale, 0d, 0d));
        V3D_Point_d r = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(2 * scale, 0d, 0d));
        V3D_Point_d b = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(0d, -2 * scale, 0d));
        V3D_Point_d t = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(0d, 2 * scale, 0d));
        V3D_Point_d f = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(0d, 0d, 2 * scale));
        V3D_Point_d a = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(0d, 0d, -2 * scale));
        V3D_Point_d lbf = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(-scale, -scale, -scale));
        V3D_Point_d lba = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(-scale, -scale, scale));
        V3D_Point_d ltf = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(-scale, scale, -scale));
        V3D_Point_d lta = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(-scale, scale, scale));
        V3D_Point_d rbf = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(scale, -scale, -scale));
        V3D_Point_d rba = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(scale, -scale, scale));
        V3D_Point_d rtf = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(scale, scale, -scale));
        V3D_Point_d rta = new V3D_Point_d(universe.env, offset, new V3D_Vector_d(scale, scale, scale));
        universe.addArea(new V3D_Triangle_d(f, lbf, ltf, rtf), Color.BLUE);
        universe.addArea(new V3D_Triangle_d(f, lbf, rbf, rtf), Color.BLUE);
        universe.addArea(new V3D_Triangle_d(l, lbf, ltf, lta), Color.RED);
        universe.addArea(new V3D_Triangle_d(l, lbf, lba, lta), Color.RED);
        universe.addArea(new V3D_Triangle_d(a, lba, lta, rta), Color.YELLOW);
        universe.addArea(new V3D_Triangle_d(a, lba, rba, rta), Color.YELLOW);
        universe.addArea(new V3D_Triangle_d(r, rbf, rtf, rta), Color.GREEN);
        universe.addArea(new V3D_Triangle_d(r, rbf, rta, rba), Color.GREEN);
        universe.addArea(new V3D_Triangle_d(t, ltf, lta, rta), Color.ORANGE);
        universe.addArea(new V3D_Triangle_d(t, rtf, ltf, rta), Color.ORANGE);
        universe.addArea(new V3D_Triangle_d(b, lbf, rbf, rba), Color.PINK);
        universe.addArea(new V3D_Triangle_d(b, lbf, lba, rba), Color.PINK);
    }
}
