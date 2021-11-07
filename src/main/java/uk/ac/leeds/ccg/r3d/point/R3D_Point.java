package uk.ac.leeds.ccg.r3d.point;

import java.io.Serializable;
import uk.ac.leeds.ccg.math.number.Math_BigRational;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Point;
import uk.ac.leeds.ccg.v3d.geometry.V3D_Vector;

/**
 * A point with an offset.
 *
 * @author Andy Turner
 */
public class R3D_Point implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The point before {@link #v} is applied.
     */
    private V3D_Point p;

    /**
     * The vector used to get a relative position of the point for rendering 
     * purposes.
     */
    public V3D_Vector v;

    /**
     * Create a new instance.
     *
     * @param p Used to initialise {@link #p} and {@link #v}. Deep copying
     * results in {@code this} being independent of {@code p}.
     */
    public R3D_Point(R3D_Point p) {
        this.p = new V3D_Point(p.p);
        this.v = new V3D_Vector(p.v);
    }

    /**
     * Create a new instance.
     *
     * @param x What {@link #x} is set to.
     * @param y What {@link #y} is set to.
     * @param z What {@link #z} is set to.
     */
    public R3D_Point(Math_BigRational x, Math_BigRational y, Math_BigRational z) {
        p = new V3D_Point(x, y, z);
        v = V3D_Vector.ZERO;
    }

    /**
     * Create a new instance.
     *
     * @param x What {@link #x} is set to.
     * @param y What {@link #y} is set to.
     * @param z What {@link #z} is set to.
     */
    public R3D_Point(double x, double y, double z) {
        p = new V3D_Point(x, y, z);
        v = V3D_Vector.ZERO;
    }

    /**
     * @return The relative point.
     */
    public V3D_Point get(Math_BigRational scale, int oom) {
        return p.apply(v.multiply(scale, oom), oom);
    }

    /**
     * @return The relative point.
     */
    public V3D_Point get(int oom) {
        return p.apply(v, oom);
    }

    /**
     * @return The relative point as a R3D_PointDoubleOffset
     */
    public R3D_PointDoubleOffset getDouble(Math_BigRational scale, int oom) {
        V3D_Point pt = get(scale, oom);
        R3D_PointDoubleOffset r = new R3D_PointDoubleOffset(
                pt.x.toBigDecimal(oom).doubleValue(),
                pt.y.toBigDecimal(oom).doubleValue(),
                pt.z.toBigDecimal(oom).doubleValue());
        return r;
    }

    /**
     * @return The relative point as a R3D_PointDoubleOffset
     */
    public R3D_PointDoubleOffset getDouble(int oom) {
        V3D_Point pt = get(oom);
        R3D_PointDoubleOffset r = new R3D_PointDoubleOffset(
                pt.x.toBigDecimal(oom).doubleValue(),
                pt.y.toBigDecimal(oom).doubleValue(),
                pt.z.toBigDecimal(oom).doubleValue());
        return r;
    }
}
