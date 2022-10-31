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
import java.util.ArrayList;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.r3d.entities.Tetrahedron;
import uk.ac.leeds.ccg.r3d.entities.Triangle;
import uk.ac.leeds.ccg.r3d.io.STL_Reader;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Envelope;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Tetrahedron;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * A class that holds reference to visible and invisible objects.
 * @author Andy Turner
 */
public class Universe {

    /**
     * A reference to the environment. 
     */
    V3D_Environment e;

    /**
     * Envelope
     */
    V3D_Envelope envelope;
    
    /**
     * The triangles to render.
     */
    public ArrayList<Triangle> triangles;

    /**
     * The tetrahedra to render.
     */
    public ArrayList<Tetrahedron> tetrahedra;

    /**
     * A single camera.
     */
    public Camera camera;

    /**
     * Create a new instance.
     *
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public Universe(V3D_Vector offset, int oom, RoundingMode rm) {
        e = new V3D_Environment();
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        /**
         * Create a AABox centred at 0,0,0 
         */
        V3D_Point[] points = new V3D_Point[8];
        V3D_Point lbf = new V3D_Point(offset, new V3D_Vector(-1,-1,-1));
        V3D_Point lba = new V3D_Point(offset, new V3D_Vector(-1,-1,1));
        V3D_Point ltf = new V3D_Point(offset, new V3D_Vector(-1,1,-1));
        V3D_Point lta = new V3D_Point(offset, new V3D_Vector(-1,1,1));
        V3D_Point rbf = new V3D_Point(offset, new V3D_Vector(1,-1,-1));
        V3D_Point rba = new V3D_Point(offset, new V3D_Vector(1,-1,1));
        V3D_Point rtf = new V3D_Point(offset, new V3D_Vector(1,1,-1));
        V3D_Point rta = new V3D_Point(offset, new V3D_Vector(1,1,1));
        tetrahedra.add(new Tetrahedron(new V3D_Tetrahedron(lbf, ltf, lta, rtf,
                oom, rm), Color.WHITE, oom, rm));
        tetrahedra.add(new Tetrahedron(new V3D_Tetrahedron(lta, lba, lbf, rba,
                oom, rm), Color.RED, oom, rm));
        tetrahedra.add(new Tetrahedron(new V3D_Tetrahedron(lta, rta, rba, rtf,
                oom, rm), Color.YELLOW, oom, rm));
        tetrahedra.add(new Tetrahedron(new V3D_Tetrahedron(lbf, rbf, rtf, rba,
                oom, rm), new Color(Color.GREEN.getRGB()), oom, rm));
        points[0] = lbf;
        points[1] = lba;
        points[2] = ltf;
        points[3] = lta;
        points[4] = rbf;
        points[5] = rba;
        points[6] = rtf;
        points[7] = rta;
        this.envelope = new V3D_Envelope(oom, rm, points);
    }
    
    /**
     * Create a new instance.
     *
     * @param path The path of the STL binary file to be read.
     * @param offset The offset for each geometry created.
     * @param oom The Order of Magnitude for the precision.
     * @param rm The RoundingMode for any rounding.
     */
    public Universe(Path path, V3D_Vector offset, int oom, RoundingMode rm) 
            throws IOException {
        e = new V3D_Environment();
        triangles = new ArrayList<>();
        tetrahedra = new ArrayList<>();
        STL_Reader data = new STL_Reader();
        data.readBinary(path, offset, oom, rm);
        V3D_Point p = data.triangles.get(0).pl.getP();
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
        envelope = new V3D_Envelope(oom, rm, xmin, xmax, ymin, ymax, zmin, zmax);
    }

    /**
     * Set the camera.
     * 
     * @param camera What {@link #camera} is set to. 
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    
    /**
     * This will be for updating the universe from one time to the next.
     */
    public void update() {
    }
}
