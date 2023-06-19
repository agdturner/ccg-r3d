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
import uk.ac.leeds.ccg.v3d.geometry.V3D_LineSegment;

/**
 * @author Andy Turner
 */
public class Line extends Entity {
    
    /**
     * The line to render.
     */
    public V3D_LineSegment l;
    
    /**
     * @param l What {@link l} is set to. 
     */
    public Line(V3D_LineSegment l) {
        this(l, Color.BLUE);
    }

    /**
     * @param l What {@link #l} is set to. 
     * @param baseColor What {@link #baseColor} is set to. 
     */
    public Line(V3D_LineSegment l, Color baseColor) {
        this.l = l;
        this.baseColor = baseColor;
    }
}
