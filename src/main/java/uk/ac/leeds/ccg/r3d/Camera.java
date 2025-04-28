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

import ch.obermuhlner.math.big.BigRational;
import java.awt.Color;
import java.awt.Dimension;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.grids.d2.Grids_2D_ID_int;
import uk.ac.leeds.ccg.math.arithmetic.Math_BigRational;
import uk.ac.leeds.ccg.r3d.entities.Area;
import uk.ac.leeds.ccg.r3d.entities.Line;
import uk.ac.leeds.ccg.r3d.entities.Point;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Geometry;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;
import uk.ac.leeds.ccg.v3d.geometry.V3D_AABB;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Area;
import uk.ac.leeds.ccg.v3d.geometry.V3D_FiniteGeometry;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Line;
import uk.ac.leeds.ccg.v3d.geometry.V3D_LineSegment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Plane;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Ray;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Frustum;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;

/**
 * Camera instances are situated in the 3D Universe. They are a frustum with
 * pixels arrayed in rows and columns on {@link #rect}. The resolution of the
 * screen is determined by the number of rows and columns.
 *
 * The closer {@link #focus} is to {@link #rect}, the bigger the field of view.
 * Typically, {@link #focus} is defined about as far from the screen as the
 * screen is wide or high. The camera does not have a lens.
 *
 * To pitch, yaw or roll the camera involves rotating both the screen and the
 * point about the centre of the screen. To pitch the camera up involves
 * rotating the screen and point clockwise in the direction of a ray from the
 * left to the right of the screen and that goes through the centre of the
 * screen. To roll the camera clockwise involves rotating the screen clockwise
 * in the direction of a vector from the point through the centre of the screen.
 * To yaw the camera clockwise involves rotating the screen and point clockwise
 * in the direction of a ray from the bottom to the top of the screen and that
 * goes through the centre of the screen.
 *
 * Rays from the point through the centre of each pixel are used to find the
 * closest geometries.
 *
 * @author Andy Turner
 */
public class Camera extends V3D_Frustum {

    private static final long serialVersionUID = 1L;

    /**
     * The number of rows of rect pixels.
     */
    public final int nrows;

    /**
     * The number of columns of rect pixels.
     */
    public final int ncols;

    /**
     * For storing rays from the camera focal point through each pixel centre.
     */
    public final HashMap<Grids_2D_ID_int, V3D_Ray> rays;

    /**
     * For storing the length/width of a pixel.
     */
    public BigRational pixelSize;

    /**
     * Create a new instance.
     *
     * @param focus The camera focus.
     * @param distance The distance from the focus to the centre of rect.
     * @param dim The nrows and ncolumns of pixels on rect.
     * @param width The width of rect.
     */
    public Camera(V3D_Environment env, V3D_Vector offset,
            V3D_Point focus, V3D_Vector direction, V3D_Vector horizontal,
            BigRational distance, BigRational pixelSize, Dimension dim,
            int oom, RoundingMode rm) {
        super(env, offset, focus, direction, horizontal, distance,
                BigRational.valueOf(dim.width).multiply(pixelSize),
                BigRational.valueOf(dim.height).multiply(pixelSize), oom, rm);
        this.pixelSize = pixelSize;
        this.nrows = dim.height;
        this.ncols = dim.width;
        this.rays = new HashMap<>();
    }

    /**
     * Create a new instance.
     *
     * @param focus The camera focus.
     * @param distance The distance from the focus to the centre of rect.
     * @param rect The frustum near rectangle.
     * @param dim The nrows and ncols of pixels on rect.
     */
    public Camera(V3D_Environment env, V3D_Vector offset,
            V3D_Point focus, V3D_Rectangle rect, Dimension dim,
            int oom, RoundingMode rm) {
        super(env, offset, focus, rect, oom, rm);
        this.pixelSize = rect.getPQR().getPQ(oom, rm).getLength(oom, rm).getSqrt(oom, rm)
                .divide(dim.height);
        this.nrows = dim.height;
        this.ncols = dim.width;
        this.rays = new HashMap<>();
    }

