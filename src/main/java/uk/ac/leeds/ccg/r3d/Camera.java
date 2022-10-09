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
import java.math.BigInteger;
import java.math.RoundingMode;
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
import uk.ac.leeds.ccg.v3d.geometry.V3D_LineSegment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Plane;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Ray;

/**
 * Camera instances are situated in the 3D Universe. They are a point behind the
 * centre of a rectangular screen. The distance of the point from the screen is
 * controlled by the position of the point and the screen. The orientation
 * defines the pitch, yaw and roll. The resolution of the screen is controlled
 * by the width and height. The pixels of the screen are square. Rays come from
 * the point through the centre of each pixel of the screen. You can also think
 * of a blinkered view for each pixel which is a bit like that of the screen
 * itself. Whatever is in the field of view may be depicted. It is possible to
 * calculate the area of each shape that would show up on a pixel working from
 * the foreground to the background.
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

    /**
     * Rays from the camera focal point through each pixel centre.
     */
    public V3D_Ray[][] rays;

//    /**
//     * The pixels of the camera.
//     */
//    public V3D_Rectangle[][] pixels;

    /**
     * For storing the length and width of a pixel (which is square).
     */
    Math_BigRational pixelSize;

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
    public Camera(V3D_Point pt, V3D_Envelope ve, int width, int height, int oom,
            RoundingMode rm) throws Exception {
        super(pt);
        // Initialise the screen
        Math_BigRational veXMin = ve.getXMin(oom);
        Math_BigRational veXMax = ve.getXMax(oom);
        Math_BigRational veYMin = ve.getYMin(oom);
        Math_BigRational veYMax = ve.getYMax(oom);
        //Math_BigRational veZMin = ve.getZMin(oom);
        Math_BigRational dx = veXMax.subtract(veXMin);
        Math_BigRational dy = veYMax.subtract(veYMin);
//        V3D_Point centroid = new V3D_Point(ve.getCentroid(oom, rm));
//        V3D_Vector n = new V3D_Vector(this, centroid, oom, rm);
//        V3D_Plane p = new V3D_Plane(centroid, n, oom, rm);
//        V3D_Rectangle r = (V3D_Rectangle) ve.getIntersection(p, oom, rm);
//        screenWidth = r.getPQR().p.getPQ().getLength(oom, rm).getSqrt(oom, rm);
//        screenHeight = r.getPQR().p.getQR().getLength(oom, rm).getSqrt(oom, rm);
        Math_BigRational hwr = dx.divide(dy);
        Math_BigRational hwr2 = Math_BigRational.valueOf(width).divide(Math_BigRational.valueOf(height));
        if (hwr.compareTo(hwr2) == 1) {
            // When scaled, the data dimension is bigger width wise than the screen
            screenWidth = dx;
            pixelSize = screenWidth.divide(width);
            screenHeight = pixelSize.multiply(height);
        } else {
            // Data is bigger height wise than the screen
            screenHeight = dy;
            pixelSize = screenHeight.divide(height);
            screenWidth = pixelSize.multiply(width);
        }
        Math_BigRational swd2 = screenWidth.divide(2);
        Math_BigRational shd2 = screenHeight.divide(2);
        Math_BigRational nswd2 = swd2.negate();
        Math_BigRational nshd2 = shd2.negate();
        Math_BigRational screenZ = Math_BigRational.ZERO;

        this.screen = new V3D_Rectangle(
                new V3D_Point(e, nswd2, nshd2, screenZ),
                new V3D_Point(e, nswd2, shd2, screenZ),
                new V3D_Point(e, swd2, shd2, screenZ),
                new V3D_Point(e, swd2, nshd2, screenZ), oom, rm);
        this.nrows = height;
        this.ncols = width;
        rays = new V3D_Ray[height][width];
        //pixelCentres = new V3D_Point[height][width];
        //pixels = new V3D_Rectangle[height][width];
        // Initialise index.
        Grids_ChunkIntFactorySinglet gcif = new Grids_ChunkIntFactorySinglet(0);
        Grids_ChunkIntFactory dgcif = new Grids_ChunkIntFactoryMap();
        Path path = Paths.get("data", "grids");
        Grids_GridIntFactory gif = new Grids_GridIntFactory(
                new Grids_Environment(), new IO_Cache(path, "v3d"), gcif, dgcif,
                height, width);
        Grids_Dimensions dim = new Grids_Dimensions(nswd2, swd2, nshd2, shd2, pixelSize);
        index = gif.create(height, width, dim);
        //index = new int[ncols][nrows];
        for (int row = 0; row < height; row++) {
            Math_BigRational y = index.getCellY(row);
            for (int col = 0; col < width; col++) {
                Math_BigRational x = index.getCellX(col);
                //pixelCentres[row][col] = new V3D_Point(pt.e, x, y, z);
                //Math_BigRational[] cellBounds = index.getCellBounds((long) row, (long) col);
//                pixels[row][col] = new V3D_Rectangle(
//                        new V3D_Point(e, cellBounds[0], cellBounds[1], screenZ),
//                        new V3D_Point(e, cellBounds[0], cellBounds[3], screenZ),
//                        new V3D_Point(e, cellBounds[2], cellBounds[3], screenZ),
//                        new V3D_Point(e, cellBounds[2], cellBounds[1], screenZ), oom, rm);
                rays[row][col] = new V3D_Ray(pt, new V3D_Point(pt.e, x, y, screenZ), oom, rm);
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
    @Override
    public void translate(V3D_Vector v, int oom, RoundingMode rm) {
        offset = offset.add(v, oom, rm);
        screen.translate(v, oom, rm);
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
    int[] render(Universe universe, V3D_Vector lighting, int oom, RoundingMode rm)
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
        for (int i = 0; i < ts.length; i++) {
            tetras[i] = new V3D_Tetrahedron(this, ts[i].triangle, oom, rm);
        }
        /**
         * Calculate the minimum distance between each and any triangle and the
         * screen and set the lighting.
         */
        System.out.println("Calculate the minimum distance between each and any"
                + " triangle and the this.");
        //TreeMap<Math_BigRational, Set<Integer>> ots = new TreeMap<>();
        Math_BigRational[] mind2t = new Math_BigRational[ts.length];
        mind2t[0] = screen.getDistanceSquared(ts[0].triangle, oom, rm);
        //Generic_Collections.addToMap(ots, mind2t[0], 0);
        Math_BigRational minimumDistT = mind2t[0];
        for (int i = 1; i < ts.length; i++) {
            if (i % 100 == 0) {
                System.out.println("Triangle " + i + " out of " + ts.length);
            }
            ts[i].setLighting(lighting, oom, rm);
            //mind2t[i] = screen.getDistanceSquared(ts[i].triangle, oom, rm);
            mind2t[i] = this.getDistanceSquared(ts[i].triangle, oom, rm);
            //Generic_Collections.addToMap(ots, mind2t[i], i);
            if (mind2t[i].compareTo(minimumDistT) == -1) {
                minimumDistT = mind2t[i];
            }
        }
        System.out.println("Minimum distance squared between any triangle and"
                + " this = " + minimumDistT.toString());
        /**
         * Calculate which triangles are in view through which pixel. Various
         * things have been tried. Currently this works by finding intersections
         * with the ray from this through the centre of each pixel. (I have also
         * used tetrahedra formed by the triangles and this.) It should be
         * possible to find the area of intersection on each pixel of each
         * triangle and then working from front to back calculate the areas of
         * each triangle on the pixel. This could then be used to assign a
         * colour to allow for things in the background not to be obscured by
         * small things in the foreground.
         */
        System.out.println("Calculate which pixels are intersected by each "
                + "triangle.");
        HashMap<Grids_2D_ID_long, Set<Integer>> tpix = new HashMap<>();
        int j = 0;
        for (int i = 0; i < ts.length; i++) {
            //for (var k : ots.keySet()) {
            //    for (var i : ots.get(k)) {
            if (j % 100 == 0) {
                System.out.println("Triangle " + (j + 1) + " out of " + ts.length + ":");
                //System.out.println(ts[i].triangle);
            }
            Set<Grids_2D_ID_long> ids = getRCs(ts[i].triangle, tetras[i], oom, rm);
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
                        //V3D_Tetrahedron tetra = tetras[in];
                        if (mind2[row][col] == null) {
                            //if (pixels[row][col].isIntersectedBy(tetra, oom, rm)) {
                            if (t.isIntersectedBy(rays[row][col], oom, rm)) {
                                V3D_Geometry ti = t.getIntersection(rays[row][col], oom, rm);
                                if (ti instanceof V3D_Point tip) {
                                    Math_BigRational d2 = tip.getDistanceSquared(this, oom, rm);
                                    mind2[row][col] = d2;
                                    index.setCell(row, col, in + 1);
                                }
                            }
                        } else {
                            if (mind2t[in].compareTo(mind2[row][col]) == -1) {
                                //if (pixels[row][col].isIntersectedBy(tetra, oom, rm)) {
                                if (t.isIntersectedBy(rays[row][col], oom, rm)) {
                                    V3D_Geometry ti = t.getIntersection(rays[row][col], oom, rm);
                                    if (ti instanceof V3D_Point tip) {
                                        Math_BigRational d2 = tip.getDistanceSquared(this, oom, rm);
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

    /**
     * get the CellIDs of those cells that intersect t.
     *
     * @param t
     * @param tetra
     * @param oom
     * @param rm
     * @return
     */
    protected Set<Grids_2D_ID_long> getRCs(V3D_Triangle t,
            V3D_Tetrahedron tetra, int oom, RoundingMode rm) {
        Set<Grids_2D_ID_long> r = new HashSet<>();
        V3D_Ray ray;
        //ray = new V3D_Ray(t.p.getP(), this, oom, rm);
        ray = new V3D_Ray(this, t.p.getP(), oom, rm);
        Grids_2D_ID_long prc = getRC(ray, oom, rm);
        //ray = new V3D_Ray(t.p.getQ(), this, oom, rm);
        ray = new V3D_Ray(this, t.p.getQ(), oom, rm);
        Grids_2D_ID_long qrc = getRC(ray, oom, rm);
        //ray = new V3D_Ray(t.p.getR(), this, oom, rm);
        ray = new V3D_Ray(this, t.p.getR(), oom, rm);
        Grids_2D_ID_long rrc = getRC(ray, oom, rm);
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
        if (maxRowIndex >= nrows) {
            maxRowIndex = nrows - 1;
        }
        if (maxColIndex >= ncols) {
            maxColIndex = ncols - 1;
        }
        for (int row = (int) minRowIndex; row < maxRowIndex + 1; row++) {
            for (int col = (int) minColIndex; col < maxColIndex + 1; col++) {
                //System.out.println("" + row + " " + col);
                //index.getCellBounds(row, col);
                //if (tetra.isIntersectedBy(this.pixels[row][col], oom, rm)) {
                if (t.isIntersectedBy(rays[row][col], oom, rm)) {
                    r.add(new Grids_2D_ID_long((long) row, (long) col));
                }
            }
        }
        return r;
    }

    /**
     *
     * @param ray A line.
     * @param g
     * @return The ID of the screen cell that tay intersects.
     */
    protected Grids_2D_ID_long getRC(V3D_Ray ray, int oom, RoundingMode rm) {
        return index.getCellID((long) getScreenRow(ray, oom, rm),
                (long) getScreenCol(ray, oom, rm));
    }

    /**
     * Calculate and return the row index of the screen that l passes through.
     *
     * @param ray A ray that intersects with the screen.
     * @return The row index of the screen that l passes through.
     */
    protected int getScreenRow(V3D_Ray ray, int oom, RoundingMode rm) {
        V3D_Point p = (V3D_Point) screen.p.getIntersection(ray, oom, rm);
//        V3D_Point p2 = ray.l.getQ(oom, rm);
//        ray.l.getP();
        
        V3D_Point px = screen.getRSP().p.getQR().l.getPointOfIntersection(p, oom, rm);
        Math_BigRational d = new Math_BigRationalSqrt(px.getDistanceSquared(p, oom, rm), oom, rm).getSqrt(oom, rm);
        
        //Math_BigRational d = ((V3D_LineSegment) screen.p.getQR().l.getLineOfIntersection(p, oom, rm)).getLength(oom, rm).getSqrt(oom, rm);
//        V3D_Point px = screen.p.getQR().l.getPointOfIntersection(p, oom, rm);
        //V3D_Point px = screen.p.getPQ().l.getPointOfIntersection(p, oom, rm);
//        Math_BigRational d = new Math_BigRationalSqrt(px.getDistanceSquared(
//                p, oom, rm), oom, rm).getSqrt(oom, rm);
//        Math_BigRational d2 = p.getDistanceSquared(screen.p.getQR().l, oom, rm);
//        Math_BigRational d = new Math_BigRationalSqrt(d2, oom, rm).getSqrt(oom, rm);
        return d.divide(pixelSize).intValue();
    }

    /**
     * Calculate and return the column index of the screen that ray passes
     * through.
     *
     * @param ray A ray that intersects with the screen.
     * @return The column index of the screen that l passes through.
     */
    protected int getScreenCol(V3D_Ray ray, int oom, RoundingMode rm) {
        V3D_Point p = (V3D_Point) screen.p.getIntersection(ray, oom, rm);
//        V3D_Point p2 = ray.l.getQ(oom, rm);
//        ray.l.getP();
        
        V3D_Point py = screen.getPQR().p.getPQ().l.getPointOfIntersection(p, oom, rm);
        Math_BigRational d = new Math_BigRationalSqrt(py.getDistanceSquared(p, oom, rm), oom, rm).getSqrt(oom, rm);
        //V3D_Point py = screen.p.getQR().l.getPointOfIntersection(p, oom, rm);
//        V3D_Point py = screen.p.getPQ().l.getPointOfIntersection(p, oom, rm);
//        Math_BigRational d = new Math_BigRationalSqrt(py.getDistanceSquared(
//                p, oom, rm), oom, rm).getSqrt(oom, rm);
//        Math_BigRational d2 = p.getDistanceSquared(screen.p.getPQ().l, oom, rm);
//        Math_BigRational d = new Math_BigRationalSqrt(d2, oom, rm).getSqrt(oom, rm);
        return d.divide(pixelSize).intValue();
    }
}
