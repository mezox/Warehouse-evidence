package client;

import client.gui.Window;

import javax.swing.*;

/**
 * Class:
 * Author: martinvy
 * Date:   28.11.2014
 * Info:
 */
public class Main {

    public static void main(String[] args) {

        Window mainWindow = new Window("PDB project");
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.setSize(1280, 720);
        mainWindow.setExtendedState(mainWindow.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        mainWindow.setVisible(true);
    }
}
