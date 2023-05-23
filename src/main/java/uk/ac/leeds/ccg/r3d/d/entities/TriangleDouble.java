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
package uk.ac.leeds.ccg.r3d.d.entities;

import java.awt.Color;
import uk.ac.leeds.ccg.r3d.entities.Entity;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_PointDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_TriangleDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_VectorDouble;
import uk.ac.leeds.ccg.v3d.geometry.d.light.V3D_VDouble;

/**
 * For visualising a triangle.
 *
 * @author Andy Turner
 */
public class TriangleDouble extends Entity {

    /**
     * The triangle geometry
     */
    public V3D_TriangleDouble triangle;

    /**
     * The normal as read in from for example an STL file. The normal can be
     * computed from the geometry using the order of the points and the right
     * hand rule. However, often the normal is provided. The direction of the
     * normal vector allows us to specify sides of the triangle which can be
     * attributed with different properties e.g. colours.
     */
    public V3D_VDouble normal;

    /**
     * An attribute as read in from for example an STL file. This could
     * represent the colour or texture or another property of the triangle.
     */
    public short attribute;

    /**
     * The base colour of the triangle.
     */
    public Color baseColor;

    /**
     * The colour of the triangle given some lighting conditions.
     */
    public Color lightingColor;

    /**
     * The colour of in ambient light.
     */
    public Color ambientColor;
    
    /**
     * Create a new instance.
     *
     * @param triangle What {@link #triangle} is set to.
     * @param normal What {@link #normal} is set to.
     * @param attribute What {@link #attribute} is set to.
     */
    public TriangleDouble(V3D_TriangleDouble triangle, V3D_VDouble normal,
            short attribute) {
        this.triangle = triangle;
        this.normal = normal;
        this.attribute = attribute;
    }

    /**
     * Create a new instance
     *
     * @param triangle What {@link #triangle} is set to.
     * @param baseColor What {@link #baseColor} is set to.
     */
    public TriangleDouble(V3D_TriangleDouble triangle, Color baseColor) {
        this.triangle = triangle;
        this.baseColor = baseColor;
        this.lightingColor = baseColor;
    }

    /**
     * Used to update {@link #lightingColor} based on the input lightVector. A
     * triangle facing the vector will be bright. One facing away will be
     * darker. This helps give a 3D like effect to rendering.
     *
     * @param pt A point away from which the normal will face. If pt is null,
     * then the normal direction is given by the right hand rule.
     * @param lightVector The direction that light is coming from.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public void setLighting(V3D_PointDouble pt, V3D_VectorDouble lightVector, 
            double ambientLight, double epsilon) {
        V3D_VectorDouble n;
        if (normal == null) {
            n = initN(pt, epsilon);
        } else {
            if (normal.isZero()) {
                n = initN(pt, epsilon);
            } else {
                n = new V3D_VectorDouble(normal);
            }
        }
        double dot = Math.abs(n.getDotProduct(lightVector));
        double lr = Math.pow(dot, 3d); 
//        double lr = dot * dot;
         lr = (lr + 1d) / (2d * (1d - ambientLight));
        lr = Math.min(1.0d, Math.max(0.0d,
                ambientLight + lr));
        int red = (int) (baseColor.getRed() * lr);
        int green = (int) (baseColor.getGreen() * lr);
        int blue = (int) (baseColor.getBlue() * lr);
        lightingColor = new Color(red, green, blue);
        initAmbientLightColour(ambientLight);
    }

    private void initAmbientLightColour(double ambientLight) {
        int red = (int) (lightingColor.getRed() * ambientLight);
        int green = (int) (lightingColor.getGreen() * ambientLight);
        int blue = (int) (lightingColor.getBlue() * ambientLight);
        this.ambientColor = new Color(red, green, blue);
    }
    
    private V3D_VectorDouble initN(V3D_PointDouble pt, double epsilon) {
        if (pt == null) {
            return triangle.getPl(epsilon).getN().getUnitVector();
        } else {
            return triangle.getPl(epsilon).getN().getUnitVector(pt);
        }
    }
}
