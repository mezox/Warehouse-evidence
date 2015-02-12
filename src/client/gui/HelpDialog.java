package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Petr Kolacek, xkolac11@stud.fit.vutbr.cz
 */
public class HelpDialog extends JDialog implements ActionListener{

    JButton ok_button = null;

    public HelpDialog() {

        setLocationRelativeTo(null);

        JPanel pContent = new JPanel();
        pContent.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;

        JLabel l1 = new JLabel("Projekt do predmetu PDB,2014", JLabel.CENTER);
        pContent.add(l1, gbc);
        gbc.gridy++;

        JLabel l2 = new JLabel("Martin Veselovsky, xvesel60@stud.fit.vutbr.cz", JLabel.CENTER);
        pContent.add(l2, gbc);
        gbc.gridy++;

        JLabel l3 = new JLabel("Tomas Kubovcik, xkubov02@stud.fit.vutbr.cz", JLabel.CENTER);
        pContent.add(l3, gbc);
        gbc.gridy++;

        JLabel l4 = new JLabel("Petr Kolacek, xkolac11@stud.fit.vutbr.cz", JLabel.CENTER);
        pContent.add(l4, gbc);
        gbc.gridy++;

        JLabel l5 = new JLabel("Projekt realizuje aplikaci, skladovou evidenci, ktera komunikuje", JLabel.CENTER);
        pContent.add(l5, gbc);
        gbc.gridy++;

        JLabel l6 = new JLabel("s prostorovou databazi a obsluhuje data v ni ulozena.", JLabel.CENTER);
        pContent.add(l6, gbc);
        gbc.gridy++;

        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        pContent.add(ok_button, gbc);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(false);
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
