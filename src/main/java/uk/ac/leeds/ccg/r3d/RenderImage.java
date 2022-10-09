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
import java.awt.image.MemoryImageSource;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.leeds.ccg.math.arithmetic.Math_BigDecimal;

import uk.ac.leeds.ccg.math.arithmetic.Math_BigInteger;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.math.number.Math_BigRationalSqrt;
import uk.ac.leeds.ccg.r3d.io.IO;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Envelope;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class RenderImage {

    /**
     * V3D_Environment.
     */
    V3D_Environment e;

    int oom;
    
    RoundingMode rm;

    /**
     * The width and height.
     */
    public Dimension size;

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
     */
    public RenderImage(Path output, Dimension size, int oom, RoundingMode rm) throws Exception {
        this.output = output;
        this.size = size;
        this.e = new V3D_Environment(new Math_BigInteger(), new Math_BigDecimal());
        this.oom = oom;
        this.rm = rm;
        this.universe = new Universe();
        V3D_Envelope ve = this.universe.init1(
                Math_BigRational.valueOf(this.size.width),
                Math_BigRational.valueOf(this.size.height), oom, rm);
        V3D_Point pt = getDefaultCameraPt(ve, oom, rm, e);
//        this.universe = new Universe();
//        V3D_Envelope ve = this.universe.init0(
//                Math_BigRational.valueOf(this.size.width),
//                Math_BigRational.valueOf(this.size.height), oom, rm);
//        V3D_Point pt = getDefaultCameraPt(ve, oom, e);
        this.universe.setCamera(new Camera(pt, ve,
                this.size.width, this.size.height, oom, rm));

    }

    /**
     * Create a new instance.
     */
    public RenderImage(Path input, Path output, Dimension size, int oom,
            RoundingMode rm) throws Exception {
        this.output = output;
        this.size = size;
        this.e = new V3D_Environment(new Math_BigInteger(), new Math_BigDecimal());
        this.oom = oom;
        this.rm = rm;
        this.universe = new Universe();
        V3D_Envelope ve = this.universe.init(input, oom, rm);
        System.out.println(ve.toString());
        V3D_Point pt = getDefaultCameraPt(ve, oom, rm, e);
        System.out.println(pt.toStringSimple(""));
        this.universe.setCamera(new Camera(pt, ve,
                this.size.width, this.size.height, oom, rm));
    }

    public static void main(String[] args) {
        try {
            //boolean test = true;
            boolean test = false;
            RoundingMode rm = RoundingMode.HALF_UP;
            if (test) {//Test 
                int oom = -6;
                int w = 100;
                int h = 75;
                Path output = Paths.get("data", "test.png");
                Dimension size = new Dimension(w, h);
                RenderImage r = new RenderImage(output, size, oom, rm);
                r.run();
            }
            if (!test) {
                int oom = -8;
//                int w = 100;
//                int h = 75;
                int w = 500;
                int h = 75 * 5;
                Path dir = Paths.get("data");
                String filename = "Utah_teapot_(solid)";
                //Path dir = Paths.get("data", "geographos");
                //String filename = "1620geographos";
                Path input = Paths.get(dir.toString(), filename + ".stl");
                Path output = Paths.get(dir.toString(), filename + "_" + w + "x" + h + ".png");
                Dimension size = new Dimension(w, h);
                RenderImage r = new RenderImage(input, output, size, oom, rm);
                r.run();
                
                
            }
            if (!test) {
                int oom = -8;
                int w = 500;
                int h = 75 * 5;
                Path dir = Paths.get("data", "geographos");
                String filename = "1620geographos";
                Path input = Paths.get(dir.toString(), filename + ".stl");
                Path output = Paths.get(dir.toString(), filename + "_" + w + "x" + h + "_" + oom + ".png");
                Dimension size = new Dimension(w, h);
                RenderImage r = new RenderImage(input, output, size, oom, rm);
                r.run();
            }
            
//            if (!test) {
//                int oom = -8;
//                int w = 500;
//                int h = 75 * 5;
//                Path dir = Paths.get("data", "Hurricane_Katrina");
//                String filename = "Katrina";
//                Path input = Paths.get(dir.toString(), filename + ".stl");
//                Path output = Paths.get(dir.toString(), filename + "_" + w + "x" + h + "_" + oom + ".png");
//                Dimension size = new Dimension(w, h);
//                RenderImage r = new RenderImage(input, output, size, oom, rm);
//                r.run();
//            }
//            
//            if (!test) {
//                int oom = -8;
//                int w = 500;
//                int h = 75 * 5;
//                Path dir = Paths.get("data", "JWST");
//                String filename = "STScI-01G2ZHTH2GGGR66SJ228TZNPKH";
//                Path input = Paths.get(dir.toString(), filename + ".stl");
//                Path output = Paths.get(dir.toString(), filename + "_" + w + "x" + h + "_" + oom + ".png");
//                Dimension size = new Dimension(w, h);
//                RenderImage r = new RenderImage(input, output, size, oom, rm);
//                r.run();
//            }
                
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void run() throws Exception {
        //BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        // Draw all the things.
        Math_BigRational P1 = Math_BigRational.ONE;
        Math_BigRational N1 = Math_BigRational.ONE.negate();
        V3D_Vector lighting = new V3D_Vector(N1, N1, N1, Math_BigRationalSqrt.ONE).getUnitVector(oom, rm);
        int[] pix = this.universe.camera.render(this.universe, lighting, oom, rm);
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
            RoundingMode rm, V3D_Environment e) {
        Math_BigRational veXMin = ve.getXMin(oom, rm);
        Math_BigRational veXMax = ve.getXMax(oom, rm);
        Math_BigRational veYMin = ve.getYMin(oom, rm);
        Math_BigRational veYMax = ve.getYMax(oom, rm);
        Math_BigRational veZMin = ve.getZMin(oom, rm);
        //Math_BigRational veZMax = ve.getZMax(oom);
        Math_BigRational dx = veXMax.subtract(veXMin);
        Math_BigRational dy = veYMax.subtract(veYMin);
        // pzd is the biggest of xr and yr subtracted from the min z of ve.
        Math_BigRational pzd = veZMin.subtract(dx.max(dy));
        V3D_Point pt = new V3D_Point(e, veXMin.add(dx.divide(2)), veYMin.add(dy.divide(2)), pzd);
        //V3D_Point pt = new V3D_Point(e, Math_BigRational.ZERO, Math_BigRational.ZERO, pzd);
        return pt;
    }

}
