/*
 * Copyright 2022 Centre for Computational Geography.
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
package uk.ac.leeds.ccg.r3d;

import java.awt.Color;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.entities.Tetrahedron;
import uk.ac.leeds.ccg.r3d.entities.Triangle;
import uk.ac.leeds.ccg.r3d.io.STL_Reader;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Envelope;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;

/**
 *
 * @author Andy Turner
 */
public class Universe {

    V3D_Environment e;

//    Math_BigRational width;
//    Math_BigRational height;

    public ArrayList<Triangle> triangles;
    public ArrayList<Tetrahedron> tetrahedra;

    public Camera camera;

    public Universe() {
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        e = new V3D_Environment();
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
//        width = camera.pixelWidth.multiply(camera.nrows);
//        height = camera.pixelHeight.multiply(camera.ncols);
    }
    
    /**
     * Loads triangles from file given by path.
     *
     * @param path The path to a binary STL file.
     * @return V3D_Envelope encompaasing all the points.
     * @throws IOException If encountered.
     */
    public V3D_Envelope init(Path path, int oom, RoundingMode rm) throws IOException {
        // Read in a Utah teapot.
        STL_Reader data = new STL_Reader();
        data.readBinary(path);
        V3D_Point p = data.triangles.get(0).p.getP();
        Math_BigRational xmin = p.getX(oom, rm);
        Math_BigRational xmax = p.getX(oom, rm);
        Math_BigRational ymin = p.getY(oom, rm);
        Math_BigRational ymax = p.getY(oom, rm);
        Math_BigRational zmin = p.getZ(oom, rm);
        Math_BigRational zmax = p.getZ(oom, rm);
        for (V3D_Triangle t : data.triangles) {
            triangles.add(new Triangle(t, Color.YELLOW));
            for (var pt : t.getPoints(oom, rm)) {
                Math_BigRational x = pt.getX(oom, rm);
                Math_BigRational y = pt.getY(oom, rm);
                Math_BigRational z = pt.getZ(oom, rm);
                xmin = xmin.min(x);
                xmax = xmax.max(x);
                ymin = ymin.min(y);
                ymax = ymax.max(y);
                zmin = zmin.min(z);
                zmax = zmax.max(z);
            }
        }
        return new V3D_Envelope(e, oom, rm, xmin, xmax, ymin, ymax, zmin, zmax);
    }

    public V3D_Envelope init0(Math_BigRational width, Math_BigRational height,
            int oom, RoundingMode rm) {
        Math_BigRational halfwidth = width.divide(2);
        Math_BigRational halfheight = height.divide(2);
        Math_BigRational xmin = halfwidth.negate();
        Math_BigRational xmax = halfwidth;
        Math_BigRational ymin = halfheight.negate();
        Math_BigRational ymax = halfheight;
        Math_BigRational z = Math_BigRational.TEN;
        // Big Yellow Triangle
        triangles.add(new Triangle(new V3D_Triangle(
                new V3D_Point(e, xmin, ymin, z),
                new V3D_Point(e, xmin, ymax, z),
                new V3D_Point(e, xmax, ymax, z), oom, rm), Color.YELLOW));
        // Big Red Triangle
        triangles.add(new Triangle(new V3D_Triangle(
                new V3D_Point(e, xmin, ymin, z),
                new V3D_Point(e, xmax, ymax, z),
                new V3D_Point(e, xmax, ymin, z), oom, rm), Color.RED));
        return new V3D_Envelope(e, oom, rm, xmin, xmax, ymin, ymax, z, z);
    }

    public V3D_Envelope init1(Math_BigRational width, Math_BigRational height,
            int oom, RoundingMode rm) {
        Math_BigRational halfwidth = width.divide(2);
        Math_BigRational halfheight = height.divide(2);
//        Math_BigRational halfwidth = width.divide(20);
//        Math_BigRational halfheight = height.divide(20);
//        Math_BigRational halfwidth = width.multiply(20);
//        Math_BigRational halfheight = height.multiply(20);

        // initCameraTest()
//        // Little Red Triangle
//        triangles.add(new Triangle(new V3D_Triangle(
//                new V3D_Point(e, halfwidth.negate().divide(2), halfheight.negate().divide(2), Math_BigRational.valueOf(5)),
//                new V3D_Point(e, Math_BigRational.ZERO, halfheight.divide(2), Math_BigRational.valueOf(5)),
//                new V3D_Point(e, halfwidth.divide(2), halfheight.divide(2).negate(), Math_BigRational.valueOf(5))), Color.RED));
        Math_BigRational quarterwidth = halfwidth.divide(2);
        Math_BigRational quarterheight = halfheight.divide(2);
        // Tetrahedra
        Math_BigRational zero = Math_BigRational.ZERO;
        Math_BigRational sx = quarterwidth.divide(2);
        Math_BigRational sz = quarterheight;
        Math_BigRational oz = sx.divide(2);
        V3D_Tetrahedron t = new V3D_Tetrahedron(
                new V3D_Point(e, quarterwidth.negate(), zero, oz),
                new V3D_Point(e, zero, quarterheight.negate(), oz),
                new V3D_Point(e, zero, quarterheight, oz),
                new V3D_Point(e, sx, zero, sz), oom, rm);
        tetrahedra.add(new Tetrahedron(t, Color.WHITE));
        return t.getEnvelope(oom, rm);
    }

//    public V3D_Envelope initCameraTest(int oom, RoundingMode rm) {
//        Math_BigRational halfwidth = width.divide(2);
//        Math_BigRational halfheight = height.divide(2);
//        Math_BigRational xmin = halfwidth.negate();
//        Math_BigRational ymin = halfheight.negate();
//        Math_BigRational z = Math_BigRational.TEN;
//        // Little Yellow Triangle
//        Triangle t = new Triangle(new V3D_Triangle(
//                new V3D_Point(e, xmin, ymin, z),
//                new V3D_Point(e, xmin, ymin.add(4), z),
//                new V3D_Point(e, xmin.add(4), ymin.add(4), z), oom, rm), Color.YELLOW);        
//        triangles.add(t);
//        return t.triangle.getEnvelope(oom, rm);
//    }

    public void update() {
    }
}
