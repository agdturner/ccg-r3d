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

public class R3D_Tetrahedron {

    protected R3D_Triangle[] triangles;
    private Color color;

    public R3D_Tetrahedron(Color color, boolean decayColor, R3D_Triangle... triangles) {
        this.color = color;
        this.triangles = triangles;
        if (decayColor) {
            this.setDecayingPolygonColor();
        } else {
            this.setColor();
        }
        this.sort();
    }

    public R3D_Tetrahedron(R3D_Triangle... triangles) {
        this.color = Color.WHITE;
        this.triangles = triangles;
        this.sort();
    }

    public void render(Graphics g) {
        for (var t : this.triangles) {
            t.render(g);
        }
    }

    public void translate(V3D_Vector v) {
        for (var t : this.triangles) {
            t.translate(v);
        }
        this.sort();
    }

    public void rotate(double xRadians, double yRadians, 
            double zRadians, boolean CW, V3D_Vector lightVector) {
        for (var t : this.triangles) {
            t.rotate(xRadians, yRadians, zRadians, CW, lightVector);
        }
        this.sort();
    }

    public void setLighting(V3D_Vector lightVector) {
        for (var t : this.triangles) {
            t.setLighting(lightVector);
        }
    }

    public R3D_Triangle[] getTriangles() {
        return this.triangles;
    }

    protected final void sort() {
        this.triangles = R3D_Triangle.sort(this.triangles);
    }

    protected final void setColor() {
        for (var t : this.triangles) {
            t.setColor(this.color);
        }
    }

    protected final void setDecayingPolygonColor() {
        double decayFactor = 0.97;
        for (var t : this.triangles) {
            t.setColor(this.color);
            int r = (int) (this.color.getRed() * decayFactor);
            int g = (int) (this.color.getGreen() * decayFactor);
            int b = (int) (this.color.getBlue() * decayFactor);
            this.color = new Color(r, g, b);
        }
    }

}
