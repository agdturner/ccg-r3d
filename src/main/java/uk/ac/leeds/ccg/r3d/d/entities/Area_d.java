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
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Area_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Point_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Triangle_d;
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Vector_d;
import uk.ac.leeds.ccg.v3d.geometry.d.light.V3D_V_d;

/**
 *
 * @author Andy Turner
 */
public class Area_d extends Entity {
    
    private static final long serialVersionUID = 1L;

    /**
     * The area.
     */
    public V3D_Area_d area;

    /**
     * The normal as read in from for example an STL file. The normal can be
     * computed from the geometry using the order of the points and the right
     * hand rule. However, often the normal is provided. The direction of the
     * normal vector allows us to specify sides of the triangle which can be
     * attributed with different properties e.g. colours.
     */
    public V3D_V_d normal;
    
    /**
     * An attribute as read in from for example an STL file. This could
     * represent the colour or texture or another property of the triangle.
     */
    public short attribute;
    
    /**
     * Create a new instance.
     *
     * @param triangle What {@link #area} is set to.
     * @param normal What {@link #normal} is set to.
     * @param attribute What {@link #attribute} is set to.
     */
    public Area_d(V3D_Triangle_d triangle, V3D_V_d normal,
            short attribute) {
        this.area = triangle;
        this.normal = normal;
        this.attribute = attribute;
    }

    /**
     * Create a new instance.
     *
     * @param area What {@link #area} is set to.
     */
    public Area_d(V3D_Area_d area) {
        this.area = area;
    }
    
    /**
     * Create a new instance.
     *
     * @param area What {@link #area} is set to.
     * @param color What {@link #color} is set to.
     */
    public Area_d(V3D_Area_d area, Color color){
        super(color);
        this.area = area;
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
    public void setLighting(V3D_Point_d pt, V3D_Vector_d lightVector, 
            double ambientLight, double epsilon) {
        V3D_Vector_d n;
        if (normal == null) {
            n = initN(pt);
        } else {
            if (normal.isZero()) {
                n = initN(pt);
            } else {
                n = new V3D_Vector_d(normal);
            }
        }
        double dot = n.getDotProduct(lightVector);
        double dot2 = dot * dot;
        if (dot < 0d) {
            dot2 = -dot2;
        }
        dot2 = (dot2 + 1) / (2d * (1d - ambientLight));
        double lr = Math.min(1d, Math.max(0d, ambientLight + dot2));
        int red = (int) (color.getRed() * lr);
        int green = (int) (color.getGreen() * lr);
        int blue = (int) (color.getBlue() * lr);
        lightingColor = new Color(red, green, blue);
        initAmbientLightColour(ambientLight);
    }

    private void initAmbientLightColour(double ambientLight) {
        int red = (int) (lightingColor.getRed() * ambientLight);
        int green = (int) (lightingColor.getGreen() * ambientLight);
        int blue = (int) (lightingColor.getBlue() * ambientLight);
        this.ambientColor = new Color(red, green, blue);
    }
    
    private V3D_Vector_d initN(V3D_Point_d pt) {
        if (pt == null) {
            return area.pl.getN().getUnitVector();
        } else {
            return area.pl.getN().getUnitVector(pt);
        }
    }
}
