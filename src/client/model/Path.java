package client.model;

import oracle.spatial.geometry.JGeometry;

import java.awt.*;

/**
 * Class:
 * Author: Martin Veselovsky
 * Date:   15.12.2014
 * Info:
 */
public class Path {

    private int path_id;
    private JGeometry geometry;
    private Shape shape;

    public Path(){

    }

    public int getPath_id() {
        return path_id;
    }

    public void setPath_id(int path_id) {
        this.path_id = path_id;
    }

    public JGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(JGeometry geometry) throws NullPointerException {
        this.geometry = geometry;
        this.shape = geometry.createShape();
        if (this.shape == null)
            throw new NullPointerException();
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
