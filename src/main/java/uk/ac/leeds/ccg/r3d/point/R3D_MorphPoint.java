package uk.ac.leeds.ccg.r3d.point;

import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * R3D_MorphPoint
 * 
 * @author Andy Turner
 */
public class R3D_MorphPoint extends R3D_Point {

    private static final long serialVersionUID = 1L;

    public R3D_PointDoubleOffset p1d;
    public R3D_PointDoubleOffset p2d;
    public R3D_Point p1;
    public R3D_Point p2;
    
    private int target;

    public R3D_MorphPoint(Math_BigRational x1, Math_BigRational y1, 
            Math_BigRational z1, Math_BigRational x2, 
            Math_BigRational y2, Math_BigRational z2, int oom) {
        super(x1, y1, z1);
        this.p1d = new R3D_Point(x1, y1, z1).getDouble(Math_BigRational.ONE, oom);
        this.p2d = new R3D_Point(x2, y2, z2).getDouble(Math_BigRational.ONE, oom);
        this.target = 2;
    }

    public void morph(double morphSpeed, int oom) {
        R3D_PointDoubleOffset p = this.getDouble(Math_BigRational.ONE, oom);
        double dist = Math.sqrt(Math.pow(p.x - p1d.x, 2) + Math.pow(p.y - p1d.y, 2) + Math.pow(p.z - p1d.z, 2));
        double totalDist = Math.sqrt(Math.pow(p2d.x - p1d.x, 2) + Math.pow(p2d.y - p1d.y, 2) + Math.pow(p2d.z - p1d.z, 2));
        
        
        double progress = this.target == 2 ? dist / totalDist : 1 - dist / totalDist;

        if (progress + morphSpeed >= 1) {
            if (this.target == 2) {
                this.v = new V3D_Vector(
                        Math_BigRational.valueOf(p2d.x),
                        Math_BigRational.valueOf(p2d.y),
                        Math_BigRational.valueOf(p2d.z), oom);
            } else {
                this.v = new V3D_Vector(
                        Math_BigRational.valueOf(p1d.x),
                        Math_BigRational.valueOf(p1d.y),
                        Math_BigRational.valueOf(p1d.z), oom);
            }
        } else {
            double deltaX = (p2d.x - p1d.x) * morphSpeed;
            double deltaY = (p2d.y - p1d.y) * morphSpeed;
            double deltaZ = (p2d.z - p1d.z) * morphSpeed;
            if (this.target == 2) {
                this.v = new V3D_Vector(
                        Math_BigRational.valueOf(deltaX),
                        Math_BigRational.valueOf(deltaY),
                        Math_BigRational.valueOf(deltaZ), oom);
            } else {
                this.v = new V3D_Vector(
                        Math_BigRational.valueOf(-deltaX),
                        Math_BigRational.valueOf(-deltaY),
                        Math_BigRational.valueOf(-deltaZ), oom);
            }
        }
    }

}
