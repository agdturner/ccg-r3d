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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Mouse implements MouseListener, MouseMotionListener, MouseWheelListener {
	
	private int mouseX = -1;
	private int mouseY = -1;
	private int mouseB = -1;
	private int scroll = 0;
	
	public int getX() {
		return this.mouseX;
	}
	
	public int getY() {
		return this.mouseY;
	}
	
	public boolean isScrollingUp() {
		return this.scroll == -1;
	}
	
	public boolean isScrollingDown() {
		return this.scroll == 1;
	}
	
	public void resetScroll() {
		this.scroll = 0;
	}
	
	public ClickType getButton() {
		switch(this.mouseB) {
		case 1:
			return ClickType.LeftClick;
		case 2:
			return ClickType.ScrollClick;
		case 3:
			return ClickType.RightClick;
		case 4:
			return ClickType.BackPage;
		case 5:
			return ClickType.ForwardPage;
		default:
			return ClickType.Unknown;
		}
	}
	
	public void resetButton() {
		this.mouseB = -1;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		scroll = event.getWheelRotation();
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		this.mouseX = event.getX();
		this.mouseY = event.getY();
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		this.mouseX = event.getX();
		this.mouseY = event.getY();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent event) {
		this.mouseB = event.getButton();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		this.mouseB = -1;
	}

}
