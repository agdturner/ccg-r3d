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
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.grids.d2.Grids_2D_ID_int;
import uk.ac.leeds.ccg.r3d.d.entities.LineDouble;
import uk.ac.leeds.ccg.r3d.d.entities.PointDouble;
import uk.ac.leeds.ccg.r3d.d.entities.TriangleDouble;
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
    public final V3D_RectangleDouble screen;

    /**
     * The epsilon.
     */
    public final double epsilon;

    /**
     * The screen pqr triangle.
     */
    public  V3D_TriangleDouble pqr;

    /**
     * The screen pq.
     */
    public  V3D_LineSegmentDouble pq;

    /**
     * The screen qr.
     */
    public  V3D_LineSegmentDouble qr;

    /**
     * The screen plane.
     */
    public  V3D_PlaneDouble screenPlane;

    /**
     * A vector of pixel magnitude in the direction to top of screen.
     */
    public  V3D_VectorDouble pqv;

    /**
     * A vector of pixel magnitude in the direction to right of screen.
     */
    public  V3D_VectorDouble qrv;

    /**
     * The screen width.
     */
    public  double screenHeight;

    /**
     * The screen width.
     */
    public  double screenWidth;

    /**
     * The number of pixels in a row of the screen.
     */
    public  int nrows;

    /**
     * The number of pixels in a column of the screen.
     */
    public  int ncols;

    /**
     * Rays from the camera focal point through each pixel centre.
     */
    public  HashMap<Grids_2D_ID_int, V3D_RayDouble> rays;

    /**
     * For storing the length and width of a pixel (which is square).
     */
    public  double pixelSize;

    /**
     * Create a new instance. (N.B. There is a lot of waste
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public CameraDouble(V3D_PointDouble pt, V3D_EnvelopeDouble ve,
            Dimension dim, double zoomFactor, double epsilon) throws Exception {
        this(pt, ve, dim, initScreen(pt, ve, zoomFactor, epsilon), epsilon);
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
    public CameraDouble(V3D_PointDouble pt, V3D_EnvelopeDouble ve,
            Dimension dim, V3D_RectangleDouble screen, double epsilon)
            throws Exception {
        super(pt);
        System.out.println("Initialise Camera.");
        // Initialise the screen
        this.screen = screen;
        this.epsilon = epsilon;
        nrows = dim.height;
        ncols = dim.width;
        init();
        System.out.println("Initialised Camera");
    }
    
    private void init() {
        pqr = screen.getPQR();
        pq = pqr.getPQ();
        //qr = pqr.getQR();
        qr = screen.getRSP().getQR();
        screenPlane = screen.getPlane(epsilon);
        pqv = pq.l.v.divide((double) nrows);
        qrv = qr.l.v.divide((double) ncols).reverse();
        screenWidth = qr.getLength();
        screenHeight = pq.getLength();
        pixelSize = screenWidth / (double) ncols;
        rays = new HashMap<>();

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
        init();
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
        int n = ncols * nrows;
        int[] pix = new int[n];
        // Collect all the triangles from the triangles and tetrahedra.
        int nTriangles = universe.triangles.size();
        int nt = nTriangles + (4 * universe.tetrahedra.size());
        if (nt > 0) {
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
             * Calculate the minimum distance between each triangle and the
             * camera focal point, order the triangles by the distance, and set
             * the lighting. This currently assumes that all of all the
             * triangles are in the picture.
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
             * For each pixel, it is possible to find the area of intersection
             * on each pixel for each triangle. To do this, use tetrahedrons
             * with the fourth point of each tetrahedron being the focal point
             * of the camera. Then working from front to back calculate the
             * areas of each triangle on the pixel not obscured by closer
             * triangles. The area information for each triangle visible at the
             * pixel level could then be used to assign a colour. This would
             * stop small things in the foreground suddenly obscuring what would
             * normally be seen behind.
             */
            System.out.println("Process each triangle working from the closest to "
                    + "the furthest.");
            V3D_VectorDouble lightingr = lighting.reverse();
            HashMap<Grids_2D_ID_int, Double> mind = new HashMap<>();
            HashMap<Grids_2D_ID_int, Integer> closestIndex = new HashMap<>();
            /**
             * idPoint is used to store the point of intersection for the
             * triangle closest to the camera. This is later used to see if that
             * point on the triangle is in shadow
             */
            HashMap<Grids_2D_ID_int, V3D_PointDouble> idPoint = new HashMap<>();
            int j = 0;

            for (double mind2 : mindOrderedTriangles.keySet()) {
                Set<Integer> triangleIndexes = mindOrderedTriangles.get(mind2);
                for (var i : triangleIndexes) {
                    if (j % nTriangles1PC == 0) {
                        System.out.println("Triangle " + (j + 1) + " out of " + ts.length + ":");
                    }
                    processTriangle(i, ts[i].triangle, mind2t,
                            mind, closestIndex, idPoint, epsilon);
                    j++;
                }
            }
            // Render each pixel, apply shadow and flip upside down for an image.
            System.out.println("Render the closest triangles applying shadow.");
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
                     * Determine if parts of each triangle that are visible from
                     * the camera are in shadow, i.e. the parts would not
                     * directly receive light from the lighting vector direction
                     * as some other triangle is in the way of this light.
                     * Normally, a triangle in such shadow appears darker. But,
                     * the way things appear in reality is complicated as
                     * surfaces absorb light and emit light. And, light emitted
                     * from surfaces can concentrate on particular parts of that
                     * same surface which in turn emit more light. Anyway, for
                     * those parts that are shaded from light coming from a
                     * particular direction, there are options about how dark to
                     * make things appear. The lighting colour can be set
                     * according to the ambient light, the colour that might
                     * have been (without shadow) could be reduced by some
                     * factor, or we can try to account for light emitted from
                     * the surface to provide variable shading. Setting to a
                     * default ambient light colour will typically make for a
                     * picture where many parts have the same murky colour which
                     * many viewers might regard as being unrealistic.
                     * Accounting for light emitted from surfaces is typically
                     * computationally demanding and more so with more surfaces
                     * with more concave features. Shading in a way is
                     * infinitely complex! Physics suggests that billions of
                     * photons (packets of energy) are emitted
                     * electromagnetically from light sources that convert mass
                     * into energy. Our vision - our eyes and our brains are
                     * geared to build up a visualisation - what we see. The
                     * process is amazing really and as things move there is a
                     * lot of calculation being done! Modelling this iteratively
                     * can help to generate more realistic images, but at what
                     * cost? Even accounting for first and second order shadows
                     * can be so computationally demanding, there has to be a
                     * value judgement about whether the effort is worth it!
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
                    if (pixelsToPopPC > 0) {
                        if (pixel % pixelsToPopPC == 0) {
                            System.out.println("Rendering pixel " + pixel + " out of " + pixelsToPop);
                        }
                    }
                    pixel++;
                    int r = nrows - x.getRow() - 1;
                    int in = (r * ncols) + x.getCol();
                    int ci = closestIndex.get(x);
                    TriangleDouble t = ts[ci];
                    pix[in] = t.lightingColor.getRGB();
                }
            }

            // Render triangle corners and edges
            // Render edges
            for (var t : universe.triangles) {
                renderLine(epsilon, new LineDouble(t.triangle.getPQ(), Color.BLACK), pix);
                renderLine(epsilon, new LineDouble(t.triangle.getQR(), Color.BLUE), pix);
                renderLine(epsilon, new LineDouble(t.triangle.getRP(), Color.GREEN), pix);
                //renderLine(epsilon, new LineDouble(t.triangle.getPQ(), Color.BLACK), pix);
                //renderLine(epsilon, new LineDouble(t.triangle.getQR(), Color.BLUE), pix);
                //renderLine(epsilon, new LineDouble(t.triangle.getRP(), Color.GREEN), pix);
            }
            // Render corners
            for (var t : universe.triangles) {
                renderPoint(epsilon, new PointDouble(t.triangle.getP(), Color.ORANGE), pix);
                renderPoint(epsilon, new PointDouble(t.triangle.getQ(), Color.ORANGE), pix);
                renderPoint(epsilon, new PointDouble(t.triangle.getR(), Color.ORANGE), pix);
            }

            if (addGraticules) {

                double xmin = universe.envelope.getXMin();
                double xmax = universe.envelope.getXMax();
                double ymin = universe.envelope.getYMin();
                double ymax = universe.envelope.getYMax();
                double zmin = universe.envelope.getZMin();
                double zmax = universe.envelope.getZMax();

                // Create axes
                V3D_PointDouble x_min = new V3D_PointDouble(new V3D_VectorDouble(xmin, 0d, 0d));
                V3D_PointDouble x_max = new V3D_PointDouble(new V3D_VectorDouble(xmax, 0d, 0d));
                if (!V3D_LineDouble.isCollinear(epsilon, x_min, x_max, this)) {
                    V3D_TriangleDouble x_axist = new V3D_TriangleDouble(x_min, x_max, this);
                    for (int r = 0; r < nrows; r++) {
                        for (int c = 0; c < ncols; c++) {
                            V3D_RectangleDouble pixel = getPixelBounds(r, c);
                            if (pixel.getIntersection(x_axist, epsilon) != null) {
                                int row = nrows - r - 1;
                                int in = (row * ncols) + c;
                                pix[in] = Color.BLUE.getRGB();
                            }
                        }
                    }
                } else {
                    V3D_LineDouble x_axis = V3D_LineDouble.X_AXIS;
                    for (int r = 0; r < nrows; r++) {
                        for (int c = 0; c < ncols; c++) {
                            V3D_RectangleDouble pixel = getPixelBounds(r, c);
                            if (pixel.getIntersection(x_axis, epsilon) != null) {
                                int row = nrows - r - 1;
                                int in = (row * ncols) + c;
                                pix[in] = Color.BLUE.getRGB();
                            }
                        }
                    }
                }
                V3D_PointDouble y_min = new V3D_PointDouble(new V3D_VectorDouble(0d, ymin, 0d));
                V3D_PointDouble y_max = new V3D_PointDouble(new V3D_VectorDouble(0d, ymax, 0d));
                if (!V3D_LineDouble.isCollinear(epsilon, y_min, y_max, this)) {
                    V3D_TriangleDouble y_axist = new V3D_TriangleDouble(y_min, y_max, this);
                    for (int r = 0; r < nrows; r++) {
                        for (int c = 0; c < ncols; c++) {
                            V3D_RectangleDouble pixel = getPixelBounds(r, c);
                            if (pixel.getIntersection(y_axist, epsilon) != null) {
                                int row = nrows - r - 1;
                                int in = (row * ncols) + c;
                                pix[in] = Color.RED.getRGB();
                            }
                        }
                    }
                } else {
                    V3D_LineDouble y_axis = V3D_LineDouble.Y_AXIS;
                    for (int r = 0; r < nrows; r++) {
                        for (int c = 0; c < ncols; c++) {
                            V3D_RectangleDouble pixel = getPixelBounds(r, c);
                            if (pixel.getIntersection(y_axis, epsilon) != null) {
                                int row = nrows - r - 1;
                                int in = (row * ncols) + c;
                                pix[in] = Color.RED.getRGB();
                            }
                        }
                    }
                }
                V3D_PointDouble z_min = new V3D_PointDouble(new V3D_VectorDouble(0d, 0d, zmin));
                V3D_PointDouble z_max = new V3D_PointDouble(new V3D_VectorDouble(0d, 0d, zmax));
                if (!V3D_LineDouble.isCollinear(epsilon, z_min, z_max, this)) {
                    V3D_TriangleDouble z_axist = new V3D_TriangleDouble(z_min, z_max, this);
                    for (int r = 0; r < nrows; r++) {
                        for (int c = 0; c < ncols; c++) {
                            V3D_RectangleDouble pixel = getPixelBounds(r, c);
                            if (pixel.getIntersection(z_axist, epsilon) != null) {
                                int row = nrows - r - 1;
                                int in = (row * ncols) + c;
                                pix[in] = Color.GREEN.getRGB();
                            }
                        }
                    }
                } else {
                    V3D_LineDouble z_axis = V3D_LineDouble.Z_AXIS;
                    for (int r = 0; r < nrows; r++) {
                        for (int c = 0; c < ncols; c++) {
                            V3D_RectangleDouble pixel = getPixelBounds(r, c);
                            if (pixel.getIntersection(z_axis, epsilon) != null) {
                                int row = nrows - r - 1;
                                int in = (row * ncols) + c;
                                pix[in] = Color.GREEN.getRGB();
                            }
                        }
                    }
                }

            }
        }
        //renderAxes(screenPlane, pq, qr, epsilon, universe, pix);
        return pix;
    }

    /**
     * For rendering a line on the image.
     *
     * @param epsilon The tolerance for intersection.
     * @param l The line to render.
     * @param pix The image.
     * @return pix
     */
    public int[] renderLine(double epsilon, LineDouble l, int[] pix) {
        Grids_2D_ID_int prc = getRC(l.l.getP(), epsilon);
        Grids_2D_ID_int qrc = getRC(l.l.getQ(), epsilon);
        // Calculate the row bounds.
        int minr = Math.min(prc.getRow(), qrc.getRow());
        int maxr = Math.max(prc.getRow(), qrc.getRow());
        int minc = Math.min(prc.getCol(), qrc.getCol());
        int maxc = Math.max(prc.getCol(), qrc.getCol());
//        int minr = Math.min(prc.getRow(), qrc.getRow()) - 1;
//        int maxr = Math.max(prc.getRow(), qrc.getRow()) + 1;
//        int minc = Math.min(prc.getCol(), qrc.getCol()) - 1;
//        int maxc = Math.max(prc.getCol(), qrc.getCol()) + 1;
        if (minr < 0) {
            minr = 0;
        }
        if (minc < 0) {
            minc = 0;
        }
        if (maxr >= nrows) {
            maxr = nrows - 1;
        }
        if (maxc >= ncols) {
            maxc = ncols - 1;
        }
        V3D_TriangleDouble t = new V3D_TriangleDouble(l.l, this);
        for (int r = minr; r <= maxr; r++) {
            for (int c = minc; c <= maxc; c++) {
//        for (int r = 0; r < nrows; r++) {
//            for (int c = 0; c < ncols; c++) {
                V3D_RectangleDouble pixel = getPixelBounds(r, c);
                //System.out.println("" + r + ", " + c);
                if (pixel.getIntersection(t, epsilon) != null) {
                    int row = nrows - r - 1;
                    int in = (row * ncols) + c;
                    pix[in] = l.baseColor.getRGB();
                }
            }
        }
        return pix;
    }

    /**
     * For rendering a point on the image.
     *
     * @param epsilon The tolerance for intersection.
     * @param p The point to render.
     * @param pix The image.
     * @return pix
     */
    public int[] renderPoint(double epsilon, PointDouble p, int[] pix) {
        V3D_LineDouble l = new V3D_LineDouble(p.p, this);
        V3D_PointDouble poi = (V3D_PointDouble) screen.getIntersection(l, epsilon);
        int r = getScreenRow(poi, epsilon);
        int c = getScreenCol(poi, epsilon);
        getScreenCol(p.p, epsilon);
        int row = nrows - r - 1;
        int in = (row * ncols) + c;
        pix[in] = p.baseColor.getRGB();
        return pix;
    }

    /**
     * For rendering the axes on the image.
     *
     * @param epsilon The tolerance for intersection.
     * @param universe
     * @param pix The image.
     * @return pix
     */
    public int[] renderAxes(V3D_PlaneDouble pl, V3D_LineSegmentDouble pq,
            V3D_LineSegmentDouble qr, double epsilon, UniverseDouble universe, int[] pix) {
        if (universe.lines != null) {
            for (var l : universe.lines) {
                renderLine(epsilon, l, pix);
            }
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
     * @param pq The left of the screen.
     * @param qr The top of the screen.
     * @param tIndex The triangle index.
     * @param t The triangle.
     * @param mind2t The minimum distance squared between each triangle and the
     * camera point.
     * @param mind2s The minimum distance squared between each triangle and the
     * camera point through a pixel.
     * @param closestIndex The indexes of the closest triangles through each
     * pixel.
     * @param idPoint The point of intersection on the closest triangle through
     * each pixel.
     * @param epsilon The tolerance within which two vectors are equal.
     */
    protected void processTriangle(int tIndex, V3D_TriangleDouble t, 
            double[] mind2t, HashMap<Grids_2D_ID_int, Double> mind2s,
            HashMap<Grids_2D_ID_int, Integer> closestIndex,
            HashMap<Grids_2D_ID_int, V3D_PointDouble> idPoint,
            double epsilon) {
        // Calculate the extent of the rows and columns the triangle is in.
        Grids_2D_ID_int prc = getRC(t.getP(), epsilon);
        Grids_2D_ID_int qrc = getRC(t.getQ(), epsilon);
        Grids_2D_ID_int rrc = getRC(t.getR(), epsilon);
        long minr = Math.min(Math.min(prc.getRow(), qrc.getRow()), rrc.getRow());
        long maxr = Math.max(Math.max(prc.getRow(), qrc.getRow()), rrc.getRow());
        long minc = Math.min(Math.min(prc.getCol(), qrc.getCol()), rrc.getCol());
        long maxc = Math.max(Math.max(prc.getCol(), qrc.getCol()), rrc.getCol());
        if (minr < 0) {
            minr = 0;
        }
        if (minc < 0) {
            minc = 0;
        }
        if (maxr >= nrows) {
            maxr = nrows - 1;
        }
        if (maxc >= ncols) {
            maxc = ncols - 1;
        }
        /**
         * Loop over the extent, and where necessary, calculate the intersection
         * to determine if the triangle is in the cell and if the distance is a
         * minimum distance.
         */
        for (int row = (int) minr; row <= maxr; row++) {
            //for (int row = 0; row < 100; row++) {
            for (int col = (int) minc; col <= maxc; col++) {
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
     * Get the pixel that the ray intersects.
     *
     * @param p A point in the universe.
     * @return The ID of the screen cell that intersects a ray from this to p,
     * or {@code null}.
     */
    protected Grids_2D_ID_int getRC(V3D_PointDouble p, double epsilon) {
        V3D_RayDouble ray = new V3D_RayDouble(this, p);
        V3D_PointDouble px = (V3D_PointDouble) screen.getIntersection(ray, epsilon);
        if (px == null) {
            return null;
        } else {
            return new Grids_2D_ID_int(
                    getScreenRow(px, epsilon),
                    getScreenCol(px, epsilon));
        }
    }

    /**
     * Calculate and return the row index of the screen that p is on.
     *
     * @param p A point on the screen.
     * @param qr The line segment that is the bottom of the screen.
     * @return The row index of the screen for the point p.
     */
    protected int getScreenRow(V3D_PointDouble p, double epsilon) {
        //V3D_PointDouble px = qr.l.getPointOfIntersection(p, epsilon);
        //double d = px.getDistance(p);
        double d = qr.getDistance(p, epsilon);
        return (int) (d / pixelSize);
    }

    /**
     * Calculate and return the column index of the screen that p is on.
     *
     * @param p A point on the screen.
     * @return The column index of the screen for the point p.
     */
    protected int getScreenCol(V3D_PointDouble p, double epsilon) {
        //V3D_PointDouble py = pq.l.getPointOfIntersection(p, epsilon);
        //double d = py.getDistance(p);
        double d = pq.getDistance(p, epsilon);
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
            V3D_VectorDouble rv = pqv.multiply(id.getRow());
            V3D_VectorDouble cv = qrv.multiply(id.getCol());
            V3D_PointDouble rcpt = new V3D_PointDouble(screen.getP());
            rcpt.translate(rv.add(cv));
            r = new V3D_RayDouble(this, rcpt);
            rays.put(id, r);
        }
        return r;
    }

    /**
     * @param row The row index for the pixel returned.
     * @param col The column index for the pixel returned.
     * @return The pixel rectangle.
     */
    public V3D_RectangleDouble getPixelBounds(int row, int col) {
        // p
        V3D_PointDouble sp = new V3D_PointDouble(screen.getP());
        V3D_PointDouble p = new V3D_PointDouble(sp);
        p.translate(pqv.multiply(row));
        p.translate(qrv.multiply(col));
        // q
        V3D_PointDouble q = new V3D_PointDouble(sp);
        q.translate(pqv.multiply(row + 1));
        q.translate(qrv.multiply(col));
        // r
        V3D_PointDouble r = new V3D_PointDouble(sp);
        r.translate(pqv.multiply(row + 1));
        r.translate(qrv.multiply(col + 1));
        // s
        V3D_PointDouble s = new V3D_PointDouble(sp);
        s.translate(pqv.multiply(row));
        s.translate(qrv.multiply(col + 1));
        return new V3D_RectangleDouble(p, q, r, s);
    }
}
