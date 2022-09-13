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

import java.awt.Graphics;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.math.number.Math_BigRationalSqrt;
import uk.ac.leeds.ccg.r3d.entities.Tetrahedron;
import uk.ac.leeds.ccg.r3d.entities.Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Geometry;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Line;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * Camera instances are situated in the 3D Universe. They are a point behind the
 * centre of a rectangular screen. The distance of the point from the screen is
 * controlled by the position of the point and the screen. The orientation
 * defines the pitch, yaw and roll. The resolution of the screen is controlled
 * by the width and height. Lines or rays come from the point through the centre
 * of each pixel of the screen. The camera renders what it sees of the visible
 * universe. For each thing that is in view through the screen, it is the
 * closest thing which is rendered.
 *
 * @author Andy Turner
 */
public class Camera extends V3D_Point {

    private static final long serialVersionUID = 1L;

    /**
     * The screen.
     */
    public V3D_Rectangle screen;

    /**
     * The number of pixels in a row of the screen.
     */
    public int width;

    /**
     * The number of pixels in a column of the screen.
     */
    public int height;

    /**
     * The lines from the camera focal point through each pixel.
     */
    public V3D_Line[][] lines;

    /**
     * For storing the width of a pixel.
     */
    Math_BigRational pixelWidth;

    /**
     * For storing the height of a pixel.
     */
    Math_BigRational pixelHeight;

    /**
     * Create a new instance.
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public Camera(V3D_Point pt, V3D_Rectangle screen, int width, int height, int oom) {
        super(pt);
        this.screen = screen;
        this.width = width;
        this.height = height;
        V3D_Point p = screen.getP();
        V3D_Point q = screen.getQ();
        //V3D_Point r = screen.getR();
        V3D_Point s = screen.getS();
        pixelWidth = new Math_BigRationalSqrt(
                p.getDistanceSquared(s, oom), oom)
                .getSqrt(oom).divide(width);
        pixelHeight = new Math_BigRationalSqrt(
                p.getDistanceSquared(q, oom), oom)
                .getSqrt(oom).divide(height);
        lines = new V3D_Line[height][width];
        Math_BigRational z = p.getZ(oom);
        Math_BigRational y = p.getY(oom).add(pixelHeight.divide(2));
        Math_BigRational x0 = p.getX(oom).add(pixelWidth.divide(2));
        for (int row = 0; row < height; row++) {
            Math_BigRational x = x0;
            for (int col = 0; col < width; col++) {
                lines[row][col] = new V3D_Line(pt, new V3D_Point(pt.e, x, y, z));
                x = x.add(pixelWidth);
            }
            y = y.add(pixelHeight);
        }
        System.out.println("Initialised Camera");
    }

    /**
     * Change the location of the camera.
     *
     * @param v The vector for the move.
     * @param oom The Order of Magnitude for the precision.
     */
    public void translate(V3D_Vector v, int oom) {
        offset = offset.add(v, oom);
        screen.apply(oom, v);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                lines[row][col].apply(oom, v);
            }
        }
    }

    /**
     * As yet this does not deal with the z of the screen.
     *
     * @param oom
     */
    void render(Graphics g, Universe universe, V3D_Vector lightingVector, int oom) {
        int[][] index = new int[height][width];
        Math_BigRational[][] mind2 = new Math_BigRational[height][width];

        int ntriangles = universe.triangles.size();
        V3D_Triangle[] ts = new V3D_Triangle[ntriangles + (4 * universe.tetrahedra.size())];
        for (int i = 0; i < ntriangles; i++) {
            ts[i] = universe.triangles.get(i).triangle;
        }
        for (int i = 0; i < universe.tetrahedra.size(); i++) {
            System.arraycopy(universe.tetrahedra.get(i).triangles, 0, ts, i + ntriangles, 4);
        }
        int j = 0;
        Tetrahedron tetrahedron = null;
        // Find which triangle is closest to each pixel 
        for (int i = 0; i < ts.length; i++) {
            V3D_Triangle t;
            if (i < ntriangles) {
                Triangle triangle = universe.triangles.get(i);
                triangle.setLighting(lightingVector);
                t = triangle.triangle;
            } else {
                //System.out.println(i - ntriangles);
                if (j == 0) {
                    tetrahedron = universe.tetrahedra.get(i - ntriangles - j);
                    tetrahedron.setLighting(lightingVector);
                    t = tetrahedron.triangles[j];
                    j++;
                } else {
                    t = tetrahedron.triangles[j];
                    if (j == 3) {
                        j = 0;
                    } else {
                        j++;
                    }
                }
            }
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    if (t.isIntersectedBy(lines[r][c], oom)) {
                        V3D_Geometry ti = t.getIntersection(lines[r][c], oom);
                        if (ti instanceof V3D_Point tip) {
                            Math_BigRational d2 = tip.getDistanceSquared(this, oom);
                            if (mind2[r][c] == null) {
                                mind2[r][c] = d2;
                                index[r][c] = i + 1;
                            } else {
                                if (d2.compareTo(mind2[r][c]) == -1) {
                                    mind2[r][c] = d2;
                                    index[r][c] = i + 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Render each pixel
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int i = index[r][c];
                if (i != 0) {
                    if ((i - 1) < ntriangles) {
                        Triangle t = universe.triangles.get(i - 1);
                        //g.setColor(t.baseColor);
                        g.setColor(t.lightingColor);
                        g.fillRect(c, r, 1, 1);
                    } else {
                        j = (i - 1 - ntriangles) % 4;
                        Tetrahedron t = universe.tetrahedra.get(i - 1 - ntriangles - j);
                        g.setColor(t.baseColors[j]);
                        //g.setColor(t.lightingColors[j]);
                        g.fillRect(c, r, 1, 1);
                    }
                }
            }
        }
        System.out.println("Rendered");
    }
}
