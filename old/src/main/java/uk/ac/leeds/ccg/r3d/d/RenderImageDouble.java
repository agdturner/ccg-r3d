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
package uk.ac.leeds.ccg.r3d.d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.MemoryImageSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.leeds.ccg.r3d.io.IO;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PointDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_RectangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_VectorDouble;

public class RenderImageDouble {

    /**
     * The width and height.
     */
    public Dimension size;

    /**
     * Universe.
     */
    public UniverseDouble universe;

    /**
     * Path to output file.
     */
    Path output;

    /**
     * Create a new instance.
     *
     * @param universe The universe.
     * @param pt The camera focal point as a unit vector.
     * @param size The preferred image size. Although a square image is
     * currently returned.
     * @throws Exception
     */
    public RenderImageDouble(UniverseDouble universe, V3D_PointDouble pt,
            Dimension size, double zoomFactor, V3D_RectangleDouble screen, 
            double epsilon) throws Exception {
        this.universe = universe;
        CameraDouble c;
        if (screen == null) {
            c = new CameraDouble(pt, universe.envelope, size.width,
                size.height, zoomFactor, epsilon);
        } else {
            c = new CameraDouble(pt, universe.envelope, size.width,
                size.height, screen, epsilon);
        }
        this.universe.setCamera(c);
        this.size = new Dimension(c.ncols, c.nrows);
    }

    public static void main(String[] args) {
        try {
            boolean run0 = true;
            //boolean run0 = false;
            //boolean run1 = true;
            boolean run1 = false;
            //boolean runUtah = true;
            boolean runUtah = false;
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
            
            
            
            Path inDataDir = Paths.get("data", "input");
            Path outDataDir = Paths.get("data", "output");
            if (run0) {
                double epsilon = 1d / 10000000d;
                int n = 1;
                //n = 5;
                int w = 100 * n;
                int h = 100 * n;
                // Init universe
                UniverseDouble universe = new UniverseDouble(V3D_VectorDouble.ZERO, epsilon);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
                //String name = "tetras6";
                //String name = "tetras5";
                String name = "triangles";
                boolean castShadow = false;
                double zoomFactor = 1.0d;
                double distance = 2.0d;
                boolean addGraticules = false;
                //boolean addGraticules = true;
                
                V3D_PointDouble c = universe.envelope.getCentroid();
                double x = c.getX();
                double y = c.getY();
                double z = universe.getMaxZ(x, y, epsilon) + 10;
                
                V3D_PointDouble pt = new V3D_PointDouble(universe.envelope.getXMin(), 
                        universe.envelope.getYMin(), z);
                System.out.println("pt " + pt);
                
                                
                double apertureWidthd2 = 5d;
                double apertureHeightd2 = 5d;
                V3D_RectangleDouble screen = new V3D_RectangleDouble(
                        new V3D_PointDouble(x - apertureWidthd2, y, z - apertureHeightd2),
                        new V3D_PointDouble(x - apertureWidthd2, y, z + apertureHeightd2),
                        new V3D_PointDouble(x + apertureWidthd2, y, z + apertureHeightd2),
                        new V3D_PointDouble(x + apertureWidthd2, y, z - apertureHeightd2));
                System.out.println("screen " + screen);
                //screen = null;
                
                double angle = -Math.PI/6d;
                // Angle the screen down
                screen = screen.rotate(new V3D_LineDouble(pt, new V3D_PointDouble(x + 1, y, z+1)), angle, epsilon);
                System.out.println("rotated screen " + screen);
                
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                int i = 1;
//                                int j = 0;
//                                int k = 1; 
//                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
//                                V3D_PointDouble pt = getCameraPt(centroid, direction,
//                                        radius * distance);
                                // Render the image
                                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, screen, epsilon);
                                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                                        + "_j=" + String.format("%,.2f", lighting.dy)
                                        + "_k=" + String.format("%,.2f", lighting.dz)
                                        + ")_ambientLight(" + ambientLight + ")";
                                Path dir = Paths.get(outDataDir.toString(), "test", name, "epsilon=" + epsilon, ls,
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                                if (castShadow) {
                                    dir = Paths.get(dir.toString(), "shadow");
                                }
                                r.output = Paths.get(dir.toString(),
                                        "test_" + r.size.width + "x" + r.size.height
                                        + "_pt(i=" + String.format("%,.2f", pt.getX())
                                        + "_j=" + String.format("%,.2f", pt.getY())
                                        + "_k=" + String.format("%,.2f", pt.getZ())
                                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
                                r.run(lighting, ambientLight, castShadow, addGraticules, epsilon);
//                            }
//                        }
//                    }
//                }
            }

            if (run1) {
                double epsilon = 1d / 10000000d;
                int n = 1;
                //n = 5;
                int w = 100 * n;
                int h = 100 * n;
                //int w = 100 * n;
                //int h = 75 * n;
                boolean addGraticules = true;
                boolean assessTopology = false;
                boolean castShadow = true;
                // Zoom factor should be at least as big as distance?
                double zoomFactor = 1d; 
                //double zoomFactor = 2d;
                //double zoomFactor = 1.5d;
                //double distance = 3d;
                double distance = 2d;
                //double distance = 0.5d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                //double ambientLight = 0d;
                double ambientLight = 1d / 20d;
                //double ambientLight = 1d / 15d;
                //double ambientLight = 1d / 2d;
                //double ambientLight = 1d;
                String name = "3361664_Platonic_Solid_Collection";
                String filename = "Icosahedron";
                Path input = Paths.get(inDataDir.toString(), name, "files", filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                double x = 0d;
                double y = 0d;
                Double maxZ = universe.getMaxZ(x, y, epsilon);
                System.out.println("(x=" + x + ", y=" +  y +  ") " + "maxZ=" + maxZ);
                x = universe.envelope.getXMin()/2d;
                y = universe.envelope.getYMin()/2d;
                maxZ = universe.getMaxZ(x, y, epsilon);
                System.out.println("(x=" + x + ", y=" +  y +  ") " + "maxZ=" + maxZ);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -5, -3).getUnitVector();
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
//                                V3D_PointDouble pt = getCameraPt(centroid, direction,
//                                        radius * distance);
//                                // Render the image
//                                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, null, epsilon);
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
//                                        + "_" + r.size.width + "x" + r.size.height
//                                        + "_pt(i=" + String.format("%,.2f", pt.getX())
//                                        + "_j=" + String.format("%,.2f", pt.getY())
//                                        + "_k=" + String.format("%,.2f", pt.getZ())
//                                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
//                                r.run(lighting, ambientLight, castShadow, addGraticules, epsilon);
//                            }
//                        }
//                    }
//                }
            }

            if (runUtah) {
                double epsilon = 1d / 10000000d;
                int n = 1;
                n = 5;
                int w = 100 * n;
                int h = 75 * n;
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
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -1; k <= 1; k++) {
                            if (!(i == 0 && j == 0 && k == 0)) {
                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                //V3D_VectorDouble direction = new V3D_VectorDouble(-1, 1, 1).getUnitVector();
                V3D_PointDouble pt = getCameraPt(centroid, direction,
                        radius * distance);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, null, epsilon);
                //V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                V3D_VectorDouble lighting = new V3D_VectorDouble(1, 2, 3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "epsilon=" + epsilon, ls,
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                if (castShadow) {
                    dir = Paths.get(dir.toString(), "shadow");
                }
                r.output = Paths.get(dir.toString(),
                        name
                        + "_" + r.size.width + "x" + r.size.height
                        + "_pt(i=" + String.format("%,.2f", pt.getX())
                        + "_j=" + String.format("%,.2f", pt.getY())
                        + "_k=" + String.format("%,.2f", pt.getZ())
                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
                r.run(lighting, ambientLight, castShadow, epsilon);
                            }
                        }
                    }
                }
            }

