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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;

import uk.ac.leeds.ccg.math.arithmetic.Math_BigInteger;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.io.IO;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Envelope;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class RenderImage {

    private static final long serialVersionUID = 1L;

    /**
     * V3D_Environment.
     */
    V3D_Environment e;

    /**
     * The width and height.
     */
    public Dimension size;

    /**
     * Universe.
     */
    public Universe universe;

    /**
     * Order of Magnitude for the precision.
     */
    public int oom;

    /**
     * Path to output file.
     */
    Path output;

    /**
     * For storing the state running or not.
     */
    private static boolean running = false;

    /**
     * Create a new instance.
     */
    public RenderImage(Path output, Dimension size, int oom) throws Exception {
        this.output = output;
        this.size = size;
        this.oom = oom;
        this.e = new V3D_Environment(new Math_BigInteger(), oom, RoundingMode.HALF_UP);
//        Math_BigRational ptx = Math_BigRational.ZERO;
//        Math_BigRational pty = Math_BigRational.ZERO;
//        Math_BigRational ptz = Math_BigRational.valueOf(this.size.height).negate();
//        V3D_Point pt = new V3D_Point(e, ptx, pty, ptz);
//
////        Math_BigRational halfwidth = Math_BigRational.valueOf(this.size.width).divide(2);
////        Math_BigRational halfheight = Math_BigRational.valueOf(this.size.height).divide(2);
////        Math_BigRational xmin = ptx.subtract(halfwidth);
////        Math_BigRational xmax = ptx.add(halfwidth);
////        Math_BigRational ymin = pty.subtract(halfheight);
////        Math_BigRational ymax = pty.add(halfheight);
////        Math_BigRational depth = Math_BigRational.ZERO;
//        Math_BigRational xmin = ptx.subtract(10);
//        Math_BigRational xmax = ptx.add(10);
//        Math_BigRational P7_5 = Math_BigRational.valueOf(15).divide(2);
//        Math_BigRational ymin = pty.subtract(P7_5);
//        Math_BigRational ymax = pty.add(P7_5);
//        Math_BigRational depth = Math_BigRational.TEN.negate();
//
//        V3D_Rectangle screen = new V3D_Rectangle(
//                new V3D_Point(e, xmin, ymin, depth),
//                new V3D_Point(e, xmin, ymax, depth),
//                new V3D_Point(e, xmax, ymax, depth),
//                new V3D_Point(e, xmax, ymin, depth));
        this.universe = new Universe();
        V3D_Envelope ve = this.universe.init0(
                Math_BigRational.valueOf(this.size.width),
                Math_BigRational.valueOf(this.size.height));
        V3D_Point pt = getDefaultCameraPt(ve, oom, e);
        this.universe.setCamera(new Camera(pt, ve,
                this.size.width, this.size.height, this.oom));
        
    }

    /**
     * Create a new instance.
     */
    public RenderImage(Path input, Path output, Dimension size, int oom) throws Exception {
        this.output = output;
        this.size = size;
        this.oom = oom;
        this.e = new V3D_Environment(new Math_BigInteger(), oom, RoundingMode.HALF_UP);
        this.universe = new Universe();
        V3D_Envelope ve = this.universe.init(input);
        V3D_Point pt = getDefaultCameraPt(ve, oom, e);
        this.universe.setCamera(new Camera(pt, ve,
                this.size.width, this.size.height, this.oom));
    }

    public static void main(String[] args) {
        try {
            //boolean test = true;
            boolean test = false;
            if (test) {//Test 
                int w = 100;
                int h = 75;
                Path output = Paths.get("data", "test.png");
                Dimension size = new Dimension(w, h);
                int oom = -3;
                RenderImage r = new RenderImage(output, size, oom);
                r.run();
            }
            if (!test) {
                int w = 100;
                int h = 75;
                Path dir = Paths.get("data");
                String filename = "Utah_teapot_(solid)";
                //Path dir = Paths.get("data", "geographos");
                //String filename = "1620geographos";
                Path input = Paths.get(dir.toString(), filename + ".stl");
                Path output = Paths.get(dir.toString(), filename + "_" + w + "x" + h + ".png");
                Dimension size = new Dimension(w, h);
                int oom = -3;
                RenderImage r = new RenderImage(input, output, size, oom);
                r.run();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void run() throws Exception {
        //BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        // Draw all the things.
        V3D_Vector lighting = new V3D_Vector(-1, -1, -1).getUnitVector(oom, e.rm);
        int[] pix = this.universe.camera.render(this.universe, lighting, oom);
        /**
         * Convert pix to an image and write to a file.
         */
        MemoryImageSource m = new MemoryImageSource(size.width, size.height, pix, 0, size.width);
        Panel panel = new Panel();
        Image image = panel.createImage(m);
        IO.imageToFile(image, "png", this.output);
        System.out.println("Rendered");
    }

    public static V3D_Point getDefaultCameraPt(V3D_Envelope ve, int oom, 
            V3D_Environment e) {
        Math_BigRational veXMin = ve.getXMin(oom);
        Math_BigRational veXMax = ve.getXMax(oom);
        Math_BigRational veYMin = ve.getYMin(oom);
        Math_BigRational veYMax = ve.getYMax(oom);
        Math_BigRational veZMin = ve.getZMin(oom);
        //Math_BigRational veZMax = ve.getZMax(oom);
        Math_BigRational xr = veXMax.subtract(veXMin);
        Math_BigRational yr = veYMax.subtract(veYMin);
        // pzd is the biggest of xr and yr subtracted from the min z of ve.
        Math_BigRational pzd = veZMin.subtract(xr.max(yr));
        V3D_Point pt = new V3D_Point(e, veXMin.add(xr.divide(2)), veYMin.add(yr.divide(2)), pzd);
        return pt;
    }

}
