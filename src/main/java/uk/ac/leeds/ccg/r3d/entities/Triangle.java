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
import java.math.RoundingMode;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * For visualising a triangle.
 *
 * @author Andy Turner
 */
public class Triangle {

    /**
     * AmbientLight is so that non black surfaces are not completely black even
     * if they are orientated opposite to the lighting vector.
     */
    private static final Math_BigRational AmbientLight = 
            Math_BigRational.ONE.divide(Math_BigRational.valueOf(20));
    
    /**
     * The triangle geometry
     */
    public V3D_Triangle triangle;

    /**
     * The base colour of the triangle.
     */
    public Color baseColor;

    /**
     * The colour of the triangle given some lighting conditions.
     */
    public Color lightingColor;

    /**
     * Create a new instance
     * @param triangle What {@link #triangle} is set to.
     * @param baseColor What {@link #baseColor} is set to.
     */
    public Triangle(V3D_Triangle triangle, Color baseColor) {
        this.triangle = triangle;
        this.baseColor = baseColor;
        this.lightingColor = baseColor;
    }

    /**
     * Used to update {@link #lightingColor} based on the input lightVector. A
     * triangle facing the vector will be bright. One facing away will be 
     * darker. This helps give a 3D like effect to rendering.
     * @param lightVector The direction that light is coming from.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public void setLighting(V3D_Vector lightVector, int oom, RoundingMode rm) {
        V3D_Vector n = triangle.pl.n.getUnitVector(oom, rm);
        Math_BigRational dot = n.getDotProduct(lightVector, oom, rm);
        Math_BigRational dot2 = dot.multiply(dot);
        if (dot.compareTo(Math_BigRational.ZERO) == -1) {
            dot2 = dot2.negate();
        }
        dot2 = (dot2.add(Math_BigRational.ONE)).divide(
                Math_BigRational.TWO.multiply(
                        Math_BigRational.ONE.subtract(AmbientLight)));
        double lightRatio = Math.min(1.0d, Math.max(0.0d,
                AmbientLight.add(dot2).doubleValue()));
        int red = (int) (baseColor.getRed() * lightRatio);
        int green = (int) (baseColor.getGreen() * lightRatio);
        int blue = (int) (baseColor.getBlue() * lightRatio);
        lightingColor = new Color(red, green, blue);
    }
}