            if (runGeographos) {
                double epsilon = 1d / 10000000d;
                int n = 1;
                n = 5;
                int w = 100 * n;
                int h = 75 * n;
                String name = "geographos";
                String filename = "1620geographos";
                boolean assessTopology = false;
                boolean castShadow = true;
                double zoomFactor = 1.0d;
                double distance = 2.0d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -1; k <= 1; k++) {
                            if (!(i == 0 && j == 0 && k == 0)) {
                                if (!(i == -1 && j == 1 && k == 1)) {
//                int i = -1;
//                int j = 1;
//                int k = 1;
                                    V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                                    V3D_PointDouble pt = getCameraPt(centroid, direction,
                                            radius * distance);
                                    // Render the image
                                    RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, null, epsilon);
                                    V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                                    String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                                            + "_j=" + String.format("%,.2f", lighting.dy)
                                            + "_k=" + String.format("%,.2f", lighting.dz)
                                            + ")_ambientLight(" + ambientLight + ")";
                                    Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls, "nset",
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                                    if (castShadow) {
                                        dir = Paths.get(dir.toString(), "shadow");
                                    }
                                    r.output = Paths.get(dir.toString(),
                                            filename
                                            + "_" + r.size.width + "x" + r.size.height
                                            + "_pt(i=" + String.format("%,.2f", pt.getX())
                                            + "_j=" + String.format("%,.2f", pt.getY())
                                            + "_k=" + String.format("%,.2f", pt.getZ())
                                            + ")_" + ls + "_epsilon=" + epsilon + ".png");
                                    r.run(lighting, ambientLight, castShadow, epsilon);
                                }
                            }
                        }
                    }
                }
            }

            if (runKatrina) {
                double epsilon = 1d / 10000000d;
                int n = 10;
                int w = 100 * n;
                int h = 75 * n;
                String name = "Hurricane_Katrina";
                String filename = "Katrina";
                boolean assessTopology = false;
                boolean castShadow = false;
                double zoomFactor = 1.0d;
                double distance = 2.0d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                V3D_VectorDouble direction = new V3D_VectorDouble(1, 1, 1).getUnitVector();
                V3D_PointDouble pt = getCameraPt(centroid, direction,
                        radius * distance);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, null, epsilon);
                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                if (castShadow) {
                    dir = Paths.get(dir.toString(), "shadow");
                }
                r.output = Paths.get(dir.toString(),
                        filename
                        + "_" + r.size.width + "x" + r.size.height
                        + "pt(i=" + String.format("%,.2f", pt.getX())
                        + "_j=" + String.format("%,.2f", pt.getY())
                        + "_k=" + String.format("%,.2f", pt.getZ())
                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
                r.run(lighting, ambientLight, castShadow, epsilon);
//                            }
//                        }
//                    }
//                }
            }
            
            if (runCuriosity) {
                double epsilon = 1d / 10000000d;
                int n = 10;
                int w = 100 * n;
                int h = 75 * n;
                String name = "Curiosity Rover 3D Printed Model";
                String filename = "Curiosity SM (Complete Print 200uM)";
                boolean assessTopology = false;
                boolean castShadow = false;
                double zoomFactor = 1.0d;
                double distance = 2.0d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, name, 
                        "Simplified Curiosity Model (Small)", filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                V3D_VectorDouble direction = new V3D_VectorDouble(1, 1, 1).getUnitVector();
                V3D_PointDouble pt = getCameraPt(centroid, direction,
                        radius * distance);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, null, epsilon);
                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                if (castShadow) {
                    dir = Paths.get(dir.toString(), "shadow");
                }
                r.output = Paths.get(dir.toString(),
                        filename
                        + "_" + r.size.width + "x" + r.size.height
                        + "pt(i=" + String.format("%,.2f", pt.getX())
                        + "_j=" + String.format("%,.2f", pt.getY())
                        + "_k=" + String.format("%,.2f", pt.getZ())
                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
                r.run(lighting, ambientLight, castShadow, epsilon);
//                            }
//                        }
//                    }
//                }
            }
            
            if (runApollo17) {
                double epsilon = 1d / 10000000d;
                int n = 10;
                int w = 100 * n;
                int h = 75 * n;
                String name = "Apollo_17";
                String filename = "Apollo_17";
                boolean assessTopology = false;
                boolean castShadow = false;
                double zoomFactor = 1.0d;
                double distance = 2.0d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, 
                        filename + ".stl");
                Color color = Color.WHITE;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                V3D_VectorDouble direction = new V3D_VectorDouble(1, 1, 1).getUnitVector();
                V3D_PointDouble pt = getCameraPt(centroid, direction,
                        radius * distance);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, null, epsilon);
                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                if (castShadow) {
                    dir = Paths.get(dir.toString(), "shadow");
                }
                r.output = Paths.get(dir.toString(),
                        filename
                        + "_" + r.size.width + "x" + r.size.height
                        + "pt(i=" + String.format("%,.2f", pt.getX())
                        + "_j=" + String.format("%,.2f", pt.getY())
                        + "_k=" + String.format("%,.2f", pt.getZ())
                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
                r.run(lighting, ambientLight, castShadow, epsilon);
//                            }
//                        }
//                    }
//                }
            }
            
            if (run3dcityloader) {            
                //https://3dcityloader.com/en/city/worldwide
                double epsilon = 1d / 10000000d;
                int n = 10;
                int w = 100 * n;
                int h = 75 * n;
                String name = "3dcityloader";
                String filename = "76d5e2d2-7410-4ab3-bd07-9faeeb08d4c4";
                boolean assessTopology = false;
                boolean castShadow = false;
                double zoomFactor = 1.0d;
                double distance = 2.0d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, filename,
                        filename + ".stl");
                Color color = Color.WHITE;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                V3D_VectorDouble direction = new V3D_VectorDouble(1, 1, 1).getUnitVector();
                V3D_PointDouble pt = getCameraPt(centroid, direction,
                        radius * distance);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, null, epsilon);
                V3D_VectorDouble lighting = new V3D_VectorDouble(1, -2, 3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                if (castShadow) {
                    dir = Paths.get(dir.toString(), "shadow");
                }
                r.output = Paths.get(dir.toString(),
                        filename
                        + "_" + r.size.width + "x" + r.size.height
                        + "pt(i=" + String.format("%,.2f", pt.getX())
                        + "_j=" + String.format("%,.2f", pt.getY())
                        + "_k=" + String.format("%,.2f", pt.getZ())
                        + ")_" + ls + "_epsilon=" + epsilon + ".png");
                r.run(lighting, ambientLight, castShadow, epsilon);
//                            }
//                        }
//                    }
//                }
            }
            
            if (runGlacier) {
                double epsilon = 1d / 10000000d;
                int n = 10;
                int w = 100 * n;
                int h = 75 * n;
                String name = "Mosaics";
                String filename = "contemp_mosaic_model";
                //String filename = "future_mosaic_model";
                
                boolean assessTopology = false;
                boolean castShadow = false;
//                double zoomFactor = 3.0d;
                double zoomFactor = 2.0d;
                double distance = 2.0d;
                //double distance = 1.0d;
                //double distance = 0.75d;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, name,
                        filename + ".stl");
                Color color = Color.WHITE;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon,
                        false);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                V3D_VectorDouble direction = new V3D_VectorDouble(1, 1, 1).getUnitVector();
                // (xMin=0.0, xMax=104.31999969482422, yMin=0.0, yMax=140.0, zMin=0.0, zMax=94.50718688964844
                // Render the image
