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
import uk.ac.leeds.ccg.data.id.Data_ID_long;

/**
 * For representing spatial entities in the universe.
 * 
 * @author Andy Turner
 */
public class Entity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * The id.
     */
    public Data_ID_long id;
    
    /**
     * Create a new instance.
     */
    public Entity(){}
    
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
     * The edge colour.
     */
    public Color colorEdge;
    
    /**
     * Create a new instance.
     * @param id What {@link #id} is set to.
     */
    public Entity(Data_ID_long id){
        this(id, Color.lightGray, Color.gray);
    }
    
    /**
     * Create a new instance.
     * @param id What {@link #id} is set to.
     * @param color What {@link #color} and {@link #colorEdge} are set to.
     */
    public Entity(Data_ID_long id, Color color){
        this(id, color, color);
    }
    
    /**
     * Create a new instance.
     * @param id What {@link #id} is set to.
     * @param color What {@link #color} is set to.
     * @param colorEdge What {@link #colorEdge} is set to.
     */
    public Entity(Data_ID_long id, Color color, Color colorEdge){
        this.id = id;
        this.baseColor = color;
        this.colorEdge = colorEdge;
    }
}
