package client.model;

/**
 * Created by Petr Kolacek
 */

import oracle.spatial.geometry.JGeometry;
import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Sector {

    private int sector_id;
    private String sector_name;
    private JGeometry placement;
    private Shape shape;
    private int regal_id;
    private int sector_id_inregal;

    private boolean rotated = false;    //true if in vertical rack

    // constructor
    public Sector() {

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

    /**
     * Maps Shape to Oracle's JGeometry
     * TODO for now works only for RECTANGLES
     * @return
     */
    public boolean shape2JGeometry()
    {
        JGeometry geom;

        int polygon = 1;
        int circle = 2;
        int line = 3;
        int rectangle = 4;
        int shapeTypeToDB = 0;
        int shapeType = 0;

        int etype = 1003;
        int gtype = 2003;

        if(shape instanceof Rectangle2D)
        {
            shapeType = rectangle;
        }
        else if (shape instanceof Polygon)
        {
            shapeType = polygon;
        }
        else if (shape instanceof Path2D) {
            shapeType = line;
            shapeTypeToDB = 1;
            etype = 2;
            gtype = 2002;
        }

        if (shapeType == 0)
            return false;

        List<Double> points = new ArrayList<Double>();

        if (shapeType != rectangle) {

            double[] coords = new double[6];

            for (PathIterator pi = shape.getPathIterator(null); !pi.isDone(); pi.next()) {

                for (int j=0;j<6;j++) {
                    coords[j] = 0.0;
                }

                int type = pi.currentSegment(coords);
                System.out.println(type+":"+coords[0]+","+coords[1]+","+coords[2]+","+coords[3]+","+coords[4]+","+coords[5]);

                if (shapeType == polygon || shapeType == line) {

                    if (type != PathIterator.SEG_LINETO && type != PathIterator.SEG_MOVETO) {
                        continue;
                    }

                    points.add(coords[0]);
                    points.add(coords[1]);
                }
            }
        }

        double[] ordinates = new double[points.size()];
        int k = 0;
        for (Double d : points) {
            ordinates[k++] = d;
            //System.out.println(d);
        }

        //geom = new JGeometry(gtype, 0, new int[]{1, etype, shapeTypeToDB}, ordinates);

        geom = new JGeometry(
                shape.getBounds2D().getMinX(), shape.getBounds2D().getMinY(),
                shape.getBounds2D().getMaxX(), shape.getBounds2D().getMaxY(),
                0
        );

        placement = geom;

        return true;
    }

    //setters
    public void setSector_id(int sector_id) {
        this.sector_id = sector_id;
    }

    public void setSector_name(String sector_name) {
        this.sector_name = sector_name;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void setPlacement(JGeometry placement) throws NullPointerException {
        this.placement = placement;
        this.shape = jGeometry2Shape(placement);
        if (this.shape == null)
            throw new NullPointerException();
    }

    public void setRegal_id(int regal_id) {
        this.regal_id = regal_id;
    }

    public void setSector_id_inregal(int sector_id_inregal) {
        this.sector_id_inregal = sector_id_inregal;
    }

    public void setRotated(boolean value)
    {
        this.rotated = value;
    }

    //getters
    public int getSector_id() {
        return this.sector_id;
    }

    public String getSector_name() {
        return this.sector_name;
    }

    public Shape getShape() {
        return this.shape;
    }

    public JGeometry getPlacement() {
        return this.placement;
    }

    public int getRegal_id() {
        return this.regal_id;
    }

    public int getSector_id_inregal() {
        return sector_id_inregal;
    }

    public boolean getRotated()
    {
        return rotated;
    }

    /**
     * Changes case's vertical orientation
     * Used when rotating case
     */
    public void changeVerticalOrientation()
    {
        this.rotated = !rotated;
    }

}
