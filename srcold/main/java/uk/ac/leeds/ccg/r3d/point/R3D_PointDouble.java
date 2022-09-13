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

/**
 * 3D Point with coordinates represented by double values.
 *
 * @author Andy Turner
 */
public class R3D_PointDouble {

    /**
     * The origin.
     */
    public final static R3D_PointDouble origin = new R3D_PointDouble(0.0d, 0.0d,
            0.0d);

    /**
     * The x coordinate.
     */
    public double x;

    /**
     * The y coordinate.
     */
    public double y;

    /**
     * The z coordinate.
     */
    public double z;

    /**
     * Create a new instance.
     *
     * @param x What {@link #x} is set to.
     * @param y What {@link #y} is set to.
     * @param z What {@link #z} is set to.
     */
    public R3D_PointDouble(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Calculate and return the distance between p1 and p2.
     *
     * @param p1 A point.
     * @param p2 Another point.
     * @return distance between p1 and p2.
     */
    public static double dist(R3D_PointDouble p1, R3D_PointDouble p2) {
        double x2 = Math.pow(p1.x - p2.x, 2);
        double y2 = Math.pow(p1.y - p2.y, 2);
        double z2 = Math.pow(p1.z - p2.z, 2);
        return Math.sqrt(x2 + y2 + z2);
    }
}
