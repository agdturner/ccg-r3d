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
import java.util.List;
import uk.ac.leeds.ccg.math.number.Math_BigRational;

import uk.ac.leeds.ccg.r3d.entity.builder.R3D_BasicEntityBuilder;
import uk.ac.leeds.ccg.r3d.input.ClickType;
import uk.ac.leeds.ccg.r3d.input.Keyboard;
import uk.ac.leeds.ccg.r3d.input.Mouse;
import uk.ac.leeds.ccg.r3d.input.UserInput;
import uk.ac.leeds.ccg.r3d.point.PointConverter;
import uk.ac.leeds.ccg.r3d.point.R3D_PointConverter;
import uk.ac.leeds.ccg.r3d.world.R3D_Camera;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

public class R3D_EntityManager {

    private final List<R3D_IEntity> entities;
    private int initialX, initialY;
    private final double mouseSensitivity = 2.5;
    private final double moveSpeed = 20;
    private final V3D_Vector lightVector = new V3D_Vector(1, 1, 1, -3).getUnitVector(-3);
    private Mouse mouse;
    private Keyboard keyboard;
    private final R3D_Camera camera;
    int oom;
    private V3D_Vector vleft;
    private V3D_Vector vright;
    private V3D_Vector vup;
    private V3D_Vector vdown;
    private V3D_Vector vforward;
    private V3D_Vector vbackward;

    public R3D_EntityManager() {
        this.entities = new ArrayList<>();
        this.camera = new R3D_Camera(Math_BigRational.valueOf(100), Math_BigRational.ZERO, Math_BigRational.ZERO);
    }

    public void init(UserInput userInput, int oom) {
        this.oom = oom;
        this.mouse = userInput.mouse;
        this.keyboard = userInput.keyboard;
        //this.entities.add(R3D_ComplexEntityBuilder.createRubiksCube(100, 0, 0, 0));
        this.entities.add(R3D_BasicEntityBuilder.createTriangle(100,
                Math_BigRational.ZERO,
                Math_BigRational.ZERO,
                Math_BigRational.ZERO, oom));
//        this.entities.add(R3D_BasicEntityBuilder.createTetrahedron(
//                100,
//                Math_BigRational.ZERO,
//                Math_BigRational.ZERO,
//                Math_BigRational.ZERO, oom));
        //this.entities.add(R3D_BasicEntityBuilder.createDiamond( Color.CYAN, 100, Math_BigRational.ZERO, Math_BigRational.ZERO, Math_BigRational.ZERO, oom) );
        //this.entities.add(R3D_BasicEntityBuilder.createSphere(Color.RED, 100, 25, Math_BigRational.ZERO, Math_BigRational.ZERO, Math_BigRational.ZERO, oom));
        //this.entities.add(R3D_BasicEntityBuilder.createCube(Color.RED, 100, 25, Math_BigRational.ZERO, Math_BigRational.ZERO, Math_BigRational.ZERO, oom));
        //this.entities.add( BasicEntityBuilder.createMorphCube( 100, Color.RED, 0, 0, 0 ) );
        this.setLighting();
        vleft = new V3D_Vector(0.0d, -moveSpeed, 0.0d, oom);
        vright = new V3D_Vector(0.0d, moveSpeed, 0.0d, oom);
        vup = new V3D_Vector(0.0d, 0.0d, moveSpeed, oom);
        vdown = new V3D_Vector(0.0d, 0.0d, -moveSpeed, oom);
        vforward = new V3D_Vector(-moveSpeed, 0.0d, 0.0d, oom);
        vbackward = new V3D_Vector(moveSpeed, 0.0d, 0.0d, oom);
        this.entities.forEach(e -> {
            e.translate(this.camera.v.reverse());
        });
    }

    /**
     * Updates.
     */
    public void update() {
        int x = this.mouse.getX();
        int y = this.mouse.getY();
        if (this.mouse.getButton() == ClickType.LeftClick) {
            int xDif = x - initialX;
            int yDif = y - initialY;
            this.rotate(0, -yDif / mouseSensitivity, -xDif / mouseSensitivity);
            //System.out.println("Left click");
        } else if (this.mouse.getButton() == ClickType.RightClick) {
            int xDif = x - initialX;
            this.rotate(-xDif / mouseSensitivity, 0, 0);
            //System.out.println("Right click");
        }
        if (this.mouse.isScrollingUp()) {
            R3D_PointConverter.zoomIn();
            //System.out.println("Zoom in");
        } else if (this.mouse.isScrollingDown()) {
            R3D_PointConverter.zoomOut();
            //System.out.println("Zoom out");
        }
        if (this.keyboard.getLeft()) {
            //this.camera.translate(vleft, oom);
            this.entities.forEach(e -> {
                e.translate(vright);
            });
            //System.out.println("Left");
        }
        if (this.keyboard.getRight()) {
            //this.camera.translate(vright, oom);
            this.entities.forEach(e -> {
                e.translate(vleft);
            });
            //System.out.println("Right");
        }
        if (this.keyboard.getUp()) {
            //this.camera.translate(vup, oom);
            this.entities.forEach(e -> {
                e.translate(vdown);
            });
            //System.out.println("Up");
        }
        if (this.keyboard.getDown()) {
            //this.camera.translate(vdown, oom);
            this.entities.forEach(e -> {
                e.translate(vup);
            });
            //System.out.println("Down");
        }
        if (this.keyboard.getForward()) {
            //this.camera.translate(vforward, oom);
            this.entities.forEach(e -> {
                e.translate(vbackward);
            });
            //System.out.println("Forward");
        }
        if (this.keyboard.getBackward()) {
            //this.camera.translate(vbackward, oom);
            this.entities.forEach(e -> {
                e.translate(vforward);
            });
            //System.out.println("Backward");
        }

        this.entities.stream().filter(e
                -> (e instanceof R3D_MorphEntity)).forEachOrdered(e -> {
            ((R3D_MorphEntity) e).morph(0.1, oom);
        });

        this.mouse.resetScroll();
        this.keyboard.update();

        initialX = x;
        initialY = y;
    }

    public void render(Graphics g) {
        this.entities.forEach(e -> {
            e.render(g);
        });
    }

    private void rotate(double xDegrees, double yDegrees, double zDegrees) {
        this.entities.forEach(e -> {
            e.rotate(xDegrees, yDegrees, zDegrees, this.lightVector);
        });
    }

    private void setLighting() {
        this.entities.forEach(e -> {
            e.setLighting(this.lightVector);
        });
    }

}
