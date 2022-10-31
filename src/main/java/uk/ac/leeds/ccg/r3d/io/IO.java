/*
 * Copyright 2021 Centre for Computational Geography, University of Leeds.
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
package uk.ac.leeds.ccg.r3d.io;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * For IO.
 * 
 * @author Andy Turner
 */
public class IO {
    
    /**
     * Create an instance.
     */
    public IO(){}
    
    /**
     * For writing out images.
     * 
     * @param image The image to write out.
     * @param format The format e.g. "jpeg", "png", ...
     * @param p The path of the file to write to.
     */
    public static void imageToFile(Image image, String format, Path p) {
        Panel panel = new Panel();
        BufferedImage bi = new BufferedImage(image.getWidth(panel), 
                image.getHeight(panel), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(image, 0, 0, panel);
        RenderedImage ri = (RenderedImage) bi;
        g2d.dispose();
        try {
            Files.createDirectories(p.getParent());
            ImageIO.write(ri, format, p.toFile());
        } catch (IOException e) {
            System.out.println("Format not recognised: " + format);
        }
    }
    
}
