package client.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Warehouse map zoom slider
 * @author Tomas Kubovcik <xkubov02@stud.fit.vutbr.cz>
 */
public class Slider extends JSlider
{
    public Slider(final DBVisualizer parent)
    {
        super();

        setOpaque(false);
        setBackground(Color.BLUE);
        setOrientation(HORIZONTAL);
        setMinimum(1);
        setMaximum(10);
        setMajorTickSpacing(1);
        setValue(1);
        setPaintLabels(true);


        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();

                if (!source.getValueIsAdjusting()) {
                    parent.setScale(source.getValue());
                    parent.repaint();
                }
            }
        });
    }



    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(getBackground());
        g2d.setComposite(AlphaComposite.SrcOver.derive(0.0f));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();

        super.paintComponent(g);
    }
}
