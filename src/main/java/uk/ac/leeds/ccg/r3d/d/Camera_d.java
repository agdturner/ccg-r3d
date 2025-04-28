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
package uk.ac.leeds.ccg.r3d.d;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.grids.d2.Grids_2D_ID_int;
import uk.ac.leeds.ccg.math.arithmetic.Math_Double;
import uk.ac.leeds.ccg.r3d.d.entities.Area_d;
import uk.ac.leeds.ccg.r3d.d.entities.Line_d;
import uk.ac.leeds.ccg.r3d.d.entities.Point_d;
import uk.ac.leeds.ccg.r3d.d.entities.Triangle_d;
import uk.ac.leeds.ccg.v3d.core.d.V3D_Environment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Geometry_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Point_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Rectangle_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Triangle_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Vector_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_AABB_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Area_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_FiniteGeometry_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Line_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineSegment_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Plane_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Ray_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Frustum_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Tetrahedron_d;

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
public class Camera_d extends V3D_Frustum_d {

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
    public final HashMap<Grids_2D_ID_int, V3D_Ray_d> rays;

    /**
     * For storing the length/width of a pixel.
     */
    public double pixelSize;

    /**
     * Create a new instance.
     *
     * @param focus The camera focus.
     * @param distance The distance from the focus to the centre of rect.
     * @param dim The nrows and ncolumns of pixels on rect.
     * @param width The width of rect.
     */
    public Camera_d(V3D_Environment_d env, V3D_Vector_d offset,
            V3D_Point_d focus, V3D_Vector_d direction, V3D_Vector_d horizontal,
            double distance, double pixelSize, Dimension dim) {
        super(env, offset, focus, direction, horizontal, distance,
                dim.width * pixelSize, dim.height * pixelSize);
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
     * @param dim The nrows and ncolumns of pixels on rect.
     */
    public Camera_d(V3D_Environment_d env, V3D_Vector_d offset,
            V3D_Point_d focus, V3D_Rectangle_d rect, Dimension dim) {
        super(env, offset, focus, rect);
        this.pixelSize = rect.getPQR().getPQ().getLength() / (double) dim.height;
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
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     * @return An image map.
     * @throws Exception
     */
    int[] render(Universe_d universe, V3D_Vector_d lighting,
            double ambientLight, boolean castShadow, boolean addGraticules,
            double epsilon)
            throws Exception {
        int n = ncols * nrows;
        int[] pix = new int[n];
        HashMap<Grids_2D_ID_int, Double> mind2s = new HashMap<>();
        // Add axes
        if (addGraticules) {
            System.out.println("Add Graticules");
            double xmin = universe.aabb.getXMin();
            double xmax = universe.aabb.getXMax();
            double ymin = universe.aabb.getYMin();
            double ymax = universe.aabb.getYMax();
            double zmin = universe.aabb.getZMin();
            double zmax = universe.aabb.getZMax();
            double scale = 10d;
            //double scale = rect.getPQR().getPQ().getLength() / 10.d;
            V3D_Point_d origin = new V3D_Point_d(env, V3D_Vector_d.ZERO);
            V3D_Point_d p = new V3D_Point_d(rect.getP());
            p.translate(new V3D_Vector_d(-scale, -scale, -scale));
            V3D_Vector_d tr = new V3D_Vector_d(origin, p);
            tr = tr.add(new V3D_Vector_d(
                    (xmax - xmin) / scale,
                    (ymax - ymin) / scale,
                    (zmax - zmin) / scale));
            System.out.println("tr=" + tr.toString());
            // Render axes
            // x axis
            V3D_Point_d x_min = new V3D_Point_d(env, new V3D_Vector_d(xmin / scale, 0d, 0d));
            V3D_Point_d x_max = new V3D_Point_d(env, new V3D_Vector_d(xmax / scale, 0d, 0d));
            V3D_LineSegment_d x_axis = new V3D_LineSegment_d(x_min, x_max);
            x_axis.translate(tr);
            System.out.println("X graticule " + x_axis.toString());
            Line_d xAxis = new Line_d(x_axis, Color.BLUE);
            V3D_Point_d xpoi = xAxis.l.l.getPointOfIntersect(focus, epsilon);
            V3D_Vector_d xpoin = new V3D_Vector_d(xpoi, focus);
            if (xpoin.isZero(epsilon)) {
                renderPoint(epsilon, mind2s, new Point_d(xAxis.l.l.getP(), xAxis.color), pix);
                renderPoint(epsilon, mind2s, new Point_d(xAxis.l.l.getQ(), xAxis.color), pix);
            } else {
                V3D_Plane_d xpoinpl = new V3D_Plane_d(xpoi, xpoin);
                renderLine(epsilon, mind2s, xAxis, xpoinpl, pix);
            }
            // y axis                
            V3D_Point_d y_min = new V3D_Point_d(env, new V3D_Vector_d(0d, ymin / scale, 0d));
            V3D_Point_d y_max = new V3D_Point_d(env, new V3D_Vector_d(0d, ymax / scale, 0d));
            V3D_LineSegment_d y_axis = new V3D_LineSegment_d(y_min, y_max);
            y_axis.translate(tr);
            System.out.println("Y graticule " + y_axis.toString());
            Line_d yAxis = new Line_d(y_axis, Color.RED);
            V3D_Point_d ypoi = yAxis.l.l.getPointOfIntersect(focus, epsilon);
            V3D_Vector_d ypoin = new V3D_Vector_d(ypoi, focus);
            if (ypoin.isZero(epsilon)) {
                renderPoint(epsilon, mind2s, new Point_d(yAxis.l.l.getP(), yAxis.color), pix);
                renderPoint(epsilon, mind2s, new Point_d(yAxis.l.l.getQ(), yAxis.color), pix);
            } else {
                V3D_Plane_d ypoinpl = new V3D_Plane_d(ypoi, ypoin);
                renderLine(epsilon, mind2s, yAxis, ypoinpl, pix);
            }
            // z axis
            V3D_Point_d z_min = new V3D_Point_d(env, new V3D_Vector_d(0d, 0d, zmin / scale));
            V3D_Point_d z_max = new V3D_Point_d(env, new V3D_Vector_d(0d, 0d, zmax / scale));
            V3D_LineSegment_d z_axis = new V3D_LineSegment_d(z_min, z_max);
            z_axis.translate(tr);
            System.out.println("Z graticule " + z_axis.toString());
            Line_d zAxis = new Line_d(z_axis, Color.GREEN);
            V3D_Point_d zpoi = zAxis.l.l.getPointOfIntersect(focus, epsilon);
            V3D_Vector_d zpoin = new V3D_Vector_d(zpoi, focus);
            if (zpoin.isZero(epsilon)) {
                renderPoint(epsilon, mind2s, new Point_d(zAxis.l.l.getP(), zAxis.color), pix);
                renderPoint(epsilon, mind2s, new Point_d(zAxis.l.l.getQ(), zAxis.color), pix);
            } else {
                V3D_Plane_d zpoinpl = new V3D_Plane_d(zpoi, zpoin);
                renderLine(epsilon, mind2s, zAxis, zpoinpl, pix);
            }
        }
        // Render Lines
        int nlines = universe.lines.size();
        universe.lines.forEach(x
                -> {
//            if (x.l.l.v.isScalarMultiple(rect.getPQR().getPQV(), epsilon)) {
//                renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getQRV()), pix);
//            } else {
//                renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getPQV()), pix);
//            }
            //if (x.l.l.v.isScalarMultiple(rect.getPQR().getPQV(), epsilon)) {
            //    if (Math_Double.equals(x.l.l.v.getDotProduct(rect.getPQR().getPQV()), 0d, epsilon)) {
                    if (Math_Double.equals(x.l.l.v.getDotProduct(rect.getPQR().getQRV()), 0d, epsilon)) {
                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getQRV()), pix);
                    } else {
                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getPQV()), pix);
                    }
//                } else {
//                    if (Math_Double.equals(x.l.l.v.getDotProduct(rect.getPQR().getQRV()), 0d, epsilon)) {
//                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getQRV()), pix);
//                    } else {
//                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getPQV()), pix);
//                    }
//                }
//            } else {
//                if (Math_Double.equals(x.l.l.v.getDotProduct(rect.getPQR().getPQV()), 0d, epsilon)) {
//                    if (Math_Double.equals(x.l.l.v.getDotProduct(rect.getPQR().getQRV()), 0d, epsilon)) {
//                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getQRV()), pix);
//                    } else {
//                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getPQV()), pix);
//                    }
//                } else {
//                    if (Math_Double.equals(x.l.l.v.getDotProduct(rect.getPQR().getQRV()), 0d, epsilon)) {
//                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getQRV()), pix);
//                    } else {
//                        renderLine(epsilon, mind2s, x, new V3D_Plane_d(x.l, rect.getPQR().getPQV()), pix);
//                    }
//                }
//            }
        });
        // Render Areas
        int nAreas = universe.areas.size();
        V3D_Point_d centroid = universe.aabb.getCentroid();
        if (nAreas > 0) {
            /**
             * Find the closest area intersects for a ray and colour the pixel
             * accordingly.
             */
            System.out.println("Calculate the minimum distance between each"
                    + " area and the camera focal point. Order the areas by"
                    + " the distance, and set the lighting.");
            TreeMap<Double, Set<Integer>> mindOrderedAreas = new TreeMap<>();
            /**
             * mind2st is the minimum distance of each each area to the camera
             * point for those parts of the area in view of the camera i.e.
             * through the frustum.
             */
            double[] mind2st = new double[nAreas];
            process(centroid, 0, universe.areas, lighting, ambientLight,
                    mindOrderedAreas, mind2st, epsilon);
            int nAP = (nAreas / 100);
            if (nAP < 1) {
                nAP = 1;
            }
            for (int i = 0; i < nAreas; i++) {
                if (i % nAP == 0) {
                    System.out.println("Area " + i + " out of " + nAreas);
                }
                process(centroid, i, universe.areas, lighting, ambientLight,
                        mindOrderedAreas, mind2st, epsilon);
            }
            System.out.println("Minimum distance squared between any area and"
                    + " the camera focal point = "
                    + mindOrderedAreas.firstKey().toString());
            System.out.println("Process each area working from the closest to "
                    + "the furthest.");
            // Process areas.
            /**
             * idPoint is used to store the point of intersection for the
             * triangle closest to the camera. This is later used to see if that
             * point on the area is in shadow
             */
            HashMap<Grids_2D_ID_int, Integer> closestIndex = new HashMap<>();
            HashMap<Grids_2D_ID_int, V3D_Point_d> idPoint = new HashMap<>();
            for (double mind2 : mindOrderedAreas.keySet()) {
                Set<Integer> triangleIndexes = mindOrderedAreas.get(mind2);
                for (var i : triangleIndexes) {
                    processArea(i, universe.areas.get(i).area, mind2st,
                            mind2s, closestIndex, idPoint, epsilon);
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
                Area_d a = universe.areas.get(ci);
                render(pix, x.getRow(), x.getCol(), a.lightingColor);
            }
        }
        return pix;
    }

    /**
     * For rendering a line on the image. Lines may be obscured by triangles and
     * each other. This will render the closest one.
     *
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     * @param l The line to render.
     * @param plane A plane on which l.l is located.
     * @param pix The image.
     */
    public void renderLine(double epsilon,
            HashMap<Grids_2D_ID_int, Double> mind2s, Line_d l,
            V3D_Plane_d plane, int[] pix) {
        if (V3D_Line_d.isCollinear(epsilon, l.l.l, this.focus)) {
            // If the line segment is collinear with this, then render as a point.
            // Not sure which end is closer, so render both.
            renderPoint(epsilon, mind2s, new Point_d(l.l.l.getP(), l.color), pix);
            renderPoint(epsilon, mind2s, new Point_d(l.l.l.getQ(), l.color), pix);
        } else {
            V3D_Triangle_d t = new V3D_Triangle_d(l.l, this.focus);
            V3D_LineSegment_d rp = t.getRP();
            V3D_LineSegment_d qr = t.getQR();
            V3D_Plane_d rectpl = rect.getPl();
            V3D_Point_d rpi = rectpl.getIntersectNonParallel(rp, epsilon);
            //V3D_FiniteGeometry_d ti = rect.getIntersectNonCoplanar(t, epsilon);
            if (rpi != null) {
                // Calculate the row and column bounds.
                int rrpi = getScreenRow(rpi, epsilon);
                int crpi = getScreenCol(rpi, epsilon);
                V3D_Point_d qri = rectpl.getIntersectNonParallel(qr, epsilon);
                if (qri != null) {
                    int rqri = getScreenRow(qri, epsilon);
                    int cqri = getScreenCol(qri, epsilon);
                    int minri;
                    int minci;
                    int maxri;
                    int maxci;
                    boolean pbq;
                    if (rrpi < rqri) {
                        pbq = true;
                        minri = rrpi;
                        maxri = rqri;
                    } else {
                        pbq = false;
                        minri = rqri;
                        maxri = rrpi;
                    }
                    boolean plq;
                    if (crpi < cqri) {
                        plq = true;
                        minci = crpi;
                        maxci = cqri;
                    } else {
                        plq = false;
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
                    V3D_Point_d sr = getPoint(rrpi, crpi, epsilon);
                    V3D_Point_d sq = getPoint(rqri, cqri, epsilon);
                    if (sr.equals(sq, epsilon)) {
                        renderPoint(epsilon, mind2s, new Point_d(rpi, l.color), pix);
                    } else {
                        // Calculate/store the screen projected line segment.
                        V3D_Line_d sl = new V3D_Line_d(sr, sq);
                        double lw = pixelSize * 2d;
                        for (int row = minri; row < maxri + 1; row++) {
                            for (int col = minci; col < maxci + 1; col++) {
                                // Calculate the pixel distance from the screen projected line segment.
                                V3D_Point_d sp = getPoint(row, col, epsilon);
                                double d = sl.getDistance(sp, epsilon);
                                if (d < lw) {
                                    V3D_Rectangle_d pixel = getPixel(row, col);
                                    if (pixel.intersects0(t, epsilon)) {
                                        Grids_2D_ID_int id = new Grids_2D_ID_int(row, col);
                                        V3D_Ray_d ray = getRay(id);
                                        V3D_Point_d ri = ray.getIntersect0(plane, epsilon);
                                        if (ri != null) {
                                            double d2;
                                            d2 = ri.getDistanceSquared(this.focus);
                                            Double d2p = mind2s.get(id);
                                            if (d2p == null) {
                                                mind2s.put(id, d2); // So closest things are at the front.
                                                render(pix, row, col, l.color);
                                            } else {
                                                if (d2 <= d2p + epsilon) {
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
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     * @param p The point to render.
     * @param pix The image.
     * @return pix
     */
    public void renderPoint(double epsilon,
            HashMap<Grids_2D_ID_int, Double> mind2s, Point_d p, int[] pix) {
        if (!focus.equals(p.p, epsilon)) {
            V3D_Ray_d ray = new V3D_Ray_d(focus, p.p);
            V3D_Point_d pt = (V3D_Point_d) rect.getIntersect(ray, epsilon);
            if (pt != null) {
                int r = getScreenRow(pt, epsilon);
                int c = getScreenCol(pt, epsilon);
                Grids_2D_ID_int id = new Grids_2D_ID_int(r, c);
                double d2 = p.p.getDistanceSquared(focus);
                Double d2p = mind2s.get(id);
                if (d2p == null) {
                    mind2s.put(id, d2);
                    render(pix, r, c, p.color);
                } else {
                    if (d2 <= d2p + epsilon) {
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
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     */
    private void process(V3D_Point_d centroid, int index,
            ArrayList<Area_d> areas, V3D_Vector_d lighting, double ambientLight,
            TreeMap<Double, Set<Integer>> mindOrderedTriangles,
            double[] mind2t, double epsilon) {
        Area_d area = areas.get(index);
        area.setLighting(centroid, lighting, ambientLight, epsilon);
        V3D_Area_d a = area.area;
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
        V3D_Point_d[] pts = a.getPointsArray();
        V3D_Plane_d tpl = a.pl;
        double mind2 = Double.MAX_VALUE;
        for (var pt : pts) {
            if (!pt.equals(focus, epsilon)) {
                V3D_Ray_d ray = new V3D_Ray_d(focus, pt);
                if (rect.intersects(ray, epsilon)) {
                    double d2 = pt.getDistanceSquared(focus);
                    mind2 = Math.min(d2, mind2);
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
     * @param mind2t The minimum distance squared for each triangle and the
     * camera point.
     * @param mind2s The minimum distances squared of all triangles through each
     * pixel.
     * @param closestIndex The indexes of the closest triangles through each
     * pixel.
     * @param idPoint The point of intersection on the closest triangle through
     * each pixel.
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     */
    protected void processArea(int tIndex, V3D_Area_d a,
            double[] mind2t, HashMap<Grids_2D_ID_int, Double> mind2s,
            HashMap<Grids_2D_ID_int, Integer> closestIndex,
            HashMap<Grids_2D_ID_int, V3D_Point_d> idPoint,
            double epsilon) {
        /**
         * Loop over the extent, and where necessary, calculate the intersection
         * to determine if the area is in the pixel and if the distance is a
         * minimum distance. The bounding box or convex hull of an area can be
         * used to find the min and max row and column rather than looking
         * everywhere.
         */
        for (int row = 0; row < nrows; row++) {
            for (int col = 0; col < ncols; col++) {
                Grids_2D_ID_int id = new Grids_2D_ID_int(row, col);
                Double mind2 = mind2s.get(id);
                if (mind2 == null) {
                    try {
                        V3D_Ray_d ray = getRay(id);
                        V3D_Point_d ti = a.getIntersectNonCoplanar(ray, epsilon);
                        if (ti != null) {
                            double d2 = ti.getDistanceSquared(focus);
                            mind2s.put(id, d2);
                            closestIndex.put(id, tIndex);
                            idPoint.put(id, ti);
                        }
                    } catch (RuntimeException ex) {
                        System.out.println("Resolution too coarse to render "
                                + "triangle: " + a.toString());
                        
                        V3D_Ray_d ray = getRay(id);
                        V3D_Point_d ti = a.getIntersectNonCoplanar(ray, epsilon);
                        if (ti != null) {
                            double d2 = ti.getDistanceSquared(focus);
                            mind2s.put(id, d2);
                            closestIndex.put(id, tIndex);
                            idPoint.put(id, ti);
                        }
                    }
                } else {
                    if (mind2t[tIndex] < mind2) {
                        try {
                            V3D_Ray_d ray = getRay(id);
                            V3D_Point_d ti = a.getIntersectNonCoplanar(ray, epsilon);
                            if (ti != null) {
                                double d2 = ti.getDistanceSquared(focus);
                                if (d2 < mind2) {
                                    mind2s.put(id, d2);
                                    closestIndex.put(id, tIndex);
                                    idPoint.put(id, ti);
                                }
                            }
                        } catch (RuntimeException ex) {
                            System.out.println("Resolution too coarse to render "
                                    + "triangle: " + a.toString());
                            
                            V3D_Ray_d ray = getRay(id);
                            V3D_Point_d ti = a.getIntersectNonCoplanar(ray, epsilon);
                            if (ti != null) {
                                // Only render areas that intersect the ray at a point and that are beyond the camera rect.
                                double d2 = ti.getDistanceSquared(focus);
                                if (d2 < mind2) {
                                    mind2s.put(id, d2);
                                    closestIndex.put(id, tIndex);
                                    idPoint.put(id, ti);
                                }
                            }
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
     * @return The ray from the camera focal point through the centre of the
     * screen pixel.
     */
    protected V3D_Ray_d getRay(Grids_2D_ID_int id) {
        V3D_Ray_d r = rays.get(id);
        if (r == null) {
            V3D_Vector_d rv = verticalUV.multiply(id.getRow());
            V3D_Vector_d cv = horizontalUV.multiply(id.getCol());
            V3D_Point_d rcpt = new V3D_Point_d(rect.getP());
            rcpt.translate(rv.add(cv));
            r = new V3D_Ray_d(focus, rcpt);
            rays.put(id, r);
        }
        return r;
    }

    /**
     * Get the pixel that the ray intersects.
     *
     * @param p A point in the universe.
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     * @return The ID of the rect cell that intersects a ray from this to focus,
     * or {@code null}.
     */
    protected Grids_2D_ID_int getRC(V3D_Point_d p, double epsilon) {
        if (focus.equals(p, epsilon)) {
            return new Grids_2D_ID_int(
                    getScreenRow(p, epsilon),
                    getScreenCol(p, epsilon));
        }
        V3D_Ray_d ray = new V3D_Ray_d(focus, p);
        //V3D_Point_d px = (V3D_Point_d) rect.getIntersect(ray, epsilon);
        V3D_Point_d px = (V3D_Point_d) ray.getIntersect(rect.getPl(), epsilon);
        if (px == null) {
            return null;
        } else {
            return new Grids_2D_ID_int(
                    getScreenRow(px, epsilon),
                    getScreenCol(px, epsilon));
        }
    }

    /**
     * Calculate and return the row index of {@code p}.
     *
     * @param p A point on the screen.
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     * @return The row index of {@code p}.
     */
    protected int getScreenRow(V3D_Point_d p, double epsilon) {
        double d = rect.getPQR().getQR().getDistance(p, epsilon);
        return nrows - (int) (d / pixelSize);
    }

    /**
     * Calculate and return the column index of {@code p}.
     *
     * @param p A point on the screen.
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     * @return The column index of {@code p}.
     */
    protected int getScreenCol(V3D_Point_d p, double epsilon) {
        double d = rect.getPQR().getPQ().getDistance(p, epsilon);
        return (int) (d / pixelSize);
    }

    /**
     * Get the centroid of the pixel indexed by {@code row} and {@code col}.
     *
     * @param row The row index for the pixel returned.
     * @param col The column index for the pixel returned.
     * @param epsilon The tolerance within which vector components are regarded
     * as equal.
     * @return The centroid point of the pixel indexed by {@code row} and
     * {@code col}..
     */
    protected V3D_Point_d getPoint(int row, int col, double epsilon) {
        V3D_Point_d p = rect.getP();
        V3D_Point_d pP = new V3D_Point_d(p);
        pP.translate(verticalUV.multiply(row + 0.5d).add(
                horizontalUV.multiply(col + 0.5d)));
        return pP;
    }

    /**
     * Get the pixel rectangle for the pixel indexed by {@code row} and
     * {@code col}.
     *
     * @param row The row index for the pixel returned.
     * @param col The column index for the pixel returned.
     * @return The pixel rectangle indexed by {@code row} and {@code col}.
     */
    public V3D_Rectangle_d getPixel(int row, int col) {
        // Get bottom left point (row=0, col=0).
        V3D_Point_d p = rect.getP();
        V3D_Point_d pP = new V3D_Point_d(p);
        pP.translate(verticalUV.multiply(row).add(horizontalUV.multiply(col)));
        // q
        V3D_Point_d pQ = new V3D_Point_d(p);
        pQ.translate(verticalUV.multiply(row + 1).add(horizontalUV.multiply(col)));
        // r
        V3D_Point_d pR = new V3D_Point_d(p);
        pR.translate(verticalUV.multiply(row + 1).add(horizontalUV.multiply(col + 1)));
        // s
        V3D_Point_d pS = new V3D_Point_d(p);
        pS.translate(verticalUV.multiply(row).add(horizontalUV.multiply(col + 1)));
        return new V3D_Rectangle_d(pP, pQ, pR, pS);
    }
}
