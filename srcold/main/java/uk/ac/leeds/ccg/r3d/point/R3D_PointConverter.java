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
package uk.ac.leeds.ccg.r3d.point;

import java.awt.Point;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.math.number.Math_BigRationalSqrt;

import uk.ac.leeds.ccg.r3d.Display;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class R3D_PointConverter {

//    private static Math_BigRational scale = Math_BigRational.valueOf(4);
//    private static final Math_BigRational ZoomFactor = Math_BigRational.valueOf(10, 12);
//
//    public static void zoomIn() {
//        scale = scale.multiply(ZoomFactor);
//    }
//
//    public static void zoomOut() {
//        scale = scale.divide(ZoomFactor);
//    }

    private static double scale = 4;
    private static final double ZoomFactor = 1.2;

    public static void zoomIn() {
        scale *= ZoomFactor;
    }

    public static void zoomOut() {
        scale /= ZoomFactor;
    }
    
    /**
     * Convert a 3D point into a 2D point.
     * @param point The 3D point to convert.
     * @return The 2D screen point.
     */
    public static Point convertPoint(R3D_PointDoubleOffset point3D) {
        double x3d = point3D.getAdjustedY() * scale;
        double y3d = point3D.getAdjustedZ() * scale;
        double depth = point3D.getAdjustedX() * scale;
        double[] newVal = scale(x3d, y3d, depth);
        int x2d = (int) (Display.WIDTH / 2 + newVal[0]);
        int y2d = (int) (Display.HEIGHT / 2 - newVal[1]);
        Point point2D = new Point(x2d, y2d);
        return point2D;
//        V3D_Point p = point.get(scale, oom);
//        Math_BigRational x3d = p.y;
//        Math_BigRational y3d = p.z;
//        Math_BigRational depth = p.x;
//        Math_BigRational[] newVal = scale(x3d, y3d, depth, oom);
//        int x2d = Display.WIDTH / 2 + newVal[0].intValue();
//        int y2d = Display.HEIGHT / 2 - newVal[1].intValue();
//        return new Point(x2d, y2d);
    }

    /**
     * Adds depth perception.
     * @param x3d
     * @param y3d
     * @param depth
     * @return 
     */
    private static double[] scale(double x3d, double y3d, double depth) {
        double dist = Math.sqrt(x3d * x3d + y3d * y3d);
        double theta = Math.atan2(y3d, x3d);
        double depth2 = 15 - depth;
        double localScale = Math.abs(1400 / (depth2 + 1400));
        dist *= localScale;
        double[] newVal = new double[2];
        newVal[0] = dist * Math.cos(theta);
        newVal[1] = dist * Math.sin(theta);
        return newVal;
    }
    
//    /**
//     * For depth perception.
//     * @param x3d
//     * @param y3d
//     * @param depth
//     * @param oom The Order of Magnitude for the precision.
//     * @return 
//     */
//    private static Math_BigRational[] scale(Math_BigRational x3d, 
//            Math_BigRational y3d, Math_BigRational depth, int oom) {
//        //double dist = new Math_BigRationalSqrt(x3d.pow(2).add(y3d.pow(2)), oom).getSqrt(oom).toBigDecimal(oom).doubleValue();
//        double x = x3d.toBigDecimal(oom).doubleValue();
//        double y = y3d.toBigDecimal(oom).doubleValue();        
//        double dist = Math.sqrt(x * x + y * y);
//        double theta = Math.atan2(y, x);
//        //BigDecimal theta = BigDecimalMath.atan2(y3d.toBigDecimal(oom), x3d.toBigDecimal(oom), mc);
//        double depth2 = 15 - depth.toBigDecimal(oom).doubleValue(); 
//        double localScale = Math.abs(1400 / (depth2 + 1400));
//        dist *= localScale;
//        Math_BigRational[] newVal = new Math_BigRational[2];
//        newVal[0] = Math_BigRational.valueOf(dist * Math.cos(theta));
//        newVal[1] = Math_BigRational.valueOf(dist * Math.sin(theta));
//        return newVal;
//    }

    public static void rotateAxisX(R3D_Point p, R3D_PointDoubleOffset pd, double degrees, int oom) {
        if (p instanceof R3D_MorphPoint) {
            R3D_MorphPoint morphPoint = (R3D_MorphPoint) p;
            rawRotateAxisX(morphPoint.p1, pd, degrees, oom);
            rawRotateAxisX(morphPoint.p2, pd, degrees, oom);
        }
        rawRotateAxisX(p, pd, degrees, oom);
    }

    public static void rotateAxisY(R3D_Point p, R3D_PointDoubleOffset pd, double degrees, int oom) {
        if (p instanceof R3D_MorphPoint) {
            R3D_MorphPoint morphPoint = (R3D_MorphPoint) p;
            rawRotateAxisY(morphPoint.p1, pd, degrees, oom);
            rawRotateAxisY(morphPoint.p2, pd, degrees, oom);
        }
        rawRotateAxisY(p, pd, degrees, oom);
    }

    public static void rotateAxisZ(R3D_Point p, R3D_PointDoubleOffset pd, double degrees, int oom) {
        if (p instanceof R3D_MorphPoint) {
            R3D_MorphPoint morphPoint = (R3D_MorphPoint) p;
            rawRotateAxisZ(morphPoint.p1, pd, degrees, oom);
            rawRotateAxisZ(morphPoint.p2, pd, degrees, oom);
        }
        rawRotateAxisZ(p, pd, degrees, oom);
    }

    private static void rawRotateAxisX(R3D_Point p, R3D_PointDoubleOffset pd, double degrees, int oom) {
        double radius = Math.sqrt(pd.y * pd.y + pd.z * pd.z);
        double theta = Math.atan2(pd.z, pd.y);
        theta -= 2 * Math.toRadians(degrees);
//        p.v = new V3D_Vector(
//                p.v.getDX(oom),
//                p.v.getDY(oom).add(Math_BigRational.valueOf(radius * Math.cos(theta))),
//                p.v.getDZ(oom).add(Math_BigRational.valueOf(radius * Math.sin(theta))),
//                oom);
        p.v = new V3D_Vector(
                p.v.getDX(),
                Math_BigRational.valueOf(radius * Math.cos(theta)),
                Math_BigRational.valueOf(radius * Math.sin(theta)),
                oom);
    }

    private static void rawRotateAxisY(R3D_Point p, R3D_PointDoubleOffset pd, double degrees, int oom) {
        double radius = Math.sqrt(pd.x * pd.x + pd.z * pd.z);
        double theta = Math.atan2(pd.x, pd.z);
        theta -= 2 * Math.toRadians(degrees);
//        p.v = new V3D_Vector(
//                p.v.getDX(oom).add(Math_BigRational.valueOf(radius * Math.sin(theta))),
//                p.v.getDY(oom),
//                p.v.getDZ(oom).add(Math_BigRational.valueOf(radius * Math.cos(theta))),
//                oom);
        p.v = new V3D_Vector(
                Math_BigRational.valueOf(radius * Math.sin(theta)),
                p.v.getDY(),
                Math_BigRational.valueOf(radius * Math.cos(theta)),
                oom);
    }

    private static void rawRotateAxisZ(R3D_Point p, R3D_PointDoubleOffset pd, double degrees, int oom) {
        double radius = Math.sqrt(pd.y * pd.y + pd.x * pd.x);
        double theta = Math.atan2(pd.y, pd.x);
        theta -= 2 * Math.toRadians(degrees);
        p.v = new V3D_Vector(
                p.v.getDX(oom).add(Math_BigRational.valueOf(radius * Math.cos(theta))),
                p.v.getDY(oom).add(Math_BigRational.valueOf(radius * Math.sin(theta))),
                p.v.getDZ(),
                oom);
//        p.v = new V3D_Vector(
//                Math_BigRational.valueOf(radius * Math.cos(theta)),
//                Math_BigRational.valueOf(radius * Math.sin(theta)),
//                p.v.getDZ(),
//                oom);
    }

}
