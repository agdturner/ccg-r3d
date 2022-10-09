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
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;

/**
 * For visualising a tetrahedron.
 * 
 * @author Andy Turner
 */
public class Tetrahedron {

    public Triangle[] triangles;

    public Tetrahedron(V3D_Tetrahedron t, Color baseColor) {
        //tetrahedron = t;
        triangles = new Triangle[4];
        triangles[0] = new Triangle(t.getPqr(), baseColor);
        triangles[1] = new Triangle(t.getPsq(), baseColor);
        triangles[2] = new Triangle(t.getQsr(), baseColor);
        triangles[3] = new Triangle(t.getSpr(), baseColor);
    }
}
