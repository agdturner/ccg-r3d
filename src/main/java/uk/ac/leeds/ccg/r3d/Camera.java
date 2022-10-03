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
import java.util.TreeMap;
import java.util.TreeSet;
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
import uk.ac.leeds.ccg.v3d.geometry.V3D_Envelope;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Ray;

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
     * The screen width.
     */
    public Math_BigRational screenHeight;

    /**
     * The screen width.
     */
    public Math_BigRational screenWidth;
    
    /**
     * The number of pixels in a row of the screen.
     */
    public int nrows;

    /**
     * The number of pixels in a column of the screen.
     */
    public int ncols;

//    /**
//     * The lines from the camera focal point through each pixel centre.
//     */
//    public V3D_Ray[][] rays;

    /**
     * The lines from the camera focal point through each pixel.
     */
    //public V3D_Point[][] pixelCentres;

    /**
     * The lines from the camera focal point through each pixel.
     */
    public V3D_Rectangle[][] pixels;

    /**
     * For storing the nrows of a pixel.
     */
    Math_BigRational pixelWidth;

    /**
     * For storing the ncols of a pixel.
     */
    Math_BigRational pixelHeight;

    /**
     * For storing the index of the closest triangle.
     */
    Grids_GridInt index;
    //int[][] index;

    /**
     * Create a new instance.
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public Camera(V3D_Point pt, V3D_Envelope ve, int width, int height, int oom) throws Exception {
        super(pt);
        Math_BigRational veXMin = ve.getXMin(oom);
        Math_BigRational veXMax = ve.getXMax(oom);
        Math_BigRational veYMin = ve.getYMin(oom);
        Math_BigRational veYMax = ve.getYMax(oom);
        Math_BigRational veZMin = ve.getZMin(oom);
        this.screen = new V3D_Rectangle(
                new V3D_Point(e, veXMin, veYMin, veZMin),
                new V3D_Point(e, veXMin, veYMax, veZMin),
                new V3D_Point(e, veXMax, veYMax, veZMin),
                new V3D_Point(e, veXMax, veYMin, veZMin));
        this.nrows = height;
        this.ncols = width;
        V3D_Triangle pqr = screen.getPQR();
        V3D_Point p = pqr.p.getP();
        V3D_Point q = pqr.p.getQ();
        //V3D_Point r = screen.getR();
        V3D_Point s = screen.getS();
        int oom2 = oom + oom;
        screenWidth = new Math_BigRationalSqrt(
                p.getDistanceSquared(s, oom2, e.rm), oom2, e.rm)
                .getSqrt(oom2, e.rm);
        pixelWidth = screenWidth.divide(width);
        screenHeight = new Math_BigRationalSqrt(
                p.getDistanceSquared(q, oom2, e.rm), oom2, e.rm)
                .getSqrt(oom2, e.rm);
        pixelHeight = screenHeight.divide(height);
        //rays = new V3D_Ray[height][width];
        //pixelCentres = new V3D_Point[height][width];
        pixels = new V3D_Rectangle[height][width];
        // Initialise index.
        Grids_ChunkIntFactorySinglet gcif = new Grids_ChunkIntFactorySinglet(0);
        Grids_ChunkIntFactory dgcif = new Grids_ChunkIntFactoryMap();
        Path path = Paths.get("data", "grids");
        Grids_GridIntFactory gif = new Grids_GridIntFactory(
                new Grids_Environment(), new IO_Cache(path, "v3d"), gcif, dgcif,
                height, width);
        Grids_Dimensions dim = new Grids_Dimensions(veXMin, veXMax, veYMin, veYMax, pixelHeight);
        index = gif.create(height, width, dim);
        //index = new int[ncols][nrows];
        Math_BigRational z = p.getZ(oom, e.rm);
        for (int row = 0; row < height; row++) {
            Math_BigRational y = index.getCellY(row);
            for (int col = 0; col < width; col++) {
                Math_BigRational x = index.getCellX(col);
                //pixelCentres[row][col] = new V3D_Point(pt.e, x, y, z);
                Math_BigRational[] cellBounds = index.getCellBounds((long) row, (long) col);
                pixels[row][col] = new V3D_Rectangle(
                        new V3D_Point(e, cellBounds[0], cellBounds[1], z),
                        new V3D_Point(e, cellBounds[0], cellBounds[3], z),
                        new V3D_Point(e, cellBounds[2], cellBounds[3], z),
                        new V3D_Point(e, cellBounds[2], cellBounds[1], z));
                //rays[row][col] = new V3D_Ray(pt, pixelCentres[row][col]);
                //rays[row][col] = new V3D_Ray(pt, new V3D_Point(pt.e, x, y, z));
            }
        }
        System.out.println("Initialised Camera");
    }
    
//    /**
//     * Create a new instance.
//     *
//     * @param p The camera observer location.
//     * @param screen The screen.
//     */
//    public Camera(V3D_Point pt, V3D_Rectangle screen, int width, int height, int oom) throws Exception {
//        super(pt);
//        this.screen = screen;
//        this.nrows = width;
//        this.ncols = height;
//        V3D_Triangle pqr = screen.getPQR();
//        V3D_Point p = pqr.p.getP();
//        V3D_Point q = pqr.p.getQ();
//        //V3D_Point r = screen.getR();
//        V3D_Point s = screen.getS();
//        pixelWidth = new Math_BigRationalSqrt(
//                p.getDistanceSquared(s, oom), oom)
//                .getSqrt(oom).divide(width);
//        pixelHeight = new Math_BigRationalSqrt(
//                p.getDistanceSquared(q, oom), oom)
//                .getSqrt(oom).divide(height);
//        lines = new V3D_Line[height][width];
//        pixelCentres = new V3D_Point[height][width];
//        Math_BigRational z = p.getZ(oom);
//        Math_BigRational y = p.getY(oom).add(pixelHeight.divide(2));
//        Math_BigRational x0 = p.getX(oom).add(pixelWidth.divide(2));
//        for (int row = 0; row < height; row++) {
//            Math_BigRational x = x0;
//            for (int col = 0; col < width; col++) {
//                pixelCentres[row][col] = new V3D_Point(pt.e, x, y, z);
//                lines[row][col] = new V3D_Line(pt, pixelCentres[row][col]);
//                x = x.add(pixelWidth);
//            }
//            y = y.add(pixelHeight);
//        }
//        // Initialise g and index.
//        Grids_ChunkIntFactorySinglet gcif = new Grids_ChunkIntFactorySinglet(0);
//        Grids_ChunkIntFactory dgcif = new Grids_ChunkIntFactoryMap();
//        Path path = Paths.get("data", "grids");
//        Grids_GridIntFactory gif = new Grids_GridIntFactory(
//                new Grids_Environment(), new IO_Cache(path, "v3d"), gcif, dgcif,
//                height, width);
//        Grids_Dimensions dim = new Grids_Dimensions(veXMin, veXMax, veYMin, veYMax, pixelHeight);
//        index = gif.create(height, width, dim);
//        //index = new int[ncols][nrows];
//        System.out.println("Initialised Camera");
//    }

    /**
     * Change the location of the camera.
     *
     * @param v The vector for the move.
     * @param oom The Order of Magnitude for the precision.
     */
    public void translate(V3D_Vector v, int oom) {
        offset = offset.add(v, oom, e.rm);
        screen.apply(v, oom, e.rm);
        for (int row = 0; row < nrows; row++) {
            for (int col = 0; col < ncols; col++) {
                //lines[row][col].apply(oom, v);
            }
        }
    }

    /**
     * As yet this does not deal with the z of the screen.
     *
     * @param oom
     */
    int[] render(Universe universe, V3D_Vector lighting, int oom)
            throws Exception {
        // mind2 will store the square of the minimum distance to any triangle.
        Math_BigRational[][] mind2 = new Math_BigRational[nrows][ncols];
        // Collect all the triangles from the triangles and tetrahedra.
        int ntriangles = universe.triangles.size();
        int nt = ntriangles + (4 * universe.tetrahedra.size());
        Triangle[] ts = new Triangle[nt];
        for (int i = 0; i < ntriangles; i++) {
            ts[i] = universe.triangles.get(i);
        }
        for (int i = 0; i < universe.tetrahedra.size(); i++) {
            System.arraycopy(universe.tetrahedra.get(i).triangles, 0, ts, i + ntriangles, 4);
        }
        V3D_Tetrahedron[] tetras = new V3D_Tetrahedron[nt];
        for (int i = 0; i < ts.length; i ++) {
            tetras[i] = new V3D_Tetrahedron(this, ts[i].triangle);
        }
        /**
         * Calculate the minimum distance between each and any triangle and the
         * screen and set the lighting.
         */
        System.out.println("Calculate the minimum distance between each and any"
                + " triangle and the screen and order triangles in terms of"
                + " their minimum distance to the screen.");
        //TreeMap<Math_BigRational, Set<Integer>> ots = new TreeMap<>();
        Math_BigRational[] mind2t = new Math_BigRational[ts.length];
        mind2t[0] = screen.getDistanceSquared(ts[0].triangle, oom, e.rm);
        //Generic_Collections.addToMap(ots, mind2t[0], 0);
        Math_BigRational minimumDistT = mind2t[0];
        for (int i = 1; i < ts.length; i++) {
            if (i % 100 == 0) {
                System.out.println("Triangle " + i + " out of " + ts.length);
            }
            ts[i].setLighting(lighting);
            mind2t[i] = screen.getDistanceSquared(ts[i].triangle, oom, e.rm);
            //Generic_Collections.addToMap(ots, mind2t[i], i);
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
        int j = 0;
        for (int i = 0; i < ts.length; i++) {
            //for (var k : ots.keySet()) {
            //    for (var i : ots.get(k)) {
            if (j % 100 == 0) {
                System.out.println("Triangle " + j + " out of " + ts.length);
            }
            Set<Grids_2D_ID_long> ids = getRCs(ts[i].triangle, tetras[i]);
            for (var id : ids) {
                Generic_Collections.addToMap(tpix, id, i);
            }
            j++;
            //}
        }
        /* 
         * Find the closest triangle to each pixel and a list of triangle 
         * indexes that are to be rendered.
         */
        System.out.println("Find the closest triangle to each pixel.");
        for (int row = 0; row < nrows; row++) {
            if (row % 10 == 0) {
                System.out.println("row " + row + " out of " + nrows);
            }
            for (int col = 0; col < ncols; col++) {
                Grids_2D_ID_long id = index.getCellID((long) row, (long) col);
                if (tpix.containsKey(id)) {
                    Set<Integer> its = tpix.get(id);
                    for (var in : its) {
                        V3D_Triangle t = ts[in].triangle;
                        V3D_Tetrahedron tetra = tetras[in];
                        if (mind2[row][col] == null) {
                            if (pixels[row][col].isIntersectedBy(tetra, oom, e.rm)) {
                            //if (pixels[row][col].isIntersectedBy(ray, oom)) {
                            //V3D_Geometry ti = t.getIntersection(lines[row][col], oom);
                            //if (ti instanceof V3D_Point tip) {
                            //    Math_BigRational d2 = tip.getDistanceSquared(this, oom);
                                Math_BigRational d2 = t.getDistanceSquared(this, oom, e.rm);
                                mind2[row][col] = d2;
                                index.setCell(row, col, in + 1);
                            }
                        } else {
                            if (mind2t[in].compareTo(mind2[row][col]) == -1) {
                                if (pixels[row][col].isIntersectedBy(tetra, oom, e.rm)) {
                                //V3D_Geometry ti = t.getIntersection(lines[row][col], oom);
                                //if (ti instanceof V3D_Point tip) {
                                    Math_BigRational d2 = t.getDistanceSquared(this, oom, e.rm);
                                    if (d2.compareTo(mind2[row][col]) == -1) {
                                        mind2[row][col] = d2;
                                        index.setCell(row, col, in + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Render each pixel flipping upside down for an image.
        System.out.println("Render each pixel");
        int noDataValue = index.getNoDataValue();
        int n = ncols * nrows;
        int[] pix = new int[n];
        for (int row = 0; row < nrows; row++) {
            if (row % 10 == 0) {
                System.out.println("row " + row + " out of " + nrows);
            }
            int r = nrows - row - 1;
            for (int c = 0; c < ncols; c++) {
                //int in = n - ((row * ncols) + c) - 1;
                //int in = (row * ncols) + c;
                int in = (r * ncols) + c;
                int ind = index.getCell(row, c);
                if (ind == noDataValue) {
                    pix[in] = Color.BLACK.getRGB();
                } else {
                    if (ind != 0) {
                        pix[in] = ts[ind - 1].lightingColor.getRGB();
                    } else {
                        pix[in] = Color.BLACK.getRGB();
                    }
                }
            }
        }
        return pix;
    }

    protected Set<Grids_2D_ID_long> getRCs(V3D_Triangle t, V3D_Tetrahedron tetra) {
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
        if (minRowIndex < 0) {
            minRowIndex = 0;
        }
        if (minColIndex < 0) {
            minColIndex = 0;
        }
        if (maxRowIndex >=  nrows) {
            maxRowIndex = nrows - 1;
        }
        if (maxColIndex >=  ncols) {
            maxColIndex = ncols - 1;
        }
        for (int row = (int) minRowIndex; row < maxRowIndex; row++) {
            for (int col = (int) minColIndex; col < maxColIndex; col++) {
                index.getCellBounds(row, col);
                //V3D_Geometry i = lines.getIntersection(this.pixels[row][col], oom);
                V3D_Geometry i = tetra.getIntersection(this.pixels[row][col], oom, e.rm);
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
        return index.getCellID((long) getScreenRow(l), (long) getScreenCol(l));
    }

    /**
     * Calculate and return the row index of the screen that l passes through.
     *
     * @param l A line that intersects with the screen.
     * @return The row index of the screen that l passes through.
     */
    protected int getScreenRow(V3D_Line l) {
        int oom = screen.e.oom;
        V3D_Point p = (V3D_Point) screen.getIntersection(l, oom, e.rm);
        V3D_Point px = screen.getRSP().p.getQR().l.getPointOfIntersection(p, oom, e.rm);
        Math_BigRational d = new Math_BigRationalSqrt(px.getDistanceSquared(p, oom, e.rm), oom, e.rm).getSqrt(oom, e.rm);
        return d.multiply(nrows).divide(screenHeight).intValue();
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
        V3D_Point p = (V3D_Point) screen.getIntersection(l, oom, e.rm);
        V3D_Point py = screen.getPQR().p.getPQ().l.getPointOfIntersection(p, oom, e.rm);
        Math_BigRational d = new Math_BigRationalSqrt(py.getDistanceSquared(p, oom, e.rm), oom, e.rm).getSqrt(oom, e.rm);
        return d.multiply(ncols).divide(screenWidth).intValue();
    }
}
