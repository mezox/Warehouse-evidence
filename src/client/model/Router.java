package client.model;

import oracle.spatial.geometry.JGeometry;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Class:
 * Author: Martin Veselovsky
 * Date:   15.12.2014
 * Info:
 */
public class Router
{
    private int router_id;
    private JGeometry point_geom;
    private JGeometry range;
    private Point2D point;
    private Shape range_shape;
    private int radius;

    // constructor
    public Router()
    {

    }

    /**
     * Maps JGeometry to Java's shapes
     * @param jGeometry geometry from database
     * @return geometry mapped to shape
     */
    public Shape jGeometry2Shape(JGeometry jGeometry)
    {
        Shape shape;

        switch (jGeometry.getType())
        {
            case JGeometry.GTYPE_POLYGON:
                shape = jGeometry.createShape();
                break;
            case JGeometry.GTYPE_COLLECTION:
                shape = jGeometry.createShape();
                break;
            default:
                return null;
        }
        return shape;
    }


    // getters
    public int getRouter_id() {
        return router_id;
    }

    public JGeometry getPoint_geom() {
        return point_geom;
    }

    public Point2D getPoint() {
        return point;
    }

    public JGeometry getRange() {
        return range;
    }

    public Shape getRangeShape() {
        return range_shape;
    }

    public int getRadius() {
        return radius;
    }

    // setters
    public void setRouter_id(int router_id) {
        this.router_id = router_id;
    }

    public void setPoint_geom(JGeometry point_geom) throws NullPointerException {
        this.point_geom = point_geom;
        this.point = point_geom.getJavaPoint();
        if (this.point == null)
            throw new NullPointerException();
    }
    public void setPoint(Point2D shape) {
        this.point = shape;

    }

    public void setRange(JGeometry range) {
        this.range = range;
        this.range_shape = jGeometry2Shape(range);
        if (this.range_shape == null)
            throw new NullPointerException();
    }
    public void setRangeShape(Shape shape) {
        this.range_shape = shape;
    }

    public void setRadius(int r) {
        this.radius = r;
    }
}
