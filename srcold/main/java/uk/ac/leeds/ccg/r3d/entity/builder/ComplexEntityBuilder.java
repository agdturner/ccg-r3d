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
import uk.ac.leeds.ccg.r3d.point.R3D_PointDoubleOffset;
import uk.ac.leeds.ccg.r3d.shapes.MyPolygon;
import uk.ac.leeds.ccg.r3d.shapes.Polyhedron;

public class ComplexEntityBuilder
{

	public static IEntity createRubiksCube( double size, double centerX, double centerY, double centerZ )
	{
		List<Polyhedron> tetras = new ArrayList<Polyhedron>( );

		double cubeSpacing = 10;

		for ( int i = -1; i < 2; i++ )
		{
			double cubeCenterX = i * ( size + cubeSpacing ) + centerX;
			for ( int j = -1; j < 2; j++ )
			{
				double cubeCenterY = j * ( size + cubeSpacing ) + centerY;
				for ( int k = -1; k < 2; k++ )
				{
					if ( i == 0 && j == 0 && k == 0 )
						continue;
					double cubeCenterZ = k * ( size + cubeSpacing ) + centerZ;

					R3D_PointDoubleOffset p1 = new R3D_PointDoubleOffset( cubeCenterX - size / 2, cubeCenterY - size / 2, cubeCenterZ - size / 2 );
					R3D_PointDoubleOffset p2 = new R3D_PointDoubleOffset( cubeCenterX - size / 2, cubeCenterY - size / 2, cubeCenterZ + size / 2 );
					R3D_PointDoubleOffset p3 = new R3D_PointDoubleOffset( cubeCenterX - size / 2, cubeCenterY + size / 2, cubeCenterZ - size / 2 );
					R3D_PointDoubleOffset p4 = new R3D_PointDoubleOffset( cubeCenterX - size / 2, cubeCenterY + size / 2, cubeCenterZ + size / 2 );
					R3D_PointDoubleOffset p5 = new R3D_PointDoubleOffset( cubeCenterX + size / 2, cubeCenterY - size / 2, cubeCenterZ - size / 2 );
					R3D_PointDoubleOffset p6 = new R3D_PointDoubleOffset( cubeCenterX + size / 2, cubeCenterY - size / 2, cubeCenterZ + size / 2 );
					R3D_PointDoubleOffset p7 = new R3D_PointDoubleOffset( cubeCenterX + size / 2, cubeCenterY + size / 2, cubeCenterZ - size / 2 );
					R3D_PointDoubleOffset p8 = new R3D_PointDoubleOffset( cubeCenterX + size / 2, cubeCenterY + size / 2, cubeCenterZ + size / 2 );

					MyPolygon polyRed = new MyPolygon( Color.RED, p5, p6, p8, p7 );
					MyPolygon polyWhite = new MyPolygon( Color.WHITE, p2, p4, p8, p6 );
					MyPolygon polyBlue = new MyPolygon( Color.BLUE, p3, p7, p8, p4 );
					MyPolygon polyGreen = new MyPolygon( Color.GREEN, p1, p2, p6, p5 );
					MyPolygon polyOrange = new MyPolygon( new Color( 255, 120, 30 ), p1, p3, p4, p2 );
					MyPolygon polyYellow = new MyPolygon( Color.YELLOW, p1, p5, p7, p3 );

					Polyhedron tetra = new Polyhedron( polyRed, polyWhite, polyBlue, polyGreen, polyOrange, polyYellow );
					tetras.add( tetra );
				}
			}
		}

		return new Entity( tetras );
	}

}
