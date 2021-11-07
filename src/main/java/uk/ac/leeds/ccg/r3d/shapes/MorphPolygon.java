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
package uk.ac.leeds.ccg.r3d.shapes;

import java.awt.Color;

import uk.ac.leeds.ccg.r3d.point.MorphPoint;
import uk.ac.leeds.ccg.r3d.point.R3D_PointDoubleOffset;

public class MorphPolygon extends MyPolygon
{
	public MorphPolygon( Color color, MorphPoint... points )
	{
		super( color, points );
	}

	public MorphPolygon( MorphPoint... points )
	{
		super( points );
	}

	public void morph( double morphSpeed )
	{
		for ( R3D_PointDoubleOffset p : this.points )
		{
			( ( MorphPoint ) p ).morph( morphSpeed );
		}
	}

	@Override
	protected void createPointsArray( R3D_PointDoubleOffset[ ] points )
	{
		this.points = new R3D_PointDoubleOffset[ points.length ];
		for ( int i = 0; i < points.length; i++ )
		{
			MorphPoint p = ( MorphPoint ) points[ i ];
			this.points[ i ] = new MorphPoint( p.p1.x, p.p1.y, p.p1.z, p.p2.x, p.p2.y, p.p2.z );
		}
	}
}
