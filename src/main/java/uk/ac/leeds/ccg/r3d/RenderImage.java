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
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.leeds.ccg.math.arithmetic.Math_BigRational;
import uk.ac.leeds.ccg.r3d.io.IO;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
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
     * @param pt The camera focal point as a unit vector.
     * @param size The preferred image size. Although a square image is
     * currently returned.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @throws Exception
     */
    public RenderImage(Universe universe, V3D_Point pt, Dimension size, 
            BigRational zoomFactor, int oom, RoundingMode rm) throws Exception {
        this.universe = universe;
        //CameraOld c = new CameraOld(pt, universe.envelope, size.width, size.height, oom, rm);
        //Camera c = new Camera(pt, universe.envelope, size, zoomFactor, oom, rm);
        Camera1 c = new Camera1(pt, universe.envelope, size.width, size.height, oom, rm);
        this.universe.setCamera(c);
    }

    public static void main(String[] args) {
        try {
            //boolean run0 = true;
            boolean run0 = false;
            boolean run1 = true;
            //boolean run1 = false;
            //boolean runUtah = true;
            boolean runUtah = false;
            //boolean runGeographos = true;
            boolean runGeographos = false;
            //boolean runKatrina = true;
            boolean runKatrina = false;
            Path inDataDir = Paths.get("data", "input");
            Path outDataDir = Paths.get("data", "output");
            RoundingMode rm = RoundingMode.HALF_UP;
            
            if (run0) {
                //int oom = -2;
                int oom = -4;
                int n = 1;
                //n = 5;
                int w = 100 * n;
                int h = 100 * n;
                BigRational zoomFactor = BigRational.ONE;
                // Init universe
                Universe universe = new Universe(V3D_Vector.ZERO, oom, rm);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_Point centroid = universe.envelope.getCentroid(oom, rm);
                BigRational radius = universe.envelope.getPoints(oom)[0]
                        .getDistance(centroid, oom, rm);
                //String name = "tetras6";
                //String name = "tetras5";
                String name = "triangles";
                boolean assessTopology = false;
                boolean castShadow = false;
                /**
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
                //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -1; k <= 1; k++) {
                            if (!(i == 0 && j == 0 && k == 0)) {
//                                int i = 1;
//                                int j = 0;
//                                int k = 1; 
                                V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
                                V3D_Point pt = getCameraPt(centroid, direction,
                                        radius.multiply(2), oom, rm);
                                // Render the image
                                RenderImage r = new RenderImage(universe, pt, size, zoomFactor, oom, rm);
                                V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
                                String ls = "lighting(i=" + Math_BigRational.round(lighting.getDX(oom, rm), -4, rm).toString()
                                        + "_j=" + Math_BigRational.round(lighting.getDY(oom, rm), -4, rm).toString()
                                        + "_k=" + Math_BigRational.round(lighting.getDZ(oom, rm), -4, rm).toString()
                                            + ")_ambientLight(" + Math_BigRational.round(ambientLight, -4, rm) + ")";
                                Path dir = Paths.get(outDataDir.toString(), "test", name, "oom=" + oom, ls);
                                if (castShadow) {
                                    dir = Paths.get(dir.toString(), "shadow");
                                }
                                r.output = Paths.get(dir.toString(),
                                        "test_" //+ r.size.width + "x" + r.size.height
                                        + "_pt(i=" + Math_BigRational.round(pt.getX(oom, rm),-4, rm).toString()
                                        + "_j=" + Math_BigRational.round(pt.getY(oom, rm),-4, rm).toString()
                                        + "_k=" + Math_BigRational.round(pt.getZ(oom, rm),-4, rm).toString()
                                        + ")_" + ls + "_oom=" + oom + ".png");
                                r.run(size, lighting, ambientLight, castShadow, oom, rm);
                            }
                        }
                    }
                }
            }

            if (run1) {
                int oom = -5;
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
                 * AmbientLight makes non black surfaces non black even if they
                 * are orientated opposite to the lighting vector.
                 */
                BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
                //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
                String name = "3361664_Platonic_Solid_Collection";
                String filename = "Icosahedron";
                Path input = Paths.get(inDataDir.toString(), name, "files", filename + ".stl");
                Color color = Color.YELLOW;
                // Init universe
                Universe universe = new Universe(input, V3D_Vector.ZERO, color,
                        assessTopology, oom, rm);
                // Detail the camera
                Dimension size = new Dimension(w, h);
                V3D_Point centroid = universe.envelope.getCentroid(oom, rm);
                BigRational radius = universe.envelope.getPoints(oom)[0]
                        .getDistance(centroid, oom, rm);
                V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -1; k <= 1; k++) {
                            if (!(i == 0 && j == 0 && k == 0)) {
                                V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
                                V3D_Point pt = getCameraPt(centroid, direction,
                                        radius.multiply(2), oom, rm);
                                // Render the image
                                RenderImage r = new RenderImage(universe, pt, size, zoomFactor, oom, rm);
                                String ls = "lighting(i=" + Math_BigRational.round(lighting.getDX(oom, rm), -4, rm).toString()
                                        + "_j=" + Math_BigRational.round(lighting.getDY(oom, rm), -4, rm).toString()
                                        + "_k=" + Math_BigRational.round(lighting.getDZ(oom, rm), -4, rm).toString()
                                            + ")_ambientLight(" + Math_BigRational.round(ambientLight, -4, rm) + ")";
                                Path dir = Paths.get(outDataDir.toString(), name, "files", "oom=" + oom, ls);
                                if (castShadow) {
                                    dir = Paths.get(dir.toString(), "shadow");
                                }
                                
                                r.output = Paths.get(dir.toString(),
                                        filename
                                        + "_" + size.width + "x" + size.height
                                        + "_pt(i=" + Math_BigRational.round(pt.getX(oom, rm),-4, rm).toString()
                                        + "_j=" + Math_BigRational.round(pt.getY(oom, rm),-4, rm).toString()
                                        + "_k=" + Math_BigRational.round(pt.getZ(oom, rm),-4, rm).toString()
                                        + ")_" + ls + "_oom=" + oom + ".png");
                                r.run(size, lighting, ambientLight, castShadow, oom, rm);
                            }
                        }
                    }
                }
            }

