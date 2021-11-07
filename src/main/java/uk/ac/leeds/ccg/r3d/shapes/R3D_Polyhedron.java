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
import java.awt.Graphics;

import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class R3D_Polyhedron {

    protected R3D_Tetrahedron[] tetrahedrons;
    private Color color;

    public R3D_Polyhedron(Color color, boolean decayColor, R3D_Tetrahedron... tetrahedrons) {
        this.color = color;
        this.tetrahedrons = tetrahedrons;
        if (decayColor) {
            this.setDecayingPolygonColor();
        } else {
            this.setPolygonColor();
        }
        this.sort();
    }

    public R3D_Polyhedron(R3D_Tetrahedron... tetrahedrons) {
        this.color = Color.WHITE;
        this.tetrahedrons = tetrahedrons;
        this.sort();
    }

    public void render(Graphics g) {
        for (R3D_Tetrahedron poly : this.tetrahedrons) {
            poly.render(g);
        }
    }

    public void translate(V3D_Vector v) {
        for (var t : this.tetrahedrons) {
            t.translate(v);
        }
        this.sort();
    }

    public void rotate(double xDegrees, double yDegrees, 
            double zDegrees, boolean CW, V3D_Vector lightVector) {
        for (var t : this.tetrahedrons) {
            t.rotate(xDegrees, yDegrees, zDegrees, CW, lightVector);
        }
        this.sort();
    }

    public void setLighting(V3D_Vector lightVector) {
        for (var t : this.tetrahedrons) {
            t.setLighting(lightVector);
        }
    }

    public R3D_Tetrahedron[] getTetrahedrons() {
        return this.tetrahedrons;
    }

    private void sort() {
        for (var t : tetrahedrons) {
            t.sort();
        }
    }

    private void setPolygonColor() {
        for (var t : this.tetrahedrons) {
            t.setColor();
        }
    }

    private void setDecayingPolygonColor() {
        for (var t : this.tetrahedrons) {
            t.setDecayingPolygonColor();
        }
    }

}
