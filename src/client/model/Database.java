package client.model;

import client.controller.SimilarityResults;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.pool.OracleDataSource;
import oracle.ord.im.OrdImage;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.BLOB;
import oracle.sql.STRUCT;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.io.File;

/**
 * Class:  Database
 * Author: Martin Veselovsky, Petr Kolacek
 * Date:   26.11.2014
 * Info:   Create connection to database with specified username and password.
 *         Load items from DB to intern object representation. Class also provides
 *         all operations with DB.
 */
public class Database {

    private static final String CString = "jdbc:oracle:thin:@gort.fit.vutbr.cz:1521:dbgort";
    private Connection con = null;

    private List<Product> products = new ArrayList<Product>();
    private List<Picture> pictures = new ArrayList<Picture>();
    private List<Sector> sectors = new ArrayList<Sector>();
    private List<Regal> regals = new ArrayList<Regal>();
    private List<Router> routers = new ArrayList<Router>();
    private List<Path> paths = new ArrayList<Path>();

    private List<SimilarityResults> ordered = new ArrayList<SimilarityResults>();    // list of similarity results
    private List<Product> searchedProducts = new ArrayList<Product>();      // list of search results
    private List<Regal> searchedRegals = new ArrayList<Regal>();      // list of search results

    private String username;
    private String password;

    private int sector_mark = 0;        //mark of a sector used as sector name
    private int pict_index = 0;         // current picture
    private int pict_id = 0;
    private Product lastClicked = null;

