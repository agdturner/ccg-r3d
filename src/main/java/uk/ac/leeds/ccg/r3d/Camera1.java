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
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.grids.d2.Grids_2D_ID_int;
import uk.ac.leeds.ccg.math.number.Math_BigRationalSqrt;
import uk.ac.leeds.ccg.r3d.entities.Line;
import uk.ac.leeds.ccg.r3d.entities.Point;
import uk.ac.leeds.ccg.r3d.entities.Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Geometry;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;
import uk.ac.leeds.ccg.v3d.geometry.V3D_AABB;
import uk.ac.leeds.ccg.v3d.geometry.V3D_FiniteGeometry;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Line;
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
public class Camera1 extends V3D_Point {

    private static final long serialVersionUID = 1L;

    /**
     * Lower left corner point of screen.
     */
    public final V3D_Point p;

    /**
     * The screen pq.
     */
    public V3D_LineSegment pq;

    /**
     * The screen qr.
     */
    public V3D_LineSegment qr;

    /**
     * The screen plane.
     */
    public V3D_Plane screenPlane;

    /**
     * The screen.
     */
    public V3D_Rectangle screen;

    /**
     * A vector pointing to the right of the screen.
     */
    //V3D_Vector vd;
    /**
     * A vector of pixel magnitude in the direction to right of screen.
     */
    public V3D_Vector qrv;

    /**
     * A vector pointing to the top of the screen.
     */
    //V3D_Vector v2d;
    /**
     * A vector of pixel magnitude in the direction to top of screen.
     */
    public V3D_Vector pqv;

    /**
     * The screen width.
     */
    public BigRational screenHeight;

    /**
     * The screen width.
     */
    public BigRational screenWidth;

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
    public HashMap<Grids_2D_ID_int, V3D_Ray> rays;

    /**
     * For storing the length and width of a pixel (which is square).
     */
    BigRational pixelSize;

    /**
     * Create a new instance. (N.B. There is a lot of waste
     *
     * @param p The camera observer location.
     * @param screen The screen.
     */
    public Camera1(V3D_Point pt, V3D_AABB ve, int width, int height, int oom,
            RoundingMode rm) throws Exception {
        super(pt);
        System.out.println("Initialise Camera.");
        // Need something orthoganol to pt and ve centroid
        V3D_Plane pl = new V3D_Plane(pt, new V3D_Vector(pt, ve.getCentroid(oom, rm), oom, rm));
        V3D_Vector pv = pl.getPV(oom, rm);
        // Initialise the screen
        //screen = ve.getViewport2(pt, pv, oom, rm);
        screen = ve.getViewport3(pt, pv, oom, rm);
        screenPlane = screen.getPl(oom, rm);
        int dim = Math.max(width, height);
        nrows = dim;
        ncols = dim;
        V3D_Triangle pqr = screen.getPQR();
        p = pqr.getPQ(oom, rm).l.getP();
        pq = pqr.getPQ(oom - 6, rm);
        pqv = new V3D_Vector(pq.l.v).divide(
                BigRational.valueOf(nrows), oom, rm);
        screenHeight = pq.getLength(oom - 6, rm).getSqrt(oom - 6, rm);
        V3D_Triangle rsp = screen.getRSP();
        qr = rsp.getQR(oom, rm);
        screenWidth = qr.getLength(oom - 6, rm).getSqrt(oom - 6, rm);
        pixelSize = screenWidth.divide(width);
        qrv = new V3D_Vector(qr.l.v).divide(
                BigRational.valueOf(ncols), oom, rm).reverse();
        rays = new HashMap<>();
        System.out.println("Initialised Camera");
    }

