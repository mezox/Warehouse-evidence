package client.gui;

import client.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;

/**
 * Class:  RightPanel
 * Author: Martin Veselovsky, Tomas Kubovcik
 * Date:   2.12.2014
 * Info:   Display properties of product.
 */
public class RightPanel extends JPanel implements ActionListener
{
    private Product displayedProduct = null;
    private Database database;

    private JLabel title = new JLabel("Product properties:");
    private JPanel properties = new JPanel(new GridLayout(8, 2));
    private JPanel picture_panel = new JPanel();
    private JPanel buttons = new JPanel();

    private JTextField product_id = new JTextField();
    private JTextField name = new JTextField();
    private JTextField manufacturer = new JTextField();
    private JTextField category = new JTextField();
    private JTextField sector = new JTextField();
    private JTextField valid_from = new JTextField();
    private JTextField valid_to = new JTextField();

    private JButton add_product = new JButton("Add");
    private JButton add_product_ok = new JButton("OK");
    private JButton add_product_cancel = new JButton("Cancel");
    private JButton delete_product = new JButton("Delete");

    private JButton edit_product = new JButton("Edit");
    private JButton add_picture = new JButton("Add picture");
    private JButton delete_picture = new JButton("Delete picture");
    private JButton rotate_picture_right = new JButton("Rotate right");
    private JButton find_similar_but = new JButton("Find similar");

    private JLabel pictureLab = new JLabel();
    private JButton pictureBut = new JButton();
    private Window parent = null;

    /**
     * Right panel constructor. This panel consists of three components,
     * these are title of right panel, properties panel where are all
     * product information and buttons panel with control buttons.
     */
    public RightPanel(Window p)
    {
        parent = p;

        // create border

        // add all labels and textFields to the properties panel
        properties.add(new JLabel("Product ID"));
        properties.add(product_id);
        product_id.setEditable(false);

        properties.add(new JLabel("Name *"));
        name.setColumns(10);
        properties.add(name);

        properties.add(new JLabel("Manufacturer *"));
        properties.add(manufacturer);

        properties.add(new JLabel("Category"));
        properties.add(category);
        category.setEditable(false);

        properties.add(new JLabel("Sector"));
        properties.add(sector);
        sector.setEditable(false);

        properties.add(new JLabel("Valid from *"));
        properties.add(valid_from);

        properties.add(new JLabel("Valid to *"));
        properties.add(valid_to);

        properties.add(new JLabel("Pictures"));
        properties.add(pictureLab);

        // add pictures components
        picture_panel.setLayout(new BoxLayout(picture_panel, BoxLayout.Y_AXIS));
        pictureBut.setToolTipText("Click to show next picture");
        pictureBut.setAlignmentX(CENTER_ALIGNMENT);
        pictureBut.setBorder(null);
        pictureBut.setContentAreaFilled(false);
        pictureBut.addActionListener(this);
        picture_panel.add(pictureBut, BorderLayout.CENTER);

        // add all buttons to buttons panel
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        add_picture.addActionListener(this);
        delete_picture.addActionListener(this);
        rotate_picture_right.addActionListener(this);
        find_similar_but.addActionListener(this);

        buttons.add(add_picture);
        buttons.add(delete_picture);
        buttons.add(rotate_picture_right);
        buttons.add(find_similar_but);


        //buttons.add(Box.createRigidArea(new Dimension(10, 10)));
        buttons.add(new JSeparator());

        buttons.add(Box.createRigidArea(new Dimension(10, 10)));
        buttons.add(new JLabel("Product: "));
        add_product.addActionListener(this);
        buttons.add(add_product);
        add_product_ok.addActionListener(this);
        buttons.add(add_product_ok);
        add_product_ok.setVisible(false);
        add_product_cancel.addActionListener(this);
        buttons.add(add_product_cancel);
        add_product_cancel.setVisible(false);

        buttons.add(Box.createRigidArea(new Dimension(10, 10)));
        buttons.add(new JSeparator());
        buttons.add(Box.createRigidArea(new Dimension(10, 10)));
        buttons.add(new JLabel("Product deleting: "));

        delete_product.addActionListener(this);
        buttons.add(delete_product);

        buttons.add(Box.createRigidArea(new Dimension(10, 10)));
        buttons.add(new JSeparator());
        buttons.add(Box.createRigidArea(new Dimension(10, 10)));
        buttons.add(new JLabel("Product editing: "));

        edit_product.addActionListener(this);
        buttons.add(edit_product);

        // set layout of right panel<
        setCustomLayout();
    }

