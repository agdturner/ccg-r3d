/*
 * Copyright 2022-2025 Centre for Computational Geography.
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
import java.io.Serializable;

/**
 * For representing spatial entities in the universe.
 * 
 * @author Andy Turner
 */
public class Entity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * The base colour.
     */
    public Color color;

    /**
     * The colour given some lighting conditions.
     */
    public Color lightingColor;

    /**
     * The colour in ambient light.
     */
    public Color ambientColor;
    
    /**
     * Create a new instance with {@link #color} {@code Color.gray}.
     */
    public Entity() {
        this(Color.gray);
    }
    
    /**
     * Create a new instance.
     * 
     * @param color What {@link #color} is set to.
     */
    public Entity(Color color){
        this.color = color;
    }
}
