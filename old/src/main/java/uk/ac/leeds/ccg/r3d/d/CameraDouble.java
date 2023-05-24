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
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.grids.d2.Grids_2D_ID_int;
import uk.ac.leeds.ccg.r3d.d.entities.TriangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Ray;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_GeometryDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PointDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_RectangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_TriangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_VectorDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_EnvelopeDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineSegmentDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PlaneDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_RayDouble;

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
public class CameraDouble extends V3D_PointDouble {

    private static final long serialVersionUID = 1L;

    /**
     * The screen.
     */
    public V3D_RectangleDouble screen;

    /**
     * A vector pointing to the right of the screen.
     */
    V3D_VectorDouble vd;

    /**
     * A vector pointing to the top of the screen.
     */
    V3D_VectorDouble v2d;

    /**
     * The screen width.
     */
    public double screenHeight;

    /**
     * The screen width.
     */
    public double screenWidth;

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
    public HashMap<Grids_2D_ID_int, V3D_RayDouble> rays;

    /**
     * For storing the length and width of a pixel (which is square).
     */
    double pixelSize;

    /**
     * Create a new instance. (N.B. There is a lot of waste
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public CameraDouble(V3D_PointDouble pt, V3D_EnvelopeDouble ve, int width,
            int height, double zoomFactor, double epsilon) throws Exception {
        this(pt, ve, width, height, initScreen(pt, ve, zoomFactor, epsilon), epsilon);
    }

    private static V3D_RectangleDouble initScreen(V3D_PointDouble pt,
            V3D_EnvelopeDouble ve, double zoomFactor, double epsilon) {
        // Need something orthoganol to pt and ve centroid
        V3D_PlaneDouble pl = new V3D_PlaneDouble(pt, new V3D_VectorDouble(pt, ve.getCentroid()));
        V3D_VectorDouble pv = pl.getPV();
        //return = ve.getViewport2(pt, pv);
        return ve.getViewport3(pt, pv, zoomFactor, epsilon);
    }

    /**
     * Create a new instance. (N.B. There is a lot of waste
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public CameraDouble(V3D_PointDouble pt, V3D_EnvelopeDouble ve, int width,
            int height, V3D_RectangleDouble screen, double epsilon) throws Exception {
        super(pt);
        System.out.println("Initialise Camera.");
        // Need something orthoganol to pt and ve centroid
        V3D_PlaneDouble pl = new V3D_PlaneDouble(pt, new V3D_VectorDouble(pt, ve.getCentroid()));
        V3D_VectorDouble pv = pl.getPV();
        // Initialise the screen
        this.screen = screen;
        int dim = Math.max(width, height);
        nrows = dim;
        ncols = dim;
        V3D_TriangleDouble screenpqr = screen.getPQR();
        screenWidth = screenpqr.getPQ().getLength();
        screenHeight = screenWidth;
        pixelSize = screenWidth / (double) width;
        initVectors(screenpqr);
        rays = new HashMap<>();
        System.out.println("Initialised Camera");
    }

    private void initVectors(V3D_TriangleDouble screenpqr) {
        vd = new V3D_VectorDouble(screenpqr.getQR().l.v).divide(ncols);
        v2d = new V3D_VectorDouble(screenpqr.getPQ().l.v).divide(nrows);
    }

    /**
     * Change the location of the camera.
     *
     * @param v The vector for the move.
     */
    @Override
    public void translate(V3D_VectorDouble v) {
        super.translate(v);
        screen.translate(v);
        initVectors(screen.getPQR());
        rays = new HashMap<>();
    }

