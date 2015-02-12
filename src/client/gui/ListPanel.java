package client.gui;

import client.controller.SimilarityResults;
import client.model.Database;
import client.model.Product;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * Class:
 * Author: Martin Veselovsky
 * Date:   12.12.2014
 * Info:
 */
public class ListPanel extends JPanel implements ListSelectionListener {

    private Window parent;
    private Database db;
    private DefaultListModel<SimilarityResults> dlist = new DefaultListModel<SimilarityResults>();
    private JList<SimilarityResults> list = new JList<SimilarityResults>();
    private int last_index;


    public ListPanel(Window parent)
    {
        this.parent = parent;
        setBackground(JColor.MINI_PANELS);

        setLayout(new GridLayout(0, 1));

        list.addListSelectionListener(this);
        list.setModel(dlist);
        add(list);
        ToolTipManager.sharedInstance().registerComponent(list);
    }

    public void changeList() {
        dlist.clear();
        for(SimilarityResults sr: db.getOrdered()) {
            dlist.addElement(sr);
        }
        list.setModel(dlist);
        revalidate();
        repaint();
    }

    public void clearList() {
        dlist.clear();
        list.setModel(dlist);
    }

    public void setDatabase(Database db) {
        this.db = db;
    }

    public String getToolTipText(MouseEvent event) {
        Point p = event.getPoint();
        int location = list.locationToIndex(p);
        SimilarityResults tip = list.getModel().getElementAt(location);
        return tip.getToolTipText();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {

        // this condition is because this happens twice on click
        if (list.getSelectedIndex() != -1 &&list.getSelectedIndex() != last_index) {
            last_index = list.getSelectedIndex();

            System.out.println(list.getSelectedValue());
            parent.right_panel.showProduct(
                list.getSelectedValue().product,
                list.getSelectedValue().picture_id
            );
//            parent.getVisualizer().setClickedSector(db.getSector(list.getSelectedValue().product.getProduct_id()));
//            parent.getVisualizer().repaint();
        }
    }
}
