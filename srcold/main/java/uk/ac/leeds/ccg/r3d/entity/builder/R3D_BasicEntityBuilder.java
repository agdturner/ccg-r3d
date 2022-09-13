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
package uk.ac.leeds.ccg.r3d.entity.builder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import uk.ac.leeds.ccg.math.number.Math_BigRational;

import uk.ac.leeds.ccg.r3d.entity.Entity;
import uk.ac.leeds.ccg.r3d.entity.IEntity;
import uk.ac.leeds.ccg.r3d.entity.MorphEntity;
import uk.ac.leeds.ccg.r3d.entity.R3D_Entity;
import uk.ac.leeds.ccg.r3d.entity.R3D_IEntity;
import uk.ac.leeds.ccg.r3d.point.MorphPoint;
import uk.ac.leeds.ccg.r3d.point.R3D_Point;
import uk.ac.leeds.ccg.r3d.point.R3D_PointDoubleOffset;
import uk.ac.leeds.ccg.r3d.shapes.MorphPolygon;
import uk.ac.leeds.ccg.r3d.shapes.MorphPolyhedron;
import uk.ac.leeds.ccg.r3d.shapes.R3D_Triangle;
import uk.ac.leeds.ccg.r3d.shapes.Polyhedron;
import uk.ac.leeds.ccg.r3d.shapes.R3D_Polyhedron;
import uk.ac.leeds.ccg.r3d.shapes.R3D_Tetrahedron;
import uk.ac.leeds.ccg.r3d.shapes.R3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_TetrahedronPoly;

public class R3D_BasicEntityBuilder {

    public static R3D_IEntity createTriangle(double size, 
            Math_BigRational centerX, 
            Math_BigRational centerY, 
            Math_BigRational centerZ, int oom) {
        Math_BigRational sized2 = Math_BigRational.valueOf(size).divide(2L);
        R3D_Point p1 = new R3D_Point(
                centerX.subtract(sized2), 
                centerY.subtract(sized2), 
                centerZ.subtract(sized2));
        R3D_Point p2 = new R3D_Point(
                centerX.add(sized2),
                centerY.subtract(sized2),
                centerZ.subtract(sized2));
        R3D_Point p3 = new R3D_Point(
                centerX.subtract(sized2), 
                centerY.add(sized2), 
                centerZ.subtract(sized2));
        R3D_Triangle triangleh = new R3D_Triangle(Color.BLUE, oom, p1, p2, p3);
        R3D_Triangle trianglet = new R3D_Triangle(Color.RED, oom, p2, p1, p3);
        return new R3D_Entity(triangleh, trianglet);
    }
    
