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
package uk.ac.leeds.ccg.r3d.entity.builder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import uk.ac.leeds.ccg.r3d.entity.Entity;
import uk.ac.leeds.ccg.r3d.entity.IEntity;
import uk.ac.leeds.ccg.r3d.entity.MorphEntity;
import uk.ac.leeds.ccg.r3d.point.MorphPoint;
import uk.ac.leeds.ccg.r3d.point.R3D_PointDoubleOffset;
import uk.ac.leeds.ccg.r3d.shapes.MorphPolygon;
import uk.ac.leeds.ccg.r3d.shapes.MorphPolyhedron;
import uk.ac.leeds.ccg.r3d.shapes.MyPolygon;
import uk.ac.leeds.ccg.r3d.shapes.Polyhedron;

public class BasicEntityBuilder
{
	public static IEntity createCube( double size, double centerX, double centerY, double centerZ )
	{
		R3D_PointDoubleOffset p1 = new R3D_PointDoubleOffset( centerX + size / 2, centerY + -size / 2, centerZ + -size / 2 );
		R3D_PointDoubleOffset p2 = new R3D_PointDoubleOffset( centerX + size / 2, centerY + size / 2, centerZ + -size / 2 );
		R3D_PointDoubleOffset p3 = new R3D_PointDoubleOffset( centerX + size / 2, centerY + size / 2, centerZ + size / 2 );
		R3D_PointDoubleOffset p4 = new R3D_PointDoubleOffset( centerX + size / 2, centerY + -size / 2, centerZ + size / 2 );
		R3D_PointDoubleOffset p5 = new R3D_PointDoubleOffset( centerX + -size / 2, centerY + -size / 2, centerZ + -size / 2 );
		R3D_PointDoubleOffset p6 = new R3D_PointDoubleOffset( centerX + -size / 2, centerY + size / 2, centerZ + -size / 2 );
		R3D_PointDoubleOffset p7 = new R3D_PointDoubleOffset( centerX + -size / 2, centerY + size / 2, centerZ + size / 2 );
		R3D_PointDoubleOffset p8 = new R3D_PointDoubleOffset( centerX + -size / 2, centerY + -size / 2, centerZ + size / 2 );

		Polyhedron tetra = new Polyhedron( new MyPolygon( Color.BLUE, p5, p6, p7, p8 ), new MyPolygon( Color.WHITE, p1, p2, p6, p5 ), new MyPolygon( Color.YELLOW, p1, p5, p8, p4 ),
				new MyPolygon( Color.GREEN, p2, p6, p7, p3 ), new MyPolygon( Color.ORANGE, p4, p3, p7, p8 ), new MyPolygon( Color.RED, p1, p2, p3, p4 ) );

		List<Polyhedron> tetras = new ArrayList<Polyhedron>( );
		tetras.add( tetra );

		return new Entity( tetras );
	}

	public static IEntity createMorphCube( double size, Color color, double centerX, double centerY, double centerZ )
	{
		MorphPoint p1 = new MorphPoint( centerX + size / 2, centerY + -size / 2, centerZ + -size / 2, centerX + size / 4, centerY + -size / 4, centerZ + -size / 4 );
		MorphPoint p2 = new MorphPoint( centerX + size / 2, centerY + size / 2, centerZ + -size / 2, centerX + size / 2, centerY + size / 2, centerZ + -size / 1 );
		MorphPoint p3 = new MorphPoint( centerX + size / 2, centerY + size / 2, centerZ + size / 2, centerX + size / 2, centerY + size / 2, centerZ + size / 1 );
		MorphPoint p4 = new MorphPoint( centerX + size / 2, centerY + -size / 2, centerZ + size / 2, centerX + size / 2, centerY + -size / 2, centerZ + size / 4 );
		MorphPoint p5 = new MorphPoint( centerX + -size / 2, centerY + -size / 2, centerZ + -size / 2, centerX + -size / 2, centerY + -size / 2, centerZ + -size / 1 );
		MorphPoint p6 = new MorphPoint( centerX + -size / 2, centerY + size / 2, centerZ + -size / 2, centerX + -size / 2, centerY + size / 2, centerZ + -size / 4 );
		MorphPoint p7 = new MorphPoint( centerX + -size / 2, centerY + size / 2, centerZ + size / 2, centerX + -size / 2, centerY + size / 2, centerZ + size / 4 );
		MorphPoint p8 = new MorphPoint( centerX + -size / 2, centerY + -size / 2, centerZ + size / 2, centerX + -size / 2, centerY + -size / 2, centerZ + size / 1 );

		Polyhedron tetra = new MorphPolyhedron( new MorphPolygon( color, p5, p6, p7, p8 ), new MorphPolygon( color, p1, p2, p6, p5 ), new MorphPolygon( color, p1, p5, p8, p4 ),
				new MorphPolygon( color, p2, p3, p7, p6 ), new MorphPolygon( color, p4, p8, p7, p3 ), new MorphPolygon( color, p1, p4, p3, p2 ) );

		List<Polyhedron> tetras = new ArrayList<Polyhedron>( );
		tetras.add( tetra );

		return new MorphEntity( tetras );
	}

