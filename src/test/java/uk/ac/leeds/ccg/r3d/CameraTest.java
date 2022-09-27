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

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import uk.ac.leeds.ccg.grids.d2.Grids_2D_ID_long;
import uk.ac.leeds.ccg.grids.d2.grid.i.Grids_GridInt;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.v3d.core.V3D_Environment;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Line;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Rectangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Triangle;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 *
 * @author Andy Turner
 */
public class CameraTest {
    
    public CameraTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getRCs method, of class Camera.
     */
    @Test
    public void testGetRCs() {
        System.out.println("getRCs");
        int width = 100;
        int height = 75;
        int oom = -3;
        V3D_Environment e = new V3D_Environment();
        Math_BigRational ptx = Math_BigRational.ZERO;
        Math_BigRational pty = Math_BigRational.ZERO;
        Math_BigRational ptz = Math_BigRational.valueOf(height).negate();
        V3D_Point pt = new V3D_Point(e, ptx, pty, ptz);
        Math_BigRational halfwidth = Math_BigRational.valueOf(width).divide(2);
        Math_BigRational halfheight = Math_BigRational.valueOf(height).divide(2);
        Math_BigRational xmin = ptx.subtract(halfwidth);
        Math_BigRational xmax = ptx.add(halfwidth);
        Math_BigRational ymin = pty.subtract(halfheight);
        Math_BigRational ymax = pty.add(halfheight);
        Math_BigRational depth = Math_BigRational.ZERO;
        V3D_Rectangle screen = new V3D_Rectangle(
                new V3D_Point(e, xmin, ymin, depth),
                new V3D_Point(e, xmin, ymax, depth),
                new V3D_Point(e, xmax, ymax, depth),
                new V3D_Point(e, xmax, ymin, depth));
        Camera instance = null;
        try {
            instance = new Camera(pt, screen, width, height, oom);
        } catch (Exception ex) {
            Logger.getLogger(CameraTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Universe universe = new Universe(instance);
        universe.init0();
        V3D_Triangle t = universe.triangles.get(0).triangle;
        Set<Grids_2D_ID_long> expResult = new HashSet<>();
        expResult.add(new Grids_2D_ID_long(68, 6));
        expResult.add(new Grids_2D_ID_long(68, 7));
        expResult.add(new Grids_2D_ID_long(68, 8));
        expResult.add(new Grids_2D_ID_long(69, 6));
        expResult.add(new Grids_2D_ID_long(69, 7));
        expResult.add(new Grids_2D_ID_long(70, 6));
        Set<Grids_2D_ID_long> result = instance.getRCs(t);
//        for (int i = 0; i < expResult.size(); i ++) {
//            assertEquals(expResult.get(i), result.get(i));
//        }
        assertEquals(expResult, result);
    }
    
}