    public static R3D_IEntity createTetrahedron(double size, 
            Math_BigRational centerX, 
            Math_BigRational centerY, 
            Math_BigRational centerZ, int oom) {
        Math_BigRational sized2 = Math_BigRational.valueOf(size).divide(2L);
        
        R3D_Point p1 = new R3D_Point(
                centerX.subtract(sized2), 
                centerY.subtract(sized2), 
                centerZ.subtract(sized2));
        R3D_Point p2 = new R3D_Point(
                centerX.add(sized2),
                centerY.subtract(sized2),
                centerZ.subtract(sized2));
        R3D_Point p3 = new R3D_Point(
                centerX.subtract(sized2), 
                centerY.add(sized2), 
                centerZ.subtract(sized2));
        R3D_Point p4 = new R3D_Point(
                centerX.add(sized2),
                centerY.subtract(sized2),
                centerZ.add(sized2));

        R3D_Tetrahedron tetrahedron = new R3D_Tetrahedron(
                new R3D_Triangle(Color.BLUE.brighter(), oom, p1, p2, p3),
                new R3D_Triangle(Color.BLUE.darker(), oom, p1, p3, p2),
                new R3D_Triangle(Color.RED.brighter(), oom, p3, p2, p4),
                new R3D_Triangle(Color.RED.darker(), oom, p3, p4, p2),
                new R3D_Triangle(Color.YELLOW.brighter(), oom, p1, p3, p4),
                new R3D_Triangle(Color.YELLOW.darker(), oom, p1, p4, p3),
                new R3D_Triangle(Color.GREEN.brighter(), oom, p1, p4, p2),
                new R3D_Triangle(Color.GREEN.darker(), oom, p1, p2, p4));

        List<R3D_Polyhedron> polyhedrons = new ArrayList<>();
        polyhedrons.add(new R3D_Polyhedron(tetrahedron));

        return new R3D_Entity(polyhedrons);
    }
    
//    public static R3D_IEntity createCube(double size, double centerX, double centerY, double centerZ, int oom) {
//        R3D_Point p1 = new R3D_Point(centerX + size / 2, centerY + -size / 2, centerZ + -size / 2);
//        R3D_Point p2 = new R3D_Point(centerX + size / 2, centerY + size / 2, centerZ + -size / 2);
//        R3D_Point p3 = new R3D_Point(centerX + size / 2, centerY + size / 2, centerZ + size / 2);
//        R3D_Point p4 = new R3D_Point(centerX + size / 2, centerY + -size / 2, centerZ + size / 2);
//        R3D_Point p5 = new R3D_Point(centerX + -size / 2, centerY + -size / 2, centerZ + -size / 2);
//        R3D_Point p6 = new R3D_Point(centerX + -size / 2, centerY + size / 2, centerZ + -size / 2);
//        R3D_Point p7 = new R3D_Point(centerX + -size / 2, centerY + size / 2, centerZ + size / 2);
//        R3D_Point p8 = new R3D_Point(centerX + -size / 2, centerY + -size / 2, centerZ + size / 2);
//
//        R3D_Polyhedron tetra = new R3D_Polyhedron(
//                new R3D_Triangle(Color.BLUE, oom, p5, p6, p7, p8),
//                new R3D_Triangle(Color.WHITE, oom, p1, p2, p6, p5),
//                new R3D_Triangle(Color.YELLOW, oom, p1, p5, p8, p4),
//                new R3D_Triangle(Color.GREEN, oom, p2, p6, p7, p3),
//                new R3D_Triangle(Color.ORANGE, oom, p4, p3, p7, p8),
//                new R3D_Triangle(Color.RED, oom, p1, p2, p3, p4));
//
//        List<R3D_Polyhedron> polyhedrons = new ArrayList<>();
//        polyhedrons.add(tetra);
//
//        return new R3D_Entity(polyhedrons);
//    }