	public static IEntity createDiamond( Color color, double size, double centerX, double centerY, double centerZ )
	{
		List<Polyhedron> tetras = new ArrayList<Polyhedron>( );

		int edges = 20;
		double inFactor = 0.8;
		R3D_PointDoubleOffset bottom = new R3D_PointDoubleOffset( centerX, centerY, centerZ - size / 2 );
		R3D_PointDoubleOffset[ ] outerPoints = new R3D_PointDoubleOffset[ edges ];
		R3D_PointDoubleOffset[ ] innerPoints = new R3D_PointDoubleOffset[ edges ];
		for ( int i = 0; i < edges; i++ )
		{
			double theta = 2 * Math.PI / edges * i;
			double xPos = -Math.sin( theta ) * size / 2;
			double yPos = Math.cos( theta ) * size / 2;
			double zPos = size / 2;
			outerPoints[ i ] = new R3D_PointDoubleOffset( centerX + xPos, centerY + yPos, centerZ + zPos * inFactor );
			innerPoints[ i ] = new R3D_PointDoubleOffset( centerX + xPos * inFactor, centerY + yPos * inFactor, centerZ + zPos );
		}

		MyPolygon polygons[] = new MyPolygon[ 2 * edges + 1 ];
		for ( int i = 0; i < edges; i++ )
		{
			polygons[ i ] = new MyPolygon( outerPoints[ i ], bottom, outerPoints[ ( i + 1 ) % edges ] );
		}
		for ( int i = 0; i < edges; i++ )
		{
			polygons[ i + edges ] = new MyPolygon( outerPoints[ i ], outerPoints[ ( i + 1 ) % edges ], innerPoints[ ( i + 1 ) % edges ], innerPoints[ i ] );
		}
		polygons[ edges * 2 ] = new MyPolygon( innerPoints );

		Polyhedron tetra = new Polyhedron( color, false, polygons );
		tetras.add( tetra );

		return new Entity( tetras );
	}

	public static IEntity createSphere( Color color, double size, int resolution, double centerX, double centerY, double centerZ )
	{
		List<Polyhedron> polyhedrons = new ArrayList<Polyhedron>( );
		List<MyPolygon> polygons = new ArrayList<MyPolygon>( );

		R3D_PointDoubleOffset bottom = new R3D_PointDoubleOffset( centerX, centerY, centerZ - size / 2 );
		R3D_PointDoubleOffset top = new R3D_PointDoubleOffset( centerX, centerY, centerZ + size / 2 );

		R3D_PointDoubleOffset[ ][ ] points = new R3D_PointDoubleOffset[ resolution - 1 ][ resolution ];

		for ( int i = 1; i < resolution; i++ )
		{
			double theta = Math.PI / resolution * i;
			double zPos = -Math.cos( theta ) * size / 2;
			double currentRadius = Math.abs( Math.sin( theta ) * size / 2 );
			for ( int j = 0; j < resolution; j++ )
			{
				double alpha = 2 * Math.PI / resolution * j;
				double xPos = -Math.sin( alpha ) * currentRadius;
				double yPos = Math.cos( alpha ) * currentRadius;
				points[ i - 1 ][ j ] = new R3D_PointDoubleOffset( centerX + xPos, centerY + yPos, centerZ + zPos );
			}
		}

		for ( int i = 1; i <= resolution; i++ )
		{
			for ( int j = 0; j < resolution; j++ )
			{
				if ( i == 1 )
				{
					polygons.add( new MyPolygon( points[ i - 1 ][ j ], points[ i - 1 ][ ( j + 1 ) % resolution ], bottom ) );
				}
				else if ( i == resolution )
				{
					polygons.add( new MyPolygon( points[ i - 2 ][ ( j + 1 ) % resolution ], points[ i - 2 ][ j ], top ) );
				}
				else
				{
					polygons.add( new MyPolygon( points[ i - 1 ][ j ], points[ i - 1 ][ ( j + 1 ) % resolution ], points[ i - 2 ][ ( j + 1 ) % resolution ], points[ i - 2 ][ j ] ) );
				}
			}
		}

		MyPolygon[ ] polygonArray = new MyPolygon[ polygons.size( ) ];
		polygonArray = polygons.toArray( polygonArray );

		Polyhedron polyhedron = new Polyhedron( color, false, polygonArray );
		polyhedrons.add( polyhedron );

		return new Entity( polyhedrons );
	}

}
