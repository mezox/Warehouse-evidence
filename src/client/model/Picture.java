package client.model;

import com.sun.corba.se.impl.interceptors.PICurrent;
import oracle.ord.im.OrdImage;
import oracle.sql.BLOB;

import java.awt.*;
import java.sql.SQLException;

/**
 * Class:
 * Author: Martin Veselovsky
 * Date:   5.12.2014
 * Info:
 */
public class Picture {

    private int picture_id;
    private int product_id;
    private OrdImage ordImage;
    private Image image;
    private String desc;
    private String path;

    // constructors
    public Picture(OrdImage img, String desc) {
        this.ordImage = img;
        this.desc = desc;
    }
    public Picture(Image img, String desc) {
        this.image = img;
        this.desc = desc;
    }
    public Picture(String path, String desc) {
        this.path = path;
        this.desc = desc;
    }


    // setters
    public void setOrdImage(OrdImage ordImage) {
        this.ordImage = ordImage;
    }
    public void setImage(Image image) {
        this.image = image;
    }
    public void setPicture_id(int picture_id) {
        this.picture_id = picture_id;
    }
    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    // getters
    public int getPicture_id() {
        return picture_id;
    }
    public int getProduct_id() {
        return product_id;
    }
    public OrdImage getOrdImg() {
        return ordImage;
    }
    public Image getImg() {
        return image;
    }
    public String getDesc() {
        return desc;
    }
    public String getPath() {
        return path;
    }
}
