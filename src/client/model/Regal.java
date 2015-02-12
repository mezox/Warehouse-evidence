package client.model;

import oracle.spatial.geometry.JGeometry;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Class:  Regal
 * Author: Martin Veselovsky
 * Date:   2.12.2014
 * Info:
 */
public class Regal
{
    public static final int       DEFAULT_WIDTH = 420;
    public static final int       DEFAULT_HEIGHT = 60;
    public static final int       SECTOR_WIDTH = 60;
    public static final int       SECTOR_HEIGHT = 30;
    public static final int       NUM_SECTORS = 7;        //regal width/SECTOR_WIDTH (sectors in row)
    public static final int       NUM_SECTORS_ALL = 14;      //NUM_SECTORS * 2 (total sector count in case)

    private int sector_width = SECTOR_WIDTH;
    private int sector_height = SECTOR_HEIGHT;
    private int num_sectors_row = NUM_SECTORS;
    private int num_sectors_all = NUM_SECTORS_ALL;

    private int regal_id;
    private String category;
    private JGeometry placement;
    private Shape shape;

    private boolean[]             sectors;    //will be used to store sectors. Maybe?
    private boolean               isVertical = false;           //case orientation flag

    public Regal()
    {
        sectors = new boolean[num_sectors_all];

        //init sectors with 'false' (empty)
        for(int i = 0; i < num_sectors_all; i++)
        {
            sectors[i] = false;
        }
    }

    public Regal(int num_sectors_all)
    {
        this.num_sectors_all = num_sectors_all;
        this.num_sectors_row = num_sectors_all / 2;

        //init sectors with 'false' (empty)
        sectors = new boolean[num_sectors_all];
        for(int i = 0; i < num_sectors_all; i++)
        {
            sectors[i] = false;
        }
    }

    /**
     * Maps JGeometry to Java's shapes
     * @param jGeometry geometry from database
     * @return geometry mapped to shape
     */
    public Shape jGeometry2Shape(JGeometry jGeometry)
    {
        Shape shape;
        switch (jGeometry.getType()) {
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
            System.out.println("line");
            shapeType = line;
            shapeTypeToDB = 1;
            etype = 2;
            gtype = 2002;
        }

        if (shapeType == 0)
            return false;

        java.util.List<Double> points = new ArrayList<Double>();

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

    // setters
    public void setRegal_id(int regal_id) {
        this.regal_id = regal_id;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setPlacement(JGeometry placement) throws NullPointerException {
        this.placement = placement;
        this.shape = jGeometry2Shape(placement);
        if (this.shape == null)
            throw new NullPointerException();

        if (shape.getBounds().getHeight() > shape.getBounds().getWidth()) {
            isVertical = true;
        }

        if(isVertical) {
            num_sectors_row = (int) shape.getBounds().getHeight() / Regal.SECTOR_WIDTH;
            num_sectors_all = (int) shape.getBounds().getHeight() / Regal.SECTOR_WIDTH * 2;
        }
        else {
            num_sectors_row = (int) shape.getBounds().getWidth() / Regal.SECTOR_WIDTH;
            num_sectors_all = (int) shape.getBounds().getWidth() / Regal.SECTOR_WIDTH * 2;
        }
    }
    public void setVerticalOrientation(boolean o)    {
        this.isVertical = o;
    }
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void setSector_width(int sector_width) {
        this.sector_width = sector_width;
    }

    public void setSector_height(int sector_height) {
        this.sector_height = sector_height;
    }

    public void setNum_sectors_row(int num_sectors_row) {
        this.num_sectors_row = num_sectors_row;
    }

    public void setNum_sectors_all(int num_sectors_all) {
        this.num_sectors_all = num_sectors_all;
    }

    // getters
    public int getSector_width() {
        return sector_width;
    }
    public int getSector_height() {
        return sector_height;
    }
    public int getNum_sectors_row() {
        return num_sectors_row;
    }
    public int getNum_sectors_all() {
        return num_sectors_all;
    }

    public int getRegal_id() {
        return regal_id;
    }
    public String getCategory() {
        return category;
    }
    public JGeometry getPlacement() {
        return placement;
    }
    public Shape getShape() {
        return shape;
    }
    public boolean[] getSectors()
    {
        return sectors;
    }
    public boolean getVerticalOrientation()
    {
        return isVertical;
    }
    public int[] getTopLeft()
    {
        int[] corner = new int[2];

        corner[0] = (int)shape.getBounds().getMinX();
        corner[1] = (int)shape.getBounds().getMinY();

        return corner;
    }

    public int[] getTopRight()
    {
        int[] corner = new int[2];

        corner[0] = (int)shape.getBounds().getMaxX();
        corner[1] = (int)shape.getBounds().getMinY();

        return corner;
    }

    public int[] getBottomLeft()
    {
        int[] corner = new int[2];

        corner[0] = (int)shape.getBounds().getMinX();
        corner[1] = (int)shape.getBounds().getMaxY();

        return corner;
    }

    public int[] getBottomRight()
    {
        int[] corner = new int[2];

        corner[0] = (int)shape.getBounds().getMaxX();
        corner[1] = (int)shape.getBounds().getMaxY();

        return corner;
    }

    public int[] getCorners()
    {
        int[] corners = new int[8];

        //top left
        corners[0] = (int)shape.getBounds().getMinX();
        corners[1] = (int)shape.getBounds().getMinY();

        //top right
        corners[2] = (int)shape.getBounds().getMaxX();
        corners[3] = (int)shape.getBounds().getMinY();

        //bottom left
        corners[4] = (int)shape.getBounds().getMinX();
        corners[5] = (int)shape.getBounds().getMaxY();

        //bottom right
        corners[6] = (int)shape.getBounds().getMaxX();
        corners[7] = (int)shape.getBounds().getMaxY();

        return corners;
    }

    /**
     * Changes case's vertical orientation
     * Used when rotating case
     */
    public void changeVerticalOrientation()
    {
        this.isVertical = !isVertical;
    }
}
