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

import uk.ac.leeds.ccg.r3d.point.MyVector;
import uk.ac.leeds.ccg.r3d.shapes.MyPolygon;
import uk.ac.leeds.ccg.r3d.shapes.Polyhedron;

public class Entity implements IEntity
{
	protected List<Polyhedron> polyhedrons;
	private MyPolygon[ ] polygons;

	public Entity( List<Polyhedron> polyhedrons )
	{
		this.polyhedrons = polyhedrons;
		List<MyPolygon> tempList = new ArrayList<>( );
		for ( Polyhedron poly : this.polyhedrons )
		{
			tempList.addAll( Arrays.asList( poly.getPolygons( ) ) );
		}
		this.polygons = new MyPolygon[ tempList.size( ) ];
		this.polygons = tempList.toArray( this.polygons );
		this.sortPolygons( );
	}

	@Override
	public void render( Graphics g )
	{
		for ( MyPolygon poly : this.polygons )
		{
			poly.render( g );
		}
	}

	@Override
	public void translate( double x, double y, double z )
	{
		for ( Polyhedron poly : this.polyhedrons )
		{
			poly.translate( x, y, z );
		}
		this.sortPolygons( );
	}

	@Override
	public void rotate( boolean CW, double xDegrees, double yDegrees, double zDegrees, MyVector lightVector )
	{
		for ( Polyhedron poly : this.polyhedrons )
		{
			poly.rotate( CW, xDegrees, yDegrees, zDegrees, lightVector );
		}
		this.sortPolygons( );
	}

	@Override
	public void setLighting( MyVector lightVector )
	{
		for ( Polyhedron poly : this.polyhedrons )
		{
			poly.setLighting( lightVector );
		}
	}

	private void sortPolygons( )
	{
		MyPolygon.sortPolygons( this.polygons );
	}
}
