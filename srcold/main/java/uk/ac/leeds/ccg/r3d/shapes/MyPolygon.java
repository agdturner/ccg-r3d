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
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.ac.leeds.ccg.r3d.point.R3D_PointDoubleOffset;
import uk.ac.leeds.ccg.r3d.point.MyVector;
import uk.ac.leeds.ccg.r3d.point.PointConverter;
import uk.ac.leeds.ccg.r3d.point.R3D_PointDouble;

public class MyPolygon {

    private static final double AmbientLight = 0.05;

    protected R3D_PointDoubleOffset[] points;

    private Color baseColor, lightingColor;
    private boolean visible;

    public MyPolygon(Color color, R3D_PointDoubleOffset... points) {
        this.baseColor = this.lightingColor = color;
        this.createPointsArray(points);
        this.updateVisibility();
    }

    public MyPolygon(R3D_PointDoubleOffset... points) {
        this.baseColor = this.lightingColor = Color.WHITE;
        this.createPointsArray(points);
        this.updateVisibility();
    }

    public void render(Graphics g) {
        if (!this.visible) {
            return;
        }

        Polygon poly = new Polygon();
        for (int i = 0; i < this.points.length; i++) {
            Point p = PointConverter.convertPoint(this.points[i]);
            poly.addPoint(p.x, p.y);
        }

        g.setColor(this.lightingColor);
        g.fillPolygon(poly);
    }

    public void translate(double x, double y, double z) {
        for (R3D_PointDoubleOffset p : points) {
            p.xOffset += x;
            p.yOffset += y;
            p.zOffset += z;
        }
        this.updateVisibility();
    }

    public void rotate(boolean CW, double xDegrees, double yDegrees, double zDegrees, MyVector lightVector) {
        for (R3D_PointDoubleOffset p : points) {
            PointConverter.rotateAxisX(p, CW, xDegrees);
            PointConverter.rotateAxisY(p, CW, yDegrees);
            PointConverter.rotateAxisZ(p, CW, zDegrees);
        }
        this.updateVisibility();
        this.setLighting(lightVector);
    }

    public double getAverageX() {
        double sum = 0;
        for (R3D_PointDoubleOffset p : this.points) {
            sum += p.x + p.xOffset;
        }

        return sum / this.points.length;
    }

    public void setColor(Color color) {
        this.baseColor = color;
    }

    public static MyPolygon[] sortPolygons(MyPolygon[] polygons) {
        List<MyPolygon> polygonsList = new ArrayList<>();

        for (MyPolygon poly : polygons) {
            polygonsList.add(poly);
        }

        Collections.sort(polygonsList, new Comparator<>() {
            @Override
            public int compare(MyPolygon p1, MyPolygon p2) {
                R3D_PointDoubleOffset p1Average = p1.getAveragePoint();
                R3D_PointDoubleOffset p2Average = p2.getAveragePoint();
                double p1Dist = R3D_PointDoubleOffset.dist(p1Average, R3D_PointDouble.origin);
                double p2Dist = R3D_PointDoubleOffset.dist(p2Average, R3D_PointDouble.origin);
                double diff = p1Dist - p2Dist;
                if (diff == 0) {
                    return 0;
                }

                return diff < 0 ? 1 : -1;
            }
        });

        for (int i = 0; i < polygons.length; i++) {
            polygons[i] = polygonsList.get(i);
        }

        return polygons;
    }

    public void setLighting(MyVector lightVector) {
        if (this.points.length < 3) {
            return;
        }

        MyVector v1 = new MyVector(this.points[0], this.points[1]);
        MyVector v2 = new MyVector(this.points[1], this.points[2]);
        MyVector normal = MyVector.normalize(MyVector.cross(v2, v1));
        double dot = MyVector.dot(normal, lightVector);
        double sign = dot < 0 ? -1 : 1;
        dot = sign * dot * dot;
        // rescale dot to be in a new range 0.2 to 0.8
        dot = (dot + 1) / 2 * (1 - AmbientLight);

        double lightRatio = Math.min(1, Math.max(0, AmbientLight + dot));
        this.updateLightingColor(lightRatio);
    }

    public boolean isVisible() {
        return this.visible;
    }

    protected void createPointsArray(R3D_PointDoubleOffset[] points) {
        this.points = new R3D_PointDoubleOffset[points.length];
        for (int i = 0; i < points.length; i++) {
            R3D_PointDoubleOffset p = points[i];
            this.points[i] = new R3D_PointDoubleOffset(p.x, p.y, p.z);
        }
    }

    private void updateVisibility() {
        this.visible = this.getAverageX() < 0;
    }

    private void updateLightingColor(double lightRatio) {
        int red = (int) (this.baseColor.getRed() * lightRatio);
        int green = (int) (this.baseColor.getGreen() * lightRatio);
        int blue = (int) (this.baseColor.getBlue() * lightRatio);
        this.lightingColor = new Color(red, green, blue);
    }

    private R3D_PointDoubleOffset getAveragePoint() {
        double x = 0;
        double y = 0;
        double z = 0;
        for (R3D_PointDoubleOffset p : this.points) {
            x += p.x + p.xOffset;
            y += p.y + p.yOffset;
            z += p.z + p.zOffset;
        }

        x /= this.points.length;
        y /= this.points.length;
        z /= this.points.length;

        return new R3D_PointDoubleOffset(x, y, z);
    }

}
