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
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_FiniteGeometryDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_LineSegmentDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PlaneDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_RayDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_TetrahedronDouble;

/**
 * Camera instances are situated in the 3D Universe. They are a point behind the
 * centre of a rectangular screen that contains square pixels arranged in rows
 * and columns. The resolution of the screen is determined by the number of rows
 * and columns.
 *
 * The closer the point is to the screen, the bigger the field of view. The
 * point can't be on the screen. Typically, the point is about as far from the
 * screen as the screen is wide or high. The camera does not have a lens that
 * bends or focuses the light.
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
 * Rays from the point through the centre of each pixel of the screen can be
 * used to find the closest geometries for simplistic rendering. For more
 * sophisticated rendering, intersections are needed to calculate areas within
 * the four triangular bounds from the point through each edge of each pixel.
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
     * A small number used as a tolerance within which two vectors are regarded
     * as equal.
     */
    public final double epsilon;

    /**
     * Lower left corner point of screen.
     */
    public V3D_PointDouble p;

    /**
     * The screen pq.
     */
    public V3D_LineSegmentDouble pq;

    /**
     * The screen qr.
     */
    public V3D_LineSegmentDouble qr;

    /**
     * The screen plane.
     */
    public V3D_PlaneDouble screenPlane;

    /**
     * A vector of pixel magnitude in the direction to top of screen.
     */
    public V3D_VectorDouble pqv;

    /**
     * A vector of pixel magnitude in the direction to right of screen.
     */
    public V3D_VectorDouble qrv;

    /**
     * The screen height.
     */
    public double screenHeight;

    /**
     * The screen width.
     */
    public double screenWidth;

    /**
     * The number of rows of screen pixels.
     */
    public int nrows;

    /**
     * The number of columns of screen pixels.
     */
    public int ncols;

    /**
     * For storing rays from the camera focal point through each pixel centre.
     */
    public HashMap<Grids_2D_ID_int, V3D_RayDouble> rays;

    /**
     * For storing the length and width of a pixel (which is square).
     */
    public double pixelSize;

    /**
     * Create a new instance.
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public CameraDouble(V3D_PointDouble p, V3D_EnvelopeDouble ve,
            Dimension dim, double zoomFactor, double epsilon) throws Exception {
        this(p, ve, dim, initScreen(p, ve, zoomFactor, epsilon), epsilon);
    }

    private static V3D_RectangleDouble initScreen(V3D_PointDouble p,
            V3D_EnvelopeDouble ve, double zoomFactor, double epsilon) {
        // Need something orthoganol to pt and ve centroid
        V3D_PlaneDouble pl = new V3D_PlaneDouble(p, new V3D_VectorDouble(p, ve.getCentroid()));
        V3D_VectorDouble pv = pl.getPV();
        //return = ve.getViewport2(pt, pv);
        return ve.getViewport3(p, pv, zoomFactor, epsilon);
    }

    /**
     * Create a new instance.
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
        p = screen.getP();
        pq = screen.pqr.getPQ();
        //qr = screen.pqr.getQR();
        qr = screen.rsp.getQR();
        screenPlane = screen.getPlane();
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
            /**
             * mind2st is the minimum distance of each triangle to the camera
             * point for those parts of the triangle in view of the camera i.e.
             * through the frustrum.
             */
            double[] mind2st = new double[ts.length];
            V3D_PointDouble centroid = universe.envelope.getCentroid();
            process(centroid, 0, ts, lighting, ambientLight, mindOrderedTriangles, mind2st, epsilon);
            int nTriangles1PC = (nTriangles / 100);
            if (nTriangles1PC < 1) {
                nTriangles1PC = 1;
            }
            for (int i = 1; i < ts.length; i++) {
                if (i % nTriangles1PC == 0) {
                    System.out.println("Triangle " + i + " out of " + ts.length);
                }
                process(centroid, i, ts, lighting, ambientLight, mindOrderedTriangles, mind2st, epsilon);
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
            HashMap<Grids_2D_ID_int, Double> mind2s = new HashMap<>();
            HashMap<Grids_2D_ID_int, Integer> closestIndex = new HashMap<>();
            /**
             * idPoint is used to store the point of intersection for the
             * triangle closest to the camera. This is later used to see if that
             * point on the triangle is in shadow
             */
            HashMap<Grids_2D_ID_int, V3D_PointDouble> idPoint = new HashMap<>();

            for (double mind2 : mindOrderedTriangles.keySet()) {
                Set<Integer> triangleIndexes = mindOrderedTriangles.get(mind2);
                for (var i : triangleIndexes) {
                    processTriangle(i, ts[i].triangle, mind2st,
                            mind2s, closestIndex, idPoint, epsilon);
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
                    int ci = closestIndex.get(x);
                    TriangleDouble t = ts[ci];
                    Color color = t.lightingColor;
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
                                color = new Color(red, green, blue);
                                break;
                            }
                        }
                    }
                    render(pix, x.getRow(), x.getCol(), color);
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
                    int ci = closestIndex.get(x);
                    TriangleDouble t = ts[ci];
                    render(pix, x.getRow(), x.getCol(), t.lightingColor);
                }
            }

            // Render triangle corners and edges
            // Render edges
            for (var t : universe.triangles) {
                renderLine(epsilon, mind2s, new LineDouble(t.triangle.getPQ(), Color.YELLOW), t.triangle.pl, pix);
                renderLine(epsilon, mind2s, new LineDouble(t.triangle.getQR(), Color.CYAN), t.triangle.pl, pix);
                renderLine(epsilon, mind2s, new LineDouble(t.triangle.getRP(), Color.MAGENTA), t.triangle.pl, pix);
                //renderLine(epsilon, mind2s, new LineDouble(t.triangle.getPQ(), Color.YELLOW), pix);
                //renderLine(epsilon, mind2s, new LineDouble(t.triangle.getQR(), Color.CYAN), pix);
                //renderLine(epsilon, mind2s, new LineDouble(t.triangle.getRP(), Color.MAGENTA), pix);

                //renderLine(epsilon, new LineDouble(t.triangle.getPQ(), Color.BLACK), pix);
                //renderLine(epsilon, new LineDouble(t.triangle.getQR(), Color.BLUE), pix);
                //renderLine(epsilon, new LineDouble(t.triangle.getRP(), Color.GREEN), pix);
            }

            if (addGraticules) {

                double xmin = universe.envelope.getXMin();
                double xmax = universe.envelope.getXMax();
                double ymin = universe.envelope.getYMin();
                double ymax = universe.envelope.getYMax();
                double zmin = universe.envelope.getZMin();
                double zmax = universe.envelope.getZMax();

                // Render axes
                // x axis
                V3D_PointDouble x_min = new V3D_PointDouble(new V3D_VectorDouble(xmin, 0d, 0d));
                V3D_PointDouble x_max = new V3D_PointDouble(new V3D_VectorDouble(xmax, 0d, 0d));
                LineDouble xAxis = new LineDouble(new V3D_LineSegmentDouble(x_min, x_max), Color.BLUE);
                V3D_PointDouble xpoi = xAxis.l.l.getPointOfIntersection(this, epsilon);
                V3D_VectorDouble xpoin = new V3D_VectorDouble(xpoi, this);
                if (!xpoin.isZero()) {
                    V3D_PlaneDouble xpoinpl = new V3D_PlaneDouble(xpoi, xpoin);
                    renderLine(epsilon, mind2s, xAxis, xpoinpl, pix);
                }
                // y axis                
                V3D_PointDouble y_min = new V3D_PointDouble(new V3D_VectorDouble(0d, ymin, 0d));
                V3D_PointDouble y_max = new V3D_PointDouble(new V3D_VectorDouble(0d, ymax, 0d));
                LineDouble yAxis = new LineDouble(new V3D_LineSegmentDouble(y_min, y_max), Color.RED);
                V3D_PointDouble ypoi = yAxis.l.l.getPointOfIntersection(this, epsilon);
                V3D_VectorDouble ypoin = new V3D_VectorDouble(ypoi, this);
                if (!ypoin.isZero()) {
                    V3D_PlaneDouble ypoinpl = new V3D_PlaneDouble(ypoi, ypoin);
                    renderLine(epsilon, mind2s, yAxis, ypoinpl, pix);
                }
                // z axis
                V3D_PointDouble z_min = new V3D_PointDouble(new V3D_VectorDouble(0d, 0d, zmin));
                V3D_PointDouble z_max = new V3D_PointDouble(new V3D_VectorDouble(0d, 0d, zmax));
                LineDouble zAxis = new LineDouble(new V3D_LineSegmentDouble(z_min, z_max), Color.GREEN);
                V3D_PointDouble zpoi = zAxis.l.l.getPointOfIntersection(this, epsilon);
                V3D_VectorDouble zpoin = new V3D_VectorDouble(zpoi, this);
                if (!zpoin.isZero()) {
                    V3D_PlaneDouble zpoinpl = new V3D_PlaneDouble(zpoi, zpoin);
                    renderLine(epsilon, mind2s, zAxis, zpoinpl, pix);
                }
            }

            // Render corners
            for (var t : universe.triangles) {
                renderPoint(epsilon, mind2s, new PointDouble(t.triangle.getP(), Color.LIGHT_GRAY), pix);
                renderPoint(epsilon, mind2s, new PointDouble(t.triangle.getQ(), Color.LIGHT_GRAY), pix);
                renderPoint(epsilon, mind2s, new PointDouble(t.triangle.getR(), Color.LIGHT_GRAY), pix);
            }
        }
        //renderAxes(screenPlane, pq, qr, epsilon, universe, pix);
        return pix;
    }

    /**
     * For rendering a line on the image. Lines may be obscured by triangles and
     * each other. This will render the closest one.
     *
     * @param epsilon The tolerance for intersection.
     * @param l The line to render.
     * @param pix The image.
     */
    public void renderLine(double epsilon,
            HashMap<Grids_2D_ID_int, Double> mind2s, LineDouble l, V3D_PlaneDouble plane, int[] pix) {
        if (V3D_LineDouble.isCollinear(l.l.l, epsilon, this)) {
            // If the line segment is collinear with this, then render as a point.
            // Not sure which end is closer, so render both.
            renderPoint(epsilon, mind2s, new PointDouble(l.l.l.getP(), l.baseColor), pix);
            renderPoint(epsilon, mind2s, new PointDouble(l.l.l.getQ(), l.baseColor), pix);
        } else {
            V3D_TriangleDouble t = new V3D_TriangleDouble(l.l, this);
            V3D_FiniteGeometryDouble ti = screen.getIntersection(t, epsilon);
            if (ti != null) {
                if (ti instanceof V3D_PointDouble tip) {
                    renderPoint(epsilon, mind2s, new PointDouble(tip, l.baseColor), pix);
                } else {
                    V3D_LineSegmentDouble til = (V3D_LineSegmentDouble) ti;
                    // Calculate the row and column bounds.
                    int minr = getScreenRow(til.getP(), epsilon);
                    int minc = getScreenCol(til.getP(), epsilon);
                    int maxr = minr;
                    int maxc = minc;
                    int sr = getScreenRow(til.getQ(), epsilon);
                    int sc = getScreenCol(til.getQ(), epsilon);
                    minr = Math.min(minr, sr);
                    minc = Math.min(minc, sc);
                    maxr = Math.max(maxr, sr);
                    maxc = Math.max(maxc, sc);
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
                    for (int r = minr; r <= maxr; r++) {
                        for (int c = minc; c <= maxc; c++) {
                            V3D_RectangleDouble pixel = getPixel(screen.getPlane(), r, c);
                            //System.out.println("" + r + ", "
                            V3D_FiniteGeometryDouble pit = pixel.getIntersection(t, epsilon);
                            if (pit != null) {
                                Grids_2D_ID_int id = new Grids_2D_ID_int(r, c);
                                V3D_RayDouble ray = getRay(id);
                                V3D_GeometryDouble ri = ray.getIntersection(plane, epsilon);
                                double d2;
                                if (ri instanceof V3D_PointDouble rip) {
                                    d2 = rip.getDistanceSquared(this);
                                    Double d2p = mind2s.get(id);
                                    if (d2p == null) {
                                        mind2s.put(id, d2); // So closest things are at the front.
                                        render(pix, r, c, l.baseColor);
                                    } else {
                                        if (d2 <= d2p + epsilon) {
                                            mind2s.put(id, d2); // So closest things are at the front.
                                            render(pix, r, c, l.baseColor);
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
     * @param epsilon The tolerance for intersection.
     * @param p The point to render.
     * @param pl The plane of the point to render. The normal is the vector to
     * this.
     * @param pix The image.
     * @return pix
     */
    public void renderPoint(double epsilon, HashMap<Grids_2D_ID_int, Double> mind2s, PointDouble p, int[] pix) {
        if (!this.equals(epsilon, p.p)) {
            V3D_RayDouble ray = new V3D_RayDouble(this, p.p);
            V3D_PointDouble pt = (V3D_PointDouble) screen.getIntersection(ray, epsilon);
            if (pt != null) {
                int r = getScreenRow(pt, epsilon);
                int c = getScreenCol(pt, epsilon);
                Grids_2D_ID_int id = new Grids_2D_ID_int(r, c);
                double d2 = p.p.getDistanceSquared(this);
                Double d2p = mind2s.get(id);
                if (d2p == null) {
                    mind2s.put(id, d2);
                    render(pix, r, c, p.baseColor);
                } else {
                    if (d2 <= d2p + epsilon) {
                        mind2s.put(id, d2);
                        render(pix, r, c, p.baseColor);
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
     * Calculate the minimum distance squared from this triangle to the camera
     * point for those parts visible through the camera screen. These are stored
     * in mind2t. The mindOrderedTriangles map is updated so that the indexes of
     * the triangles are stored against the minimum distances. Additionally,
     * this sets the colour of the triangle based on the lighting vector and
     * ambientLight.
     *
     * @param centroid
     * @param index
     * @param ts
     * @param lighting
     * @param ambientLight
     * @param mindOrderedTriangles
     * @param mind2t
     * @param epsilon
     */
    private void process(V3D_PointDouble centroid, int index, TriangleDouble[] ts,
            V3D_VectorDouble lighting, double ambientLight,
            TreeMap<Double, Set<Integer>> mindOrderedTriangles,
            double[] mind2t, double epsilon) {
        ts[index].setLighting(centroid, lighting, ambientLight, epsilon);
        V3D_TriangleDouble t = ts[index].triangle;

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
        V3D_FiniteGeometryDouble i;
        if (t.isIntersectedBy(this, epsilon)) {
            i = screen.getIntersection(t, epsilon);
        } else {
            V3D_TetrahedronDouble tetra = new V3D_TetrahedronDouble(this, t, epsilon);
            i = tetra.getIntersection(screen, epsilon);
        }
        if (i == null) {
            return;
        }
        V3D_PointDouble[] pts = i.getPoints();
        V3D_PlaneDouble tpl = t.pl;
        double mind2 = Double.MAX_VALUE;
        for (var pt : pts) {
            if (!pt.equals(epsilon, this)) {
                V3D_RayDouble tpr = new V3D_RayDouble(this, pt);
                V3D_PointDouble poi = (V3D_PointDouble) tpr.getIntersection(tpl, epsilon);
                double d2 = poi.getDistanceSquared(this);
                mind2 = Math.min(d2, mind2);
            }
        }
        mind2t[index] = mind2;
        Generic_Collections.addToMap(mindOrderedTriangles, mind2t[index], index);
    }

    /**
     * Get the CellIDs of those cells that intersect with triangle.
     *
     * @param tIndex The triangle index.
     * @param t The triangle.
     * @param mind2t The minimum distance squared for each triangle and the
     * camera point.
     * @param mind2s The minimum distances squared of all triangles through each
     * pixel.
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

        V3D_TetrahedronDouble tetra = new V3D_TetrahedronDouble(this, t, epsilon);
        V3D_FiniteGeometryDouble tetrai = tetra.getIntersection(screen, epsilon);
        if (tetrai != null) {
            // Calculate the extent of the rows and columns the triangle is in.
            int minr = Integer.MAX_VALUE;
            int maxr = Integer.MIN_VALUE;
            int minc = Integer.MAX_VALUE;
            int maxc = Integer.MIN_VALUE;
            V3D_PointDouble[] tetraipts = tetrai.getPoints();
            for (var pt : tetraipts) {
                int r = getScreenRow(pt, epsilon);
                int c = getScreenCol(pt, epsilon);
                minr = Math.min(r, minr);
                maxr = Math.max(r, maxr);
                minc = Math.min(c, minc);
                maxc = Math.max(c, maxc);
            }
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
             * Loop over the extent, and where necessary, calculate the
             * intersection to determine if the triangle is in the pixel and if
             * the distance is a minimum distance.
             */
            for (int row = minr; row <= maxr; row++) {
                for (int col = minc; col <= maxc; col++) {
                    Grids_2D_ID_int id = new Grids_2D_ID_int(row, col);
                    Double mind2 = mind2s.get(id);
                    if (mind2 == null) {
                        try {
                            V3D_RayDouble ray = getRay(id);
                            V3D_GeometryDouble ti = t.getIntersection(ray, epsilon);
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
                                V3D_RayDouble ray = getRay(id);
                                V3D_GeometryDouble ti = t.getIntersection(ray, epsilon);
                                if (ti != null) {
                                    // Only render triangles that intersect the ray at a point and that are beyond the camera screen.
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
    }

    /**
     * Get the pixel that the ray intersects.
     *
     * @param p A point in the universe.
     * @return The ID of the screen cell that intersects a ray from this to p,
     * or {@code null}.
     */
    protected Grids_2D_ID_int getRC(V3D_PointDouble p, double epsilon) {
        if (this.equals(epsilon, p)) {
            return new Grids_2D_ID_int(
                    getScreenRow(p, epsilon),
                    getScreenCol(p, epsilon));
        }
        V3D_RayDouble ray = new V3D_RayDouble(this, p);
        //V3D_PointDouble px = (V3D_PointDouble) screen.getIntersection(ray, epsilon);
        V3D_PointDouble px = (V3D_PointDouble) ray.getIntersection(screen.getPlane(), epsilon);
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
        //double d = pq.getDistance(p, epsilon);
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
        //double d = qr.getDistance(p, epsilon);
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
            V3D_PointDouble rcpt = new V3D_PointDouble(p);
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
    public V3D_RectangleDouble getPixel(V3D_PlaneDouble pl, int row, int col) {
        // p
        V3D_PointDouble pP = new V3D_PointDouble(p);
        pP.translate(pqv.multiply(row).add(qrv.multiply(col)));
        // q
        V3D_PointDouble pQ = new V3D_PointDouble(p);
        pQ.translate(pqv.multiply(row + 1).add(qrv.multiply(col)));
        // r
        V3D_PointDouble pR = new V3D_PointDouble(p);
        pR.translate(pqv.multiply(row + 1).add(qrv.multiply(col + 1)));
        // s
        V3D_PointDouble pS = new V3D_PointDouble(p);
        pS.translate(pqv.multiply(row).add(qrv.multiply(col + 1)));
        return new V3D_RectangleDouble(pP, pQ, pR, pS);
    }
}
