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
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import uk.ac.leeds.ccg.math.arithmetic.Math_BigInteger;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class Display extends Canvas implements Runnable {

    /**
     * Thread for the display.
     */
    private Thread thread;

    /**
     * Frame for the display.
     */
    private final JFrame frame;

    /**
     * Title for {@link #frame}.
     */
    private static final String TITLE = "Render 3D";

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
    public Display() {
        this.frame = new JFrame();
        //this.size = new Dimension(200, 150);
        this.size = new Dimension(100, 75);
        this.setPreferredSize(size);
        this.oom = -3;
        V3D_Environment e = new V3D_Environment(this.oom, new Math_BigInteger());
        Math_BigRational ptx = Math_BigRational.ZERO;
        Math_BigRational pty = Math_BigRational.ZERO;
        Math_BigRational ptz = Math_BigRational.valueOf(this.size.height).negate();
        V3D_Point pt = new V3D_Point(e, ptx, pty, ptz);
        Math_BigRational halfwidth = Math_BigRational.valueOf(this.size.width).divide(2);
        Math_BigRational halfheight = Math_BigRational.valueOf(this.size.height).divide(2);
//        Math_BigRational xmin = ptx.subtract(halfwidth);
//        Math_BigRational xmax = ptx.add(halfwidth);
//        Math_BigRational ymin = pty.subtract(halfheight);
//        Math_BigRational ymax = pty.add(halfheight);
        Math_BigRational xmin = ptx.subtract(10);
        Math_BigRational xmax = ptx.add(10);
        Math_BigRational P7_5 = Math_BigRational.valueOf(15).divide(2);
        Math_BigRational ymin = pty.subtract(P7_5);
        Math_BigRational ymax = pty.add(P7_5);
        Math_BigRational depth = Math_BigRational.ZERO;
        V3D_Rectangle screen = new V3D_Rectangle(
                new V3D_Point(e, xmin, ymin, depth),
                new V3D_Point(e, xmin, ymax, depth),
                new V3D_Point(e, xmax, ymax, depth),
                new V3D_Point(e, xmax, ymin, depth));
        Camera camera = new Camera(pt, screen,
                 this.size.width, this.size.height, this.oom);
        this.universe = new Universe(camera);
    }

    public static void main(String[] args) {
        Display display = new Display();
        display.frame.setTitle(TITLE);
        display.frame.add(display);
        display.frame.pack();
        display.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Location should be the centre of the screen using null. 
        display.frame.setLocationRelativeTo(null);
        display.frame.setResizable(false);
        display.frame.setVisible(true);
        display.start();
    }

    public synchronized void start() {
        running = true;
        this.thread = new Thread(this, "Display");
        this.thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            this.thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        /**
         * lastTime records the time at the start of the run.
         */
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();

        double updatesPerSecond = 60.0d;
        /**
         * ns is the number of updates per nanosecond if we are updating at
         * updatesPerSecond updates per second.
         */
        final double ns = 1000000000.0 / updatesPerSecond;

        /**
         * Progress towards next update.
         */
        double delta = 0;
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            /**
             * Use a while loop here instead of an if as delta might get more
             * than 2.
             */
            while (delta >= 1) {
                update();
                delta--;
                render();
                frames++;
            }

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                this.frame.setTitle(TITLE + " | " + frames + " fps");
                frames = 0;
            }
        }

        stop();
    }

    int image_number = 0;

    /**
     * Used to render things to the screen.
     */
    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        /**
         * If there is no BufferStrategy, then create it.
         */
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        /**
         * Graphics to draw things is created from the BufferStrategy.
         */
        Graphics g = bs.getDrawGraphics();

        /**
         * Set the background to be black.
         */
        g.setColor(Color.BLACK);
        int w = size.width + frame.getInsets().left + frame.getInsets().right;
        int h = size.height + frame.getInsets().top + frame.getInsets().bottom;
        g.fillRect(0, 0, w, h);

        // Draw all the things.
        this.universe.camera.render(g, this.universe, new V3D_Vector(-1, -1, -1), oom);

        bs.show();

        // Write to image
        Image image = this.createImage(w, h);
        g.drawImage(image, w, h, this);
        g.dispose();

        image_number++;
    }

    private void update() {
        this.universe.update();
    }

}
