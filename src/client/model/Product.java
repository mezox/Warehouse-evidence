package client.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Class:  Product
 * Author: Martin Veselovsky, Tomas Kubovcik
 * Date:   26.11.2014
 * Info:   Object representation of database item in Product table.
 */
public class Product
{
    private int product_id;
    private String name;
    private String manufacturer;
    private int regal_id;
    private int sector_id;
    private String valid_from;
    private String valid_to;

    public Product()
    {
    }

    // setters
    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    public void setRegal_id(int regal_id) {
        this.regal_id = regal_id;
    }
    public void setSector_id(int sector_id) {
        this.sector_id = sector_id;
    }
    public void setValidFrom(String valid_from){
        this.valid_from = valid_from;
    }
    public void setValidTo(String valid_to){
        this.valid_to = valid_to;
    }

    // getters
    public int getProduct_id() {
        return product_id;
    }
    public String getName() {
        return name;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public int getRegal_id() {
        return regal_id;
    }
    public int getSector_id() {
        return sector_id;
    }
    public String getValidFrom(){
        return valid_from;
    }
    public String getValidTo() {
        return valid_to;
    }
}
