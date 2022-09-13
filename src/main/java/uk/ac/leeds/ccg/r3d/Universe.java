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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.entities.Tetrahedron;
import uk.ac.leeds.ccg.r3d.entities.Triangle;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;

/**
 *
 * @author Andy Turner
 */
public class Universe {

    public ArrayList<Triangle> triangles;
    public ArrayList<Tetrahedron> tetrahedra;
    
    public Camera camera;

    public Universe(Camera camera) {
        this.camera = camera;
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        V3D_Environment e = new V3D_Environment();
        Math_BigRational halfwidth = Math_BigRational.valueOf(camera.width).divide(2);
        Math_BigRational halfheight = Math_BigRational.valueOf(camera.height).divide(2);
        // Big Yellow Triangle
        triangles.add(new Triangle(new V3D_Triangle(
                new V3D_Point(e, halfwidth.negate(), halfheight.negate(), Math_BigRational.TEN),
                new V3D_Point(e, Math_BigRational.ZERO, halfheight, Math_BigRational.TEN),
                new V3D_Point(e, halfwidth, halfheight.negate(), Math_BigRational.TEN)), Color.YELLOW));
        // Smaller Red Triangle
        triangles.add(new Triangle(new V3D_Triangle(
                new V3D_Point(e, halfwidth.negate().divide(2), halfheight.negate().divide(2), Math_BigRational.valueOf(5)),
                new V3D_Point(e, Math_BigRational.ZERO, halfheight.divide(2), Math_BigRational.valueOf(5)),
                new V3D_Point(e, halfwidth.divide(2), halfheight.divide(2).negate(), Math_BigRational.valueOf(5))), Color.RED));
        Math_BigRational quarterwidth = Math_BigRational.valueOf(camera.width).divide(4);
        Math_BigRational quarterheight = Math_BigRational.valueOf(camera.height).divide(4);
        // Tetrahedra
        V3D_Tetrahedron t = new V3D_Tetrahedron( 
                new V3D_Point(e, quarterwidth.negate(), Math_BigRational.ZERO, Math_BigRational.valueOf(3)),
                new V3D_Point(e, Math_BigRational.ZERO, quarterheight.negate(), Math_BigRational.valueOf(3)),
                new V3D_Point(e, Math_BigRational.ZERO, quarterheight, Math_BigRational.valueOf(3)),
                new V3D_Point(e, Math_BigRational.TEN, Math_BigRational.ZERO, quarterheight));
        tetrahedra.add(new Tetrahedron(t, Color.WHITE));
    }
    
    public void update(){}
}
