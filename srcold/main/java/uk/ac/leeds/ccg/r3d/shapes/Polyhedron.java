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

import uk.ac.leeds.ccg.r3d.point.MyVector;

public class Polyhedron {

    protected MyPolygon[] polygons;
    private Color color;

    public Polyhedron(Color color, boolean decayColor, MyPolygon... polygons) {
        this.color = color;
        this.polygons = polygons;
        if (decayColor) {
            this.setDecayingPolygonColor();
        } else {
            this.setPolygonColor();
        }
        this.sortPolygons();
    }

    public Polyhedron(MyPolygon... polygons) {
        this.color = Color.WHITE;
        this.polygons = polygons;
        this.sortPolygons();
    }

    public void render(Graphics g) {
        for (MyPolygon poly : this.polygons) {
            poly.render(g);
        }
    }

    public void translate(double x, double y, double z) {
        for (MyPolygon p : this.polygons) {
            p.translate(x, y, z);
        }
        this.sortPolygons();
    }

    public void rotate(boolean CW, double xDegrees, double yDegrees, double zDegrees, MyVector lightVector) {
        for (MyPolygon p : this.polygons) {
            p.rotate(CW, xDegrees, yDegrees, zDegrees, lightVector);
        }
        this.sortPolygons();
    }

    public void setLighting(MyVector lightVector) {
        for (MyPolygon p : this.polygons) {
            p.setLighting(lightVector);
        }
    }

    public MyPolygon[] getPolygons() {
        return this.polygons;
    }

    private void sortPolygons() {
        MyPolygon.sortPolygons(this.polygons);
    }

    private void setPolygonColor() {
        for (MyPolygon poly : this.polygons) {
            poly.setColor(this.color);
        }
    }

    private void setDecayingPolygonColor() {
        double decayFactor = 0.97;
        for (MyPolygon poly : this.polygons) {
            poly.setColor(this.color);
            int r = (int) (this.color.getRed() * decayFactor);
            int g = (int) (this.color.getGreen() * decayFactor);
            int b = (int) (this.color.getBlue() * decayFactor);
            this.color = new Color(r, g, b);
        }
    }

}