    /**
     * Renders the visible universe as an image map.
     *
     * Each pixel is set to the colour of the closest geometry intersecting the
     * ray from the {@link #focus} through the pixel centre.
     *
     * Calculating shadow and varying colour based on lighting adds significant
     * complication.
     *
     * @param universe
     * @param lighting The lighting vector.
     * @return An image map.
     * @throws Exception
     */
    int[] render(Universe universe, V3D_Vector lighting,
            BigRational ambientLight, boolean castShadow, boolean addGraticules,
            int oom, RoundingMode rm) throws Exception {
        int n = ncols * nrows;
        int[] pix = new int[n];
        HashMap<Grids_2D_ID_int, BigRational> mind2s = new HashMap<>();
        // Add axes
        if (addGraticules) {
            System.out.println("Add Graticules");
            BigRational xmin = universe.aabb.getXMin(oom);
            BigRational xmax = universe.aabb.getXMax(oom);
            BigRational ymin = universe.aabb.getYMin(oom);
            BigRational ymax = universe.aabb.getYMax(oom);
            BigRational zmin = universe.aabb.getZMin(oom);
            BigRational zmax = universe.aabb.getZMax(oom);
            BigRational scale = BigRational.TEN;
            //double scale = rect.getPQR().getPQ().getLength() / 10.d;
            V3D_Point origin = new V3D_Point(env, V3D_Vector.ZERO);
            V3D_Point p = new V3D_Point(rect.getP(oom, rm));
            BigRational scaleNeg = scale.negate();
            p.translate(new V3D_Vector(scaleNeg, scaleNeg, scaleNeg), oom, rm);
            V3D_Vector tr = new V3D_Vector(origin, p, oom, rm);
            tr = tr.add(new V3D_Vector(
                    (xmax.subtract(xmin)).divide(scale),
                    (ymax.subtract(ymin)).divide(scale),
                    (zmax.subtract(zmin)).divide(scale)), oom, rm);
            System.out.println("tr=" + tr.toString());
            // Render axes
            // x axis
            V3D_Point x_min = new V3D_Point(env, new V3D_Vector(xmin.divide(scale), BigRational.ZERO, BigRational.ZERO));
            V3D_Point x_max = new V3D_Point(env, new V3D_Vector(xmax.divide(scale), BigRational.ZERO, BigRational.ZERO));
            V3D_LineSegment x_axis = new V3D_LineSegment(x_min, x_max, oom, rm);
            x_axis.translate(tr, oom, rm);
            System.out.println("X graticule " + x_axis.toString());
            Line xAxis = new Line(x_axis, Color.BLUE);
            V3D_Point xpoi = xAxis.l.l.getPointOfIntersect(focus, oom, rm);
            V3D_Vector xpoin = new V3D_Vector(xpoi, focus, oom, rm);
            if (xpoin.isZero(oom, rm)) {
                renderPoint(oom, rm, mind2s, new Point(xAxis.l.l.getP(), xAxis.color), pix);
                renderPoint(oom, rm, mind2s, new Point(xAxis.l.l.getQ(oom, rm), xAxis.color), pix);
            } else {
                V3D_Plane xpoinpl = new V3D_Plane(xpoi, xpoin);
                renderLine(oom, rm, mind2s, xAxis, xpoinpl, pix);
            }
            // y axis                
            V3D_Point y_min = new V3D_Point(env, new V3D_Vector(BigRational.ZERO, ymin.divide(scale), BigRational.ZERO));
            V3D_Point y_max = new V3D_Point(env, new V3D_Vector(BigRational.ZERO, ymax.divide(scale), BigRational.ZERO));
            V3D_LineSegment y_axis = new V3D_LineSegment(y_min, y_max, oom, rm);
            y_axis.translate(tr, oom, rm);
            System.out.println("Y graticule " + y_axis.toString());
            Line yAxis = new Line(y_axis, Color.RED);
            V3D_Point ypoi = yAxis.l.l.getPointOfIntersect(focus, oom, rm);
            V3D_Vector ypoin = new V3D_Vector(ypoi, focus, oom, rm);
            if (ypoin.isZero(oom, rm)) {
                renderPoint(oom, rm, mind2s, new Point(yAxis.l.l.getP(), yAxis.color), pix);
                renderPoint(oom, rm, mind2s, new Point(yAxis.l.l.getQ(oom, rm), yAxis.color), pix);
            } else {
                V3D_Plane ypoinpl = new V3D_Plane(ypoi, ypoin);
                renderLine(oom, rm, mind2s, yAxis, ypoinpl, pix);
            }
            // z axis
            V3D_Point z_min = new V3D_Point(env, new V3D_Vector(BigRational.ZERO, BigRational.ZERO, zmin.divide(scale)));
            V3D_Point z_max = new V3D_Point(env, new V3D_Vector(BigRational.ZERO, BigRational.ZERO, zmax.divide(scale)));
            V3D_LineSegment z_axis = new V3D_LineSegment(z_min, z_max, oom, rm);
            z_axis.translate(tr, oom, rm);
            System.out.println("Z graticule " + z_axis.toString());
            Line zAxis = new Line(z_axis, Color.GREEN);
            V3D_Point zpoi = zAxis.l.l.getPointOfIntersect(focus, oom, rm);
            V3D_Vector zpoin = new V3D_Vector(zpoi, focus, oom, rm);
            if (zpoin.isZero(oom, rm)) {
                renderPoint(oom, rm, mind2s, new Point(zAxis.l.l.getP(), zAxis.color), pix);
                renderPoint(oom, rm, mind2s, new Point(zAxis.l.l.getQ(oom, rm), zAxis.color), pix);
            } else {
                V3D_Plane zpoinpl = new V3D_Plane(zpoi, zpoin);
                renderLine(oom, rm, mind2s, zAxis, zpoinpl, pix);
            }
        }
        // Render Lines
        int nlines = universe.lines.size();
        universe.lines.forEach(x
                -> {
//            if (x.l.l.v.isScalarMultiple(rect.getPQR().getPQV(oom, rm), oom, rm)) {
//                renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getQRV(oom, rm), oom, rm), pix);
//            } else {
//                renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getPQV(oom, rm), oom, rm), pix);
//            }    
//            if (x.l.l.v.isScalarMultiple(rect.getPQR().getPQV(oom, rm), oom, rm)) {
//                if (Math_BigRational.equals(x.l.l.v.getDotProduct(rect.getPQR().getPQV(oom, rm), oom, rm), BigRational.ZERO, oom)) {
//                    renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getPQV(oom, rm), oom, rm), pix);
//                } else {
                    if (Math_BigRational.equals(x.l.l.v.getDotProduct(rect.getPQR().getQRV(oom, rm), oom, rm), BigRational.ZERO, oom)) {
                        //renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getPQV(oom, rm), oom, rm), pix);
                        renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getQRV(oom, rm), oom, rm), pix);
                    } else {
         //               renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getQRV(oom, rm), oom, rm), pix);
                        renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getPQV(oom, rm), oom, rm), pix);
                    }