    /**
     *  Set layout of right panel components.
     */
    public void setCustomLayout() {

        // set layout
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // global constraints
        c.insets = new Insets(10, 10, 10, 10);  // outer padding
        c.ipadx = 20;                           // inner padding
        c.fill = GridBagConstraints.HORIZONTAL; // orientation

        // title
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0; c.gridy = 0;
        add(title, c);

        // properties
        c.weighty = 0;
        c.gridx = 0; c.gridy = 1;
        add(properties, c);

        // picture_panel
        c.weighty = 1.0;
        c.gridx = 0; c.gridy = 2;
        add(picture_panel, c);

        // buttons
        c.anchor = GridBagConstraints.PAGE_END;
        c.weighty = 0;
        c.gridx = 0; c.gridy = 3;
        add(buttons, c);
    }

    /**
     * Set working database
     * @param db database
     */
    public void setDatabase(Database db)
    {
        database = db;
    }


    /**
     * Set product info to all textFields in properties panel
     * @param product Marked product
     */
    public void showProduct(Product product, int pict_id)
    {
        if (product != displayedProduct || pict_id != 0) {
            displayedProduct = product;

            product_id.setText(Integer.toString(product.getProduct_id()));
            name.setText(product.getName());
            manufacturer.setText(product.getManufacturer());
            category.setText(database.getRegal(product.getRegal_id()).getCategory());
            sector.setText(Integer.toString(product.getSector_id()));
            valid_from.setText(product.getValidFrom());
            valid_to.setText(product.getValidTo());

            class MyRunnable implements Runnable {
                private Product product;
                private int pict_id;

                public MyRunnable(Product product, int pict_id) {
                    this.product = product;
                    this.pict_id = pict_id;
                }

                public void run() {
                    database.loadPictures(product, pict_id);
                    setPicture();
                }
            }
            Thread t = new Thread(new MyRunnable(product, pict_id));
            t.start();

            this.pictureBut.setIcon(new ImageIcon("lib/circle.gif"));
        }
    }

    /**
     * Clear product properties in right panel.
     */
    public void clear() {
        displayedProduct = null;

        product_id.setText("");
        name.setText("");
        manufacturer.setText("");
        category.setText("");
        sector.setText("");
        valid_from.setText("");
        valid_to.setText("");

        pictureBut.setIcon(null);
        pictureLab.setText("");

        revalidate();
        repaint();
    }

    /**
     * Check if the rightpanel is correctly filled when adding product
     */
    public boolean checkFullFilled() {

        if (name.getText().equals("")) {
            return false;
        }

        if (manufacturer.getText().equals("")) {
            return false;
        }

        if (valid_from.getText().equals("")) {
            return false;
        }

        if (valid_to.getText().equals("")) {
            return false;
        }

        return true;
    }


    /**
     * Update selected product with values from fields
     * or fill new product with these information.
     */
    public void fillProduct() {
        displayedProduct.setName(name.getText());
        displayedProduct.setManufacturer(manufacturer.getText());
        //displayedProduct.setCategory(category.getText());
        //displayedProduct.setSector(Integer.parseInt(sector.getText()));

//        displayedProduct.setValidFrom(valid_from.getText());
//        displayedProduct.setValidTo(valid_to.getText());

//        for (Picture pict: displayedProduct.getPictures())
//            displayedProduct.addPicture(pict);
    }

