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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.nio.file.Paths;

import javax.swing.JFrame;
import uk.ac.leeds.ccg.math.arithmetic.Math_BigInteger;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.io.IO;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class RenderImage {

    private static final long serialVersionUID = 1L;

    /**
     * The width and height.
     */
    public final Dimension size;

    /**
     *
     */
    public Universe universe;

    public int oom;

    /**
     * For storing the state running or not.
     */
    private static boolean running = false;

    /**
     * Create a new instance.
     */
    public RenderImage() throws Exception {
        this.size = new Dimension(100, 75);
        this.oom = -3;
        V3D_Environment e = new V3D_Environment(this.oom, new Math_BigInteger());
        Math_BigRational ptx = Math_BigRational.ZERO;
        Math_BigRational pty = Math_BigRational.ZERO;
        Math_BigRational ptz = Math_BigRational.valueOf(this.size.height).negate();
        V3D_Point pt = new V3D_Point(e, ptx, pty, ptz);
        
//        Math_BigRational halfwidth = Math_BigRational.valueOf(this.size.width).divide(2);
//        Math_BigRational halfheight = Math_BigRational.valueOf(this.size.height).divide(2);
//        Math_BigRational xmin = ptx.subtract(halfwidth);
//        Math_BigRational xmax = ptx.add(halfwidth);
//        Math_BigRational ymin = pty.subtract(halfheight);
//        Math_BigRational ymax = pty.add(halfheight);
//        Math_BigRational depth = Math_BigRational.ZERO;
        
        Math_BigRational xmin = ptx.subtract(10);
        Math_BigRational xmax = ptx.add(10);
        Math_BigRational P7_5 = Math_BigRational.valueOf(15).divide(2);
        Math_BigRational ymin = pty.subtract(P7_5);
        Math_BigRational ymax = pty.add(P7_5);
        Math_BigRational depth = Math_BigRational.TEN.negate();

        V3D_Rectangle screen = new V3D_Rectangle(
                new V3D_Point(e, xmin, ymin, depth),
                new V3D_Point(e, xmin, ymax, depth),
                new V3D_Point(e, xmax, ymax, depth),
                new V3D_Point(e, xmax, ymin, depth));
        Camera camera = new Camera(pt, screen,
                this.size.width, this.size.height, this.oom);
        this.universe = new Universe(camera);
        //this.universe.init0();
        //this.universe.init1();
        this.universe.initUtah();
    }

    public static void main(String[] args) {
        try {
            new RenderImage().run();
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void run() throws Exception {
        BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
//        Graphics g = bi.getGraphics();
//        /**
//         * Set the background to be black.
//         */
//        g.setColor(Color.BLUE);
//        int w = size.width;
//        int h = size.height;
//        g.fillRect(0, 0, w, h);

        // Draw all the things.
        V3D_Vector lighting = new V3D_Vector(-1, -1, -1).getUnitVector(oom);
        int[] pix = this.universe.camera.render(this.universe, lighting, oom);

//        for (int i = 0; i < pix.length; i ++) {
//            if (pix[i] != 0) {
//                System.out.println("" + i + ", " + pix[i]);
//            }
//        }
        /**
         * Convert graphics to an image and write to a file.
         */
        MemoryImageSource m = new MemoryImageSource(size.width, size.height, pix, 0, size.width);
        Panel panel = new Panel();
//        Graphics g = panel.getGraphics();
//        /**
//         * Set the background to be black.
//         */
//        g.setColor(Color.BLACK);
//        int w = size.width;
//        int h = size.height;
//        g.fillRect(0, 0, w, h);

        Image image = panel.createImage(m);
        // Image image = this.createImage(m);
        //Image image = this.createImage(w, h);
        //g.drawImage(image, w, h, panel);
        //g.dispose();
        IO.imageToFile(image, "png", Paths.get("data", "test.png"));
        System.out.println("Rendered");
    }
}
