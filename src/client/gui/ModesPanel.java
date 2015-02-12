package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel enabling/disabling some of the work modes
 * @author Tomas Kubovcik <xkubov02@stud.fit.vutbr.cz>
 */
public class ModesPanel extends JPanel implements ActionListener
{
    private JCheckBox caseResizeMode = new JCheckBox("Enable case resize", false);
    private JCheckBox caseRotateMode = new JCheckBox("Enable case rotate", false);
    private JCheckBox caseMoveMode = new JCheckBox("Enable case move", false);
    private JCheckBox productMoveMode = new JCheckBox("Enable product move", false);
    private JCheckBox zoomMode = new JCheckBox("Enable zoom", false);

    private JCheckBox routerMoveMode = new JCheckBox("Enable router move", false);

    private JPanel modes = new JPanel();
    private Window parent;

    public ModesPanel(Window p)
    {
        parent = p;

        setBackground(JColor.MIDDLE_PANEL);

        modes.setLayout(new GridLayout(6,1));
        modes.add(caseResizeMode);
        modes.add(caseRotateMode);
        modes.add(caseMoveMode);
        modes.add(productMoveMode);
        modes.add(zoomMode);
        modes.add(routerMoveMode);

        caseResizeMode.setBackground(JColor.MIDDLE_PANEL);
        caseRotateMode.setBackground(JColor.MIDDLE_PANEL);
        caseMoveMode.setBackground(JColor.MIDDLE_PANEL);
        productMoveMode.setBackground(JColor.MIDDLE_PANEL);
        zoomMode.setBackground(JColor.MIDDLE_PANEL);
        routerMoveMode.setBackground(JColor.MIDDLE_PANEL);

        caseMoveMode.addActionListener(this);
        caseRotateMode.addActionListener(this);
        caseResizeMode.addActionListener(this);
        productMoveMode.addActionListener(this);
        zoomMode.addActionListener(this);
        routerMoveMode.addActionListener(this);

        add(modes);
    }

