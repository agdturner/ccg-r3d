/*
 * Copyright 2021 Centre for Computational Geography.
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
package uk.ac.leeds.ccg.r3d.shapes;

import java.awt.Color;

public class R3D_MorphTetrahedron extends R3D_Tetrahedron {

    public R3D_MorphTetrahedron(Color color, boolean decayColor, R3D_MorphTriangle... triangles) {
        super(color, decayColor, triangles);
    }

    public R3D_MorphTetrahedron(R3D_MorphTriangle... triangles) {
        super(triangles);
    }

    public void morph(double morphSpeed, int oom) {
        for (R3D_Triangle triangle : this.triangles) {
            ((R3D_MorphTriangle) triangle).morph(morphSpeed, oom);
        }
    }
}
