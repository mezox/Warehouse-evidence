package client.gui;

import java.awt.*;

/**
 * Abstract class containing RGB color defintions
 * for objects in application
 * @author Tomas Kubovcik <xkubov02@stud.fit.vutbr.cz>
 */
abstract class JColor
{
    public static Color MINI_PANELS = new Color(230,230,250);
    public static Color MIDDLE_PANEL = new Color(119,136,153);
    public static Color SECTOR_SELECTED = new Color(255,165,0);
    public static Color SECTOR_SELECTED_TRANSPARENT = new Color((float) 0.2, (float) 0.1, (float) 0, (float) 0.4);
    public static Color TYPE_NOTEBOOKS = new Color(255,178,102);
    public static Color TYPE_FRIDGES = new Color(153,153,255);
    public static Color CASE_SELECTED = new Color(255,160,122);
    public static Color WINDOW_BG = new Color(224,224,224);
    public static Color SECTOR_EMPTY = new Color(144,238,144);
    public static Color SECTOR_NOT_EMPTY = new Color(255,99,71);
    public static Color ROUTER_PLACEMENT = new Color(25,25,112, 128);
}