    public void setPicture() {
        if (database.getPicture() != null && database.getPicture().getImg() != null) {

            this.pictureBut.setIcon(
                new ImageIcon(database.getPicture().getImg()
                    .getScaledInstance(180, 180, Image.SCALE_SMOOTH)));
            pictureLab.setText(Integer.toString(database.getNumberOfPictures()));
            revalidate();
            repaint();
        }
        else {
            pictureBut.setIcon(null);
            pictureLab.setText("");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // select directory
        if (e.getSource() == add_picture) {
            JFileChooser chooser = new JFileChooser(new File("."));
            Integer returnVal = chooser.showOpenDialog(this);

            for (int i = 0; i < database.getSectors().size(); i++)
                System.out.println(database.getSectors().get(i).getSector_name());

            // get path of selected folder
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File picture = chooser.getSelectedFile();
                if (picture.isFile() &&
                        (picture.getName().contains(".jpg") ||
                         picture.getName().contains(".png"))
                    ) {
                    try {
                        Picture pict = new Picture(picture.getAbsolutePath(), picture.getName());
                        //this.pictures.add(new Picture(ImageIO.read(picture), picture.getName()));

                        // add picture to database
                        database.addPicture(pict, displayedProduct);

                        // display added picture in panel
                        setPicture();
                    }
                    catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        // delete currently displayed picture
        else if (e.getSource() == delete_picture) {
            try {
                if (database.getPicture() != null) {
                    database.deletePicture(database.getPicture());
                    setPicture();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        else if (e.getSource() == rotate_picture_right) {
            database.rotatePicture(database.getPicture());
            setPicture();
        }

        else if (e.getSource() == find_similar_but) {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        parent.listPanel.clearList();
                        database.getSimilarImage(database.getPicture(), displayedProduct, 0.3, 0.3, 0.1, 0.3);
                        parent.listPanel.changeList();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Error occurred while searching similar pictures.");
                        parent.listPanel.revalidate();
                        parent.listPanel.repaint();
                    }

                    find_similar_but.setEnabled(true);
                    find_similar_but.setIcon(null);
                }
            }).start();
            find_similar_but.setEnabled(false);
            find_similar_but.setIcon(new ImageIcon("lib/circle1.gif"));
        }

        // click on pictures, display next
        else if (e.getSource() == pictureBut) {
            database.incPict_index();
            setPicture();
        }

        // click on Add product button
        else if (e.getSource() == add_product){

            System.out.println("'Product adding' mode turned on");

            add_product.setVisible(false);
            add_product_ok.setVisible(true);
            add_product_cancel.setVisible(true);

            parent.getVisualizer().setProductMoveMode(false);
            parent.getVisualizer().setCaseEditMode(false);
            parent.getVisualizer().setZoomMode(false);
            parent.getVisualizer().setProductAddMode(true);

            parent.getVisualizer().repaint();

        } else if (e.getSource() == add_product_ok) {
            if (checkFullFilled()) {

                Sector clickedSector = parent.getVisualizer().getClickedSector();

                if (clickedSector != null) {

                    Product newProduct = new Product();
                    newProduct.setName(this.name.getText());
                    newProduct.setManufacturer(this.manufacturer.getText());
                    newProduct.setSector_id(clickedSector.getSector_id());
                    newProduct.setRegal_id(clickedSector.getRegal_id());

                    try {
                        newProduct.setValidFrom(valid_from.getText());
                        newProduct.setValidTo(valid_to.getText());

                        database.addProduct(newProduct);
                        parent.getVisualizer().setProductAddMode(false);
                        parent.getVisualizer().repaint();
                        add_product.setVisible(true);
                        add_product_ok.setVisible(false);
                        add_product_cancel.setVisible(false);
                        revalidate();
                        repaint();
                        System.out.println("'Product adding' mode turned off");

                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                        System.out.println("Wrong format of dates.");
                    }
                } else {
                    InfoDialog add_info_ok = new InfoDialog("Select empty sector!");
                    add_info_ok.setModal(true);
                    add_info_ok.setVisible(true);
                }

            } else {

                InfoDialog info = new InfoDialog("Please fill in product card properly!");
                info.setModal(true);
                info.setVisible(true);

                System.out.println("Fill in product card, please.");
            }

        } else if (e.getSource() == add_product_cancel) {
            add_product.setVisible(true);
            add_product_ok.setVisible(false);
            add_product_cancel.setVisible(false);
            parent.getVisualizer().setProductAddMode(false);
            parent.getVisualizer().repaint();
        }

        else if (e.getSource() == delete_product) {

            if (parent.getVisualizer().getClickedSector() != null) {
                for (Product p : database.getProducts()) {
                    if (p.getSector_id() == parent.getVisualizer().getClickedSector().getSector_id()) {
                        try {
                            database.deleteProduct(p);
                            parent.getVisualizer().repaint();
                            this.clear();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }

            } else {
                System.out.println("Select product to delete!");
                InfoDialog info_delete = new InfoDialog("Select product to delete!");
                info_delete.setModal(true);
                info_delete.setVisible(true);
            }

        }

        // click on Edit product button
        else if (e.getSource() == edit_product && displayedProduct != null) {
            try {
                fillProduct();
                if (displayedProduct.getName().equals("") ||
                        displayedProduct.getManufacturer().equals("")) {
                    System.out.println("Missing value in Name field or Manufacturer field.");
                }
                // update product
                else {
                    database.updateProduct(displayedProduct);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
                System.out.println("Database error occurred while updating product information.");
            }
        }
    }
}

//TODO Sektor v paneli sa ti neupdatuje po presunuti
