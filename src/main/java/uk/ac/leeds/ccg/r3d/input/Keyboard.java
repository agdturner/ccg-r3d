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
package uk.ac.leeds.ccg.r3d.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {
	
	private final boolean[] keys = new boolean[66568];
	private boolean left, right, up, down, forward, backward;
	
	public void update() {
		this.left = this.keys[KeyEvent.VK_LEFT] || this.keys[KeyEvent.VK_A];
		this.right = this.keys[KeyEvent.VK_RIGHT] || this.keys[KeyEvent.VK_D];
		this.forward = this.keys[KeyEvent.VK_UP] || this.keys[KeyEvent.VK_W];
		this.backward = this.keys[KeyEvent.VK_DOWN] || this.keys[KeyEvent.VK_S];
		this.up = this.keys[KeyEvent.VK_SPACE];
		this.down = this.keys[KeyEvent.VK_SHIFT];
	}
	
	public boolean getDown() {
		return this.down;
	}
	
	public boolean getUp() {
		return this.up;
	}
	
	public boolean getRight() {
		return this.right;
	}
	
	public boolean getLeft() {
		return this.left;
	}
	
	public boolean getForward() {
		return this.forward;
	}
	
	public boolean getBackward() {
		return this.backward;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		keys[event.getKeyCode()] = true;
	}

	@Override
	public void keyReleased(KeyEvent event) {
		keys[event.getKeyCode()] = false;
	}

	@Override
	public void keyTyped(KeyEvent event) {
		// TODO Auto-generated method stub
		
	}

}
