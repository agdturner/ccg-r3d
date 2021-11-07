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

import uk.ac.leeds.ccg.r3d.point.R3D_MorphPoint;

public class R3D_MorphTriangle extends R3D_Triangle {

    private static final long serialVersionUID = 1L;

    public R3D_MorphTriangle(Color color, int oom, R3D_MorphPoint p, R3D_MorphPoint q, R3D_MorphPoint r) {
        super(color, oom, p, q, r);
    }

    public R3D_MorphTriangle(int oom, R3D_MorphPoint p, R3D_MorphPoint q, R3D_MorphPoint r) {
        super(oom, p, q, r);
    }

    public void morph(double morphSpeed, int oom) {
        for (var p : this.points) {
            ((R3D_MorphPoint) p).morph(morphSpeed, oom);
        }
    }
}
