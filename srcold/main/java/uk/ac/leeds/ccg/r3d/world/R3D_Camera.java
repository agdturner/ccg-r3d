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
package uk.ac.leeds.ccg.r3d.world;

import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.point.R3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class R3D_Camera extends R3D_Point {

    private static final long serialVersionUID = 1L;
    
    //private double roll, pitch, yaw;

    public R3D_Camera(Math_BigRational x, Math_BigRational y, Math_BigRational z) {
        super(x, y, z);
    }

    public void translate(V3D_Vector v, int oom) {
        super.v = super.v.add(v, oom);
    }

}
