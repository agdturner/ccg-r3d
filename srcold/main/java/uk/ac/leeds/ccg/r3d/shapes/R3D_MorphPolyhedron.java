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

public class R3D_MorphPolyhedron extends R3D_Polyhedron {

    public R3D_MorphPolyhedron(Color color, boolean decayColor, 
            R3D_MorphTetrahedron... tetrahedrons) {
        super(color, decayColor, tetrahedrons);
    }

    public R3D_MorphPolyhedron(R3D_MorphTetrahedron... tetrahedrons) {
        super(tetrahedrons);
    }

    public void morph(double morphSpeed, int oom) {
        for (var t : this.tetrahedrons) {
            ((R3D_MorphTetrahedron) t).morph(morphSpeed, oom);
        }
    }
}