//                }            
//            } else {
//                if (Math_BigRational.equals(x.l.l.v.getDotProduct(rect.getPQR().getPQV(oom, rm), oom, rm), BigRational.ZERO, oom)) {
//                    renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getPQV(oom, rm), oom, rm), pix);
//                } else {
//                    if (Math_BigRational.equals(x.l.l.v.getDotProduct(rect.getPQR().getQRV(oom, rm), oom, rm), BigRational.ZERO, oom)) {
//                        renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getPQV(oom, rm), oom, rm), pix);
//                    } else {
//                        renderLine(oom, rm, mind2s, x, new V3D_Plane(x.l, rect.getPQR().getQRV(oom, rm), oom, rm), pix);
//                    }
//                }
//            }
        });
        // Render Areas
        int nAreas = universe.areas.size();
        V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
        if (nAreas > 0) {
            /**
             * Find the closest area intersects for a ray and colour the pixel
             * accordingly.
             */
            System.out.println("Calculate the minimum distance between each"
                    + " area and the camera focal point. Order the areas by"
                    + " the distance, and set the lighting.");
            TreeMap<BigRational, Set<Integer>> mindOrderedAreas = new TreeMap<>();
            /**
             * mind2st is the minimum distance of each each area to the camera
             * focus point for those parts of the area in view of the camera
             * i.e. through the frustum.
             */
            BigRational[] mind2st = new BigRational[nAreas];
            process(centroid, 0, universe.areas, lighting, ambientLight,
                    mindOrderedAreas, mind2st, oom, rm);
            int nAP = (nAreas / 100);
            if (nAP < 1) {
                nAP = 1;
            }
            for (int i = 1; i < nAreas; i++) {
                if (i % nAP == 0) {
                    System.out.println("Area " + i + " out of " + nAreas);
                }
                process(centroid, i, universe.areas, lighting, ambientLight,
                        mindOrderedAreas, mind2st, oom, rm);
            }
            System.out.println("Minimum distance squared between any area and"
                    + " the camera focal point = "
                    + mindOrderedAreas.firstKey().toString());
            System.out.println("Process each area working from the closest to "
                    + "the furthest.");
            // Process areas.
            /**
             * idPoint is used to store the point of intersection. This could be
             * used later for example to identify if that point on the area is
             * in a shadow...
             */
            HashMap<Grids_2D_ID_int, Integer> closestIndex = new HashMap<>();
            HashMap<Grids_2D_ID_int, V3D_Point> idPoint = new HashMap<>();
            for (BigRational mind2 : mindOrderedAreas.keySet()) {
                Set<Integer> triangleIndexes = mindOrderedAreas.get(mind2);
                for (var i : triangleIndexes) {
                    processArea(i, universe.areas.get(i).area, mind2st,
                            mind2s, closestIndex, idPoint, oom, rm);
                }
            }
            // Render pixels
            System.out.println("Render the closest area.");
            int pixelsToPop = closestIndex.size();
            int pixelsToPopPC = pixelsToPop / 100;
            int pixel = 0;
            for (var x : closestIndex.keySet()) {
                if (pixelsToPopPC > 0) {
                    if (pixel % pixelsToPopPC == 0) {
                        System.out.println("Rendering pixel " + pixel
                                + " out of " + pixelsToPop);
                    }
                }
                pixel++;
                int ci = closestIndex.get(x);
                Area a = universe.areas.get(ci);
                render(pix, x.getRow(), x.getCol(), a.lightingColor);
            }
        }
        return pix;
    }

    /**
     * For rendering a line on the image.Lines may be obscured by areas and each
     * other.
     *
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @param l The line to render.
     * @param mind2s
     * @param plane A plane on which l.l is located.
     * @param pix The image.
     */
    public void renderLine(int oom, RoundingMode rm,
            HashMap<Grids_2D_ID_int, BigRational> mind2s, Line l,
            V3D_Plane plane, int[] pix) {
        if (V3D_Line.isCollinear(oom, rm, l.l.l, this.focus)) {
            // If the line segment is collinear with this, then render as a point.
            // Not sure which end is closer, so render both.
            renderPoint(oom, rm, mind2s, new Point(l.l.l.getP(), l.color), pix);
            renderPoint(oom, rm, mind2s, new Point(l.l.l.getQ(oom, rm), l.color), pix);
        } else {
            V3D_Triangle t = new V3D_Triangle(l.l, this.focus, oom, rm);
            V3D_LineSegment rp = t.getRP(oom, rm);
            V3D_LineSegment qr = t.getQR(oom, rm);
            V3D_Plane rectpl = rect.getPl(oom, rm);
            V3D_Point rpi = rectpl.getIntersectNonParallel(rp, oom, rm);
            //V3D_FiniteGeometry ti = rect.getIntersect(t, oom, rm);
            if (rpi != null) {
                // Calculate the row and column bounds.
                int rrpi = getScreenRow(rpi, oom, rm);
                int crpi = getScreenCol(rpi, oom, rm);
                V3D_Point qri = rectpl.getIntersectNonParallel(qr, oom, rm);
                if (qri != null) {
                    int rqri = getScreenRow(qri, oom, rm);
                    int cqri = getScreenCol(qri, oom, rm);
                    int minri;
                    int minci;
                    int maxri;
                    int maxci;
                    if (rrpi < rqri) {
                        minri = rrpi;
                        maxri = rqri;
                    } else {
                        minri = rqri;
                        maxri = rrpi;
                    }
                    if (crpi < cqri) {
                        minci = crpi;
                        maxci = cqri;
                    } else {
                        minci = cqri;
                        maxci = crpi;
                    }
                    /**
                     * As the calculation of row and column is imprecise, add a
                     * row a column to ensure rendering.
                     */
                    minri -= 1;
                    maxri += 1;
                    minci -= 1;
                    maxci += 1;
                    // Clip to bounds
                    if (minri < 0) {
                        minri = 0;
                    }
                    if (minci < 0) {
                        minci = 0;
                    }
                    if (maxri >= nrows) {
                        maxri = nrows - 1;
                    }
                    if (maxci >= ncols) {
                        maxci = ncols - 1;
                    }
                    V3D_Point sr = getPoint(rrpi, crpi, oom, rm);
                    V3D_Point sq = getPoint(rqri, cqri, oom, rm);
                    if (sr.equals(sq, oom, rm)) {
                        renderPoint(oom, rm, mind2s, new Point(rpi, l.color), pix);
                    } else {
                        // Calculate/store the screen projected line segment.
                        V3D_Line sl = new V3D_Line(sr, sq, oom, rm);
                        BigRational lw = pixelSize.multiply(2);
                        for (int row = minri; row < maxri + 1; row++) {
                            for (int col = minci; col < maxci + 1; col++) {
                                // Calculate the pixel distance from the screen projected line segment.
                                V3D_Point sp = getPoint(row, col, oom, rm);
                                BigRational d = sl.getDistance(sp, oom, rm);
                                if (d.compareTo(lw) == -1) {
                                    V3D_Rectangle pixel = getPixel(row, col, oom, rm);
                                    if (pixel.intersects0(t, oom, rm)) {
                                        Grids_2D_ID_int id = new Grids_2D_ID_int(row, col);
                                        V3D_Ray ray = getRay(id, oom, rm);
                                        //V3D_Point ri = pixel.getIntersectNonCoplanar(ray, oom, rm);
                                        V3D_Point ri = ray.getIntersectNonCoplanar(plane, oom, rm);
                                        if (ri != null) {
                                            BigRational d2;
                                            d2 = ri.getDistanceSquared(this.focus, oom, rm);
                                            BigRational d2p = mind2s.get(id);
                                            if (d2p == null) {
                                                mind2s.put(id, d2); // So closest things are at the front.
                                                render(pix, row, col, l.color);
                                            } else {
                                                //if (d2 <= d2p + epsilon) {
                                                if (d2.compareTo(d2p) == -1) {
                                                //if (d2.compareTo(d2p) != 1) {
                                                    mind2s.put(id, d2); // So closest things are at the front.
                                                    render(pix, row, col, l.color);
                                                } else {
                                                    // There is a closer line already rendered for the pixel.
                                                    int debug = 1;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * For rendering a point on the image.
     *
     * @param oom
     * @param rm The tolerance for intersection.
     * @param p The point to render.
     * @param mind2s
     * @param pix The image.
     */
    public void renderPoint(int oom, RoundingMode rm,
            HashMap<Grids_2D_ID_int, BigRational> mind2s, Point p, int[] pix) {
        if (!focus.equals(p.p, oom, rm)) {
            V3D_Ray ray = new V3D_Ray(focus, p.p, oom, rm);
            V3D_Point pt = (V3D_Point) rect.getIntersect(ray, oom, rm);
            if (pt != null) {
                int r = getScreenRow(pt, oom, rm);
                int c = getScreenCol(pt, oom, rm);
                Grids_2D_ID_int id = new Grids_2D_ID_int(r, c);
                BigRational d2 = p.p.getDistanceSquared(focus, oom, rm);
                BigRational d2p = mind2s.get(id);
                if (d2p == null) {
                    mind2s.put(id, d2);
                    render(pix, r, c, p.color);
                } else {
                    //if (d2 <= d2p + epsilon) {
                    if (d2.compareTo(d2p) == -1) {
                        mind2s.put(id, d2);
                        render(pix, r, c, p.color);
                    }
                }
            }
        }
    }

    private void render(int[] pix, int r, int c, Color color) {
        int row = nrows - r - 1;
        int in = (row * ncols) + c;
        if (!(in < 0 || in >= pix.length)) {
            pix[in] = color.getRGB();
        }
    }

    /**
     * Calculate the minimum distance squared from this area to the camera point
     * for those parts visible through the camera frustum. These are stored in
     * mind2t. The mindOrderedTriangles map is updated so that the indexes of
     * the triangles are stored against the minimum distances. Additionally,
     * this sets the colour of the triangle based on the lighting vector and
     * ambientLight.
     *
     * @param centroid
     * @param index
     * @param areas
     * @param lighting
     * @param ambientLight
     * @param mindOrderedTriangles
     * @param mind2t
     * @param oom, rm
     */
    private void process(V3D_Point centroid, int index,
            ArrayList<Area> areas, V3D_Vector lighting, BigRational ambientLight,
            TreeMap<BigRational, Set<Integer>> mindOrderedTriangles,
            BigRational[] mind2t, int oom, RoundingMode rm) {
        Area area = areas.get(index);
        area.setLighting(centroid, lighting, ambientLight, oom, rm);
        V3D_Area a = area.area;
        /**
         * Algorithm:
         *
         * 1. Get the intersection between the tetrahedron made from the
         * triangle and this and the camera screen.
         *
         * 2. Get the points of the intersection and create rays that start from
         * this and go through these points.
         *
         * 3. Find the points of intersection of the rays and the plane of the
         * triangle (use the plane just in case rounding results in an
         * intersection not being found).
         *
         * 4. Calculate the distance from this to the points of intersection and
         * store the minimum of these distances
         */
        V3D_Point[] pts = a.getPointsArray(oom, rm);
        V3D_Plane tpl = a.getPl(oom, rm);
        BigRational mind2 = BigRational.valueOf(Double.MAX_VALUE);
        for (var pt : pts) {
            if (!pt.equals(focus, oom, rm)) {
                V3D_Ray ray = new V3D_Ray(focus, pt, oom, rm);
                if (rect.intersects(ray, oom, rm)) {
                    BigRational d2 = pt.getDistanceSquared(focus, oom, rm);
                    mind2 = BigRational.min(d2, mind2);
                }
            }
        }
        mind2t[index] = mind2;
        Generic_Collections.addToMap(mindOrderedTriangles, mind2t[index], index);
    }

    /**
     * Get the CellIDs of those cells that intersect with area.
     *
     * @param tIndex The triangle index.
     * @param a The area.
     * @param mind2a The minimum distance squared for each area and the
     * camera point.
     * @param mind2s The minimum distances squared of geometries through each
     * pixel.
     * @param closestIndex The indexes of the closest geometries through each
     * pixel.
     * @param idPoint The point of intersection on the closest triangle through
     * each pixel.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    protected void processArea(int tIndex, V3D_Area a,
            BigRational[] mind2a, 
            HashMap<Grids_2D_ID_int, BigRational> mind2s,
            HashMap<Grids_2D_ID_int, Integer> closestIndex,
            HashMap<Grids_2D_ID_int, V3D_Point> idPoint,
            int oom, RoundingMode rm) {
        /**
         * Loop over the extent, and where necessary, calculate the intersection
         * to determine if the triangle is in the pixel and if the distance is a
         * minimum distance.
         */
        for (int row = 0; row < nrows; row++) {
            for (int col = 0; col < ncols; col++) {
                Grids_2D_ID_int id = new Grids_2D_ID_int(row, col);
                BigRational mind2 = mind2s.get(id);
                if (mind2 == null) {
                    try {
                        V3D_Ray ray = getRay(id, oom, rm);
                        V3D_Point ti = a.getIntersectNonCoplanar(ray, oom, rm);
                        if (ti != null) {
                            BigRational d2 = ti.getDistanceSquared(focus, oom, rm);
                            mind2s.put(id, d2);
                            closestIndex.put(id, tIndex);
                            idPoint.put(id, ti);
                        }
                    } catch (RuntimeException ex) {
                        System.out.println("Resolution too coarse to render "
                                + "triangle: " + a.toString());
                    }
                } else {
                    //if (mind2t[tIndex] < mind2) {
                    if (mind2a[tIndex].compareTo(mind2) == -1) {
                        try {
                            V3D_Ray ray = getRay(id, oom, rm);
                            V3D_Point ti = a.getIntersectNonCoplanar(ray, oom, rm);
                            if (ti != null) {
                                // Only render triangles that intersect the ray at a point and that are beyond the camera rect.
                                BigRational d2 = ti.getDistanceSquared(focus, oom, rm);
                                //if (d2 < mind2) {
                                if (d2.compareTo(mind2) == -1) {
                                    mind2s.put(id, d2);
                                    closestIndex.put(id, tIndex);
                                    idPoint.put(id, ti);
                                }
                            }
                        } catch (RuntimeException ex) {
                            System.out.println("Resolution too coarse to render "
                                    + "triangle: " + a.toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * For getting a ray from the camera focal point through the centre of the
     * screen pixel with ID id.
     *
     * @param id The ID of the screen pixel.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The ray from the camera focal point through the centre of the
     * screen pixel.
     */
    protected V3D_Ray getRay(Grids_2D_ID_int id, int oom, RoundingMode rm) {
        V3D_Ray r = rays.get(id);
        if (r == null) {
            V3D_Vector rv = verticalUV.multiply(id.getRow(), oom, rm);
            V3D_Vector cv = horizontalUV.multiply(id.getCol(), oom, rm);
            V3D_Point rcpt = new V3D_Point(rect.getP(oom, rm));
            rcpt.translate(rv.add(cv, oom, rm), oom, rm);
            r = new V3D_Ray(focus, rcpt, oom, rm);
            rays.put(id, r);
        }
        return r;
    }

    /**
     * Get the pixel that the ray intersects.
     *
     * @param p A point in the universe.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The ID of the rect cell that intersects a ray from this to focus,
     * or {@code null}.
     */
    protected Grids_2D_ID_int getRC(V3D_Point p, int oom, RoundingMode rm) {
        if (focus.equals(p, oom, rm)) {
            return new Grids_2D_ID_int(
                    getScreenRow(p, oom, rm),
                    getScreenCol(p, oom, rm));
        }
        V3D_Ray ray = new V3D_Ray(focus, p, oom, rm);
        //V3D_Point px = (V3D_Point) rect.getIntersect(ray, oom, rm);
        V3D_Point px = (V3D_Point) ray.getIntersect(rect.getPl(oom, rm), oom, rm);
        if (px == null) {
            return null;
        } else {
            return new Grids_2D_ID_int(
                    getScreenRow(px, oom, rm),
                    getScreenCol(px, oom, rm));
        }
    }

    /**
     * Calculate and return the row index of {@code p}.
     *
     * @param p A point on the screen.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The row index of {@code p}.
     */
    protected int getScreenRow(V3D_Point p, int oom, RoundingMode rm) {
        BigRational d = rect.getPQR().getQR(oom, rm).getDistance(p, oom, rm);
        return nrows - (d.divide(pixelSize)).intValue();
    }

    /**
     * Calculate and return the column index of {@code p}.
     *
     * @param p A point on the screen.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The column index of {@code p}.
     */
    protected int getScreenCol(V3D_Point p, int oom, RoundingMode rm) {
        BigRational d = rect.getPQR().getPQ(oom, rm).getDistance(p, oom, rm);
        return (d.divide(pixelSize)).intValue();
    }

    /**
     * Get the centroid of the pixel indexed by {@code row} and {@code col}.
     *
     * @param row The row index for the pixel returned.
     * @param col The column index for the pixel returned.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The centroid point of the pixel indexed by {@code row} and
     * {@code col}.
     */
    protected V3D_Point getPoint(int row, int col, int oom, RoundingMode rm) {
        V3D_Point p = rect.getP(oom, rm);
        V3D_Point pP = new V3D_Point(p);
        pP.translate(
                verticalUV.multiply(
                        BigRational.valueOf(row + 0.5d), oom, rm).add(
                        horizontalUV.multiply(
                                BigRational.valueOf(col + 0.5d),
                                oom, rm), oom, rm), oom, rm);
        return pP;
    }

    /**
     * Get the pixel rectangle for the pixel indexed by {@code row} and
     * {@code col}.
     *
     * @param row The row index for the pixel returned.
     * @param col The column index for the pixel returned.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The pixel rectangle indexed by {@code row} and {@code col}.
     */
    public V3D_Rectangle getPixel(int row, int col, int oom, RoundingMode rm) {
        // Get bottom left point (row=0, col=0).
        V3D_Point p = rect.getP(oom, rm);
        V3D_Point pP = new V3D_Point(p);
        pP.translate(
                verticalUV.multiply(row, oom, rm).add(
                        horizontalUV.multiply(col, oom, rm), oom, rm), oom, rm);
        // q
        V3D_Point pQ = new V3D_Point(p);
        pQ.translate(
                verticalUV.multiply(row + 1, oom, rm).add(
                        horizontalUV.multiply(col, oom, rm), oom, rm), oom, rm);
        // r
        V3D_Point pR = new V3D_Point(p);
        pR.translate(
                verticalUV.multiply(row + 1, oom, rm).add(
                        horizontalUV.multiply(col + 1, oom, rm), oom, rm), oom, rm);
        // s
        V3D_Point pS = new V3D_Point(p);
        pS.translate(
                verticalUV.multiply(row, oom, rm).add(
                        horizontalUV.multiply(col + 1, oom, rm), oom, rm), oom, rm);
        return new V3D_Rectangle(pP, pQ, pR, pS, oom, rm);
    }
}
