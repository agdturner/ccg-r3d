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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.point.R3D_PointDoubleOffset;
import uk.ac.leeds.ccg.r3d.point.R3D_Point;
import uk.ac.leeds.ccg.r3d.point.R3D_PointConverter;
import uk.ac.leeds.ccg.r3d.point.R3D_PointDouble;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class R3D_Triangle implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double AMBIENT_LIGHT = 0.05;
    
    protected R3D_Point[] points;
    protected int oom;
    
    private Color baseColor, lightingColor;
    private boolean visible;

    public R3D_Triangle(Color color, int oom, R3D_Point p, R3D_Point q, R3D_Point r) {
        this.oom = oom;
        this.baseColor = this.lightingColor = color;
        this.createPointsArray(p, q, r);
        this.updateVisibility();
    }

    public R3D_Triangle(int oom, R3D_Point p, R3D_Point q, R3D_Point r) {
        this(Color.WHITE, oom, p, q, r);
    }

    public void render(Graphics g) {
        if (!this.visible) {
            return;
        }
        Polygon polygon = new Polygon();
        for (var point : this.points) {
            R3D_PointDoubleOffset pd = point.getDouble(oom);
            Point p = R3D_PointConverter.convertPoint(pd);
            polygon.addPoint(p.x, p.y);
        }
        g.setColor(this.lightingColor);
        g.fillPolygon(polygon);
    }

    /**
     * For translating the points.
     * @param v The vector for the translation.
     */
    public void translate(V3D_Vector v) {
        for (var point : points) {
            point.v = point.v.add(v, oom);
            //point.v = v.add(v, oom);
        }
        this.updateVisibility();
    }

    public void rotate(double xRadians, double yRadians, double zRadians, boolean CW, V3D_Vector lightVector) {
        for (var point : points) {
            R3D_PointDoubleOffset pd = point.getDouble(oom);
            R3D_PointConverter.rotateAxisX(point, pd, CW,xRadians, oom);
            R3D_PointConverter.rotateAxisY(point, pd, CW,yRadians, oom);
            R3D_PointConverter.rotateAxisZ(point, pd, CW,zRadians, oom);
        }
        this.updateVisibility();
        this.setLighting(lightVector);
    }

    public double getAverageX() {
        Math_BigRational sum = Math_BigRational.ZERO;
        for (var point : this.points) {
            V3D_Point pg = point.get(oom);
            sum = sum.add(pg.x);
        }
        return sum.divide(this.points.length).toBigDecimal(oom).doubleValue();
    }

    public void setColor(Color color) {
        this.baseColor = color;
    }

    public static R3D_Triangle[] sort(R3D_Triangle[] triangles) {
        List<R3D_Triangle> trianglesList = new ArrayList<>();
        trianglesList.addAll(Arrays.asList(triangles));

        Collections.sort(trianglesList, (R3D_Triangle p1, R3D_Triangle p2) -> {
            R3D_PointDoubleOffset p1Average = p1.getAveragePoint();
            R3D_PointDoubleOffset p2Average = p2.getAveragePoint();
            double p1Dist = R3D_PointDoubleOffset.dist(p1Average, R3D_PointDouble.origin);
            double p2Dist = R3D_PointDoubleOffset.dist(p2Average, R3D_PointDouble.origin);
            double diff = p1Dist - p2Dist;
            if (diff == 0) {
                return 0;
            }
            
            return diff < 0 ? 1 : -1;
        });

        for (int i = 0; i < triangles.length; i++) {
            triangles[i] = trianglesList.get(i);
        }

        return triangles;
    }

    public void setLighting(V3D_Vector lightVector) {
        if (this.points.length < 3) {
            return;
        }
        V3D_Vector v1 = new V3D_Vector(this.points[0].get(oom), this.points[1].get(oom), oom);
        V3D_Vector v2 = new V3D_Vector(this.points[1].get(oom), this.points[2].get(oom), oom);
        V3D_Vector normal = v2.getCrossProduct(v1, oom).getUnitVector(oom);
        double dot = normal.getDotProduct(lightVector, oom).toBigDecimal(oom).doubleValue();
        double sign = dot < 0 ? -1 : 1;
        dot = sign * dot * dot;
        dot = (dot + 1) / 2 * (1 - AMBIENT_LIGHT);

        double lightRatio = Math.min(1, Math.max(0, AMBIENT_LIGHT + dot));
        this.updateLightingColor(lightRatio);
    }

    public boolean isVisible() {
        return this.visible;
    }

    protected final void createPointsArray(R3D_Point p, R3D_Point q, R3D_Point r) {
        this.points = new R3D_Point[3];
        this.points[0] = new R3D_Point(p);
        this.points[1] = new R3D_Point(q);
        this.points[2] = new R3D_Point(r);
    }

    private void updateVisibility() {
        this.visible = this.getAverageX() < 0;
        //this.visible = true;
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
        for (var point : this.points) {
            R3D_PointDoubleOffset pg = point.getDouble(oom);
            x += pg.x;
            y += pg.y;
            z += pg.z;
        }
        x /= this.points.length;
        y /= this.points.length;
        z /= this.points.length;
        return new R3D_PointDoubleOffset(x, y, z);
    }

}
