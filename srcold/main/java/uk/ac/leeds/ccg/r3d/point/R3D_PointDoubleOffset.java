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
package uk.ac.leeds.ccg.r3d.point;

import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;

public class R3D_PointDoubleOffset extends R3D_PointDouble {

    public double xOffset, yOffset, zOffset;

    public R3D_PointDoubleOffset(double x, double y, double z) {
        super(x, y, z);
        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;
    }

    public R3D_PointDoubleOffset(V3D_Point p, int oom) {
        super(p.x.toDouble(), p.y.toDouble(), p.z.doubleValue());
        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;
    }

    
    public double getAdjustedX() {
        return this.x + this.xOffset;
    }

    public double getAdjustedY() {
        return this.y + this.yOffset;
    }

    public double getAdjustedZ() {
        return this.z + this.zOffset;
    }

}
