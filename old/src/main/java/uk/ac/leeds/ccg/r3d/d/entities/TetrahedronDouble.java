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
import uk.ac.leeds.ccg.v3d.geometry.d.V3D_TetrahedronDouble;

/**
 * For visualising a tetrahedron.
 * 
 * @author Andy Turner
 */
public class TetrahedronDouble {

    /**
     * The triangles that comprise the edge surface of the tetrahedron.
     */
    public TriangleDouble[] triangles;

    /**
     * Creates a new instance.
     * @param tetrahedron The geometry of the tetrahedron.
     * @param baseColor What the base colour of each triangle is set to.
     * @param lightVector The direction that light is coming from.
     */
    public TetrahedronDouble(V3D_TetrahedronDouble tetrahedron, Color baseColor) {
        triangles = new TriangleDouble[4];
        triangles[0] = new TriangleDouble(tetrahedron.getPqr(), baseColor);
        triangles[1] = new TriangleDouble(tetrahedron.getPsq(), baseColor);
        triangles[2] = new TriangleDouble(tetrahedron.getQsr(), baseColor);
        triangles[3] = new TriangleDouble(tetrahedron.getSpr(), baseColor);
    }
}
