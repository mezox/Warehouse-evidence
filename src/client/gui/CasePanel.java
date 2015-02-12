package client.gui;

import client.model.Regal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class CasePanel extends JPanel implements ActionListener
{
    private Window      parent;

    private JLabel      categoryLabel = new JLabel("Category *");
    private JLabel      positionXLabel = new JLabel("Position X");
    private JLabel      positionYLabel = new JLabel("Position Y");

    private JTextField  caseCategoryTxtBox = new JTextField();
    private JTextField  caseXcoordTxtBox = new JTextField("0");
    private JTextField  caseYcoordTxtBox = new JTextField("0");

    private JButton     addCaseBtn = new JButton("Add new");
    private JButton     deleteCaseBtn = new JButton("Delete selected");
    private JButton     rotateCaseBtn = new JButton("Rotate selected");;

    private JPanel      properties = new JPanel();
    private JPanel      buttons = new JPanel();

    private JLabel      title = new JLabel("Case properties:");

    private LoadingDialog ld = null;

    public CasePanel(Window p)
    {
        parent = p;

        setLayout(new GridLayout(3,1));
        setBackground(JColor.MINI_PANELS);

        properties.setLayout(new GridLayout(3,2));

        properties.setBackground(JColor.MINI_PANELS);

        properties.add(categoryLabel);
        properties.add(caseCategoryTxtBox);
        properties.add(positionXLabel);
        properties.add(caseXcoordTxtBox);
        properties.add(positionYLabel);
        properties.add(caseYcoordTxtBox);

        rotateCaseBtn.addActionListener(this);
        addCaseBtn.addActionListener(this);
        deleteCaseBtn.addActionListener(this);

        buttons.setLayout(new GridLayout(3, 1));
        buttons.add(addCaseBtn);
        buttons.add(deleteCaseBtn);
        buttons.add(rotateCaseBtn);

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
        c.insets = new Insets(5,5,5,5);  // outer padding
        c.fill = GridBagConstraints.HORIZONTAL; // orientation

        // title
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0; c.gridy = 0;
        add(title, c);

        // properties
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 1;
        add(properties, c);

        // buttons panel
        c.weighty = 0.8;
        c.gridx = 0;
        c.gridy = 2;
        add(buttons, c);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == addCaseBtn)
        {
            //parent.getVisualizer().setAddCaseMode(true);
            String caseHead = caseCategoryTxtBox.getText();

            Rectangle rect = new Rectangle(
                    Integer.parseInt(caseXcoordTxtBox.getText()),
                    Integer.parseInt(caseYcoordTxtBox.getText()),
                    Regal.DEFAULT_WIDTH,
                    Regal.DEFAULT_HEIGHT);

            final Regal newRegal = new Regal();

            newRegal.setCategory(caseHead);
            newRegal.setShape(rect);
            newRegal.setVerticalOrientation(false);
            newRegal.shape2JGeometry();

            ld = new LoadingDialog("Generating sectors ...");
            ld.setLocationRelativeTo(getParent());

            //Insert new Case into database, generate sectors and also insert them to DB
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        parent.getVisualizer().getDatabase().addRegal(newRegal);
                        parent.getVisualizer().getDatabase().initSectors(newRegal);
                        parent.getVisualizer().repaint();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }

                    if (ld != null && ld.isVisible())
                    {
                        ld.dispose();
                    }
                }
            }).start();

            ld.setVisible(true);
        }
        else if(e.getSource() == rotateCaseBtn)
        {
            parent.getVisualizer().rotateSelectedCase();
            parent.getVisualizer().repaint();
        }
        else if(e.getSource() == deleteCaseBtn)
        {
            try {
                parent.getVisualizer().getDatabase().deleteRegal(parent.getVisualizer().getClickedCase());
                parent.getVisualizer().repaint();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }
}