//                V3D_PointDouble pt = getCameraPt(centroid, direction,
//                        radius * distance);
//                V3D_RectangleDouble screen = null;
                double x = 50d;
                double y = 70d;
                //double z = 90d;
                //double z = 60d;
                double z = 190d;
                V3D_PointDouble pt = new V3D_PointDouble(x, y, z);
                distance = 5.0d;
                double apertureWidthd2 = 5d;
                double apertureHeightd2 = 5d;
                zoomFactor = 5.0d;
                V3D_RectangleDouble screen = new V3D_RectangleDouble(
                        new V3D_PointDouble(x - apertureWidthd2, 75d, z - apertureHeightd2),
                        new V3D_PointDouble(x - apertureWidthd2, 75d, z + apertureHeightd2),
                        new V3D_PointDouble(x + apertureWidthd2, 75d, z + apertureHeightd2),
                        new V3D_PointDouble(x + apertureWidthd2, 75d, z - apertureHeightd2));
                double angle = (Math.PI/6);
                for (int angles = 1; angles < 2; angles ++){
                //double angle = (Math.PI/6) * angles;
                // Angle the screen down
                screen = screen.rotate(new V3D_LineDouble(pt, new V3D_PointDouble(x + 1, y, z + 1)), angle, epsilon);
                //screen = screen.rotate(V3D_LineDouble.Z_AXIS, angle, epsilon);
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, zoomFactor, screen, epsilon);
                V3D_VectorDouble lighting = new V3D_VectorDouble(1, -2, 3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir;
                if (screen == null) {
                    dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
                                        "zoomFactor=" + zoomFactor, "distance=" + distance);
                } else {
                    dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls,
                                         "_distance=" + distance);
                }
                if (castShadow) {
                    dir = Paths.get(dir.toString(), "shadow");
                }
                r.output = Paths.get(dir.toString(),
                        filename
                        + "_" + r.size.width + "x" + r.size.height
                        + "pt(i=" + String.format("%,.2f", pt.getX())
                        + "_j=" + String.format("%,.2f", pt.getY())
                        + "_k=" + String.format("%,.2f", pt.getZ())
                        + ")_angle=" + String.format("%,.2f", angle)
                        + "_aperture=" + apertureWidthd2+ "x" + apertureHeightd2
                        + "_" + ls + "_epsilon=" + epsilon + ".png");
                r.run(lighting, ambientLight, castShadow, epsilon);
            }
//                            }
//                        }
//                    }
//                }
            }
            
            
                    
            
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * The process for rendering and image.
     *
     * @throws Exception
     */
    public void run(V3D_VectorDouble lighting, double ambientLight,
            boolean castShadow, double epsilon) throws Exception {
        run(lighting, ambientLight, castShadow, false, epsilon);
    }
    
    /**
     * The process for rendering an image.
     *
     * @throws Exception
     */
    public void run(V3D_VectorDouble lighting, double ambientLight,
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
    public static V3D_PointDouble getCameraPt(V3D_PointDouble pt,
            V3D_VectorDouble v, double distance) {
        V3D_PointDouble r = new V3D_PointDouble(pt);
        r.translate(v.multiply(distance));
        return r;
    }
}
