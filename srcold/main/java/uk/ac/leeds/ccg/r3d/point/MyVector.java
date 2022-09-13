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

public class MyVector {

    public double x, y, z;

    public MyVector() {
        this.x = this.y = this.z = 0;
    }

    public MyVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MyVector(R3D_PointDoubleOffset p1, R3D_PointDoubleOffset p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
        this.z = p2.z - p1.z;
    }

    public static double dot(MyVector v1, MyVector v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static MyVector cross(MyVector v1, MyVector v2) {
        return new MyVector(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x);
    }

    public static MyVector normalize(MyVector v) {
        double magnitude = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        return new MyVector(v.x / magnitude, v.y / magnitude, v.z / magnitude);
    }

}