    /**
     * Invoked when an action occurs.
     * Handles checkboxes selections
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        //Case resizing checkbox handler
        if(e.getSource() == caseResizeMode)
        {
            if(caseResizeMode.isSelected())
            {
                parent.getVisualizer().resetFlags();
                parent.getVisualizer().setCaseEditMode(true);

                //disable other modes in visualizer
                parent.getVisualizer().setCaseRotateMode(false);
                parent.getVisualizer().setCaseMoveMode(false);
                parent.getVisualizer().setProductMoveMode(false);
                parent.getVisualizer().setZoomMode(false);
                parent.getVisualizer().setRouterMoveMode(false);

                //disable local mode variables
                caseMoveMode.setSelected(false);
                caseRotateMode.setSelected(false);
                productMoveMode.setSelected(false);
                zoomMode.setSelected(false);
                routerMoveMode.setSelected(false);
            }
            else
            {
                parent.getVisualizer().resetFlags();
            }
        }
        //Case rotate checkbox handler
        else if(e.getSource() == caseRotateMode)
        {
            if(caseRotateMode.isSelected())
            {
                parent.getVisualizer().resetFlags();
                parent.getVisualizer().setCaseRotateMode(true);

                //disable other modes in visualizer
                parent.getVisualizer().setCaseEditMode(false);
                parent.getVisualizer().setCaseMoveMode(false);
                parent.getVisualizer().setProductMoveMode(false);
                parent.getVisualizer().setZoomMode(false);
                routerMoveMode.setSelected(false);

                //disable local mode variables
                caseResizeMode.setSelected(false);
                routerMoveMode.setSelected(false);
                productMoveMode.setSelected(false);
                zoomMode.setSelected(false);
                caseMoveMode.setSelected(false);
            }
            else
            {
                parent.getVisualizer().resetFlags();
            }
        }
        //Case movement checkbox handler
        else if(e.getSource() == caseMoveMode)
        {
            if(caseMoveMode.isSelected())
            {
                parent.getVisualizer().resetFlags();
                parent.getVisualizer().setCaseMoveMode(true);

                //disable other modes in visualizer
                parent.getVisualizer().setCaseRotateMode(false);
                parent.getVisualizer().setCaseEditMode(false);
                parent.getVisualizer().setProductMoveMode(false);
                parent.getVisualizer().setZoomMode(false);
                routerMoveMode.setSelected(false);

                //disable local mode variables
                caseResizeMode.setSelected(false);
                productMoveMode.setSelected(false);
                zoomMode.setSelected(false);
                routerMoveMode.setSelected(false);
                caseRotateMode.setSelected(false);
            }
            else
            {
                parent.getVisualizer().resetFlags();
            }
        }
        //Case resizing checkbox handler
        else if(e.getSource() == productMoveMode)
        {
            if(productMoveMode.isSelected())
            {
                parent.getVisualizer().resetFlags();
                parent.getVisualizer().setProductMoveMode(true);

                //disable other modes in visualizer
                parent.getVisualizer().setCaseRotateMode(false);
                parent.getVisualizer().setCaseEditMode(false);
                parent.getVisualizer().setCaseMoveMode(false);
                parent.getVisualizer().setZoomMode(false);
                parent.getVisualizer().setRouterMoveMode(false);

                //disable local mode variables
                caseMoveMode.setSelected(false);
                caseResizeMode.setSelected(false);
                zoomMode.setSelected(false);
                routerMoveMode.setSelected(false);
                caseRotateMode.setSelected(false);
            }
            else
            {
                parent.getVisualizer().resetFlags();
            }
        }
        //Case resizing checkbox handler
        else if(e.getSource() == zoomMode)
        {
            if(zoomMode.isSelected())
            {
                parent.getVisualizer().resetFlags();
                parent.getVisualizer().setZoomMode(true);

                //disable other modes in visualizer
                parent.getVisualizer().setProductMoveMode(false);
                parent.getVisualizer().setCaseMoveMode(false);
                parent.getVisualizer().setCaseEditMode(false);
                parent.getVisualizer().setCaseRotateMode(false);
                parent.getVisualizer().setZoomMode(false);

                //disable local mode variables
                caseMoveMode.setSelected(false);
                caseRotateMode.setSelected(false);
                caseResizeMode.setSelected(false);
                productMoveMode.setSelected(false);
                routerMoveMode.setSelected(false);
            }
            else
            {
                parent.getVisualizer().resetFlags();
            }
        }
        //Router movement handler
        else if(e.getSource() == routerMoveMode)
        {
            if(routerMoveMode.isSelected())
            {
                parent.getVisualizer().resetFlags();
                parent.getVisualizer().setRouterMoveMode(true);

                //disable other modes in visualizer
                parent.getVisualizer().setCaseRotateMode(false);
                parent.getVisualizer().setZoomMode(false);
                parent.getVisualizer().setProductMoveMode(false);
                parent.getVisualizer().setCaseMoveMode(false);
                parent.getVisualizer().setCaseEditMode(false);

                //disable local mode variables
                caseMoveMode.setSelected(false);
                caseRotateMode.setSelected(false);
                caseResizeMode.setSelected(false);
                productMoveMode.setSelected(false);
                zoomMode.setSelected(false);
            }
            else
            {
                parent.getVisualizer().resetFlags();
            }
        }
    }

    public void clear() {

        //disable all modes in visualizer
        parent.getVisualizer().setCaseRotateMode(false);
        parent.getVisualizer().setZoomMode(false);
        parent.getVisualizer().setProductMoveMode(false);
        parent.getVisualizer().setCaseMoveMode(false);
        parent.getVisualizer().setCaseEditMode(false);
        parent.getVisualizer().setRouterMoveMode(false);

        //disable all local mode variables
        caseMoveMode.setSelected(false);
        caseRotateMode.setSelected(false);
        caseResizeMode.setSelected(false);
        productMoveMode.setSelected(false);
        zoomMode.setSelected(false);
        routerMoveMode.setSelected(false);

    }

}
