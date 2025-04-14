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

import ch.obermuhlner.math.big.BigRational;
import java.awt.Color;
import java.math.RoundingMode;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Area;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;
import uk.ac.leeds.ccg.v3d.geometry.light.V3D_V;

/**
 * An area.
 *
 * @author Andy Turner
 */
public class Area extends Entity {

    private static final long serialVersionUID = 1L;

    /**
     * The area
     */
    public V3D_Area area;

    /**
     * The normal as read in from for example an STL file. The normal can be
     * computed from the geometry using the order of the points and the right
     * hand rule. However, often the normal is provided. The direction of the
     * normal vector allows us to specify sides of the triangle which can be
     * attributed with different properties e.g. colours.
     */
    public V3D_V normal;

    /**
     * An attribute as read in from for example an STL file. This could
     * represent the colour or texture or another property.
     */
    public short attribute;

    /**
     * Create a new instance.
     *
     * @param area What {@link #area} is set to.
     * @param normal What {@link #normal} is set to.
     * @param attribute What {@link #attribute} is set to.
     */
    public Area(V3D_Area area, V3D_V normal, short attribute) {
        this.area = area;
        this.normal = normal;
        this.attribute = attribute;
    }

    /**
     * Create a new instance
     *
     * @param area What {@link #area} is set to.
     * @param color What {@link #color} is set to.
     */
    public Area(V3D_Area area, Color color) {
        this.area = area;
        this.color = color;
        this.lightingColor = color;
    }

    /**
     * Used to update {@link #lightingColor} based on the input lightVector.A
 triangle facing the vector will be bright. One facing away will be
 darker. This helps give a 3D like effect to rendering.
     *
     * @param pt A point away from which the normal will face. If pt is null,
     * then the normal direction is given by the right hand rule.
     * @param lightVector The direction that light is coming from.
     * @param ambientLight
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public void setLighting(V3D_Point pt, V3D_Vector lightVector, 
            BigRational ambientLight, int oom, RoundingMode rm) {
        V3D_Vector n;
        if (normal == null) {
            n = initN(pt, oom, rm);
        } else {
            if (normal.isZero()) {
                n = initN(pt, oom, rm);
            } else {
                n = new V3D_Vector(normal);
            }
        }
        BigRational dot = n.getDotProduct(lightVector, oom, rm);
        BigRational dot2 = dot.multiply(dot);
        if (dot.compareTo(BigRational.ZERO) == -1) {
            dot2 = dot2.negate();
        }
        dot2 = (dot2.add(BigRational.ONE)).divide((
                BigRational.TWO.multiply((
                        BigRational.ONE.subtract(ambientLight)))));
        double lr = Math.min(1d, Math.max(0d,
                ambientLight.add(dot2).doubleValue()));
        int red = (int) (color.getRed() * lr);
        int green = (int) (color.getGreen() * lr);
        int blue = (int) (color.getBlue() * lr);
        lightingColor = new Color(red, green, blue);
        initAmbientLightColour(ambientLight.doubleValue());
    }

    private void initAmbientLightColour(double ambientLight) {
        int red = (int) (lightingColor.getRed() * ambientLight);
        int green = (int) (lightingColor.getGreen() * ambientLight);
        int blue = (int) (lightingColor.getBlue() * ambientLight);
        this.ambientColor = new Color(red, green, blue);
    }
    
    private V3D_Vector initN(V3D_Point pt, int oom, RoundingMode rm) {
        if (pt == null) {
            return area.getPl(oom, rm).getN().getUnitVector(oom, rm);
        } else {
            return area.getPl(oom, rm).getN().getUnitVector(pt, oom, rm);
        }
    }
}
