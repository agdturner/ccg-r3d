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
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PointDouble;
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
            Dimension size, double epsilon) throws Exception {
        this.universe = universe;
        CameraDouble c = new CameraDouble(pt, universe.envelope, size.width,
                size.height, epsilon);
        this.universe.setCamera(c);
        this.size = new Dimension(c.ncols, c.nrows);
    }

    public static void main(String[] args) {
        try {
            //boolean run0 = true;
            boolean run0 = false;
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
                boolean assessTopology = false;
                boolean castShadow = false;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -1; k <= 1; k++) {
                            if (!(i == 0 && j == 0 && k == 0)) {
//                                int i = 1;
//                                int j = 0;
//                                int k = 1; 
                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                                V3D_PointDouble pt = getCameraPt(centroid, direction,
                                        radius * 2d);
                                // Render the image
                                RenderImageDouble r = new RenderImageDouble(universe, pt, size, epsilon);
                                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                                        + "_j=" + String.format("%,.2f", lighting.dy)
                                        + "_k=" + String.format("%,.2f", lighting.dz)
                                        + ")_ambientLight(" + ambientLight + ")";
                                Path dir = Paths.get(outDataDir.toString(), "test", name, "epsilon=" + epsilon, ls);
                                if (castShadow) {
                                    dir = Paths.get(dir.toString(), "shadow");
                                }
                                r.output = Paths.get(dir.toString(),
                                        "test_" + r.size.width + "x" + r.size.height
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

            if (run1) {
                double epsilon = 1d / 10000000d;
                int n = 1;
                //n = 5;
                int w = 100 * n;
                int h = 100 * n;
                //int w = 100 * n;
                //int h = 75 * n;
                boolean assessTopology = false;
                boolean castShadow = false;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                String name = "3361664_Platonic_Solid_Collection";
                String filename = "Icosahedron";
                Path input = Paths.get(inDataDir.toString(), name, "files", filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_PointDouble centroid = universe.envelope.getCentroid();
                double radius = universe.envelope.getPoints()[0]
                        .getDistance(centroid);
                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -1; k <= 1; k++) {
                            if (!(i == 0 && j == 0 && k == 0)) {
                                V3D_VectorDouble direction = new V3D_VectorDouble(i, j, k).getUnitVector();
                                V3D_PointDouble pt = getCameraPt(centroid, direction,
                                        radius * 2d);
                                // Render the image
                                RenderImageDouble r = new RenderImageDouble(universe, pt, size, epsilon);
                                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                                        + "_j=" + String.format("%,.2f", lighting.dy)
                                        + "_k=" + String.format("%,.2f", lighting.dz)
                                        + ")_ambientLight(" + ambientLight + ")";
                                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls);
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

            if (runUtah) {
                double epsilon = 1d / 10000000d;
                int n = 1;
                n = 5;
                int w = 100 * n;
                int h = 75 * n;
                boolean assessTopology = false;
                boolean castShadow = false;
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
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon);
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
                        radius * 2d);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, epsilon);
                //V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                V3D_VectorDouble lighting = new V3D_VectorDouble(1, 2, 3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "epsilon=" + epsilon, ls);
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
                boolean castShadow = false;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon);
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
                                            radius * 2d);
                                    // Render the image
                                    RenderImageDouble r = new RenderImageDouble(universe, pt, size, epsilon);
                                    V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                                    String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                                            + "_j=" + String.format("%,.2f", lighting.dy)
                                            + "_k=" + String.format("%,.2f", lighting.dz)
                                            + ")_ambientLight(" + ambientLight + ")";
                                    Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls, "nset");
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
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                double ambientLight = 1d / 20d;
                Path input = Paths.get(inDataDir.toString(), name, filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                UniverseDouble universe = new UniverseDouble(input,
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon);
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
                        radius * 2d);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, epsilon);
                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls);
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
                        V3D_VectorDouble.ZERO, color, assessTopology, epsilon);
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
                        radius * 2d);
                // Render the image
                RenderImageDouble r = new RenderImageDouble(universe, pt, size, epsilon);
                V3D_VectorDouble lighting = new V3D_VectorDouble(-1, -2, -3).getUnitVector();
                String ls = "lighting(i=" + String.format("%,.2f", lighting.dx)
                        + "_j=" + String.format("%,.2f", lighting.dy)
                        + "_k=" + String.format("%,.2f", lighting.dz)
                        + ")_ambientLight(" + ambientLight + ")";
                Path dir = Paths.get(outDataDir.toString(), name, "files", "epsilon=" + epsilon, ls);
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
        int[] pix = universe.camera.render(this.universe, lighting,
                ambientLight, castShadow, epsilon);
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