//            if (runUtah) {
//                int oom = -3;
//                /**
//                 * VE V3D_Envelope(xMin=-8.16388988, xMax=9.41236687,
//                 * yMin=-5.36976051, yMax=5.55727768, zMin=2E-8,
//                 * zMax=8.57152748)
//                 */
//                oom = -10;
//                int n = 1;
//                n = 5;
//                int w = 100 * n;
//                int h = 75 * n;
//                boolean assessTopology = false;
//                boolean castShadow = false;
//                /**
//                 * AmbientLight makes non black surfaces non black even if they
//                 * are orientated opposite to the lighting vector.
//                 */
//                BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(20));
//                //BigRational ambientLight = BigRational.ONE.divide(BigRational.valueOf(5));
//                String name = "Utah_teapot_(solid)";
//                Color color = Color.YELLOW;
//                Path input = Paths.get(inDataDir.toString(), name, name + ".stl");
//                // Init universe
//                Universe universe = new Universe(input, V3D_Vector.ZERO, color,
//                        assessTopology, oom, rm);
//                // Detail the camera
//                Dimension size = new Dimension(w, h);
//                V3D_Point centroid = universe.envelope.getCentroid(oom, rm);
//                BigRational radius = universe.envelope.getPoints(oom, rm)[0]
//                        .getDistance(centroid, oom, rm);
////                for (int i = -1; i <= 1; i++) {
////                    for (int j = -1; j <= 1; j++) {
////                        for (int k = -1; k <= 1; k++) {
////                            if (!(i == 0 && j == 0 && k == 0)) {
////                                V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
//                V3D_Vector direction = new V3D_Vector(-1, 1, 1).getUnitVector(oom, rm);
//                V3D_Point pt = getCameraPt(centroid, direction,
//                        radius.multiply(4).divide(2), oom, rm);
//                // Render the image
//                RenderImage r = new RenderImage(universe, pt, size, oom, rm);
//                //V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
//                V3D_Vector lighting = new V3D_Vector(1, 2, 3).getUnitVector(oom, rm);
//                String ls = "lighting(i=" + lighting.getDX(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_j=" + lighting.getDY(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_k=" + lighting.getDZ(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + ")_ambientLight(" + ambientLight.round(-4, rm) + ")";
//                Path dir = Paths.get(outDataDir.toString(), name, "oom=" + oom, ls);
//                if (castShadow) {
//                    dir = Paths.get(dir.toString(), "shadow");
//                }
//                r.output = Paths.get(dir.toString(),
//                        name
//                        + "_" + r.size.width + "x" + r.size.height
//                        + "_pt(i=" + pt.getX(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_j=" + pt.getY(oom, rm).round(-4, rm).getStringValue().trim()
//                        + "_k=" + pt.getZ(oom, rm).round(-4, rm).getStringValue().trim()
//                        + ")_" + ls + "_oom=" + oom + ".png");
//                r.run(lighting, ambientLight, castShadow);
////                            }
////                        }
////                    }
////                }
//            }
//
//            if (runGeographos) {
//                //int oom = -2;
//                //int oom = -4;
//                //int oom = -7;
//                int oom = -8;
//                int n = 1;
//                n = 5;
//                int w = 100 * n;
//                int h = 75 * n;
//                String name = "geographos";
//                String filename = "1620geographos";
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
//                V3D_Point centroid = universe.envelope.getCentroid(oom, rm);
//                BigRational radius = universe.envelope.getPoints(oom, rm)[0]
//                        .getDistance(centroid, oom, rm);
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        for (int k = -1; k <= 1; k++) {
//                            if (!(i == 0 && j == 0 && k == 0)) {
//                                if (!(i == -1 && j == 1 && k == 1)) {
////                int i = -1;
////                int j = 1;
////                int k = 1;
//                                    V3D_Vector direction = new V3D_Vector(i, j, k).getUnitVector(oom, rm);
//                                    V3D_Point pt = getCameraPt(centroid, direction,
//                                            radius.multiply(2), oom, rm);
//                                    // Render the image
//                                    RenderImage r = new RenderImage(universe, pt, size, oom, rm);
//                                    V3D_Vector lighting = new V3D_Vector(-1, -2, -3).getUnitVector(oom, rm);
//                                    String ls = "lighting(i=" + lighting.getDX(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + "_j=" + lighting.getDY(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + "_k=" + lighting.getDZ(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + ")_ambientLight(" + ambientLight.round(-4, rm) + ")";
//                                    Path dir = Paths.get(outDataDir.toString(), name, "files", "oom=" + oom, ls, "nset");
//                                    if (castShadow) {
//                                        dir = Paths.get(dir.toString(), "shadow");
//                                    }
//                                    r.output = Paths.get(dir.toString(),
//                                            filename
//                                            + "_" + r.size.width + "x" + r.size.height
//                                            + "_pt(i=" + pt.getX(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + "_j=" + pt.getY(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + "_k=" + pt.getZ(oom, rm).round(-4, rm).getStringValue().trim()
//                                            + ")_" + ls + "_oom=" + oom + ".png");
//                                    r.run(lighting, ambientLight, castShadow);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (runKatrina) {
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
//                V3D_Point centroid = universe.envelope.getCentroid(oom, rm);
//                BigRational radius = universe.envelope.getPoints(oom, rm)[0]
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
        int[] pix = universe.camera.render(this.universe, lighting, ambientLight, castShadow, oom, rm);
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
}