    /**
     * Creates an image map of the universe.
     *
     * @param universe
     * @param lighting The lighting vector.
     * @return An image map.
     * @throws Exception
     */
    int[] render(UniverseDouble universe, V3D_VectorDouble lighting,
            double ambientLight, boolean castShadow, boolean addGraticules,
            double epsilon)
            throws Exception {
        // Collect all the triangles from the triangles and tetrahedra.
        int nTriangles = universe.triangles.size();
        int nt = nTriangles + (4 * universe.tetrahedra.size());
        TriangleDouble[] ts = new TriangleDouble[nt];
        for (int i = 0; i < nTriangles; i++) {
            ts[i] = universe.triangles.get(i);
        }
        for (int i = 0; i < universe.tetrahedra.size(); i++) {
            System.arraycopy(universe.tetrahedra.get(i).triangles, 0, ts, (i * 4) + nTriangles, 4);
        }

//        /**
//         * Currently, if a ray intersects a triangle, no matter how small, if 
//         * that triangle is the closest, then that is the triangle rendered in
//         * the pixel. What would produce a more realistic rendering is to 
//         * calculate the area of intersect minus the overlap of nearer 
//         * intersects of each tetrahedron at the pixel level. Then a composite
//         * colour for each pixel can be generated. As this would involve 
//         * calculating all the intersections with tetrahedra, the commented code
//         * section is left here for the time being.
//         */
//        V3D_Tetrahedron[] tetrahedrons = new V3D_Tetrahedron[nt];
//        for (int i = 0; i < ts.length; i++) {
//            //System.out.println(i + " out of " + ts.length);
//            tetras[i] = new V3D_Tetrahedron(this, ts[i].triangle);
//        }
        /**
         * Calculate the minimum distance between each triangle and the camera
         * focal point, order the triangles by the distance, and set the
         * lighting. This currently assumes that all of all the triangles are in
         * the picture.
         */
        System.out.println("Calculate the minimum distance between each"
                + " triangle and the camera focal point, order the triangles by"
                + " the distance, and set the lighting.");
        TreeMap<Double, Set<Integer>> mindOrderedTriangles = new TreeMap<>();
        double[] mind2t = new double[ts.length];
        V3D_PointDouble centroid = universe.envelope.getCentroid();
        process(centroid, 0, ts, lighting, ambientLight, mindOrderedTriangles, mind2t, epsilon);
        int nTriangles1PC = (nTriangles / 100);
        if (nTriangles1PC < 1) {
            nTriangles1PC = 1;
        }
        for (int i = 1; i < ts.length; i++) {
            if (i % nTriangles1PC == 0) {
                System.out.println("Triangle " + i + " out of " + ts.length);
            }
            process(centroid, i, ts, lighting, ambientLight, mindOrderedTriangles, mind2t, epsilon);
        }
        System.out.println("Minimum distance squared between any triangle and"
                + " the camera focal point = "
                + mindOrderedTriangles.firstKey().toString());
        /**
         * For each pixel, it is possible to find the area of intersection on
         * each pixel for each triangle. To do this, use tetrahedrons with the
         * fourth point of each tetrahedron being the focal point of the camera.
         * Then working from front to back calculate the areas of each triangle
         * on the pixel not obscured by closer triangles. The area information
         * for each triangle visible at the pixel level could then be used to
         * assign a colour. This would stop small things in the foreground
         * suddenly obscuring what would normally be seen behind.
         */
        System.out.println("Process each triangle working from the closest to "
                + "the furthest.");
        V3D_VectorDouble lightingr = lighting.reverse();
        HashMap<Grids_2D_ID_int, Double> mind = new HashMap<>();
        HashMap<Grids_2D_ID_int, Integer> closestIndex = new HashMap<>();
        /**
         * idPoint is used to store the point of intersection for the triangle
         * closest to the camera. This is later used to see if that point on the
         * triangle is in shadow
         */
        HashMap<Grids_2D_ID_int, V3D_PointDouble> idPoint = new HashMap<>();
        int j = 0;
        V3D_TriangleDouble screenpqr = screen.getPQR();
        V3D_LineSegmentDouble pq = screenpqr.getPQ();
        V3D_LineSegmentDouble qr = screen.getRSP().getQR();
        V3D_PlaneDouble screenPlane = screenpqr.getPl();
        for (double mind2 : mindOrderedTriangles.keySet()) {
            Set<Integer> triangleIndexes = mindOrderedTriangles.get(mind2);
            for (var i : triangleIndexes) {
                if (j % nTriangles1PC == 0) {
                    System.out.println("Triangle " + (j + 1) + " out of " + ts.length + ":");
                }
                processTriangle(screenPlane, pq, qr, i, ts[i].triangle, mind2t,
                        mind, closestIndex, idPoint, epsilon);
                j++;
            }
        }
        // Render each pixel, apply shadow and flip upside down for an image.
        System.out.println("Render the closest triangles applying shadow.");
        int n = ncols * nrows;
        int[] pix = new int[n];
        int pixelsToPop = closestIndex.size();
        int pixelsToPopPC = pixelsToPop / 100;
        if (castShadow) {
            int pixel = 0;
            for (var x : closestIndex.keySet()) {
                if (pixel % pixelsToPopPC == 0) {
                    System.out.println("Rendering pixel " + pixel + " out of " + pixelsToPop);
                }
                pixel++;
                int r = nrows - x.getRow() - 1;
                int in = (r * ncols) + x.getCol();
                int ci = closestIndex.get(x);
                TriangleDouble t = ts[ci];
                int rgb = t.lightingColor.getRGB();
                /**
                 * Determine if parts of each triangle that are visible from the
                 * camera are in shadow, i.e. the parts would not directly
                 * receive light from the lighting vector direction as some
                 * other triangle is in the way of this light. Normally, a
                 * triangle in such shadow appears darker. But, the way things
                 * appear in reality is complicated as surfaces absorb light and
                 * emit light. And, light emitted from surfaces can concentrate
                 * on particular parts of that same surface which in turn emit
                 * more light. Anyway, for those parts that are shaded from
                 * light coming from a particular direction, there are options
                 * about how dark to make things appear. The lighting colour can
                 * be set according to the ambient light, the colour that might
                 * have been (without shadow) could be reduced by some factor,
                 * or we can try to account for light emitted from the surface
                 * to provide variable shading. Setting to a default ambient
                 * light colour will typically make for a picture where many
                 * parts have the same murky colour which many viewers might
                 * regard as being unrealistic. Accounting for light emitted
                 * from surfaces is typically computationally demanding and more
                 * so with more surfaces with more concave features. Shading in
                 * a way is infinitely complex! Physics suggests that billions
                 * of photons (packets of energy) are emitted
                 * electromagnetically from light sources that convert mass into
                 * energy. Our vision - our eyes and our brains are geared to
                 * build up a visualisation - what we see. The process is
                 * amazing really and as things move there is a lot of
                 * calculation being done! Modelling this iteratively can help
                 * to generate more realistic images, but at what cost? Even
                 * accounting for first and second order shadows can be so
                 * computationally demanding, there has to be a value judgement
                 * about whether the effort is worth it!
                 */
                // Apply shadow
                V3D_RayDouble ray = new V3D_RayDouble(idPoint.get(x), lightingr);
                for (int i = 0; i < universe.triangles.size(); i++) {
                    if (i != ci) {
                        if (universe.triangles.get(i).triangle.getIntersection(ray, epsilon) != null) {
                            //rgb = t.ambientColor.getRGB();
//                            int red = (t.lightingColor.getRed() + t.ambientColor.getRed()) / 2;
//                            int green = (t.lightingColor.getGreen() + t.ambientColor.getGreen()) / 2;
//                            int blue = (t.lightingColor.getBlue() + t.ambientColor.getBlue()) / 2;
                            int red = (int) (t.lightingColor.getRed() * 0.9d);
                            int green = (int) (t.lightingColor.getGreen() * 0.9d);
                            int blue = (int) (t.lightingColor.getBlue() * 0.9d);
                            rgb = new Color(red, green, blue).getRGB();
                            break;
                        }
                    }
                }
                pix[in] = rgb;
            }
        } else {
            int pixel = 0;
            for (var x : closestIndex.keySet()) {
                if (pixel % pixelsToPopPC == 0) {
                    System.out.println("Rendering pixel " + pixel + " out of " + pixelsToPop);
                }
                pixel++;
                int r = nrows - x.getRow() - 1;
                int in = (r * ncols) + x.getCol();
                int ci = closestIndex.get(x);
                TriangleDouble t = ts[ci];
                pix[in] = t.lightingColor.getRGB();
            }
        }
        if (addGraticules) {
            double xmin = universe.envelope.getXMin();
            double xmax = universe.envelope.getXMax();
            double ymin = universe.envelope.getYMin();
            double ymax = universe.envelope.getYMax();
            double zmin = universe.envelope.getZMin();
            double zmax = universe.envelope.getZMax();
//            // Create x axis
//            V3D_PointDouble x_min = new V3D_PointDouble(new V3D_VectorDouble(xmin, 0d, 0d));
//            V3D_PointDouble x_max = new V3D_PointDouble(new V3D_VectorDouble(xmax, 0d, 0d));
//            if (!V3D_LineDouble.isCollinear(epsilon, x_min, x_max, this)) {
//            V3D_TriangleDouble x_axist = new V3D_TriangleDouble(x_min, x_max, this);
//            Grids_2D_ID_int px_min = getRC(screenPlane, x_min, pq, qr, epsilon);
//            Grids_2D_ID_int px_max = getRC(screenPlane, x_max, pq, qr, epsilon);
//            int minrow = Math.min(px_min.getRow(), px_max.getRow());
//            int maxrow = Math.max(px_min.getRow(), px_max.getRow());
//            int mincol = Math.min(px_min.getCol(), px_max.getCol());
//            int maxcol = Math.max(px_min.getCol(), px_max.getCol());
//            for (int r = minrow; r <= maxrow; r ++ ) {
//                for (int c = mincol; c <= maxcol; c ++ ) {
//                    V3D_RectangleDouble pixel = getPixelBounds(r, c);
//                    if (pixel.getIntersection(x_axist, epsilon) != null) {
//                        int row = nrows - r - 1;
//                        int in = (row * ncols) + c;
//                        pix[in] = Color.PINK.getRGB();
//                    }
//                }
//            }
//            }
            // Create y axis
            V3D_PointDouble y_min = new V3D_PointDouble(new V3D_VectorDouble(0d, ymin, 0d));
            V3D_PointDouble y_max = new V3D_PointDouble(new V3D_VectorDouble(0d, ymax, 0d));
            if (!V3D_LineDouble.isCollinear(epsilon, y_min, y_max, this)) {
            V3D_TriangleDouble y_axist = new V3D_TriangleDouble(y_min, y_max, this);
            
            y_axist.getPl();
            
            Grids_2D_ID_int py_min = getRC(screenPlane, y_min, pq, qr, epsilon);
            Grids_2D_ID_int py_max = getRC(screenPlane, y_max, pq, qr, epsilon);
            int minrow = Math.min(py_min.getRow(), py_max.getRow());
            int maxrow = Math.max(py_min.getRow(), py_max.getRow());
            int mincol = Math.min(py_min.getCol(), py_max.getCol());
            int maxcol = Math.max(py_min.getCol(), py_max.getCol());
            for (int r = minrow; r <= maxrow; r ++ ) {
                for (int c = mincol; c <= maxcol; c ++ ) {
                    V3D_RectangleDouble pixel = getPixelBounds(r, c);
                    
                    try {
                    if (pixel.getIntersection(y_axist, epsilon) != null) {
                        int row = nrows - r - 1;
                        int in = (row * ncols) + c;
                        pix[in] = Color.PINK.getRGB();
                    }
                    } catch (RuntimeException e) {
                        if (pixel.getIntersection(y_axist, epsilon) != null) {
                        int row = nrows - r - 1;
                        int in = (row * ncols) + c;
                        pix[in] = Color.PINK.getRGB();
                    }
                    }
                    
                }
            }
            }

//            
//            V3D_PointDouble y_min = new V3D_PointDouble(offset, new V3D_VectorDouble(0d, ymin, 0d));
//            V3D_PointDouble y_max = new V3D_PointDouble(offset, new V3D_VectorDouble(0d, ymax, 0d));
//            V3D_PointDouble z_min = new V3D_PointDouble(offset, new V3D_VectorDouble(0d, 0d, zmin));
//            V3D_PointDouble z_max = new V3D_PointDouble(offset, new V3D_VectorDouble(0d, 0d, zmax));
//            Grids_2D_ID_int py_min = getRC(screenPlane, y_min, pq, qr, epsilon);
//            Grids_2D_ID_int py_max = getRC(screenPlane, y_max, pq, qr, epsilon);
//            Grids_2D_ID_int pz_min = getRC(screenPlane, z_min, pq, qr, epsilon);
//            Grids_2D_ID_int pz_max = getRC(screenPlane, z_max, pq, qr, epsilon);
//                    
//            V3D_RayDouble x_minpt = new V3D_RayDouble(x_min, this);
//            V3D_RayDouble x_maxpt = new V3D_RayDouble(x_max, this);
//            V3D_RayDouble y_minpt = new V3D_RayDouble(y_min, this);
//            V3D_RayDouble y_maxpt = new V3D_RayDouble(y_max, this);
//            V3D_RayDouble z_minpt = new V3D_RayDouble(z_min, this);
//            V3D_RayDouble z_maxpt = new V3D_RayDouble(z_max, this);
//            
            
        }
        return pix;
    }

