package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Created by Petr Kolacek, xkolac11
 */
public class InfoDialog extends JDialog implements ActionListener {

    JButton ok_button = null;

    public InfoDialog(String msg) {

        setLocationRelativeTo(null);

        JPanel pContent = new JPanel();
        pContent.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridy = 0;

        JLabel info = new JLabel(msg, JLabel.CENTER);
        pContent.add(info, gbc);

        gbc.gridy++;

        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        pContent.add(ok_button, gbc);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);

        setContentPane(pContent);

        pack();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        this.setModal(false);
        this.setVisible(false);

    }
}
