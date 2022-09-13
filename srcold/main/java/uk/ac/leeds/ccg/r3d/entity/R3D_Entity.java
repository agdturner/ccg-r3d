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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.leeds.ccg.r3d.shapes.R3D_Triangle;
import uk.ac.leeds.ccg.r3d.shapes.R3D_Polyhedron;
import uk.ac.leeds.ccg.r3d.shapes.R3D_Tetrahedron;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class R3D_Entity implements R3D_IEntity {

    protected List<R3D_Polyhedron> polyhedrons;
    protected List<R3D_Tetrahedron> tetrahedrons;
    protected List<R3D_Triangle> triangles;

    private R3D_Triangle[] allTriangles;

    public R3D_Entity(List<R3D_Polyhedron> polyhedrons) {
        this.polyhedrons = polyhedrons;
        List<R3D_Triangle> tempList = new ArrayList<>();
        for (R3D_Polyhedron p : polyhedrons) {
            R3D_Tetrahedron[] tetrahedrons = p.getTetrahedrons();
            for (R3D_Tetrahedron t : tetrahedrons) {
                tempList.addAll(Arrays.asList(t.getTriangles()));
            }
        }
        this.allTriangles = new R3D_Triangle[tempList.size()];
        this.allTriangles = tempList.toArray(this.allTriangles);
        this.sort();
    }

    public R3D_Entity(R3D_Triangle... triangles) {
        this.triangles = Arrays.asList(triangles);
        this.allTriangles = triangles;
        this.sort();
    }

    @Override
    public void render(Graphics g) {
        for (var t : this.allTriangles) {
            t.render(g);
        }
    }

    @Override
    public void translate(V3D_Vector v) {
        if (this.polyhedrons != null) {
            this.polyhedrons.forEach(p -> {
                p.translate(v);
            });
        }
        if (this.tetrahedrons != null) {
            this.tetrahedrons.forEach(t -> {
                t.translate(v);
            });
        }
        if (this.triangles != null) {
            this.triangles.forEach(t -> {
                t.translate(v);
            });
        }
        this.sort();
    }

    @Override
    public void rotate(double xDegrees, double yDegrees, double zDegrees, V3D_Vector lightVector) {
        if (this.polyhedrons != null) {
            this.polyhedrons.forEach(p -> {
                p.rotate(xDegrees, yDegrees, zDegrees, lightVector);
            });
        }
        if (this.tetrahedrons != null) {
            this.tetrahedrons.forEach(t -> {
                t.rotate(xDegrees, yDegrees, zDegrees, lightVector);
            });
        }
        if (this.triangles != null) {
            this.triangles.forEach(t -> {
                t.rotate(xDegrees, yDegrees, zDegrees, lightVector);
            });
        }
        this.sort();
    }

    @Override
    public void setLighting(V3D_Vector lightVector) {
        if (this.polyhedrons != null) {
            this.polyhedrons.forEach(p -> {
                p.setLighting(lightVector);
            });
        }
        if (this.tetrahedrons != null) {
            this.tetrahedrons.forEach(t -> {
                t.setLighting(lightVector);
            });
        }
        if (this.triangles != null) {
            this.triangles.forEach(t -> {
                t.setLighting(lightVector);
            });
        }
    }

    private void sort() {
        R3D_Triangle.sort(this.allTriangles);
    }
}