    private void process(V3D_PointDouble centroid, int index, TriangleDouble[] ts,
            V3D_VectorDouble lighting, double ambientLight,
            TreeMap<Double, Set<Integer>> mindOrderedTriangles,
            double[] mind2t, double epsilon) {
        ts[index].setLighting(centroid, lighting, ambientLight, epsilon);
        mind2t[index] = ts[index].triangle.getDistanceSquared(this, epsilon);
        Generic_Collections.addToMap(mindOrderedTriangles, mind2t[index], index);
    }

    /**
     * Get the CellIDs of those cells that intersect with triangle.
     *
     * @param t The triangle.
     * @param epsilon The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    protected void processTriangle(V3D_PlaneDouble pl, V3D_LineSegmentDouble pq,
            V3D_LineSegmentDouble qr,
            int tIndex, V3D_TriangleDouble t, double[] mind2t,
            HashMap<Grids_2D_ID_int, Double> mind2s,
            HashMap<Grids_2D_ID_int, Integer> closestIndex,
            HashMap<Grids_2D_ID_int, V3D_PointDouble> idPoint,
            double epsilon) {
        // Only consider parts of triangles that are beyond the screen plane?
        Grids_2D_ID_int prc = getRC(pl, t.getP(), pq, qr, epsilon);
        Grids_2D_ID_int qrc = getRC(pl, t.getQ(), pq, qr, epsilon);
        Grids_2D_ID_int rrc = getRC(pl, t.getR(), pq, qr, epsilon);
        int[] eRCI = getExtremeRowColIndex(prc, qrc, rrc);
        long minRowIndex = eRCI[0];
        long maxRowIndex = eRCI[2];
        long minColIndex = eRCI[1];
        long maxColIndex = eRCI[3];
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
        /**
         * Loop over the extent, and where necessary, calculate the intersection
         * to determine if the triangle is in the cell and if the distance is a
         * minimum distance.
         */
        for (int row = (int) minRowIndex; row < maxRowIndex + 1; row++) {
            //for (int row = 0; row < 100; row++) {
            for (int col = (int) minColIndex; col < maxColIndex + 1; col++) {
                Grids_2D_ID_int id = new Grids_2D_ID_int(row, col);
                Double mind2 = mind2s.get(id);
                if (mind2 == null) {
                    try {
                        V3D_GeometryDouble ti = t.getIntersection(getRay(id), epsilon);
                        if (ti != null) {
                            // Only render triangles that intersect the ray at a point.
                            if (ti instanceof V3D_PointDouble tip) {
                                double d2 = tip.getDistanceSquared(this);
                                mind2s.put(id, d2);
                                closestIndex.put(id, tIndex);
                                idPoint.put(id, tip);
                            }
                        }
                    } catch (RuntimeException ex) {
                        System.out.println("Resolution too coarse to render "
                                + "triangle: " + t.toStringSimple(""));
                    }
                } else {
                    if (mind2t[tIndex] < mind2) {
                        try {
                            V3D_GeometryDouble ti = t.getIntersection(getRay(id), epsilon);
                            if (ti != null) {
                                // Only render triangles that intersect the ray at a point.
                                if (ti instanceof V3D_PointDouble tip) {
                                    double d2 = tip.getDistanceSquared(this);
                                    if (d2 < mind2) {
                                        mind2s.put(id, d2);
                                        closestIndex.put(id, tIndex);
                                        idPoint.put(id, tip);
                                    }
                                }
                            }
                        } catch (RuntimeException ex) {
                            System.out.println("Resolution too coarse to render "
                                    + "triangle: " + t.toStringSimple(""));
                        }
                    }
                }
            }
        }
    }

    /**
     * @param ids The set of cell ids
     * @return r[] where; r[0] is the minimum row, r[1] is the minimum col,
     * r[2] is the maximum row, r[3] is the maximum col.
     */
    public int[] getExtremeRowColIndex(Grids_2D_ID_int... ids) {
        int[] r = new int[4];
        for (var id: ids) {
            if (id != null) {
                int row = id.getRow();
                int col = id.getCol();
                r[0] = row;
                r[1] = col;
                r[2] = row;
                r[3] = col;
                break;
            }
        }
        for (var id: ids) {
            if (id != null) {
                int row = id.getRow();
                int col = id.getCol();
                r[0] = Math.min(r[0], row);
                r[1] = Math.min(r[1], col);
                r[2] = Math.max(r[2], row);
                r[3] = Math.max(r[3], col);
                break;
            }
        }
        return r;
    }
    
    /**
     * Get the pixel that the ray intersects.
     *
     * @param pl The screen plane.
     * @param tp The triangle point.
     * @param pq The left edge of the screen.
     * @param qr The top edge of the screen.
     * @return The IDs of the screen cells that the ray intersects.
     */
    protected Grids_2D_ID_int getRC(V3D_PlaneDouble pl, V3D_PointDouble tp,
            V3D_LineSegmentDouble pq,
            V3D_LineSegmentDouble qr, double epsilon) {
        V3D_PointDouble pi = pl.getPointOfProjectedIntersection(tp, epsilon);
        return new Grids_2D_ID_int(
                getScreenRow(pi, qr, epsilon),
                getScreenCol(pi, pq, epsilon));
//        if (this.equals(tp, epsilon)) {
//            return new Grids_2D_ID_int();
//        V3D_RayDouble ray = new V3D_RayDouble(this, tp);
////        V3D_PointDouble pv = (V3D_PointDouble) screen.getIntersection(ray);
////        if (pv == null) {
////            return null;
////        }
//        V3D_PointDouble p = (V3D_PointDouble) ray.getIntersection(pl, epsilon);
//        if (p == null) {
//            //The ray must point away from the screen as the triangle point is not through it.
//            p = pl.getPointOfProjectedIntersection(tp, epsilon);
//            return new Grids_2D_ID_int(-getScreenRow(p, qr, epsilon),
//                    -getScreenCol(p, pq, epsilon));
//        } else {
//            return new Grids_2D_ID_int(getScreenRow(p, qr, epsilon),
//                    getScreenCol(p, pq, epsilon));
//        }
//        }
//        return 
    }

    /**
     * Calculate and return the row index of the screen that pv is on.
     *
     * @param p The point on the screen.
     * @param pq The line segment from of the top or bottom of the screen.
     * @return The row index of the screen that ray passes through.
     */
    protected int getScreenRow(V3D_PointDouble p, V3D_LineSegmentDouble qr,
            double epsilon) {
        V3D_PointDouble px = qr.l.getPointOfIntersection(p, epsilon);
        double d = px.getDistance(p);
        return (int) (d / pixelSize);
    }

    /**
     * Calculate and return the column index of the screen that pv is on.
     *
     * @param p The point on the screen.
     * @param pq The line segment from of the left or right the screen.
     * @return The column index of the screen that l passes through.
     */
    protected int getScreenCol(V3D_PointDouble p, V3D_LineSegmentDouble pq,
            double epsilon) {
        V3D_PointDouble py = pq.l.getPointOfIntersection(p, epsilon);
        double d = py.getDistance(p);
        return (int) (d / pixelSize);
    }

    /**
     * For getting a ray from the camera focal point through the centre of the
     * screen pixel with ID id.
     *
     * @param id The ID of the screen pixel.
     * @return The ray from the camera focal point through the centre of the
     * screen pixel.
     */
    protected V3D_RayDouble getRay(Grids_2D_ID_int id) {
        V3D_RayDouble r = rays.get(id);
        if (r == null) {
            V3D_VectorDouble rv = v2d.multiply(id.getRow());
            V3D_VectorDouble cv = vd.multiply(id.getCol());
            V3D_PointDouble rcpt = new V3D_PointDouble(screen.getP());
            rcpt.translate(rv.add(cv));
            r = new V3D_RayDouble(this, rcpt);
            rays.put(id, r);
        }
        return r;
    }
    
    public V3D_RectangleDouble getPixelBounds(int row, int col) {
        // p
        V3D_PointDouble p = new V3D_PointDouble(screen.getP());
        V3D_VectorDouble pq = screen.getPQR().getPQ().l.v.divide((double) nrows * row);
        V3D_VectorDouble qr = screen.getPQR().getQR().l.v.divide((double) ncols * col);
        p.translate(pq);
        p.translate(qr);
        // q
        V3D_PointDouble q = new V3D_PointDouble(screen.getP());
        pq = screen.getPQR().getPQ().l.v.divide((double) nrows * row + 1);
        qr = screen.getPQR().getQR().l.v.divide((double) ncols * col);
        q.translate(pq);
        q.translate(qr);
        // r
        V3D_PointDouble r = new V3D_PointDouble(screen.getP());
        pq = screen.getPQR().getPQ().l.v.divide((double) nrows * row + 1);
        qr = screen.getPQR().getQR().l.v.divide((double) ncols * col + 1);
        r.translate(pq);
        r.translate(qr);
        // r
        V3D_PointDouble s = new V3D_PointDouble(screen.getP());
        pq = screen.getPQR().getPQ().l.v.divide((double) nrows * row);
        qr = screen.getPQR().getQR().l.v.divide((double) ncols * col + 1);
        s.translate(pq);
        s.translate(qr);
        return new V3D_RectangleDouble(p, q, r, s);        
    }
}
