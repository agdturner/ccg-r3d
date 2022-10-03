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
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * For visualising a triangle.
 * 
 * @author Andy Turner
 */
public class Triangle {

    private static final Math_BigRational AmbientLight = Math_BigRational.ONE.divide(Math_BigRational.valueOf(20));
    public V3D_Triangle triangle;
    public Color baseColor;
    public Color lightingColor;
    

    public Triangle(V3D_Triangle t, Color baseColor) {
        this.triangle = t;
        this.baseColor = baseColor;
        this.lightingColor = baseColor;
    }

    public void setLighting(V3D_Vector lightVector) {
        V3D_Vector n = triangle.p.getN(triangle.e.oom, triangle.e.rm).getUnitVector(triangle.e.oom, triangle.e.rm);
        Math_BigRational dot = n.getDotProduct(lightVector, triangle.e.oom, triangle.e.rm);
        Math_BigRational dot2 = dot.multiply(dot);
        if (dot.compareTo(Math_BigRational.ZERO) == -1) {
            dot2 = dot2.negate();
        }
        dot2 = (dot2.add(Math_BigRational.ONE)).divide(Math_BigRational.TWO.multiply(Math_BigRational.ONE.subtract(AmbientLight)));
        double lightRatio = Math.min(1.0d, Math.max(0.0d, AmbientLight.add(dot2).doubleValue()));
        this.updateLightingColor(lightRatio);
    }

    private void updateLightingColor(double lightRatio) {
        int red = (int) (this.baseColor.getRed() * lightRatio);
        int green = (int) (this.baseColor.getGreen() * lightRatio);
        int blue = (int) (this.baseColor.getBlue() * lightRatio);
        this.lightingColor = new Color(red, green, blue);
    }
}
