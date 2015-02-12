package client.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Loading Dialog, displays progress bar when complicated
 * action is in progress
 * @author Tomas Kubovcik <xkubov02@stud.fit.vutbr.cz>
 */
public class LoadingDialog extends JDialog
{
    public LoadingDialog(String msg)
    {
        setUndecorated(true);
        JPanel pContent = new JPanel();
        pContent.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridy = 0;

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        pContent.add(progress, gbc);

        gbc.gridy++;
        JLabel info = new JLabel(msg, JLabel.CENTER);
        pContent.add(info, gbc);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);

        setContentPane(pContent);

        pack();
    }
}