    public Database(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    //<editor-fold desc="Regal methods: add, delete, update, searchRegalsByDate">
    public void addRegal(Regal regal) throws SQLException{

        PreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // regal geometry
            STRUCT regal_geometry = JGeometry.store(con, regal.getPlacement());

            // SQL query
            ps = con.prepareStatement("INSERT INTO REGALS VALUES(REGALS_SEQ.nextval,?,?)");
            ps.setString(1, regal.getCategory());
            ps.setObject(2, regal_geometry);

            if (ps.executeUpdate() > 0) {
                System.out.println("Regal " + regal.getRegal_id() + " (" + regal.getCategory() + ") inserted!");
            } else {
                System.out.println("Regal inserting failed!");
            }

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select REGALS_SEQ.currval FROM DUAL");
            if (rs.next())
                regal.setRegal_id(rs.getInt(1));

            regals.add(regal);

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }
    public void deleteRegal(Regal regal) throws SQLException {

        PreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // SQL query
            ps = con.prepareStatement("DELETE FROM REGALS WHERE regal_id =?");
            ps.setInt(1, regal.getRegal_id());

            if (ps.executeUpdate() > 0) {
                System.out.println("Regal " + regal.getRegal_id() + " (" + regal.getCategory() + ") deleted!");
            } else {
                System.out.println("Regal deleting failed!");
            }

            // delete from local lists
            List<Sector> toDelete = new ArrayList<Sector>();
            for (Sector s: sectors) {
                if (s.getRegal_id() == regal.getRegal_id())
                    toDelete.add(s);
            }
            for (Sector s: toDelete)
                sectors.remove(s);
            regals.remove(regal);

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }
    public void updateRegal(Regal regal) {
        PreparedStatement ps = null;
        PreparedStatement pss = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // regal geometry
            STRUCT regal_geometry = JGeometry.store(con, regal.getPlacement());

            // SQL query
            ps = con.prepareStatement("UPDATE REGALS SET category=?,placement=? WHERE regal_id=?");
            ps.setString(1, regal.getCategory());
            ps.setObject(2, regal_geometry);
            ps.setInt(3, regal.getRegal_id());

            if (ps.executeUpdate() > 0) {
                System.out.println("Regal " + regal.getRegal_id() + " (" + regal.getCategory() + ") updated!");
            } else {
                System.out.println("Regal updating failed!");
            }

            // SQL query for update all sectors inside regal
            for (Sector sector: sectors) {
                if (sector.getRegal_id() == regal.getRegal_id()) {
                    pss = con.prepareStatement(
                        "UPDATE SECTORS SET SECTOR_PLACEMENT = ? WHERE SECTOR_ID = ?");
                    STRUCT sector_geometry = JGeometry.store(con, sector.getPlacement());
                    pss.setObject(1, sector_geometry);
                    pss.setObject(2, sector.getSector_id());
                    pss.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
//            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void searchRegalsByDate(String valid_from, String valid_to) {

        OraclePreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // SQL query
            ps = (OraclePreparedStatement) con.prepareStatement (
                    "SELECT DISTINCT REGALS.REGAL_ID, CATEGORY FROM REGALS " +
                    "INNER JOIN PRODUCTS ON REGALS.REGAL_ID = PRODUCTS.REGAL_ID " +
                    "WHERE PRODUCT_ID NOT IN " +
                        "(SELECT PRODUCT_ID FROM PRODUCTS WHERE " +
                        "(VALID_FROM NOT BETWEEN ? AND ?) AND " +
                        "(VALID_TO   NOT BETWEEN ? AND ?) " +
                    ")"
            );

            SimpleDateFormat date = new SimpleDateFormat("yyyy-mm-dd");
            java.sql.Date from = new java.sql.Date(date.parse(valid_from).getTime());
            java.sql.Date to = new java.sql.Date(date.parse(valid_to).getTime());

            ps.setDate(1, from);
            ps.setDate(2, to);
            ps.setDate(3, from);
            ps.setDate(4, to);

            searchedRegals.clear();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Regal r = new Regal();
                r.setRegal_id(rs.getInt("regal_id"));
                r.setCategory(rs.getString("category"));
                
                searchedRegals.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Product methods: add, delete, update, searchProductsByDate, computeAreaOfProducts">
    /**
     * Add product to database
     * @param product Product to add
     * @throws SQLException
     */
    public void addProduct(Product product) throws SQLException{

        PreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // SQL query
            ps = con.prepareStatement("INSERT INTO PRODUCTS " +
                "VALUES(PRODUCTS_SEQ.nextval,?,?,?,?,TO_DATE(?, 'yyyy-mm-dd'),TO_DATE(?, 'yyyy-mm-dd'))");
            ps.setString(1, product.getName());
            ps.setString(2, product.getManufacturer());
            ps.setInt(3, product.getRegal_id());
            ps.setInt(4, product.getSector_id());
            ps.setString(5, product.getValidFrom());
            ps.setString(6, product.getValidTo());

            if (ps.executeUpdate() > 0){
                System.out.println("Product inserted!");
            } else {
                System.out.println("Product inserting failed!");
            }

            // get assigned product id
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select PRODUCTS_SEQ.currval FROM DUAL");
            if (rs.next())
                product.setProduct_id(rs.getInt(1));
            products.add(product);

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }

    /**
     * Delete product from database
     * @param product Product to delete
     * @throws SQLException
     */
    public void deleteProduct(Product product) throws SQLException{

        PreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // SQL query
            ps = con.prepareStatement("DElETE FROM PRODUCTS WHERE product_id=?");
            ps.setInt(1, product.getProduct_id());

            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("Product deleted!");
            } else {
                System.out.println("Product delete failed!");
            }

            products.remove(product);

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }

    /**
     * Update product in database
     * @param product Product to update
     * @throws SQLException
     */
    public void updateProduct(Product product) throws SQLException{

        OraclePreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            System.out.println("autocommit state: " + con.getAutoCommit());

            // SQL query
            ps = (OraclePreparedStatement) con.prepareStatement
                    ("UPDATE PRODUCTS " +
                        "SET name=?, manufacturer=?,regal_id=?,sector_id=? " +
                        "WHERE product_id = ?");


//            ("UPDATE PRODUCTS " +
//                    "SET name=?, manufacturer=?,regal_id=?,sector_id=?,valid_from=?,valid_to=? " +
//                    "WHERE product_id = ?");

            ps.setString(1, product.getName());
            ps.setString(2, product.getManufacturer());
            ps.setInt(3, product.getRegal_id());
            ps.setInt(4, product.getSector_id());
            ps.setInt(5, product.getProduct_id());
            //ps.setDate(6, product.getValidFrom());
            //ps.setDate(7, product.getValidTo());

            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("Product updated!");
            } else {
                System.out.println("Product update failed!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }

    /**
     * Search all product which were valid between chosen date interval
     * @param valid_from star date of interval
     * @param valid_to end date of interval
     */
    public void searchProductsByDate(String valid_from, String valid_to) {

        OraclePreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // SQL query
            ps = (OraclePreparedStatement) con.prepareStatement (
            "SELECT * FROM PRODUCTS WHERE PRODUCT_ID NOT IN " +
                "(SELECT PRODUCT_ID FROM PRODUCTS WHERE " +
                    "(VALID_FROM NOT BETWEEN ? AND ?) AND " +
                    "(VALID_TO   NOT BETWEEN ? AND ?) )"
            );

            SimpleDateFormat date = new SimpleDateFormat("yyyy-mm-dd");
            java.sql.Date from = new java.sql.Date(date.parse(valid_from).getTime());
            java.sql.Date to = new java.sql.Date(date.parse(valid_to).getTime());

            ps.setDate(1, from);
            ps.setDate(2, to);
            ps.setDate(3, from);
            ps.setDate(4, to);

            searchedProducts.clear();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProduct_id(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setManufacturer(rs.getString("manufacturer"));
                p.setValidFrom(rs.getDate("valid_from").toString());
                p.setValidTo(rs.getDate("valid_to").toString());

                searchedProducts.add(p);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compute area of all used sectors.
     * @return area
     */
    public String computeAreaOfProducts() {
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        try {
            // create a OracleDataSource instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // connect to the database
            con = ods.getConnection(this.username, this.password);

            ps = con.prepareStatement(
                "SELECT SDO_GEOM.SDO_AREA( SDO_AGGR_UNION( SDOAGGRTYPE( s.SECTOR_PLACEMENT, 0.005)), 0.005) area " +
                "FROM SECTORS s INNER JOIN PRODUCTS ON s.SECTOR_ID = PRODUCTS.SECTOR_ID");

            ps2 = con.prepareStatement(
                "SELECT SDO_GEOM.SDO_AREA( SDO_AGGR_UNION( SDOAGGRTYPE( r.PLACEMENT, 0.005)), 0.005) area FROM REGALS r"
            );

            ResultSet rs = ps.executeQuery();
            ResultSet rs2 = ps2.executeQuery();
            if (rs.next() && rs2.next()) {
                int i = rs.getInt("area");
                int j = rs2.getInt("area");

                return Integer.toString(i) + " / " + Integer.toString(j) + "  ( " + i/(j/100) +" % )";
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Pictures methods: loadPictures, add, delete, rotate, getSimilarImage">
    public void loadPictures(Product product, int pict_id) {

        // when click on same product but different picture in similarity results list
        if (product == lastClicked) {
            this.pict_id = pict_id;
        }
        else {

            OraclePreparedStatement ps = null;
            OracleResultSet prs = null;

            try {
                // create an oracle data source instance
                OracleDataSource ods = new OracleDataSource();

                // set connection string
                ods.setURL(CString);

                // get connection
                con = ods.getConnection(this.username, this.password);

                // load pictures
                Statement stmt1 = con.createStatement();

                prs = (OracleResultSet) stmt1.executeQuery
                    ("select * from PICTURES where PRODUCT_ID = " + product.getProduct_id());

                pictures.clear();
                while (prs.next()) {
                    // Create Image from Image Input Stream
                    OrdImage imgProxy = (OrdImage) prs.getORAData("picture", OrdImage.getORADataFactory());

                    // converting OrdImage to Image
                    InputStream is = new BufferedInputStream(imgProxy.getContent().getBinaryStream());
                    ImageInputStream iis = ImageIO.createImageInputStream(is);
                    Picture picture = new Picture(ImageIO.read(iis), prs.getString("description"));

                    picture.setPicture_id(prs.getInt("PICTURE_ID"));
                    picture.setProduct_id(prs.getInt("PRODUCT_ID"));

                    pictures.add(picture);
                }

                pict_index = 0;
                this.pict_id = pict_id;
                this.lastClicked = product;

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param picture Picture to add
     * @param product Picture to add to this product
     * @throws SQLException
     */
    public void addPicture(Picture picture, Product product) throws SQLException {

        OraclePreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection and set false autocommit
            con = ods.getConnection(this.username, this.password);
            con.setAutoCommit(false);

            // SQL query to insert blank record
            OraclePreparedStatement pstmt = (OraclePreparedStatement)
                    con.prepareStatement("INSERT INTO PICTURES (PICTURE_ID, PRODUCT_ID, PICTURE) " +
                            "VALUES (PICTURES_SEQ.nextval, ?, ORDSYS.ORDImage.init())");
            pstmt.setInt(1, product.getProduct_id());
            pstmt.executeUpdate();
            pstmt.close();

            // SQL query to select inserted record
            Statement stmt2 = con.createStatement();
            String query = "SELECT PICTURE_ID, PICTURE FROM PICTURES ORDER BY PICTURE_ID DESC FOR UPDATE";
            OracleResultSet rs2 = (OracleResultSet) stmt2.executeQuery(query);
            rs2.next();
            OrdImage imageProxy = (OrdImage) rs2.getORAData("PICTURE", OrdImage.getORADataFactory());
            picture.setPicture_id(rs2.getInt("PICTURE_ID"));
            rs2.close();
            stmt2.close();

            // load image from file
            imageProxy.loadDataFromFile(picture.getPath());
            imageProxy.setProperties();

            // update inserted blank record with image
            query = "UPDATE PICTURES SET PICTURE = ? WHERE PICTURE_ID = ?";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(query);
            opstmt.setORAData(1, imageProxy);
            opstmt.setInt(2, picture.getPicture_id());
            opstmt.executeUpdate();
            opstmt.close();

            // --
            // create SI_StillImage and compute image properties
            PreparedStatement pstmtUpdate2 = con.prepareStatement(
                "UPDATE PICTURES p " +
                "SET p.PICTURE_SI = SI_StillImage(p.PICTURE.getContent()) " +
                "WHERE PICTURE_ID = ?");
            try {
                pstmtUpdate2.setInt(1, picture.getPicture_id());
                pstmtUpdate2.executeUpdate();
            } finally {
                pstmtUpdate2.close();
            }
            PreparedStatement pstmtUpdate3 = con.prepareStatement(
                "UPDATE PICTURES SET " +
                    "PICTURE_AC = SI_AverageColor(PICTURE_SI), " +
                    "PICTURE_CH = SI_ColorHistogram(PICTURE_SI), " +
                    "PICTURE_PC = SI_PositionalColor(PICTURE_SI), " +
                    "PICTURE_TX = SI_Texture(PICTURE_SI) " +
                "WHERE PICTURE_ID = ?");
            try {
                pstmtUpdate3.setInt(1, picture.getPicture_id());
                pstmtUpdate3.executeUpdate();
            } finally {
                pstmtUpdate3.close();
            }

            // ---
            // convert OrdImage to Java Image
            BLOB blob = imageProxy.getContent();
            int length = (int) blob.length();
            byte[] bytes = blob.getBytes(1, length);
            Image img = Toolkit.getDefaultToolkit().createImage(bytes);
            picture.setImage(img);

            pictures.add(picture);
            pict_index = pictures.size() - 1;

            con.commit();

            System.out.println("Picture inserted!");

        } catch (SQLException e) {
            e.printStackTrace();    // may be error while converting OrdImage
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO exception");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }

    /**
     * @param picture Picture to delete
     */
    public void deletePicture(Picture picture) throws SQLException {

        PreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // SQL query
            ps = con.prepareStatement("DELETE FROM PICTURES WHERE PICTURE_ID=?");
            ps.setInt(1, picture.getPicture_id());

            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("Picture deleted!");
            } else {
                System.out.println("Picture delete failed!");
            }

            pictures.remove(picture);
            pict_index = pictures.size() - 1;

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }

    /**
     * @param picture Picture to update
     */
    public void rotatePicture(Picture picture) {
        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection and set false autocommit
            con = ods.getConnection(this.username, this.password);

            // call defined procedure
            PreparedStatement stmt = con.prepareStatement("CALL ROTATE_IMAGE(?)");
            stmt.setInt(1, picture.getPicture_id());
            stmt.execute();

            // SQL query for get rotated image
            Statement stmt2 = con.createStatement();
            String query = "SELECT PICTURE FROM PICTURES WHERE PICTURE_ID = " + picture.getPicture_id();
            OracleResultSet rs2 = (OracleResultSet) stmt2.executeQuery(query);
            rs2.next();
            OrdImage imageProxy = (OrdImage) rs2.getORAData("PICTURE", OrdImage.getORADataFactory());
            rs2.close();
            stmt2.close();

            // --
            // create SI_StillImage and compute image properties
            PreparedStatement pstmtUpdate2 = con.prepareStatement(
                "UPDATE PICTURES p " +
                    "SET p.PICTURE_SI = SI_StillImage(p.PICTURE.getContent()) " +
                    "WHERE PICTURE_ID = ?");
            try {
                pstmtUpdate2.setInt(1, picture.getPicture_id());
                pstmtUpdate2.executeUpdate();
            } finally {
                pstmtUpdate2.close();
            }
            PreparedStatement pstmtUpdate3 = con.prepareStatement(
                "UPDATE PICTURES SET " +
                    "PICTURE_AC = SI_AverageColor(PICTURE_SI), " +
                    "PICTURE_CH = SI_ColorHistogram(PICTURE_SI), " +
                    "PICTURE_PC = SI_PositionalColor(PICTURE_SI), " +
                    "PICTURE_TX = SI_Texture(PICTURE_SI) " +
                "WHERE PICTURE_ID = ?");
            try {
                pstmtUpdate3.setInt(1, picture.getPicture_id());
                pstmtUpdate3.executeUpdate();
            } finally {
                pstmtUpdate3.close();
            }


            // convert OrdImage to Java Image
            BLOB blob = imageProxy.getContent();
            int length = (int) blob.length();
            byte[] bytes = blob.getBytes(1, length);
            Image img = Toolkit.getDefaultToolkit().createImage(bytes);
            picture.setImage(img);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param picture find similar to this
     */
    public void getSimilarImage(Picture picture, Product product, double wAC, double wCH, double wPC, double wTX) {
        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection and set false autocommit
            con = ods.getConnection(this.username, this.password);

            // create query to find all pictures ordered by similarity to specified picture
            String query =
            "SELECT dst.PICTURE_ID, dst.PRODUCT_ID, " +
                "SI_ScoreByFtrList(" +
                    "new SI_FeatureList (src.PICTURE_AC, ?," +
                                        "src.PICTURE_CH, ?," +
                                        "src.PICTURE_PC, ?," +
                                        "src.PICTURE_TX, ?)," +
                    "dst.PICTURE_SI " +
                ") AS SIMILARITY " +
            "FROM PICTURES src, PICTURES dst " +
            "WHERE src.PICTURE_ID <> dst.PICTURE_ID AND src.PICTURE_ID = ? " +
            "ORDER BY SIMILARITY ASC";

            OraclePreparedStatement ps = (OraclePreparedStatement) con.prepareStatement(query);
            ps.setDouble(1, wAC);
            ps.setDouble(2, wCH);
            ps.setDouble(3, wPC);
            ps.setDouble(4, wTX);
            ps.setInt(5, picture.getPicture_id());

            // get results
            OracleResultSet rs = (OracleResultSet) ps.executeQuery();

            ordered.clear();
            ordered.add(new SimilarityResults(0.0, picture.getPicture_id(), product));
            int i = 0;
            while(rs.next()) {
                if (i++ > 9)
                    break;
                System.out.println(rs.getDouble("SIMILARITY"));
                ordered.add(
                    new SimilarityResults(
                        rs.getDouble("SIMILARITY"), rs.getInt("PICTURE_ID"), getProduct(rs.getInt("PRODUCT_ID")))
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL Error occurred while searching similar pictures.");
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //</editor-fold>">

    //<editor-fold desc="Sectors methods: add, update, getNearest, getNearestSectorInSameRegal, checkRackResizing, deleteSectorsAfterRegalResizing">
    /**
     * @param sector Sector to add
     * @throws SQLException
     */
    public void addSector(Sector sector) throws SQLException{

        PreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // sector geometry
            STRUCT sector_geometry = JGeometry.store(con, sector.getPlacement());

            // SQL query
            ps = con.prepareStatement("INSERT INTO SECTORS VALUES(sectors_seq.nextval,?,?,?,?)");
            ps.setString(1, sector.getSector_name());
            ps.setObject(2, sector_geometry);
            ps.setInt(3, sector.getRegal_id());
            ps.setInt(4, sector.getSector_id_inregal());

            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("Sector inserted! (sector " + sector.getSector_name() +
                                   " in regal " + sector.getRegal_id() + ")");
            } else {
                System.out.println("Sector inserting failed!");
            }

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select SECTORS_SEQ.currval FROM DUAL");
            if (rs.next())
                sector.setSector_id(rs.getInt(1));

            sectors.add(sector);


        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }

    /**
     * @param sector Sector to update
     * @throws SQLException
     */
    public void updateSector(Sector sector) throws SQLException{

        PreparedStatement ps = null;

        try {
            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // sector geometry
            STRUCT sector_geometry = JGeometry.store(con, sector.getPlacement());

            // SQL query
            ps = con.prepareStatement("UPDATE SECTORS SET SECTOR_NAME=?, SECTOR_PLACEMENT=?, SECTOR_ID_INREGAL=?, REGAL_ID=? WHERE SECTOR_ID=?");
            ps.setString(1, sector.getSector_name());
            ps.setObject(2, sector_geometry);
            ps.setInt(3, sector.getSector_id_inregal());
            ps.setInt(4, sector.getRegal_id());
            ps.setInt(5,sector.getSector_id());


            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("Sector edited! (sector " + sector.getSector_name() +
                        " in regal " + sector.getRegal_id() + ")");
            } else {
                System.out.println("Sector editing failed!");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (ps != null) ps.close();
        }
    }


    public Sector getNearestSectorInSameRegal(Sector sector) throws SQLException {

        Sector nearest = null;

        boolean flag = false;

        // sector parameters
        JGeometry sector_placement = sector.getPlacement();
        int regal_id = sector.getRegal_id();

        try{

            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            oracle.sql.STRUCT sector_geometry = JGeometry.store(con,sector_placement);

            //System.out.print(sector_geometry);

            String query = "SELECT * FROM (SELECT * FROM SECTORS WHERE regal_id=? AND SDO_EQUAL(sector_placement, ?) != 'TRUE') WHERE SDO_NN(sector_placement, ?) = 'TRUE'";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, regal_id);
            ps.setObject(2, sector_geometry);
            ps.setObject(3, sector_geometry);
           // ps.setObject(2, sector_geometry);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                Product product = this.getProductBySectorID(rs.getInt("sector_id"));
                if (product == null) {
                    nearest = new Sector();
                    nearest.setSector_id(rs.getInt("sector_id"));
                    nearest.setSector_name(rs.getString("sector_name"));
                    nearest.setPlacement(JGeometry.load(rs.getBytes("sector_placement")));
                    nearest.setSector_id_inregal(rs.getInt("sector_id_inregal"));
                    nearest.setRegal_id(regal_id);
                    break;
                }

            }


        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
        }

        return nearest;
    }

    public Sector getNearestSector(Sector sector) throws SQLException {

        Sector nearest = null;

        boolean flag = false;

        // sector parameters
        JGeometry sector_placement = sector.getPlacement();

        try{

            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            oracle.sql.STRUCT sector_geometry = JGeometry.store(con,sector_placement);

            //System.out.print(sector_geometry);

            String query = "SELECT * FROM (SELECT * FROM SECTORS WHERE SDO_EQUAL(sector_placement, ?) != 'TRUE') WHERE SDO_NN(sector_placement, ?) = 'TRUE'";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setObject(1, sector_geometry);
            ps.setObject(2, sector_geometry);
            // ps.setObject(2, sector_geometry);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Product product = this.getProductBySectorID(rs.getInt("sector_id"));
                if (product == null) {
                    nearest = new Sector();
                    nearest.setSector_id(rs.getInt("sector_id"));
                    nearest.setSector_name(rs.getString("sector_name"));
                    nearest.setPlacement(JGeometry.load(rs.getBytes("sector_placement")));
                    nearest.setSector_id_inregal(rs.getInt("sector_id_inregal"));
                    nearest.setRegal_id(rs.getInt("regal_id"));
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
        }

        return nearest;

    }

    /**
     * Function for check, if we can
     * do a resizing of rack
     * (we cannot resize against filled sector)
     */
    public boolean checkRackResizing(Regal regal) throws SQLException {

        // need to use regal_id for interact with sectors in resized regal
        int regal_id = regal.getRegal_id();
        int cnt = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean canResize = true;

        try{

            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // we will be operating with regal geometry
            STRUCT regal_geometry = JGeometry.store(con, regal.getPlacement());

            // set query string
            String query = "SELECT * FROM SECTORS WHERE regal_id=? AND SDO_COVEREDBY(sector_placement,?) != 'TRUE'";
            ps = con.prepareStatement(query);
            ps.setInt(1, regal_id);
            ps.setObject(2, regal_geometry);
            rs = ps.executeQuery();
            //now we have all sectors from defined regal(rack)
            while (rs.next()) {

                Product product = this.getProductBySectorID(rs.getInt("sector_id"));
                if (product != null) {
                    canResize = false;
                    break;
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
        }

        return canResize;

    }

    /**
     * Function for deleting empty sectors from rack after resizing
     * @param regal Resized regal
     */
    public void deleteSectorsAfterRegalResizing(Regal regal) throws SQLException{

        // need to use regal_id for interact with sectors in resized regal
        int regal_id = regal.getRegal_id();
        PreparedStatement ps = null;
        PreparedStatement psd = null;
        ResultSet rs = null;

        try{

            // create an oracle data source instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // get connection
            con = ods.getConnection(this.username, this.password);

            // we will be operating with regal geometry
            STRUCT regal_geometry = JGeometry.store(con, regal.getPlacement());

            // set query string
            String query = "SELECT * FROM SECTORS WHERE regal_id=? AND SDO_COVEREDBY(sector_placement,?) != 'TRUE'";
            ps = con.prepareStatement(query);
            ps.setInt(1,regal_id);
            ps.setObject(2, regal_geometry);
            rs = ps.executeQuery();
            //now we have all sectors from defined regal(rack)
            while (rs.next()) {

                String queryd = "DELETE FROM SECTORS WHERE sector_id=?";
                psd = con.prepareStatement(queryd);
                psd.setInt(1,rs.getInt("sector_id"));
                psd.execute();

                    System.out.println("Sector " + rs.getInt("sector_id") + " succesfully deleted.");

                // delete from local list
                sectors.remove(getSector(rs.getInt("sector_id")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
        }

    }
    //</editor-fold>">

    //<editor-fold desc="Routers methods: addRouter, updateRouter, computeAreaofRouters">
    /**
     * Insert created router into database. This calls update router method for
     * compute range of router depending on radius.
     * @param router Router with center point geometry set
     */
    public void addRouter(Router router) {
        PreparedStatement ps = null;
        try {
            // create a OracleDataSource instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // connect to the database
            con = ods.getConnection(this.username, this.password);

            // regal geometry
            STRUCT router_geometry = JGeometry.store(con, router.getPoint_geom());

            // SQL query
            ps = con.prepareStatement("INSERT INTO ROUTERS(ROUTER_ID, ROUTER_PLACEMENT) VALUES (ROUTERS_SEQ.nextval,?)");
            ps.setObject(1, router_geometry);

            if (ps.executeUpdate() > 0) {
                System.out.println("Router inserted!");
            } else {
                System.out.println("Router inserting failed!");
            }

            // get assigned ID
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select ROUTERS_SEQ.currval FROM DUAL");
            if (rs.next())
                router.setRouter_id(rs.getInt(1));

            routers.add(router);

            // update radius
            updateRouter(router);


        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compute range of router depending on radius
     * @param router Router to update
     */
    public void updateRouter(Router router) {
        PreparedStatement ps = null;
        try {
            // create a OracleDataSource instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // connect to the database
            con = ods.getConnection(this.username, this.password);

            double x = router.getPoint_geom().getJavaPoint().getX();
            double y = router.getPoint_geom().getJavaPoint().getY();

            router.setRange(
                new JGeometry(2003, 0,
                    new int[]{1, 1003, 4},
                    new double[]{
                            x, y - router.getRadius(),
                            x + router.getRadius(), y,
                            x, y + router.getRadius()})
            );

            STRUCT router_point = JGeometry.store(con, router.getPoint_geom());
            STRUCT router_radius = JGeometry.store(con, router.getRange());

            ps = con.prepareStatement("UPDATE ROUTERS SET ROUTER_PLACEMENT = ?, ROUTER_RADIUS = ? WHERE ROUTER_ID = ?");
            ps.setObject(1, router_point);
            ps.setObject(2, router_radius);
            ps.setInt(3, router.getRouter_id());

            if (ps.executeUpdate() > 0) {
                System.out.println("Router updated!");
            } else {
                System.out.println("Router updating failed!");
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int computeAreaOfRouters() {
        PreparedStatement ps = null;
        try {
            // create a OracleDataSource instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // connect to the database
            con = ods.getConnection(this.username, this.password);

            ps = con.prepareStatement(
                "SELECT SDO_GEOM.SDO_AREA( SDO_AGGR_UNION( " +
                    "SDOAGGRTYPE( r.ROUTER_RADIUS, 0.005)), 0.005) area FROM ROUTERS r");

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("area");

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
    //</editor-fold>">

    public void addPath(Path path) {
        PreparedStatement ps = null;
        try {
            // create a OracleDataSource instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // connect to the database
            con = ods.getConnection(this.username, this.password);

            // regal geometry
            STRUCT path_geometry = JGeometry.store(con, path.getGeometry());

            // SQL query
            ps = con.prepareStatement("INSERT INTO PATHS VALUES (PATHS_SEQ.nextval,?)");
            ps.setObject(1, path_geometry);

            if (ps.executeUpdate() > 0) {
                System.out.println("Path inserted!");
            } else {
                System.out.println("Path inserting failed!");
            }

            // get assigned ID
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select PATHS_SEQ.currval FROM DUAL");
            if (rs.next())
                path.setPath_id(rs.getInt(1));

            paths.add(path);

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create connection to DB and load all items
     * from Products table to list of Product objects.
     * @throws SQLException
     */
    public void loadDB() throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;
        OracleResultSet prs = null;

        try {
            // create a OracleDataSource instance
            OracleDataSource ods = new OracleDataSource();

            // set connection string
            ods.setURL(CString);

            // connect to the database
            con = ods.getConnection(this.username, this.password);
            System.out.println("Connection successful.");

            // create statement
            stmt = con.createStatement();

            // load regals
            rs = stmt.executeQuery("select * from REGALS");
            while (rs.next()) {
                Regal regal = new Regal();

                regal.setRegal_id(rs.getInt("regal_id"));
                regal.setCategory(rs.getString("category"));
                regal.setPlacement(JGeometry.load(rs.getBytes("placement")));

                regals.add(regal);
                System.out.print(".");
            }
            System.out.println(" " + regals.size() + " regals loaded.");

            // load sectors
            rs = stmt.executeQuery("select * from SECTORS ORDER BY SECTOR_ID ASC");
            while (rs.next()) {
                Sector sector = new Sector();

                sector.setSector_id(rs.getInt("sector_id"));
                sector.setSector_name(rs.getString("sector_name"));
                sector.setPlacement(JGeometry.load(rs.getBytes("sector_placement")));
                sector.setRegal_id(rs.getInt("regal_id"));
                sector.setSector_id_inregal(rs.getInt("sector_id_inregal"));

                //set sector rotation in rack
                if(sector.getShape().getBounds().getHeight() > sector.getShape().getBounds().getWidth())  {
                    sector.setRotated(true);
                } else {
                    sector.setRotated(false);
                }

                sectors.add(sector);
                System.out.print(".");
            }
            System.out.println(" " + sectors.size() + " sectors loaded.");

            // load products
            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
            rs = stmt.executeQuery("select * from PRODUCTS");
            while (rs.next()) {
                Product product = new Product();

                product.setProduct_id(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setManufacturer(rs.getString("manufacturer"));
                product.setRegal_id(rs.getInt("regal_id"));
                product.setSector_id(rs.getInt("sector_id"));
                product.setValidFrom(rs.getDate("valid_from").toString());
                product.setValidTo(rs.getDate("valid_to").toString());

                products.add(product);
                System.out.print(".");
            }
            System.out.println(" " + products.size() + " products loaded.");

            // load routers
            rs = stmt.executeQuery("select * from ROUTERS");
            while (rs.next()) {
                Router router = new Router();
                router.setRouter_id(rs.getInt("router_id"));
                router.setPoint_geom(JGeometry.load(rs.getBytes("router_placement")));
                router.setRange(JGeometry.load(rs.getBytes("router_radius")));
                router.setRadius((int)router.getRangeShape().getBounds().getWidth()/2);

                routers.add(router);
                System.out.print(".");
            }
            System.out.println(" " + routers.size() + " routers loaded.");

            // load paths
            rs = stmt.executeQuery("select * from PATHS");
            while (rs.next()) {
                Path path = new Path();
                path.setPath_id(rs.getInt("path_id"));
                path.setGeometry(JGeometry.load(rs.getBytes("geometry")));

                paths.add(path);
                System.out.print(".");
            }
            System.out.println(" " + paths.size() + " paths loaded.");

        } catch (SQLException e) {
            e.printStackTrace();
            throw (e);
        } catch (NullPointerException e) {
            e.printStackTrace();    // conversion from SDO_GEOMETRY to Shape failed
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            con.close();
            if (stmt != null) stmt.close();
            if (rs != null) rs.close();
            if (prs != null) prs.close();
        }
    }

    /**
     * Insert initial data
     */
    public void initDb() {

        try {
            // regals and sectors

            Regal r1 = new Regal(10);
            r1.setCategory("Acer");
            r1.setPlacement(
                    new JGeometry(2003, 0, new int[]{1, 1003, 3}, new double[]{150,20, 450,80})
            );
            addRegal(r1);
            initSectors(r1);

            Regal r2 = new Regal(10);
            r2.setCategory("Asus");
            r2.setPlacement(
                    new JGeometry(2003, 0, new int[]{1, 1003, 3}, new double[]{450,20, 750,80})
            );
            addRegal(r2);
            initSectors(r2);

            Regal r3 = new Regal();
            r3.setCategory("Apple");
            r3.setPlacement(
                    new JGeometry(2003, 0, new int[]{1, 1003, 3}, new double[]{150,150, 570,210})
            );
            addRegal(r3);
            initSectors(r3);

            Regal r4 = new Regal(10);
            r4.setCategory("Lenovo");
            r4.setPlacement(
                    new JGeometry(2003, 0, new int[]{1, 1003, 3}, new double[]{150,280, 450,340})
            );
            addRegal(r4);
            initSectors(r4);

            Regal r5 = new Regal(4);
            r5.setCategory("Toshiba");
            r5.setPlacement(
                    new JGeometry(2003, 0, new int[]{1, 1003, 3}, new double[]{150,400, 270,460})
            );
            addRegal(r5);
            initSectors(r5);

            Regal r6 = new Regal();
            r6.setCategory("HP");
            r6.setPlacement(
                    new JGeometry(2003, 0, new int[]{1, 1003, 3}, new double[]{20,20, 80,440})
            );
            addRegal(r6);
            initSectors(r6);

            Regal r7 = new Regal(10);
            r7.setCategory("Dell");
            r7.setPlacement(
                    new JGeometry(2003, 0, new int[]{1, 1003, 3}, new double[]{690,100, 750,400})
            );
            addRegal(r7);
            initSectors(r7);


            // products from files

            // create dictionary - product name -> list of pictures
            File lib_pic = new File("lib/pictures/png");
            Map<String, List<String>> map = new HashMap<String, List<String>>();
            File[] files = lib_pic.listFiles();
            if (files != null) {
                for (File file: files) {
                    if (file.getName().contains("png")) {

                        String key = file.getName().substring(0, file.getName().indexOf("__"));
                        if ( map.containsKey(key)) {
                            map.get(key).add(file.getName());
                        }
                        else {
                            map.put(key, new ArrayList<String>());
                        }
                    }
                }
            }

            // create products with pictures
            for (Map.Entry<String, List<String>> entry: map.entrySet()) {

                // create product
                Product p = new Product();
                p.setName(entry.getKey().substring(entry.getKey().indexOf(" ") + 1, entry.getKey().length()));
                p.setManufacturer(entry.getKey().substring(0, entry.getKey().indexOf(" ")));

                // set regal
                p.setRegal_id(-1);
                for (Regal r: regals) {
                    if (entry.getKey().toLowerCase().contains(r.getCategory().toLowerCase())) {
                        p.setRegal_id(r.getRegal_id());
                        break;
                    }
                }
                // set sector
                if (p.getRegal_id() != -1) {
                    try {
                        Sector assigned = null;
                        for (Sector s : sectors) {
                            if (s.getRegal_id() == p.getRegal_id()) {
                                assigned = getNearestSectorInSameRegal(s);
                                break;
                            }
                        }
                        if (assigned != null) {
                            p.setSector_id(assigned.getSector_id());
                            p.setValidFrom(generateDate(2013, 2014));
                            p.setValidTo(generateDate(2015, 2015));
                            addProduct(p);

                            // add pictures
                            for (String pic: entry.getValue()) {
                                System.out.println("-----> " + pic);
                                Picture pi = new Picture("lib/pictures/png/" + pic, pic);
                                try {
                                    addPicture(pi, p);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    System.out.println("picture adding error");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }


            // routers

            for (int i = 1; i < 7; i++) {
                Router ro = new Router();

                int j = 1;
                if (i % 2 == 0) j = 4;

                ro.setPoint_geom(
                        JGeometry.createPoint(new double[]{80.0 * i, 80.0 * j}, 2, 0)
                );
                ro.setRadius(100);
                addRouter(ro);
            }


            // paths

            Path pa = new Path();
            pa.setGeometry(
                new JGeometry(2002, 0,
                    new int[]{1, 2, 1},
                    new double[]{120,0, 120,120, 650,120, 650,260, 120,260, 120,0})
            );
            addPath(pa);

            Path pa2 = new Path();
            pa2.setGeometry(
                new JGeometry(2002, 0,
                    new int[]{1, 2, 1},
                    new double[]{120,0, 120,480, 300,480, 300,380, 120,380, 120,0})
            );
            addPath(pa2);


            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate random date in year interval
     * @param fromYear start of year interval
     * @param toYear end of year interval
     * @return random date as string
     */
    public String generateDate(int fromYear, int toYear) {
        GregorianCalendar gc = new GregorianCalendar();

        int year = randBetween(fromYear, toYear);
        gc.set(GregorianCalendar.YEAR, year);

        int dayOfYear = randBetween(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gc.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);

        return (gc.get(GregorianCalendar.YEAR) + "-" + gc.get(GregorianCalendar.MONTH) + "-" + gc.get(GregorianCalendar.DAY_OF_MONTH));
    }

    /**
     * Generate random number in range
     * @param start start
     * @param end end
     * @return random int in range
     */
    public static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

    /**
     * Init sectors in regal
     */
    public void initSectors(Regal regal) throws SQLException {
        sector_mark = 1;

        int x = (int) regal.getShape().getBounds2D().getMinX();
        int y = (int) regal.getShape().getBounds2D().getMinY();

        //store default position for next row/column
        int xx = x;
        int yy = y;

        //horizontal rack
        if(!regal.getVerticalOrientation())
        {
            for (int j = 0; j < regal.getNum_sectors_all(); j++)
            {
                Sector sector = new Sector();
                sector.setSector_name("Sector" + sector_mark);
                sector.setShape(new Rectangle(xx, yy, Regal.SECTOR_WIDTH, Regal.SECTOR_HEIGHT));
                sector.shape2JGeometry();
                sector.setRegal_id(regal.getRegal_id());

                //add sector to database
                addSector(sector);

                xx += Regal.SECTOR_WIDTH;

                if (xx >= (x + regal.getNum_sectors_row() * Regal.SECTOR_WIDTH))
                {
                    xx = x;
                    yy = y + Regal.SECTOR_HEIGHT;
                }
                sector_mark++;
            }
        }
        else
        //vertical rack
        {
            for (int j = 0; j < regal.getNum_sectors_all(); j++)
            {
                Sector sector = new Sector();
                sector.setSector_name("Sector" + sector_mark);
                sector.setShape(new Rectangle(xx, yy, Regal.SECTOR_HEIGHT, Regal.SECTOR_WIDTH));
                sector.shape2JGeometry();
                sector.setRegal_id(regal.getRegal_id());

                //add sector to database
                addSector(sector);

                yy += Regal.SECTOR_WIDTH;

                if (yy >= (y + regal.getNum_sectors_row() * Regal.SECTOR_WIDTH))
                {
                    yy = y;
                    xx = x + Regal.SECTOR_HEIGHT;
                }
                sector_mark++;
            }
        }
    }

    /**
     *  Increment picture index to show next picture.
     */
    public void incPict_index() {
        if (pict_id == 0) {
            pict_index = ++pict_index % getNumberOfPictures();
        } else {
            pict_id = 0;
            pict_index = (pictures.indexOf(getPicture()) + 1) % getNumberOfPictures();
        }
    }

    // getters
    public List<Product> getProducts() {
        return products;
    }
    public Product getProduct(int id) {
        for (Product p: products) {
            if (p.getProduct_id() == id)
                return p;
        }
        return null;
    }
    public Product getProductBySectorID(int id) {
        for (Product p: products) {
            if (p.getSector_id() == id)
                return p;
        }
        return null;
    }

    public List<Sector> getSectors() {
        return sectors;
    }
    public Sector getSector(int id) {
        for (Sector s: sectors) {
            if (s.getSector_id() == id)
                return s;
        }
        return null;
    }
    public List<Regal> getRegals() {
        return regals;
    }
    public Regal getRegal(int id) {
        for (Regal r: regals) {
            if (r.getRegal_id() == id)
                return r;
        }
        return null;
    }
    public Picture getPicture() {
        if (pictures != null && pictures.size() > 0) {

            if (pict_id == 0) {
                return pictures.get(pict_index);
            }
            else {
                for (Picture picture : pictures) {
                    if (picture.getPicture_id() == pict_id)
                        return picture;
                }
                return null;
            }
        }
        else {
            return null;
        }
    }
    public int getNumberOfPictures() {
        return pictures.size();
    }

    public int getSector_mark() {
        return this.sector_mark;
    }

    public void setSector_mark(int sector_mark) {
        this.sector_mark = sector_mark;
    }

    public List<SimilarityResults> getOrdered() {
        return ordered;
    }

    public List<Router> getRouters() {
        return routers;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public List<Product> getSearchedProducts() {
        return searchedProducts;
    }

    public List<Regal> getSearchedRegals() {
        return searchedRegals;
    }
}