    public static IEntity createMorphCube(double size, Color color, double centerX, double centerY, double centerZ) {
        MorphPoint p1 = new MorphPoint(centerX + size / 2, centerY + -size / 2, centerZ + -size / 2, centerX + size / 4, centerY + -size / 4, centerZ + -size / 4);
        MorphPoint p2 = new MorphPoint(centerX + size / 2, centerY + size / 2, centerZ + -size / 2, centerX + size / 2, centerY + size / 2, centerZ + -size / 1);
        MorphPoint p3 = new MorphPoint(centerX + size / 2, centerY + size / 2, centerZ + size / 2, centerX + size / 2, centerY + size / 2, centerZ + size / 1);
        MorphPoint p4 = new MorphPoint(centerX + size / 2, centerY + -size / 2, centerZ + size / 2, centerX + size / 2, centerY + -size / 2, centerZ + size / 4);
        MorphPoint p5 = new MorphPoint(centerX + -size / 2, centerY + -size / 2, centerZ + -size / 2, centerX + -size / 2, centerY + -size / 2, centerZ + -size / 1);
        MorphPoint p6 = new MorphPoint(centerX + -size / 2, centerY + size / 2, centerZ + -size / 2, centerX + -size / 2, centerY + size / 2, centerZ + -size / 4);
        MorphPoint p7 = new MorphPoint(centerX + -size / 2, centerY + size / 2, centerZ + size / 2, centerX + -size / 2, centerY + size / 2, centerZ + size / 4);
        MorphPoint p8 = new MorphPoint(centerX + -size / 2, centerY + -size / 2, centerZ + size / 2, centerX + -size / 2, centerY + -size / 2, centerZ + size / 1);

        Polyhedron tetra = new MorphPolyhedron(new MorphPolygon(color, p5, p6, p7, p8), new MorphPolygon(color, p1, p2, p6, p5), new MorphPolygon(color, p1, p5, p8, p4),
                new MorphPolygon(color, p2, p3, p7, p6), new MorphPolygon(color, p4, p8, p7, p3), new MorphPolygon(color, p1, p4, p3, p2));

        List<Polyhedron> tetras = new ArrayList<Polyhedron>();
        tetras.add(tetra);

        return new MorphEntity(tetras);
    }

//    public static R3D_IEntity createDiamond(Color color, double size, Math_BigRational centerX, Math_BigRational centerY, Math_BigRational centerZ, int oom) {
//        List<R3D_Polyhedron> polyhedrons = new ArrayList<>();
//        Math_BigRational sized2 = Math_BigRational.valueOf(size).divide(2L);
//        R3D_Point bottom = new R3D_Point(centerX, centerY, centerZ.subtract(sized2));
//        int edges = 20;
//        double inFactor = 0.8;
//        R3D_Point[] outerPoints = new R3D_Point[edges];
//        R3D_Point[] innerPoints = new R3D_Point[edges];
//        for (int i = 0; i < edges; i++) {
//            double theta = 2 * Math.PI / edges * i;
//            double xPos = -Math.sin(theta) * size / 2;
//            double yPos = Math.cos(theta) * size / 2;
//            double zPos = size / 2;
//            outerPoints[i] = new R3D_Point(
//                    centerX.add(Math_BigRational.valueOf(xPos)),
//                    centerY.add(Math_BigRational.valueOf(yPos)),
//                    centerZ.add(Math_BigRational.valueOf(zPos * inFactor)));
//            innerPoints[i] = new R3D_Point(
//                    centerX.add(Math_BigRational.valueOf(xPos * inFactor)),
//                    centerY.add(Math_BigRational.valueOf(yPos * inFactor)),
//                    centerZ.add(Math_BigRational.valueOf(zPos)));
//        }
//
//        R3D_Triangle triangles[] = new R3D_Triangle[2 * edges + 1];
//        for (int i = 0; i < edges; i++) {
//            triangles[i] = new R3D_Triangle(oom, outerPoints[i], bottom, outerPoints[(i + 1) % edges]);
//        }
////        for (int i = 0; i < edges; i++) {
////            triangles[i + edges] = new R3D_Triangle(
////                    outerPoints[i], 
////                    outerPoints[(i + 1) % edges],
////                    innerPoints[(i + 1) % edges], 
////                    innerPoints[i]);
////        }
//        //triangles[edges * 2] = new R3D_Triangle(innerPoints);
//
//        R3D_Polyhedron polyhedron = new R3D_Polyhedron(color, false, triangles);
//        polyhedrons.add(polyhedron);
//
//        return new R3D_Entity(polyhedrons);
//    }
//
//    public static R3D_IEntity createSphere(Color color, double size, 
//            int resolution, Math_BigRational centerX, 
//            Math_BigRational centerY, Math_BigRational centerZ, int oom) {
//        List<R3D_Polyhedron> polyhedrons = new ArrayList<>();
//        List<R3D_Triangle> triangles = new ArrayList<>();
//        Math_BigRational sized2 = Math_BigRational.valueOf(size).divide(2L);
//        R3D_Point bottom = new R3D_Point(centerX, centerY, centerZ.subtract(sized2));
//        R3D_Point top = new R3D_Point(centerX, centerY, centerZ.add(sized2));
//
//        R3D_Point[][] points = new R3D_Point[resolution - 1][resolution];
//
//        for (int i = 1; i < resolution; i++) {
//            double theta = Math.PI / resolution * i;
//            double zPos = -Math.cos(theta) * size / 2;
//            double currentRadius = Math.abs(Math.sin(theta) * size / 2);
//            for (int j = 0; j < resolution; j++) {
//                double alpha = 2 * Math.PI / resolution * j;
//                double xPos = -Math.sin(alpha) * currentRadius;
//                double yPos = Math.cos(alpha) * currentRadius;
//                points[i - 1][j] = new R3D_Point(
//                        centerX.doubleValue() + xPos,
//                        centerY.doubleValue() + yPos,
//                        centerZ.doubleValue() + zPos);
//            }
//        }
//        
//        
//        for (int i = 1; i <= resolution; i++) {
//            for (int j = 0; j < resolution; j++) {
//                if (i == 1) {
//                    triangles.add(new R3D_Triangle(oom, points[i - 1][j], points[i - 1][(j + 1) % resolution], bottom));
//                } else if (i == resolution) {
//                    triangles.add(new R3D_Triangle(oom, points[i - 2][(j + 1) % resolution], points[i - 2][j], top));
//                } else {
//                    triangles.add(new R3D_Triangle(oom, points[i - 1][j], points[i - 1][(j + 1) % resolution], points[i - 2][(j + 1) % resolution], points[i - 2][j]));
//                }
//            }
//        }
//
//        R3D_Triangle[] triangleArray = new R3D_Triangle[triangles.size()];
//        triangleArray = triangles.toArray(triangleArray);
//
//        R3D_Polyhedron polyhedron = new R3D_Polyhedron(color, false, triangleArray);
//        polyhedrons.add(polyhedron);
//
//        return new R3D_Entity(polyhedrons);
//    }

}
