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
package uk.ac.leeds.ccg.r3d.entity;

import java.awt.Graphics;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public interface R3D_IEntity {

    void render(Graphics g);

    void translate(V3D_Vector v);

    void rotate(double xDegrees, double yDegrees, double zDegrees, V3D_Vector lightVector);

    void setLighting(V3D_Vector lightVector);

}
