package client.gui;

import client.model.Database;
import client.model.Product;
import client.model.Regal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class:
 * Author: Martin Veselovsky
 * Date:   15.12.2014
 * Info:
 */
public class SearchDialog extends JDialog implements ActionListener {

    private Window parent;
    private Database db = null;
    private JPanel properties = new JPanel();
    private JTextField valid_from = new JTextField();
    private JTextField valid_to = new JTextField();
    private JRadioButton products = new JRadioButton("Products");
    private JRadioButton regals = new JRadioButton("Regals");
    private JButton search = new JButton("Search");

    public SearchDialog(Window parent) {
        super(parent, "Search in warehouse by date", false);
        this.parent = parent;

        setLocationRelativeTo(parent);
        setPreferredSize(new Dimension(300,300));

        properties.setLayout(new GridLayout(0, 1, 20, 10));

        // radio buttons
        ButtonGroup g = new ButtonGroup();
        g.add(products);
        g.add(regals);

        products.setSelected(true);

        JPanel radios = new JPanel();
        radios.add(products);
        radios.add(regals);
        properties.add(radios);

        // labels and text fields
        properties.add(new JLabel("Valid from"));
        properties.add(valid_from);

        properties.add(new JLabel("Valid to"));
        properties.add(valid_to);

        properties.add(search);
        search.addActionListener(this);

        properties.add(Box.createVerticalGlue());

        getRootPane().setDefaultButton(search);
        add(properties);
        pack();
    }

    public void setDatabase(Database db) {
        this.db = db;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == search) {
            if (!valid_from.getText().equals("") && !valid_to.getText().equals("") && db != null) {
                search.setIcon(new ImageIcon("lib/circle.gif"));

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (products.isSelected()) {
                            db.searchProductsByDate(valid_from.getText(), valid_to.getText());

                            for (Product p: db.getSearchedProducts()) {
                                parent.getPrintStreamOutputPanel().println(
                                        p.getManufacturer() + " " + p.getName() + " --> " +
                                                p.getValidFrom() + " - " + p.getValidTo()
                                );
                            }
                        }
                        else if (regals.isSelected()) {
                            db.searchRegalsByDate(valid_from.getText(), valid_to.getText());

                            for (Regal r: db.getSearchedRegals()) {
                                parent.getPrintStreamOutputPanel().println(
                                    "ID " + r.getRegal_id() + " - " + r.getCategory()
                                );
                            }
                        }

                        search.setIcon(null);
                        parent.getPrintStreamOutputPanel().println("-----------");
                    }
                }).start();
            }
        }
    }
}
