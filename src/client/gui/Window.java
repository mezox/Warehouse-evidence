package client.gui;

import client.controller.CustomOutputStream;
import client.model.Database;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.border.Border;

public class Window extends JFrame implements ActionListener
{
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    
    private static JMenuBar mb;
    
    private static JMenu menu_view;
    private static JMenu menu_db;
    private static JMenu menu_query;
    private static JMenu menu_help;
    
    private static JCheckBoxMenuItem menu_view_routers_range;
    private static JCheckBoxMenuItem menu_view_routers;
    private static JCheckBoxMenuItem menu_view_paths;
    
    private static JMenuItem menu_db_con;
    private static JMenuItem menu_db_dc;
    private static JMenuItem menu_db_init;

    private static JMenuItem menu_help_view;

    private static JMenuItem menu_query_search;
    private static JMenuItem menu_query_areaOfRouters;
    private static JMenuItem menu_query_areaOfProducts;
    private PrintStream printStreamOutputPanel;

    private GridBagConstraints gbc = new GridBagConstraints();

    private String[] login_info;

    private Database db;
    private DBVisualizer visual;
    private LoadingDialog           ld = null;

    public RightPanel right_panel;
    public OptionsPanel optionsPanel;
    public ModesPanel modesPanel;
    public JPanel bottom;
    public JScrollPane sp;
    public ListPanel listPanel;
    private JTabbedPane tabs;

    /**
     * Create main Window (frame) and add all components to this.
     * Main components are window menu, visual panel, right panel
     * and bottom panel.
     * @param title title of window
     */
    public Window(String title)
    {
        super(title);
        setLayout(new BorderLayout());

        // menu
        createMenu();

        // main panel includes visual panel and bottom panel
        JPanel main = new JPanel(new BorderLayout());

        // right panels
        createRightPanels();

        // visualization panel
        visual = new DBVisualizer(right_panel, this);

        sp = new JScrollPane(visual);
        //sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        main.add(sp);
//        main.add(visual);

        // bottom panel
        createBottomPanel();
        main.add(bottom, BorderLayout.PAGE_END);

        // add to window
        add(main, BorderLayout.CENTER);
        add(optionsPanel, BorderLayout.EAST);
    }

    /**
     * Create menu for main window.
     */
    public void createMenu()
    {
        mb = new JMenuBar();

        //build the 1st level of menu
        menu_view = new JMenu("View");
        menu_db = new JMenu("Database");
        menu_query = new JMenu("Queries");
        menu_help = new JMenu("Help");

        //build the 2nd level of Database menu
        menu_db_con = new JMenuItem("Connect to database");
        menu_db_init = new JMenuItem("Init database");
        menu_db_dc = new JMenuItem("Close connection");

        menu_help_view = new JMenuItem("About project");

        menu_db.add(menu_db_con);
        menu_db.add(menu_db_init);
        menu_db.add(menu_db_dc);

        menu_help.add(menu_help_view);

        menu_db_con.addActionListener(this);
        menu_db_init.addActionListener(this);
        menu_db_dc.addActionListener(this);
        menu_help_view.addActionListener(this);

        //build the 2nd level of View menu
        menu_view_routers = new JCheckBoxMenuItem("Show routers' positions");
        menu_view_routers_range = new JCheckBoxMenuItem("Show router ranges");
        menu_view_paths = new JCheckBoxMenuItem("Show robot paths");

        menu_view.add(menu_view_routers);
        menu_view.add(menu_view_routers_range);
        menu_view.add(menu_view_paths);

        menu_query_search = new JMenuItem("Search products by date");
        menu_query.add(menu_query_search);
        menu_query_search.addActionListener(this);
        menu_query_areaOfRouters = new JMenuItem("Compute area of routers ranges");
        menu_query.add(menu_query_areaOfRouters);
        menu_query_areaOfRouters.addActionListener(this);
        menu_query_areaOfProducts = new JMenuItem("Compute area of products");
        menu_query.add(menu_query_areaOfProducts);
        menu_query_areaOfProducts.addActionListener(this);

        menu_view_routers.addActionListener(this);
        menu_view_routers_range.addActionListener(this);
        menu_view_paths.addActionListener(this);

        mb.add(menu_view);
        mb.add(menu_db);
        mb.add(menu_query);
        mb.add(menu_help);

        setJMenuBar(mb);
    }

    /**
     * Create RightPanel.
     * Create CasePanel, ModesPanel, JPanel and groups them to boxPanel.
     * All these group to OptionsPanel.
     */
    public void createRightPanels() {

        // right panel - product properties, pictures
        right_panel = new RightPanel(this);

        // other right panels
        CasePanel tp = new CasePanel(this);
        modesPanel = new ModesPanel(this);
        listPanel = new ListPanel(this);

        // group all other right panels into one
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new GridLayout(0,1));
        boxPanel.add(tp);
        boxPanel.add(modesPanel);

        //JLabel rl = new JLabel("Results of pictures similarity:");
        //boxPanel.add(rl);

        //listPanel.setPreferredSize(new Dimension(200,300));
        boxPanel.add(listPanel);

