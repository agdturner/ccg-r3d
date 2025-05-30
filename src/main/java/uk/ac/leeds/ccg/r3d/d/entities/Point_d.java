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
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_Point_d;

/**
 * A class for rendering points.
 * 
 * @author Andy Turner
 */
public class Point_d extends Entity {

    private static final long serialVersionUID = 1L;
    
    /**
     * The point to render.
     */
    public V3D_Point_d p;
    
    
    /**
     * @param p What {@link #p} is set to. 
     */
    public Point_d(V3D_Point_d p) {
        this(p, Color.BLUE);
    }

    /**
     * @param p What {@link #p} is set to. 
     * @param color What {@link #color} is set to. 
     */
    public Point_d(V3D_Point_d p, Color color) {
        this.p = p;
        this.color = color;
    }
}
