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

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.grids.core.Grids_Environment;
import uk.ac.leeds.ccg.grids.d2.Grids_2D_ID_long;
import uk.ac.leeds.ccg.grids.d2.chunk.i.Grids_ChunkIntFactory;
import uk.ac.leeds.ccg.grids.d2.chunk.i.Grids_ChunkIntFactoryMap;
import uk.ac.leeds.ccg.grids.d2.chunk.i.Grids_ChunkIntFactorySinglet;
import uk.ac.leeds.ccg.grids.d2.grid.Grids_Dimensions;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.math.number.Math_BigRationalSqrt;
import uk.ac.leeds.ccg.r3d.entities.Tetrahedron;
import uk.ac.leeds.ccg.r3d.entities.Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Geometry;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Line;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;
import uk.ac.leeds.ccg.grids.d2.grid.i.Grids_GridInt;
import uk.ac.leeds.ccg.grids.d2.grid.i.Grids_GridIntFactory;
import uk.ac.leeds.ccg.io.IO_Cache;

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
     * The lines from the camera focal point through each pixel.
     */
    public V3D_Point[][] pixelCentres;

    /**
     * For storing the width of a pixel.
     */
    Math_BigRational pixelWidth;

    /**
     * For storing the height of a pixel.
     */
    Math_BigRational pixelHeight;

    /**
     * For storing the index of the closest triangle.
     */
    Grids_GridInt index;

    /**
     * Create a new instance.
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public Camera(V3D_Point pt, V3D_Rectangle screen, int width, int height, int oom) throws Exception {
        super(pt);
        this.screen = screen;
        this.width = width;
        this.height = height;
        V3D_Triangle pqr = screen.getPQR();
        V3D_Point p = pqr.p.getP();
        V3D_Point q = pqr.p.getQ();
        //V3D_Point r = screen.getR();
        V3D_Point s = screen.getS();
        pixelWidth = new Math_BigRationalSqrt(
                p.getDistanceSquared(s, oom), oom)
                .getSqrt(oom).divide(width);
        pixelHeight = new Math_BigRationalSqrt(
                p.getDistanceSquared(q, oom), oom)
                .getSqrt(oom).divide(height);
        lines = new V3D_Line[height][width];
        pixelCentres = new V3D_Point[height][width];
        Math_BigRational z = p.getZ(oom);
        Math_BigRational y = p.getY(oom).add(pixelHeight.divide(2));
        Math_BigRational x0 = p.getX(oom).add(pixelWidth.divide(2));
        for (int row = 0; row < height; row++) {
            Math_BigRational x = x0;
            for (int col = 0; col < width; col++) {
                pixelCentres[row][col] = new V3D_Point(pt.e, x, y, z);
                lines[row][col] = new V3D_Line(pt, pixelCentres[row][col]);
                x = x.add(pixelWidth);
            }
            y = y.add(pixelHeight);
        }
        // Initialise g and index.
        Grids_ChunkIntFactorySinglet gcif = new Grids_ChunkIntFactorySinglet(0);
        Grids_ChunkIntFactory dgcif = new Grids_ChunkIntFactoryMap();
        Path path = Paths.get("C:", "Users", "agdtu", "grids");
        Grids_GridIntFactory gif = new Grids_GridIntFactory(
                new Grids_Environment(), new IO_Cache(path, "v3d"), gcif, dgcif,
                height, width);
        Grids_Dimensions dim = new Grids_Dimensions(height, width);
        index = gif.create(height, width, dim);
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
    int[] render(Universe universe, V3D_Vector lightingVector, int oom)
            throws Exception {
        // mind2 will store the square of the minimum distance to any triangle.
        Math_BigRational[][] mind2 = new Math_BigRational[height][width];
        // Collect all the triangles from the triangles and tetrahedra.
        int ntriangles = universe.triangles.size();
        Triangle[] ts = new Triangle[ntriangles + (4 * universe.tetrahedra.size())];
        for (int i = 0; i < ntriangles; i++) {
            ts[i] = universe.triangles.get(i);
        }
        for (int i = 0; i < universe.tetrahedra.size(); i++) {
            System.arraycopy(universe.tetrahedra.get(i).triangles, 0, ts, i + ntriangles, 4);
        }
        /**
         * Calculate the minimum distance between each and any triangle and the
         * screen. (N.B. Calculating the maximum distance of any object in the
         * field of view is tricky!)
         */
        System.out.println("Calculate minimum distance between each and any"
                + " triangle and the screen.");
        Math_BigRational[] mind2t = new Math_BigRational[ts.length];
        mind2t[0] = screen.getDistanceSquared(ts[0].triangle, oom);
        Math_BigRational minimumDistT = mind2t[0];
        for (int i = 1; i < ts.length; i++) {
            if (i % 100 == 0) {
                System.out.println("Triangle " + i + " out of " + ts.length);
            }
            mind2t[i] = screen.getDistanceSquared(ts[i].triangle, oom);
            if (mind2t[i].compareTo(minimumDistT) == -1) {
                minimumDistT = mind2t[i];
            }
        }
        System.out.println("Minimum distance squared between any triangle and"
                + " the screen = " + minimumDistT.toString());
        /*
         * Calculate which pixels are intersected by each triangle.
         */
        System.out.println("Calculate which pixels are intersected by each "
                + "triangle.");
        HashMap<Grids_2D_ID_long, Set<Integer>> tpix = new HashMap<>();
        for (int i = 0; i < ts.length; i++) {
            if (i % 100 == 0) {
                System.out.println("Triangle " + i + " out of " + ts.length);
            }
            Set<Grids_2D_ID_long> ids = getRCs(ts[i].triangle);
            for (var id : ids) {
                Generic_Collections.addToMap(tpix, id, i);
            }
        }
        /* 
         * Find the closest triangle to each pixel.
         */
        System.out.println("Find the closest triangle to each pixel.");
        for (int row = 0; row < height; row++) {
            if (row % 10 == 0) {
                System.out.println("row " + row + " out of " + height);
            }
            int r = height - row;
            for (int c = 0; c < width; c++) {
                Grids_2D_ID_long id = index.getCellID(r, c);
                if (tpix.containsKey(id)) {
                    Set<Integer> its = tpix.get(id);
                    for (var i : its) {
                        V3D_Triangle t = ts[i].triangle;
                        if (mind2[row][c] == null) {
                            V3D_Geometry ti = t.getIntersection(lines[row][c], oom);
                            if (ti instanceof V3D_Point tip) {
                                Math_BigRational d2 = tip.getDistanceSquared(this, oom);
                                mind2[row][c] = d2;
                                index.setCell(r, c, i);
                            }
                        } else {
                            if (mind2t[i].compareTo(mind2[row][c]) == -1) {
                                V3D_Geometry ti = t.getIntersection(lines[row][c], oom);
                                if (ti instanceof V3D_Point tip) {
                                    Math_BigRational d2 = tip.getDistanceSquared(this, oom);
                                    if (d2.compareTo(mind2[row][c]) == -1) {
                                        mind2[row][c] = d2;
                                        index.setCell(r, c, i);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Render each pixel
        System.out.println("Render each pixel");
        int[] pix = new int[height * width];
        for (int row = 0; row < height; row++) {
            int r = height - row;
            for (int c = 0; c < width; c++) {
                int in = (row * width) + c;
                int i = index.getCell(r, c);
                if (i != index.getNoDataValue()) {
                    pix[in] = ts[i].lightingColor.getRGB();
                } else {
                    pix[in] = Color.BLACK.getRGB();
                }
            }
        }
        return pix;
    }

    protected Set<Grids_2D_ID_long> getRCs(V3D_Triangle t) {
        int oom = screen.e.oom;
        Set<Grids_2D_ID_long> r = new HashSet<>();
        V3D_Line l;
        l = new V3D_Line(t.p.getP(), this);
        Grids_2D_ID_long prc = getRC(l);
        l = new V3D_Line(t.p.getQ(), this);
        Grids_2D_ID_long qrc = getRC(l);
        l = new V3D_Line(t.p.getR(), this);
        Grids_2D_ID_long rrc = getRC(l);
        long minRowIndex = Math.min(Math.min(prc.getRow(), qrc.getRow()), rrc.getRow());
        long maxRowIndex = Math.max(Math.max(prc.getRow(), qrc.getRow()), rrc.getRow());
        long minColIndex = Math.min(Math.min(prc.getCol(), qrc.getCol()), rrc.getCol());
        long maxColIndex = Math.max(Math.max(prc.getCol(), qrc.getCol()), rrc.getCol());
        for (int row = (int) minRowIndex; row <= maxRowIndex; row++) {
            int rowf = (int) index.getNRows() - row;
            for (int col = (int) minColIndex; col <= maxColIndex; col++) {
                V3D_Geometry i = t.getIntersection(this.lines[rowf][col], oom);
                if (i != null) {
                    r.add(new Grids_2D_ID_long(row, col));
                }
            }
        }
        return r;
    }

    /**
     *
     * @param l A line.
     * @param g
     * @return The ID of the screen cell that l intersects.
     */
    protected Grids_2D_ID_long getRC(V3D_Line l) {
        long r = index.getNRows() - getScreenRow(l);
        long c = getScreenCol(l);
        return index.getCellID(r, c);
    }

    /**
     * Calculate and return the row index of the screen that l passes through.
     *
     * @param l A line that intersects with the screen.
     * @return The row index of the screen that l passes through.
     */
    protected int getScreenRow(V3D_Line l) {
        int oom = screen.e.oom;
        V3D_Point p = (V3D_Point) screen.getIntersection(l, oom);
        V3D_Point px = screen.getRSP().p.getQR().l.getPointOfIntersection(p, oom);
        Math_BigRational d = new Math_BigRationalSqrt(px.getDistanceSquared(p, oom), oom).getSqrt(oom);
        return d.divide(pixelHeight).intValue();
    }

    /**
     * Calculate and return the column index of the screen that l passes
     * through.
     *
     * @param l A line that intersects with the screen.
     * @return The column index of the screen that l passes through.
     */
    protected int getScreenCol(V3D_Line l) {
        int oom = screen.e.oom;
        V3D_Point p = (V3D_Point) screen.getIntersection(l, oom);
        V3D_Point py = screen.getPQR().p.getPQ().l.getPointOfIntersection(p, oom);
        Math_BigRational d = new Math_BigRationalSqrt(py.getDistanceSquared(p, oom), oom).getSqrt(oom);
        return d.divide(pixelWidth).intValue();
    }
}
