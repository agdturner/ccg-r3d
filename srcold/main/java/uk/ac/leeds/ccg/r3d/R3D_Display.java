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
import java.awt.image.BufferStrategy;
import java.io.Serializable;

import javax.swing.JFrame;
import uk.ac.leeds.ccg.r3d.entity.R3D_EntityManager;
import uk.ac.leeds.ccg.r3d.input.UserInput;

public class R3D_Display extends Canvas implements Serializable, Runnable {

    private static final long serialVersionUID = 1L;

    /**
     * Thread for the display.
     */
    private Thread thread;
    
    /**
     * Frame for the display.
     */
    private JFrame frame;

    /**
     * Title for {@link #frame}.
     */
    private static String title = "Render 3D";
    
    /**
     * The width of this.
     */
    public static final int WIDTH = 800;

    /**
     * The height of this.
     */
    public static final int HEIGHT = 600;
    
    /**
     * For storing the state running or not.
     */
    private static boolean running = false;

    private final R3D_EntityManager entityManager;

    private final UserInput userInput;

    /**
     * Create a new instance.
     */
    public R3D_Display() {
        this.frame = new JFrame();

        Dimension size = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(size);

        this.userInput = new UserInput();

        this.entityManager = new R3D_EntityManager();

        this.addMouseListener(this.userInput.mouse);
        this.addMouseMotionListener(this.userInput.mouse);
        this.addMouseWheelListener(this.userInput.mouse);
        this.addKeyListener(this.userInput.keyboard);
    }

    public static void main(String[] args) {
        R3D_Display display = new R3D_Display();
        display.frame.setTitle(title);
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
        int oom = -7;
        
        this.entityManager.init(this.userInput, oom);

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
                this.frame.setTitle(title + " | " + frames + " fps");
                frames = 0;
            }
        }

        stop();
    }

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
         * There should be a BufferStrategy, so get the Graphics to draw things on.
         */
        Graphics g = bs.getDrawGraphics();

        /**
         * Set the background to be black.
         */
        g.setColor(Color.BLACK);
        int w = WIDTH + frame.getInsets().left + frame.getInsets().right;
        int h = HEIGHT + frame.getInsets().top + frame.getInsets().bottom;
                
        g.fillRect(0, 0, w, h);

        this.entityManager.render(g);

        g.dispose();
        bs.show();

    }

    private void update() {
        this.entityManager.update();
    }

}