        // create panels block on window's right side
        optionsPanel = new OptionsPanel(right_panel, boxPanel);
    }

    /**
     * Create tabbed bottom panel containing terminal and output panels.
     */
    public void createBottomPanel() {

        // bottom panel
        bottom = new JPanel();
        bottom.setBackground(JColor.WINDOW_BG);
        bottom.setLayout(new GridLayout(1,0));

        // bottom tabs
        tabs = new JTabbedPane(JTabbedPane.TOP);

        // terminal panel
        JPanel terminal = new JPanel(new GridLayout(1, 0));
        JTextArea textArea = new JTextArea("", 10, 10);
        JScrollPane scrollPane = new JScrollPane(textArea);
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        terminal.add(scrollPane);
        tabs.addTab("terminal", terminal);

        // output panel
        JPanel output = new JPanel(new GridLayout(1, 0));
        JTextArea outputTextArea = new JTextArea();
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        printStreamOutputPanel = new PrintStream(new CustomOutputStream(outputTextArea));
        output.add(outputScrollPane);
        tabs.addTab("output", output);

        // add tabs to bottom panel, add bottom to main
        bottom.add(tabs);
        bottom.setPreferredSize(new Dimension(850, 150));
    }


    /**
     * Action listeners on mouse click.
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev)
    {
        //clicked on 'Connect to Database' from Database menu
        if (ev.getSource() == menu_db_con)
        {
            LogInDialog l_dialog = new LogInDialog(this);
            l_dialog.setModal(true);
            login_info = l_dialog.getLoginInfo();

            if(login_info[0].length() > 0 && login_info[1].length() > 0)
            {
                db = new Database(login_info[0], login_info[1]);

                ld = new LoadingDialog("Connecting to database...");
                ld.setLocationRelativeTo(getParent());

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try {
                            db.loadDB();
                        }
                        catch (Exception e)
                        {
                            //Dispose LoadingDialog if its visible
                            if (ld != null && ld.isVisible())
                            {
                                ld.dispose();
                            }
                            System.out.println("Error with connecting to the database occurred.");
                            return;
                        }

                        if (ld != null && ld.isVisible())
                        {
                            ld.dispose();
                        }
                    }
                }).start();

                ld.setVisible(true);

                visual.setDatabase(db);
                right_panel.setDatabase(db);
                listPanel.setDatabase(db);

                /*try {
                    visual.initSectors();   //create sector rectangles
                } catch (SQLException e) {
                    e.printStackTrace();
                }*/
                visual.repaint();
            }
        }
        else if(ev.getSource() == menu_db_init) {
            if (db != null) {

                ld = new LoadingDialog("Initialization...");
                ld.setLocationRelativeTo(getParent());

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try {
                            db.initDb();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();

                            //Dispose LoadingDialog if its visible
                            if (ld != null && ld.isVisible())
                            {
                                ld.dispose();
                            }
                            System.out.println("Error with initialization occurred.");
                            return;
                        }

                        if (ld != null && ld.isVisible())
                        {
                            ld.dispose();
                        }
                    }
                }).start();

                ld.setVisible(true);
                visual.repaint();
            }
        }

        //clicked on 'Close connection' from Database menu
        else if(ev.getSource() == menu_db_dc)
        {
            db = null;
            visual.setDatabase(db);
            right_panel.clear();
            modesPanel.clear();
            visual.revalidate();
            visual.repaint();
        }
        else if(ev.getSource() == menu_view_routers_range)
        {
            if(menu_view_routers_range.isSelected()) {
                visual.showRanges(true);
                visual.repaint();
            }
            else
            {
                visual.showRanges(false);
                visual.repaint();
            }
        }
        else if(ev.getSource() == menu_view_routers)
        {
            if(menu_view_routers.isSelected()) {
                visual.showPlacement(true);
                visual.repaint();
            }
            else
            {
                visual.showPlacement(false);
                visual.repaint();
            }
        }
        else if(ev.getSource() == menu_query_search)
        {
            if (db != null) {
                SearchDialog dialog = new SearchDialog(this);
                dialog.setDatabase(db);
                dialog.setVisible(true);
                tabs.setSelectedIndex(1);
            }
        }else if (ev.getSource() == menu_help_view) {

            HelpDialog hd = new HelpDialog();
            hd.setVisible(true);

        }
        else if(ev.getSource() == menu_query_areaOfRouters)
        {
            tabs.setSelectedIndex(1);
            printStreamOutputPanel.println("Area of all routers ranges: " + db.computeAreaOfRouters());
        }
        else if(ev.getSource() == menu_query_areaOfProducts)
        {
            tabs.setSelectedIndex(1);
            printStreamOutputPanel.println(
                "Capacity of warehouse: " + db.computeAreaOfProducts());
        }
    }

    /**
     * Changes font in all child JComponents of selected component
     * @param component component in which we want to change font
     * @param font  font
     */
    public static void changeFont ( Component component, Font font )
    {
        component.setFont ( font );
        if ( component instanceof Container )
        {
            for ( Component child : ( ( Container ) component ).getComponents () )
            {
                changeFont ( child, font );
            }
        }
    }


    //getters
    public int getWindowWidth() {
        return WIDTH;
    }
    public int getWindowHeight() {
        return HEIGHT;
    }
    public DBVisualizer getVisualizer() { return visual; }

    public PrintStream getPrintStreamOutputPanel() {
        return printStreamOutputPanel;
    }
}

