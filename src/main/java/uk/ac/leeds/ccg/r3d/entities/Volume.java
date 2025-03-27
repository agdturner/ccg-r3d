/*
 * Copyright 2022-2025 Andy Turner, University of Leeds.
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
import uk.ac.leeds.ccg.v3d.geometry.V3D_Volume;

/**
 * A volume.
 * 
 * @author Andy Turner
 */
public class Volume extends Entity {

    private static final long serialVersionUID = 1L;

    /**
     * The volume geometry.
     */
    public V3D_Volume volume;

    /**
     * Creates a new instance.
     * 
     * @param v What {@link #volume} is set to.
     * @param c What {@link #color} and {@link #lightingColor} are set to.
     */
    public Volume(V3D_Volume v, Color c) {
        this.volume = v;
        this.color = c;
        this.lightingColor = c;
    }
}
