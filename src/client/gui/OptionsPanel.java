package client.gui;

import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel
{
    private RightPanel rightPanel;
    private JPanel boxpanel;

    public OptionsPanel(RightPanel r, JPanel b)
    {
        rightPanel = r;
        boxpanel = b;

        setLayout(new BorderLayout());
        add(r);
        add(b, BorderLayout.EAST);
    }
}
