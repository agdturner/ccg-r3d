/*
 * Copyright 2022 Centre for Computational Geography.
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
package uk.ac.leeds.ccg.r3d.entities;

import java.awt.Color;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;
//import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * For visualising a tetrahedron.
 * 
 * @author Andy Turner
 */
public class Tetrahedron {

    private static final Math_BigRational AmbientLight = Math_BigRational.ONE.divide(Math_BigRational.valueOf(20));
    //public V3D_Tetrahedron tetrahedron;
    public Triangle[] triangles;

    public Tetrahedron(V3D_Tetrahedron t, Color baseColor) {
        //tetrahedron = t;
        triangles = new Triangle[4];
        triangles[0] = new Triangle(t.getPqr(), baseColor);
        triangles[1] = new Triangle(t.getPsq(), baseColor);
        triangles[2] = new Triangle(t.getQsr(), baseColor);
        triangles[3] = new Triangle(t.getSpr(), baseColor);
    }

    public void setLighting(V3D_Vector lightVector) {
        for (int i = 0; i < 4; i++) {
            V3D_Vector n = triangles[i].triangle.p.getN(triangles[0].triangle.e.oom);
            Math_BigRational dot = n.getDotProduct(lightVector, triangles[i].triangle.e.oom);
            Math_BigRational dot2 = dot.multiply(dot);
            if (dot.compareTo(Math_BigRational.ZERO) == -1) {
                dot2 = dot2.negate();
            }
            dot2 = (dot2.add(Math_BigRational.ONE)).divide(Math_BigRational.TWO.multiply(Math_BigRational.ONE.subtract(AmbientLight)));
            double lightRatio = Math.min(1.0d, Math.max(0.0d, AmbientLight.add(dot2).doubleValue()));
            this.updateLightingColor(i, lightRatio);
        }
    }

    private void updateLightingColor(int i, double lightRatio) {
        int red = (int) (triangles[i].baseColor.getRed() * lightRatio);
        int green = (int) (triangles[i].baseColor.getGreen() * lightRatio);
        int blue = (int) (triangles[i].baseColor.getBlue() * lightRatio);
        triangles[i].lightingColor = new Color(red, green, blue);
    }
}