    /**
     * Creates an image map of the universe.
     *
     * @param universe
     * @param lighting The lighting vector.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return An image map.
     * @throws Exception
     */
    int[] render(Universe universe, V3D_Vector lighting,
            BigRational ambientLight, boolean castShadow, int oom,
            RoundingMode rm) throws Exception {
        int oomn4 = oom - 4;
        // Collect all the triangles from the triangles and tetrahedra.
        int ntriangles = universe.triangles.size();
        int n = ncols * nrows;
        int[] pix = new int[n];
        int nt = ntriangles + (4 * universe.tetrahedra.size());
        if (nt > 0) {
            Triangle[] ts = new Triangle[nt];
            for (int i = 0; i < ntriangles; i++) {
                ts[i] = universe.triangles.get(i);
            }
            for (int i = 0; i < universe.tetrahedra.size(); i++) {
                System.arraycopy(universe.tetrahedra.get(i).triangles, 0, ts, (i * 4) + ntriangles, 4);
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
//            tetras[i] = new V3D_Tetrahedron(this, ts[i].triangle, oom, rm);
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
            TreeMap<BigRational, Set<Integer>> mindOrderedTriangles
                    = new TreeMap<>();
            BigRational[] mind2t = new BigRational[ts.length];
            V3D_Point centroid = universe.aabb.getCentroid(oom, rm);
            process(centroid, 0, ts, lighting, ambientLight, mindOrderedTriangles, mind2t, oom, rm);
            for (int i = 1; i < ts.length; i++) {
                if (i % 100 == 0) {
                    System.out.println("Triangle " + i + " out of " + ts.length);
                }
                process(centroid, i, ts, lighting, ambientLight, mindOrderedTriangles, mind2t, oom, rm);
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
            V3D_Vector lightingr = lighting.reverse();
            
            HashMap<Grids_2D_ID_int, BigRational> mind2s = new HashMap<>();
            HashMap<Grids_2D_ID_int, Integer> closestIndex = new HashMap<>();
            /**
             * idPoint is used to store the point of intersection for the
             * triangle closest to the camera. This is later used to see if that
             * point on the triangle is in shadow
             */
            HashMap<Grids_2D_ID_int, V3D_Point> idPoint = new HashMap<>();
            int j = 0;
            for (BigRational mind2 : mindOrderedTriangles.keySet()) {
                Set<Integer> triangleIndexes = mindOrderedTriangles.get(mind2);
                for (var i : triangleIndexes) {
                    if (j % 100 == 0) {
                        System.out.println("Triangle " + (j + 1) + " out of " + ts.length + ":");
                    }
                    processTriangle(i,
                            ts[i].triangle, mind2t, mind2s, closestIndex, idPoint,
                            oomn4, rm);
                    j++;
                }
            }
            // Render each pixel, apply shadow and flip upside down for an image.
            System.out.println("Render the closest triangles applying shadow.");
            int pixelsToPop = closestIndex.size();
            if (castShadow) {
                int pixel = 0;
                for (var x : closestIndex.keySet()) {
                    if (pixel % 100 == 0) {
                        System.out.println("Rendering pixel " + pixel + " out of " + pixelsToPop);
                    }
                    pixel++;
                    int r = nrows - x.getRow() - 1;
                    int in = (r * ncols) + x.getCol();
                    int ci = closestIndex.get(x);
                    Triangle t = ts[ci];
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
                    V3D_Ray ray = new V3D_Ray(idPoint.get(x), lightingr);
                    for (int i = 0; i < universe.triangles.size(); i++) {
                        if (i != ci) {
                            if (universe.triangles.get(i).triangle.getIntersect(ray, oomn4, rm) != null) {
                                rgb = t.ambientColor.getRGB();
                                break;
                            }
                        }
                    }
                    pix[in] = rgb;
                }
            } else {
                int pixel = 0;
                for (var x : closestIndex.keySet()) {
                    if (pixel % 100 == 0) {
                        System.out.println("Rendering pixel " + pixel + " out of " + n);
                    }
                    pixel++;
                    int ci = closestIndex.get(x);
                    Triangle t = ts[ci];
                    render(pix, x.getRow(), x.getCol(), t.lightingColor);
                }
            }
            
            // Render triangle edges
            // Render edges
            for (var t : universe.triangles) {
                V3D_Plane tp = t.triangle.getPl(oom, rm);
                renderLine(mind2s, new Line(t.triangle.getPQ(oom, rm), Color.YELLOW), tp, pix, oom, rm);
                renderLine(mind2s, new Line(t.triangle.getQR(oom, rm), Color.CYAN), tp, pix, oom, rm);
                renderLine(mind2s, new Line(t.triangle.getRP(oom, rm), Color.MAGENTA), tp, pix, oom, rm);
                //renderLine(epsilon, mind2s, new LineDouble(t.triangle.getPQ(), Color.YELLOW), pix);
                //renderLine(epsilon, mind2s, new LineDouble(t.triangle.getQR(), Color.CYAN), pix);
                //renderLine(epsilon, mind2s, new LineDouble(t.triangle.getRP(), Color.MAGENTA), pix);

                //renderLine(epsilon, new LineDouble(t.triangle.getPQ(), Color.BLACK), pix);
                //renderLine(epsilon, new LineDouble(t.triangle.getQR(), Color.BLUE), pix);
                //renderLine(epsilon, new LineDouble(t.triangle.getRP(), Color.GREEN), pix);
            }
            // Render corners
            for (var t : universe.triangles) {
                renderPoint(mind2s, new Point(t.triangle.getP(oom, rm), Color.LIGHT_GRAY), pix, oom, rm);
                renderPoint(mind2s, new Point(t.triangle.getQ(oom, rm), Color.LIGHT_GRAY), pix, oom, rm);
                renderPoint(mind2s, new Point(t.triangle.getR(oom, rm), Color.LIGHT_GRAY), pix, oom, rm);
            }
        }
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
    public void renderLine(HashMap<Grids_2D_ID_int, BigRational> mind2s, Line l, 
            V3D_Plane plane, int[] pix, int oom, RoundingMode rm) {
        if (V3D_Line.isCollinear(oom, rm, l.l.l, this)) {
            // If the line segment is collinear with this, then render as a point.
            // Not sure which end is closer, so render both.
//            renderPoint(mind2s, new Point(l.l.l.getP(), l.baseColor), pix, oom, rm);
//            renderPoint(mind2s, new Point(l.l.l.getQ(oom, rm), l.baseColor), pix, oom, rm);
            renderPoint(mind2s, new Point(l.l.getP(), l.baseColor), pix, oom, rm);
            renderPoint(mind2s, new Point(l.l.getQ(oom, rm), l.baseColor), pix, oom, rm);
        } else {
            //V3D_Triangle t = new V3D_Triangle(l.l.l.getP(), l.l.l.getQ(oom, rm), this, oom, rm);
            V3D_Triangle t = new V3D_Triangle(l.l.getP(), l.l.getQ(oom, rm), this, oom, rm);
            V3D_FiniteGeometry ti = screen.getIntersect(t, oom, rm);
            if (ti != null) {
                if (ti instanceof V3D_Point tip) {
                    renderPoint(mind2s, new Point(tip, l.baseColor), pix, oom, rm);
                } else {
                    V3D_LineSegment til = (V3D_LineSegment) ti;
                    // Calculate the row and column bounds.
                    int minr = getScreenRow(til.getP(), oom, rm);
                    int minc = getScreenCol(til.getP(), oom, rm);
                    int maxr = minr;
                    int maxc = minc;
                    int sr = getScreenRow(til.getQ(oom, rm), oom, rm);
                    int sc = getScreenCol(til.getQ(oom, rm), oom, rm);
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
                            V3D_Rectangle pixel = getPixel(screenPlane, r, c, oom, rm);
                            //System.out.println("" + r + ", "
                            V3D_FiniteGeometry pit = pixel.getIntersect(t, oom, rm);
                            if (pit != null) {
                                Grids_2D_ID_int id = new Grids_2D_ID_int(r, c);
                                V3D_Ray ray = getRay(id, oom, rm);
                                V3D_Geometry ri = ray.getIntersect(plane, oom, rm);
                                BigRational d2;
                                if (ri instanceof V3D_Point rip) {
                                    d2 = rip.getDistanceSquared(this, oom, rm);
                                    BigRational d2p = mind2s.get(id);
                                    if (d2p == null) {
                                        mind2s.put(id, d2); // So closest things are at the front.
                                        render(pix, r, c, l.baseColor);
                                    } else {
                                        //if (d2 <= d2p + epsilon) {
                                        if (d2.compareTo(d2p) != 1) {
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
    public void renderPoint(HashMap<Grids_2D_ID_int, BigRational> mind2s, Point p,
            int[] pix, int oom, RoundingMode rm) {
        if (!this.equals(p.p, oom, rm)) {
            V3D_Ray ray = new V3D_Ray(this, p.p, oom, rm);
            V3D_Point pt = (V3D_Point) screen.getIntersect(ray, oom, rm);
            if (pt != null) {
                int r = getScreenRow(pt, oom, rm);
                int c = getScreenCol(pt, oom, rm);
                Grids_2D_ID_int id = new Grids_2D_ID_int(r, c);
                BigRational d2 = p.p.getDistanceSquared(this, oom, rm);
                BigRational d2p = mind2s.get(id);
                if (d2p == null) {
                    mind2s.put(id, d2);
                    render(pix, r, c, p.baseColor);
                } else {
                    //if (d2 <= d2p + epsilon) {
                    if (d2.compareTo(d2p) != 1) {
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

    private void process(V3D_Point centroid, int index, Triangle[] ts,
            V3D_Vector lighting, BigRational ambientLight,
            TreeMap<BigRational, Set<Integer>> mindOrderedTriangles,
            BigRational[] mind2t, int oom, RoundingMode rm) {
        ts[index].setLighting(centroid, lighting, ambientLight, oom, rm);
        mind2t[index] = ts[index].triangle.getDistanceSquared(this, oom - 4, rm);
        Generic_Collections.addToMap(mindOrderedTriangles, mind2t[index], index);
    }

    /**
     * Get the CellIDs of those cells that intersect with triangle.
     *
     * @param t The triangle.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    protected void processTriangle(int tIndex, V3D_Triangle t,
            BigRational[] mind2t,
            HashMap<Grids_2D_ID_int, BigRational> mind2s,
            HashMap<Grids_2D_ID_int, Integer> closestIndex,
            HashMap<Grids_2D_ID_int, V3D_Point> idPoint,
            int oom,
            RoundingMode rm) {
        //int oomn4 = oom - 4;
        // Calculate the extent of the rows and columns the triangle is in.
        V3D_Ray ray;
        ray = new V3D_Ray(this, t.getP(oom, rm), oom, rm);
        Grids_2D_ID_int prc = getRC(ray, oom, rm);
        ray = new V3D_Ray(this, t.getQ(oom, rm), oom, rm);
        Grids_2D_ID_int qrc = getRC(ray, oom, rm);
        ray = new V3D_Ray(this, t.getR(oom, rm), oom, rm);
        Grids_2D_ID_int rrc = getRC(ray, oom, rm);
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
        /**
         * Loop over the extent, and where necessary, calculate the intersection
         * to determine if the triangle is in the cell and if the distance is a
         * minimum distance.
         */
        for (int row = (int) minRowIndex; row < maxRowIndex + 1; row++) {
            //for (int row = 0; row < 100; row++) {
            for (int col = (int) minColIndex; col < maxColIndex + 1; col++) {
                Grids_2D_ID_int id = new Grids_2D_ID_int(row, col);
                BigRational mind2 = mind2s.get(id);
                if (mind2 == null) {
                    try {
                        V3D_Geometry ti = t.getIntersect(getRay(id, oom, rm), oom, rm);
                        if (ti != null) {
                            // Only render triangles that intersect the ray at a point.
                            if (ti instanceof V3D_Point tip) {
                                BigRational d2 = tip.getDistanceSquared(this, oom, rm);
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
                    if (mind2t[tIndex].compareTo(mind2) != 1) {
                        try {
                            V3D_Geometry ti = t.getIntersect(getRay(id, oom, rm), oom, rm);
                            if (ti != null) {
                                // Only render triangles that intersect the ray at a point.
                                if (ti instanceof V3D_Point tip) {
                                    BigRational d2 = tip.getDistanceSquared(this, oom, rm);
                                    if (d2.compareTo(mind2) == -1) {
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
     * @param pl The screen plane.
     * @param ray The ray to intersect with the screen.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The IDs of the screen cells that the ray intersects.
     */
    protected Grids_2D_ID_int getRC(V3D_Ray ray, int oom,
            RoundingMode rm) {
        V3D_Point sp = (V3D_Point) ray.getIntersect(screenPlane, oom, rm);
        return new Grids_2D_ID_int(getScreenRow(sp, oom, rm),
                getScreenCol(sp, oom, rm));
    }

    /**
     * Calculate and return the row index of the screen that pv is on.
     *
     * @param p The point on the screen.
     * @param pq The line segment from of the top or bottom of the screen.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The row index of the screen that ray passes through.
     */
    //protected int getScreenRow(V3D_Point p, V3D_LineSegment pq, int oom, RoundingMode rm) {
    protected int getScreenRow(V3D_Point p, int oom, RoundingMode rm) {
        //V3D_Point px = pq.l.getPointOfIntersect(p, oom, rm);
        V3D_Point px = qr.l.getPointOfIntersect(p, oom, rm);
        BigRational d = new Math_BigRationalSqrt(px.getDistanceSquared(
                p, oom, rm), oom, rm).getSqrt(oom, rm);
        return d.divide(pixelSize).intValue();
    }

    /**
     * Calculate and return the column index of the screen that pv is on.
     *
     * @param p The point on the screen.
     * @param qr The line segment from of the left or right the screen.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     * @return The column index of the screen that l passes through.
     */
    //protected int getScreenCol(V3D_Point p, V3D_LineSegment qr, int oom, RoundingMode rm) {
    protected int getScreenCol(V3D_Point p, int oom, RoundingMode rm) {
        //V3D_Point py = qr.l.getPointOfIntersect(p, oom, rm);
        V3D_Point py = pq.l.getPointOfIntersect(p, oom, rm);
        BigRational d = new Math_BigRationalSqrt(py.getDistanceSquared(
                p, oom, rm), oom, rm).getSqrt(oom, rm);
        return d.divide(pixelSize).intValue();
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
            //V3D_Vector rv = v2d.multiply(BigRational.valueOf(id.getRow()), oom, rm);
            V3D_Vector rv = pqv.multiply(BigRational.valueOf(id.getRow()), oom, rm);
            //V3D_Vector cv = vd.multiply(BigRational.valueOf(id.getCol()), oom, rm);
            V3D_Vector cv = qrv.multiply(BigRational.valueOf(id.getCol()), oom, rm);
            V3D_Point rcpt = new V3D_Point(screen.getP(oom, rm));
            rcpt.translate(rv.add(cv, oom, rm), oom, rm);
            r = new V3D_Ray(this, rcpt, oom, rm);
            rays.put(id, r);
        }
        return r;
    }

    /**
     * @param row The row index for the pixel returned.
     * @param col The column index for the pixel returned.
     * @return The pixel rectangle.
     */
    public V3D_Rectangle getPixel(V3D_Plane pl, int row, int col, int oom,
            RoundingMode rm) {
        // p
        V3D_Point pP = new V3D_Point(p);
        pP.translate(pqv.multiply(row, oom, rm).add(qrv.multiply(col, oom, rm), oom, rm), oom, rm);
        // q
        V3D_Point pQ = new V3D_Point(p);
        pQ.translate(pqv.multiply(row + 1, oom, rm).add(qrv.multiply(col, oom, rm), oom, rm), oom, rm);
        // r
        V3D_Point pR = new V3D_Point(p);
        pR.translate(pqv.multiply(row + 1, oom, rm).add(qrv.multiply(col + 1, oom, rm), oom, rm), oom, rm);
        // s
        V3D_Point pS = new V3D_Point(p);
        pS.translate(pqv.multiply(row, oom, rm).add(qrv.multiply(col + 1, oom, rm), oom, rm), oom, rm);
        return new V3D_Rectangle(pP, pQ, pR, pS, oom, rm);
    }
}
