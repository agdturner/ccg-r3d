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
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;

/**
 *
 * @author Andy Turner
 */
public class Universe {

    V3D_Environment e;

    Math_BigRational width;
    Math_BigRational height;

    public ArrayList<Triangle> triangles;
    public ArrayList<Tetrahedron> tetrahedra;

    public Camera camera;

    public Universe(Camera camera) {
        this.camera = camera;
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        e = new V3D_Environment();
        width = camera.pixelWidth.multiply(camera.width);
        height = camera.pixelHeight.multiply(camera.height);
    }

    public void initUtah() {
        try {
            // Read in a Utah teapot.
            STL_Reader data = new STL_Reader();
            data.readBinary(
                    Paths.get("C:", "Users", "agdtu", "src", "agdt", "java",
                            "generic", "ccg-render3d", "data",
                            "Utah_teapot_(solid).stl"));
            V3D_Point p = data.triangles.get(0).p.getP();
            Math_BigRational xmin = p.getX(e.oom);
            Math_BigRational xmax = p.getX(e.oom);
            Math_BigRational ymin = p.getY(e.oom);
            Math_BigRational ymax = p.getY(e.oom);
            Math_BigRational zmin = p.getZ(e.oom);
            Math_BigRational zmax = p.getZ(e.oom);
            for (V3D_Triangle t : data.triangles) {
                triangles.add(new Triangle(t, Color.YELLOW));
                for (var pt : t.getPoints()) {
                    Math_BigRational x = pt.getX(e.oom);
                    Math_BigRational y = pt.getY(e.oom);
                    Math_BigRational z = pt.getZ(e.oom);
                    xmin = xmin.min(x);
                    xmax = xmax.max(x);
                    ymin = ymin.min(y);
                    ymax = ymax.max(y);
                    zmin = zmin.min(z);
                    zmax = zmax.max(z);
                }
            }
            System.out.println("xmin " + xmin);
            System.out.println("xmax " + xmax);
            System.out.println("ymin " + ymin);
            System.out.println("ymax " + ymax);
            System.out.println("zmin " + zmin);
            System.out.println("zmax " + zmax);
//xmin - 8.164
//xmax 9.412
//ymin - 5.37
//ymax 5.557
//zmin 0
//zmax  8.572
        } catch (IOException ex) {
            Logger.getLogger(Universe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void init0() {
        Math_BigRational halfwidth = width.divide(2);
        Math_BigRational halfheight = height.divide(2);
        // Big Yellow Triangle
        triangles.add(new Triangle(new V3D_Triangle(
                new V3D_Point(e, halfwidth.negate(), halfheight.negate(), Math_BigRational.TEN),
                new V3D_Point(e, halfwidth.negate(), halfheight, Math_BigRational.TEN),
                new V3D_Point(e, halfwidth, halfheight, Math_BigRational.TEN)), Color.YELLOW));
        // Big Red Triangle
        triangles.add(new Triangle(new V3D_Triangle(
                new V3D_Point(e, halfwidth.negate(), halfheight.negate(), Math_BigRational.TEN),
                new V3D_Point(e, halfwidth, halfheight, Math_BigRational.TEN),
                new V3D_Point(e, halfwidth, halfheight.negate(), Math_BigRational.TEN)), Color.RED));
    }

    public void init1() {
        Math_BigRational halfwidth = width.divide(2);
        Math_BigRational halfheight = height.divide(2);
//        // Little Yellow Triangle
//        triangles.add(new Triangle(new V3D_Triangle(
//                new V3D_Point(e, halfwidth.negate(), halfheight.negate(), Math_BigRational.TEN),
//                new V3D_Point(e, halfwidth.negate(), halfheight.negate().add(4), Math_BigRational.TEN),
//                new V3D_Point(e, halfwidth.negate().add(4), halfheight.negate().add(4), Math_BigRational.TEN)), Color.YELLOW));
//        // Little Red Triangle
//        triangles.add(new Triangle(new V3D_Triangle(
//                new V3D_Point(e, halfwidth.negate().divide(2), halfheight.negate().divide(2), Math_BigRational.valueOf(5)),
//                new V3D_Point(e, Math_BigRational.ZERO, halfheight.divide(2), Math_BigRational.valueOf(5)),
//                new V3D_Point(e, halfwidth.divide(2), halfheight.divide(2).negate(), Math_BigRational.valueOf(5))), Color.RED));
        Math_BigRational quarterwidth = halfwidth.divide(2);
        Math_BigRational quarterheight = halfheight.divide(2);
        // Tetrahedra
        V3D_Tetrahedron t = new V3D_Tetrahedron(
                new V3D_Point(e, quarterwidth.negate(), Math_BigRational.ZERO, Math_BigRational.valueOf(3)),
                new V3D_Point(e, Math_BigRational.ZERO, quarterheight.negate(), Math_BigRational.valueOf(3)),
                new V3D_Point(e, Math_BigRational.ZERO, quarterheight, Math_BigRational.valueOf(3)),
                new V3D_Point(e, Math_BigRational.TEN, Math_BigRational.ZERO, quarterheight));
        tetrahedra.add(new Tetrahedron(t, Color.WHITE));
    }

    public void update() {
    }
}
