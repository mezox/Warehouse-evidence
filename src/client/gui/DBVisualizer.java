package client.gui;

import client.model.*;
import oracle.spatial.geometry.JGeometry;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import javax.swing.*;

/**
 * Panel for drawing spatial data and its transformations
 * @author Tomas Kubovcik <xkubov02@stud.fit.vutbr.cz>, Petr Kolacek <xkolac11@stud.fit.vutbr.cz>
 * */
public class DBVisualizer extends JPanel implements MouseListener,MouseMotionListener, MouseWheelListener
{
    private Database    database = null;        //reference to database
    private RightPanel  rightPanel = null;      //product panel
    private int         tdx = 0;                //Last Mouse X Coord
    private int         tdy = 0;                //Last Mouse Y Coord

    private Sector      clickedSector = null;   //Selected Sector
    private Regal       clickedCase = null;     //Selected Case
    private Rectangle   movingSector = null;    //Selected Sector 'copy' for product movement
    private Rectangle   caseBeforeResize = null;
    private Product     clickedProduct = null;
    private Router      clickedRouter = null;


    private boolean     pressOut = false;       //mouse button released flag

    //Modes Enable/Disable, flags
    private boolean     caseResizeMode = false;
    private boolean     caseMoveMode = false;
    private boolean     productMoveMode = false;
    private boolean     productAddMode = false;
    private boolean     zoomMode = false;
    private int         cornerGrabbed = -1;
    private boolean     showRoutersRanges = false;
    private boolean     showRoutersPlacement = false;
    private boolean     routerMoveMode = false;
    private boolean     caseRotateMode = false;



    private boolean     disableMouse = false;

    private double      sC = 1.0;               //scale coefficient
    private Window      parent;
    private Dimension   dim = new Dimension(500, 500);




    int xDiff, yDiff;
    Container c;

    /**
     * Constructor
     *
     * @param rp     Options panel
     * @param parent parent Window
     */
    public DBVisualizer(RightPanel rp, Window parent)
    {
        rightPanel = rp;
        this.parent = parent;

        setBackground(JColor.WINDOW_BG);

        //add mouse listeners
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createHorizontalGlue());

        setPreferredSize(new Dimension(500, 500));
    }

    /**
     * Paints shapes to canvas
     * @param g graphics
     */
    public void paint(Graphics g)
    {
        super.paint(g);

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        //draw something if db is loaded
        if (database != null)
        {
            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHints(rh);

            //setScale if in zoom mode
            if(zoomMode)
                g2D.scale(sC, sC);

            //regals visualization
            drawCases(g2D);

            // draw paths
            for (Path path: database.getPaths()) {
                g2D.setPaint(Color.BLACK);
                g2D.draw(path.getShape());
            }

            // draw all sectors (green)
            if (productMoveMode || productAddMode)
                drawSectors(g2D);

            // products visualization
            for (Product product : database.getProducts())
            {
                Sector c = database.getSector(product.getSector_id());
                Shape shape = c.getShape();

                g2D.setPaint(JColor.SECTOR_NOT_EMPTY);

                if(c.equals(clickedSector))
                {
                    g2D.setPaint(JColor.SECTOR_SELECTED);
                }

                // stationary objects
                g2D.fill(shape);
                g2D.setPaint(Color.BLACK);
                g2D.draw(shape);
                g2D.setPaint(Color.BLACK);
                g2D.drawString(Integer.toString(product.getSector_id()), (int) shape.getBounds().getCenterX() - 8, (int) shape.getBounds().getCenterY() + 5);
            }

            drawRouters(g2D);

            /*if(clickedRouter != null)
                System.out.println("shape:" + clickedRouter.getRangeShape());*/

            /**
             * Router movement
             */
            if(routerMoveMode && clickedRouter != null)
            {
                clickedRouter.setPoint_geom(JGeometry.createPoint(new double[]{tdx, tdy}, 2, 0));
                clickedRouter.setRangeShape(null);
                drawCircle(g2D, tdx, tdy, clickedRouter.getRadius()*2);

            }

            /**
             * Moving and Repainting of selected product in product move mode
             */
            if(productMoveMode && clickedSector != null && movingSector != null)
            {
                int dx, dy;

                //get sector's center
                dx = (int) (movingSector.getBounds2D().getCenterX());
                dy = (int) (movingSector.getBounds2D().getCenterY());

                //check if we are inside window
                checkPosition();

                Rectangle productR = new Rectangle(
                        (int)movingSector.getBounds().getMinX() + (tdx - dx),
                        (int)movingSector.getBounds().getMinY() + (tdy - dy),
                        (int)movingSector.getBounds().getWidth(),
                        (int)movingSector.getBounds().getHeight());

                g2D.setPaint(JColor.SECTOR_SELECTED);
                movingSector = productR;

                g2D.fill(movingSector);
                g2D.setPaint(Color.BLACK);
                g2D.draw(movingSector);
            }

            /**
             * Repainting of selected sector in product add mode
             */
            if (productAddMode) {

                if (clickedSector != null) {
                    g2D.setPaint(JColor.SECTOR_SELECTED_TRANSPARENT);
                    g2D.fill(clickedSector.getShape());
                } else {
                    drawEmptySectors(g2D);
                }


            }


            /**
             * Moving and Repainting of selected case in caseMove mode
             * Also moves and repaint sectors contained in case, if any
             */
            if (clickedCase != null && caseMoveMode)
            {
                //get clicked case shape
                Regal r = clickedCase;
                Shape cShape = clickedCase.getShape();

                int dx, dy;

                //get sector's center
                dx = (int) (cShape.getBounds2D().getCenterX());
                dy = (int) (cShape.getBounds2D().getCenterY());

                //check if we are inside window
                checkPosition();

                /**
                 * Should be done by AffineTransformation but it transforms
                 * shape from Rectangle2D to Path2D which is undesired
                 */
                Rectangle caseR = new Rectangle(
                        (int)cShape.getBounds().getMinX() + (tdx - dx),
                        (int)cShape.getBounds().getMinY() + (tdy - dy),
                        (int)cShape.getBounds().getWidth(),
                        (int)cShape.getBounds().getHeight());

                clickedCase.setShape(caseR);
                clickedCase.shape2JGeometry();

                int width;
                int height;


                if(clickedCase.getVerticalOrientation()) {
                    width = Regal.SECTOR_HEIGHT;
                    height = Regal.SECTOR_WIDTH;
                }
                else
                {
                    width = Regal.SECTOR_WIDTH;
                    height = Regal.SECTOR_HEIGHT;
                }

                /**
                 * sectors' movement to new case position
                 * also could be done by affine transforms
                 */
                for(Sector s: database.getSectors())
                {
                    //check if selected case contains sector
                    if(s.getRegal_id() == clickedCase.getRegal_id())
                    {
                        Shape ss = s.getShape();

                        Rectangle sectorR = new Rectangle(
                                (int)ss.getBounds().getMinX() + (tdx - dx),
                                (int)ss.getBounds().getMinY() + (tdy - dy),
                                width,
                                height);

                        s.setShape(sectorR);
                        s.shape2JGeometry();
                    }
                }
            }
        }
    }

    /**
     * Checks if the object's bounding box is contained within the window.  If the shape
     * is not contained within the window, it is redrawn so that it is adjacent
     * to the edge of the window and just inside the window.
     */
    private void checkPosition()
    {
        int w = 0;
        int h = 0;

        if (productMoveMode)
        {
            w = (int) movingSector.getBounds2D().getWidth();
            h = (int) movingSector.getBounds2D().getHeight();
        }
        else if (caseMoveMode)
        {
            w = (int) clickedCase.getShape().getBounds2D().getWidth();
            h = (int) clickedCase.getShape().getBounds2D().getHeight();
        }

        //out off right edge
        if ((tdx + w / 2) > getWidth())
        {
            tdx = getWidth() - (w / 2);
        }

        //out off left edge
        if ((tdx - w / 2) < 0)
        {
            tdx = w / 2;
        }

        //out off bottom edge
        if ((tdy + h / 2) > getHeight())
        {
            tdy = getHeight() - (h / 2);
        }

        //out off top edge
        if ((tdy - h / 2) < 0)
        {
            tdy = h / 2;
        }
    }


    /**
     * Invoked when a mouse button has been pressed on a component.     *
     *
     * @param e event
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        //actualize current mouse cursor position
        tdx = e.getX();
        tdy = e.getY();

        //TODO mozno sa moze odmazat
        // return if zoomed or not connected or product movement is disabled
        if ((sC > 1.0 && sC < 10.0) || database == null) {
            return;
        }

        // zoom mode
        if (zoomMode) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            xDiff = e.getX();
            yDiff = e.getY();
        }

        if(caseMoveMode || caseRotateMode)
            findCaseUnderCursor(e);
        else if (clickedCase != null && caseResizeMode) {
            findClickedCorner();
        }
        else if(productMoveMode && movingSector == null && clickedSector == null)
        {
            Shape ss = null;

            //find sector we clicked on
            for (Sector sector : database.getSectors())
            {
                if (sector.getShape().contains((double) tdx, (double) tdy))
                {
                    //store selected sector and its shape
                    clickedSector = sector;
                    ss = sector.getShape();

                    //Find product stored in found sector
                    for(Product product: database.getProducts())
                    {
                        //if found create copy of sector as rectangle
                        if(product.getSector_id() == clickedSector.getSector_id())
                        {
                            //create rectangle representing clicked sector(product)
                            clickedProduct = product;

                            movingSector = new Rectangle(
                                    (int)ss.getBounds().getMinX(),
                                    (int)ss.getBounds().getMinY(),
                                    (int)ss.getBounds().getWidth(),
                                    (int)ss.getBounds().getHeight());

                            return;
                        }
                    }
                }
            }
        }
        //we are in productAddMode
        else {
            if (productAddMode) {

                rightPanel.clear();

                Sector s = null;
                boolean engagedSector = false;

                for (Sector sector : database.getSectors()) {
                    if (sector.getShape().contains((double) tdx, (double) tdy)) {
                        s = sector;
                    }
                }

                if (s != null) {
                    for (Product product : database.getProducts()) {
                        if (product.getSector_id() == s.getSector_id()) {
                            System.out.println("Sector already used.");
                            InfoDialog addInfo = new InfoDialog("Sector is already used.");
                            addInfo.setModal(true);
                            addInfo.setVisible(true);
                            clickedSector = null;
                            repaint();
                            engagedSector = true;
                            break;
                        }
                    }

                    if (!engagedSector) {
                        System.out.println("Unused sector, please fill in product card.");
                        clickedSector = s;
                        repaint();
                    }
                }


            }
            //router movement
            else if(routerMoveMode)
            {
                for(Router r: database.getRouters())
                {
                    //create bounding box around router position to
                    //select router
                    Point p = new Point(
                            (int)r.getPoint().getX(),
                            (int)r.getPoint().getY());

                    Rectangle bb = new Rectangle((int)p.getX() - 10, (int)p.getY() -10, 20, 20);

                    if(bb.contains(tdx,tdy))
                    {
                        clickedRouter = r;

                        break;
                    }
                }
            }
            // click on product to display properties
            else {

                //find shape that has been clicked on
                for (Product product : database.getProducts()) {
                    Sector s = database.getSector(product.getSector_id());
                    if (s.getShape().contains((double) tdx, (double) tdy)) {
                        rightPanel.showProduct(product, 0);
                        this.clickedSector = s;
                        break;
                    }
                    //this.clickedSector=null;
                    repaint();
                }

                // moving product
                if (productMoveMode) {
                    if (clickedSector != null) {
                        // Checks whether or not the cursor is inside of the rectangle
                        // while the user is pressing the mouse.
                        if (clickedSector.getShape().contains(e.getX(), e.getY())) {
                            repaint();
                        } else {
                            pressOut = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e event
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        //we are in product move mode and got selected product to move
        if (clickedSector != null && productMoveMode && movingSector != null)
        {
            Sector newSector = null;

            //find sector under mouse cursor and if its different than clicked sector
            //store product in it
            for (Sector s : database.getSectors())
            {
                if (s.getShape().contains(tdx, tdy) && !clickedSector.equals(s))
                {
                    newSector = s;

                    Product p = database.getProductBySectorID(s.getSector_id());

                    //sector is taken
                    if(p != null)
                    {
                        //finds nearest empty sector
                        try {
                            newSector = database.getNearestSectorInSameRegal(s);
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }

                    //update product's regal and sector
                    clickedProduct.setSector_id(newSector.getSector_id());
                    clickedProduct.setRegal_id(newSector.getRegal_id());

                    //update product in DB
                    try {
                        database.updateProduct(clickedProduct);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }

            clickedProduct = null;
            clickedSector = null;
            movingSector = null;

            repaint();
        }


        //we are in case change size mode and we got one selected
        else if ((clickedCase != null && caseResizeMode) || (clickedCase != null && caseMoveMode))
        {
            if (caseResizeMode)
            {

                // initial shape
                Regal initial = new Regal();
                initial.setShape(caseBeforeResize);

                // set initial coordinates
                double initial_maxX = initial.getShape().getBounds().getWidth();
                double initial_minX = initial.getShape().getBounds().getMinX();
                double initial_maxY = initial.getShape().getBounds().getHeight();
                double initial_minY = initial.getShape().getBounds().getMinY();

                // set new coordinates
                double new_maxX = clickedCase.getShape().getBounds().getWidth();
                double new_minX = clickedCase.getShape().getBounds().getMinX();
                double new_maxY = clickedCase.getShape().getBounds().getHeight();
                double new_minY = clickedCase.getShape().getBounds().getMinY();

                // check if we are setting bigger or smaller rack
                if (new_maxX > initial_maxX || new_maxY > initial_maxY || initial_minX > new_minX || initial_minY > new_minY ) {
                    System.out.println("Resizing rack to BIGGER.");

                    System.out.println(new_maxY);
                    System.out.println(initial_maxY);
                    System.out.println(initial_minY);
                    System.out.println(new_minY);

                    //resizing horizontal rack directly to right
                    if (new_maxX > initial_maxX && new_minX == initial_minX ) {

                        System.out.println("Resizing horizontal rack directly to right.");

                        //first we need to know number of new sectors
                        int new_sectors_cnt = (int)(((new_maxX - initial_maxX) * new_maxY) / (Regal.SECTOR_WIDTH * Regal.SECTOR_HEIGHT));
                        System.out.println("Number of new sectors: " + new_sectors_cnt);

                        int xx = (int)(initial_minX + initial_maxX);
                        int yy = (int)initial_minY;

                        for (int i = 0; i < new_sectors_cnt; i++) {
                            Sector sector = new Sector();
                            sector.setSector_name("Sector " + database.getSector_mark());
                            sector.setShape(new Rectangle(xx, yy, Regal.SECTOR_WIDTH, Regal.SECTOR_HEIGHT));
                            sector.shape2JGeometry();
                            sector.setRegal_id(clickedCase.getRegal_id());

                            try {
                                database.addSector(sector);
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }

                            xx += Regal.SECTOR_WIDTH;

                            if (xx >= ((int)(initial_minX + initial_maxX) + (new_sectors_cnt / 2) * Regal.SECTOR_WIDTH))
                            {
                                xx = (int)(initial_minX + initial_maxX);
                                yy = (int)initial_minY + Regal.SECTOR_HEIGHT;
                            }
                            int sm = database.getSector_mark();
                            database.setSector_mark(sm++);


                        }

                    //resizing horizontal rack directly to left
                    }else if (new_maxX > initial_maxX && new_minX != initial_minX) {

                        System.out.println("Resizing horizontal rack directly to left.");

                        //first we need to know number of new sectors
                        int new_sectors_cnt = (int)(((new_maxX - initial_maxX) * new_maxY) / (Regal.SECTOR_WIDTH * Regal.SECTOR_HEIGHT));
                        System.out.println("Number of new sectors: " + new_sectors_cnt);

                        int xx = (int)new_minX;
                        int yy = (int)new_minY;

                        for (int i = 0; i < new_sectors_cnt; i++) {
                            Sector sector = new Sector();
                            sector.setSector_name("Sector " + database.getSector_mark());
                            sector.setShape(new Rectangle(xx, yy, Regal.SECTOR_WIDTH, Regal.SECTOR_HEIGHT));
                            sector.shape2JGeometry();
                            sector.setRegal_id(clickedCase.getRegal_id());

                            try {
                                database.addSector(sector);
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }

                            xx += Regal.SECTOR_WIDTH;

                            if (xx >= ((int)new_minX + (new_sectors_cnt / 2) * Regal.SECTOR_WIDTH))
                            {
                                xx = (int)new_minX;
                                yy = (int)new_minY + Regal.SECTOR_HEIGHT;
                            }
                            int sm = database.getSector_mark();
                            database.setSector_mark(sm++);

                        }

                    //resizing vertical rack directly to up
                    }else if (new_maxY > initial_maxY && new_minY != initial_minY) {

                        System.out.println("Resizing vertical rack directly to up.");

                        //first we need to know number of new sectors
                        int new_sectors_cnt = (int)(initial_maxX  * (new_maxY - initial_maxY) / (Regal.SECTOR_WIDTH * Regal.SECTOR_HEIGHT));
                        System.out.println("Number of new sectors: " + new_sectors_cnt);

                        int xx = (int)new_minX;
                        int yy = (int)new_minY;

                        for (int i = 0; i < new_sectors_cnt; i++) {
                            Sector sector = new Sector();
                            sector.setSector_name("Sector " + database.getSector_mark());
                            sector.setShape(new Rectangle(xx, yy, Regal.SECTOR_HEIGHT, Regal.SECTOR_WIDTH));
                            sector.shape2JGeometry();
                            sector.setRegal_id(clickedCase.getRegal_id());

                            try {
                                database.addSector(sector);
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }

                            yy += Regal.SECTOR_WIDTH;

                            if (yy >= ((int)new_minY + (new_sectors_cnt / 2) * Regal.SECTOR_WIDTH))
                            {
                                yy = (int)new_minY;
                                xx = (int)new_minX + Regal.SECTOR_HEIGHT;
                            }
                            int sm = database.getSector_mark();
                            database.setSector_mark(sm++);

                        }

                    // resizing vertical rack directly to down
                    }else if (new_maxY > initial_maxY && new_minY == initial_minY) {

                        System.out.println("Resizing vertical rack directly to down.");

                        //first we need to know number of new sectors
                        int new_sectors_cnt = (int)(initial_maxX  * (new_maxY - initial_maxY) / (Regal.SECTOR_WIDTH * Regal.SECTOR_HEIGHT));
                        System.out.println("Number of new sectors: " + new_sectors_cnt);

                        int xx = (int)initial_minX;
                        int yy = (int)(initial_minY + initial_maxY);

                        for (int i = 0; i < new_sectors_cnt; i++) {
                            Sector sector = new Sector();
                            sector.setSector_name("Sector " + database.getSector_mark());
                            sector.setShape(new Rectangle(xx, yy, Regal.SECTOR_HEIGHT, Regal.SECTOR_WIDTH));
                            sector.shape2JGeometry();
                            sector.setRegal_id(clickedCase.getRegal_id());

                            try {
                                database.addSector(sector);
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }

                            yy += Regal.SECTOR_WIDTH;

                            if (yy >= ((int)(initial_minY + initial_maxY) + (new_sectors_cnt / 2) * Regal.SECTOR_WIDTH))
                            {
                                yy = (int)(initial_minY + initial_maxY);
                                xx = (int)initial_minX + Regal.SECTOR_HEIGHT;
                            }
                            int sm = database.getSector_mark();
                            database.setSector_mark(sm++);

                        }

                    }

                    clickedCase.shape2JGeometry();
                    database.updateRegal(clickedCase);

                } else {
                    System.out.println("Setting rack smaller.");

                    try {
                        boolean result = database.checkRackResizing(clickedCase);

                        if (result) {
                            database.deleteSectorsAfterRegalResizing(clickedCase);
                            clickedCase.shape2JGeometry();
                            database.updateRegal(clickedCase);

                        }
                        else
                        {
                            clickedCase.setShape(caseBeforeResize);
                            System.out.println("Warning: Cannot resize case over existing product!");
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }

            }

            else if (caseMoveMode) {
                database.updateRegal(clickedCase);
            }

            clickedCase = null;           //reset selected case
            cornerGrabbed = -1;           //reset corner grab flag
            movingSector = null;
            caseBeforeResize = null;

            //set default cursor
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            repaint();
        }
        else if(routerMoveMode && clickedRouter != null)
        {
            repaint();
            database.updateRouter(clickedRouter);
            clickedRouter = null;
        }else if (productAddMode && clickedSector != null) {

        } else {
            pressOut = false;
            disableMouse = false;
            //clickedSector = null;
            movingSector = null;
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p/>
     * Due to platform-dependent Drag&amp;Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&amp;Drop operation.
     *
     * @param e event
     */
    @Override
    public void mouseDragged(MouseEvent e) {

        //update mouse coords
        tdx = e.getX();
        tdy = e.getY();

        if (zoomMode) {
            c = this.getParent();
            if (c instanceof JViewport) {
                JViewport jv = (JViewport) c;
                Point p = jv.getViewPosition();
                int newX = p.x - (e.getX() - xDiff);
                int newY = p.y - (e.getY() - yDiff);

                int maxX = this.getWidth() - jv.getWidth();
                int maxY = this.getHeight() - jv.getHeight();
                if (newX < 0)
                    newX = 0;
                if (newX > maxX)
                    newX = maxX;
                if (newY < 0)
                    newY = 0;
                if (newY > maxY)
                    newY = maxY;

                jv.setViewPosition(new Point(newX, newY));
            }
        }

        /**
         * Resizing rectangle
         */
        if (cornerGrabbed > -1) {
            AffineTransform af = new AffineTransform();

            //get resizing regal object
            Regal r = clickedCase;
            Shape rack = clickedCase.getShape();

            //store min and max X coordinates
            int minx = (int) rack.getBounds().getMinX();
            int maxx = (int) rack.getBounds().getMaxX();

            //store min and max Y coordinates
            int miny = (int) rack.getBounds().getMinY();
            int maxy = (int) rack.getBounds().getMaxY();

            /**
             * Horizontal | Vertical case check
             */
            Rectangle2D rackR = (Rectangle2D) rack;

            //we are rotating horizontal rack
            if (!r.getVerticalOrientation())
            {
                //resize from left to right and back
                if (Math.abs(tdx - minx) % Regal.SECTOR_WIDTH == 0 && cornerGrabbed == 1) {
                    rackR.setRect(
                            tdx,
                            rackR.getBounds().getMinY(),
                            rackR.getBounds().getWidth() + (rackR.getBounds().getMinX() - tdx),
                            rackR.getBounds().getHeight());
                }

                //resize from right to left and back
                else if (Math.abs(tdx - maxx) % Regal.SECTOR_WIDTH == 0 && cornerGrabbed == 2) {
                    rackR.setRect(
                            rackR.getMinX(),
                            rackR.getMinY(),
                            rackR.getWidth() + (tdx - rackR.getMaxX()),
                            rackR.getHeight());
                }
            }
            //we are rotating vertical rack
            else {
                //resize from top to bottom and back
                if (Math.abs(tdy - miny) % Regal.SECTOR_WIDTH == 0 && cornerGrabbed == 3) {
                    rackR.setRect(
                            rackR.getBounds().getMinX(),
                            tdy,
                            rackR.getBounds().getWidth(),
                            (rackR.getBounds().getHeight()) + (rackR.getBounds().getMinY() - tdy));
                }
                //resize from bottom to top and back
                else if (Math.abs(tdy - maxy) % Regal.SECTOR_WIDTH == 0 && cornerGrabbed == 4) {
                    rackR.setRect(
                            rackR.getMinX(),
                            rackR.getMinY(),
                            rackR.getWidth(),
                            (rackR.getHeight()) + (tdy - rackR.getMaxY()));
                }
            }

            //also update JGeometry
            clickedCase.shape2JGeometry();
        }

        if (!pressOut && (productMoveMode || caseResizeMode || caseMoveMode || routerMoveMode)) {
            repaint();
        }
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e event
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        if(caseResizeMode) {
            findCaseUnderCursor(e);
        }
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e event
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e event
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     * <p/>
     * Handles mouse cursor change if its hovering some specific shapes.
     * Used in resizing, etc.
     *
     * @param e event
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        if (database != null) {
            //check if rack is selected and if we are in edit mode
            if (clickedCase != null && caseResizeMode) {
                if (cornerGrabbed == -1) {
                    //get corners coords
                    int[] corners = clickedCase.getCorners();

                    //create rectangles used to check intersection with corners
                    Rectangle r1 = new Rectangle(corners[0] - 10, corners[1] - 10, 20, 20);
                    Rectangle r2 = new Rectangle(corners[2] - 10, corners[3] - 10, 20, 20);
                    Rectangle r3 = new Rectangle(corners[4] - 10, corners[5] - 10, 20, 20);
                    Rectangle r4 = new Rectangle(corners[6] - 10, corners[7] - 10, 20, 20);

                    //is horizontal?
                    if (!clickedCase.getVerticalOrientation()) {
                        if (r1.contains(e.getX(), e.getY()) || r3.contains(e.getX(), e.getY())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                        } else if (r2.contains(e.getX(), e.getY()) || r4.contains(e.getX(), e.getY())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                        } else
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    } else {
                        if (r1.contains(e.getX(), e.getY()) || r2.contains(e.getX(), e.getY())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                        } else if (r3.contains(e.getX(), e.getY()) || r4.contains(e.getX(), e.getY())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                        } else
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            } else if (zoomMode) {
                tdx = e.getX();
                tdy = e.getY();
                repaint();
            }
        }
    }

    /**
     * Invoked when the mouse wheel is rotated.
     * Set's scale coefficient a.k.a zooms in/out
     *
     * @param e event
     * @see java.awt.event.MouseWheelEvent
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (zoomMode) {
            int notches = e.getWheelRotation();

            if (sC > 1.0 && sC < 1.2 && notches < 0)
                sC = 1.0;
            else if (sC < 10.0 && sC > 9.8 && notches > 0)
                sC = 10.0;
            else if (sC > 0.9 && sC < 1.1 && notches > 0)
                sC += 0.2;
            else if (sC < 10.1 && sC > 9.9 && notches < 0)
                sC -= 0.2;
            else if (sC > 1.1 && sC < 9.9) {
                if (notches > 0)
                    sC += 0.2;
                else
                    sC -= 0.2;
            }

            int newPSX = (int) (dim.getWidth() * sC);
            int newPSY = (int) (dim.getWidth() * sC);

            this.setPreferredSize(new Dimension(newPSX, newPSY));

            repaint();
        }
    }

    /**
     * Sets scale of drawing geometry
     *
     * @param scale size(x,y,z)
     */
    public void setScale(int scale) {
        this.sC = scale;
    }


    /**
     * Enable/Disable case movement
     *
     * @param value true to enable/false to disable
     */
    public void setCaseMoveMode(boolean value) {
        caseMoveMode = value;
    }

    /**
     * Enable/Disable zooming
     *
     * @param value true to enable/false to disable
     */
    public void setZoomMode(boolean value) {
        zoomMode = value;
    }

    /**
     * Enable/Disable rack size edit
     *
     * @param value true to enable/false to disable
     */
    public void setCaseEditMode(boolean value) {
        caseResizeMode = value;
    }

    /**
     * Enable/Disable product movement
     *
     * @param value true to enable/false to disable
     */
    public void setProductMoveMode(boolean value) {
        productMoveMode = value;
    }

    /**
     * Enable/Disable product adding
     *
     * @param value true to enable/false to disable
     */
    public void setProductAddMode(boolean value) {
        productAddMode = value;
    }


    /**
     * Draws selected rack's corner (one that has been clicked on)
     * to indicate possibility of resize
     *
     * @param g    graphics
     * @param rack chosen rack
     */
    private void drawCaseCorners(Graphics2D g, Regal rack) {
        //store current paint color
        Color c = (Color) g.getPaint();

        int[] coords = rack.getCorners();

        //iterate over corners and draw them as quads
        for (int i = 0; i < 8; i = i + 2)
        {
            g.setPaint(Color.RED);
            g.fillRect(coords[i] - 2, coords[i + 1] - 4, 6, 6);
        }

        //set color to previous paint color
        g.setPaint(c);
    }


    /**
     * Draws racks
     *
     * @param g2D graphics context
     */
    public void drawCases(Graphics2D g2D)
    {
        for (int i = 0; i < database.getRegals().size(); i++)
        {
            Regal regal = database.getRegals().get(i);
            Shape shape = regal.getShape();

            g2D.setPaint(Color.BLACK);
            g2D.drawString(regal.getCategory(), shape.getBounds().x, shape.getBounds().y - 1);
            g2D.setPaint(Color.GRAY);

            //drawing selected rack in edit mode
            if ((caseResizeMode || caseMoveMode || caseRotateMode) && (clickedCase == regal)) {
                g2D.setPaint(JColor.CASE_SELECTED);
            }

            g2D.fill(shape);
            g2D.setPaint(Color.BLACK);
            g2D.draw(shape);

            if (caseResizeMode && (clickedCase == regal))
                drawCaseCorners(g2D, regal);
        }
    }

    /**
     * Draws all sectors
     */
    public void drawSectors(Graphics2D g2D)
    {
        for (Sector s : database.getSectors())
        {
            if(clickedCase != null)
                g2D.setPaint(JColor.SECTOR_SELECTED);
            else
                g2D.setPaint(JColor.SECTOR_EMPTY);

            g2D.fill(s.getShape());
            g2D.setPaint(Color.BLACK);
            g2D.draw(s.getShape());

            int dxy = s.getShape().getBounds().height / 2;
            g2D.drawString(Integer.toString(s.getSector_id()), s.getShape().getBounds().x, s.getShape().getBounds().y + dxy);
        }
    }

    /**
     * Draws empty sectors
     */
    public void drawEmptySectors(Graphics2D g2D)
    {
        for (Sector s : database.getSectors()) {
            if (database.getProductBySectorID(s.getSector_id()) == null) {
                if (clickedCase != null)
                    g2D.setPaint(JColor.SECTOR_SELECTED);
                else
                    g2D.setPaint(JColor.SECTOR_EMPTY);

                g2D.fill(s.getShape());
                g2D.setPaint(Color.BLACK);
                g2D.draw(s.getShape());

                int dxy = s.getShape().getBounds().height / 2;
                g2D.drawString(Integer.toString(s.getSector_id()), s.getShape().getBounds().x, s.getShape().getBounds().y + dxy);
            }

        }
    }

    /**
     * Rotates selected case
     * Could be done by AffineTransformation but rotating rectangle
     * transforms shape from Rectangle2D to Path2D which is undesired
     */
    public void rotateSelectedCase()
    {
        if (clickedCase != null) {
            Rectangle rect = new Rectangle(
                    (int) (clickedCase.getShape().getBounds().getCenterX() - clickedCase.getShape().getBounds().getHeight() / 2),
                    (int) (clickedCase.getShape().getBounds().getCenterY() - clickedCase.getShape().getBounds().getWidth() / 2),
                    (int) clickedCase.getShape().getBounds().getHeight(),
                    (int) clickedCase.getShape().getBounds().getWidth());

            Rectangle sectorR;
            int j = 0;
            int var = 0;

            for(Sector s: database.getSectors())
            {
                //check if selected case contains sector
                if(s.getRegal_id() == clickedCase.getRegal_id())
                {
                    Shape ss = s.getShape();

                    //get minX & minY case coord
                    int minx = (int)(rect.getMinX());
                    int miny = (int)(rect.getMinY());

                    int width;
                    int height;

                    //transforming vertical to horizontal
                    if(clickedCase.getVerticalOrientation())
                    {
                        width = Regal.SECTOR_WIDTH;
                        height = Regal.SECTOR_HEIGHT;
                    }
                    //transforming horizontal to vertical
                    else
                    {
                        width = Regal.SECTOR_HEIGHT;
                        height = Regal.SECTOR_WIDTH;
                    }

                    if(!clickedCase.getVerticalOrientation()) {

                        System.out.println("num: " + clickedCase.getNum_sectors_row());
                        if(j < clickedCase.getNum_sectors_row()) {
                            sectorR = new Rectangle(
                                    minx + width,
                                    miny + var,
                                    width,
                                    height);
                        }
                        else {
                            if(j == clickedCase.getNum_sectors_row())
                                var = 0;

                            sectorR = new Rectangle(
                                    minx,
                                    miny + var,
                                    width,
                                    height);
                        }
                    }
                    else {
                        if(j < clickedCase.getNum_sectors_row()) {
                            sectorR = new Rectangle(
                                    (minx + var),
                                    (miny + height),
                                    width,
                                    height);
                        }
                        else {
                            if(j == clickedCase.getNum_sectors_row())
                                var = 0;

                            sectorR = new Rectangle(
                                    (minx + var),
                                    (miny),
                                    width,
                                    height);
                        }
                    }

                    var += Regal.SECTOR_WIDTH;
                    j++;

                    s.changeVerticalOrientation();
                    s.setShape(sectorR);
                    s.shape2JGeometry();

                    try {
                        database.updateSector(s);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            }

            //update object's shape
            clickedCase.setShape(rect);

            //update object's rotation
            clickedCase.changeVerticalOrientation();
            clickedCase.shape2JGeometry();
            database.updateRegal(clickedCase);
        }
    }

    /**
     * Reset's flags enabling working modes
     * Used to prevent unwanted behavior
     */
    public void resetFlags() {
        caseMoveMode = false;
        caseResizeMode = false;
        caseRotateMode = false;
        routerMoveMode = false;
        productMoveMode = false;
        zoomMode = false;

        cornerGrabbed = -1;
        clickedCase = null;
        clickedSector = null;

        sC = 1.0;

        repaint();
    }

    /**
     * Getter for clickedSector
     */
    public Sector getClickedSector() {
        return this.clickedSector;
    }

    /**
     * Setter for clickedSector
     */
    public void setClickedSector(Sector sector) {
        this.clickedSector = sector;
    }

    /**
     * Getter fo clickedCase
     */
    public Regal getClickedCase() {
        return clickedCase;
    }

    /**
     * Finds case that is under actual position of mouse cursor
     * Used in case resizing and movement mode
     */
    private boolean findCaseUnderCursor(MouseEvent e)
    {
        //finds rack
        for (int i = 0; i < database.getRegals().size(); i++)
        {
            Regal regal = database.getRegals().get(i);

            if (regal.getShape().getBounds().contains(e.getX(), e.getY()))
            {
                //store reference to found case
                clickedCase = regal;

                //store default case dimension if resizing wrong
                caseBeforeResize = new Rectangle(
                        (int)clickedCase.getShape().getBounds().getMinX(),
                        (int)clickedCase.getShape().getBounds().getMinY(),
                        (int)clickedCase.getShape().getBounds().getWidth(),
                        (int)clickedCase.getShape().getBounds().getHeight()
                );

                //repaint found case to SELECTED CASE color
                repaint();

                return true;
            }
        }

        return false;
    }

    /**
     * Resizing case, determines which one from set of corners (top|bottom|left|right)
     * has been clicked and sets flag
     */
    private void findClickedCorner()
    {
        //get case's corners
        int[] coords = clickedCase.getCorners();

        Rectangle tr;

        //go through corners and determine if we clicked on some
        for (int i = 0; i < 8; i = i + 2)
        {
            //rectangles for corners
            tr = new Rectangle(coords[i] - 10, coords[i + 1] - 10, 20, 20);

            if (tr.contains(tdx, tdy)) {
                //horizontal case and grabbed left corners
                if (!clickedCase.getVerticalOrientation() && (i == 0 || i == 4))
                    cornerGrabbed = 1;

                    //horizontal case and grabbed right corners
                else if (!clickedCase.getVerticalOrientation() && (i == 2 || i == 6))
                    cornerGrabbed = 2;

                    //vertical case and grabbed top corners
                else if (clickedCase.getVerticalOrientation() && (i == 0 || i == 2))
                    cornerGrabbed = 3;

                    //vertical case and grabbed bottom corners
                else if (clickedCase.getVerticalOrientation() && (i == 4 || i == 6))
                    cornerGrabbed = 4;

                break;
            }
        }
    }


    /**
     *
     * @param g graphics context
     * @param x x coord
     * @param y y coord
     * @param r radius
     */
    public void drawCircle(Graphics2D g, int x, int y, int r)
    {
        x = x - (r/2);
        y = y - (r/2);

        g.setPaint(JColor.ROUTER_PLACEMENT);
        g.fillOval(x,y,r,r);
    }

    /**
     * Draws routers based on selection mode
     * - draw only positions
     * - draw only ranges
     * - draw both
     * @param g2D   graphics context
     */
    private void drawRouters(Graphics2D g2D)
    {
            for (Router router : database.getRouters())
            {
                Point2D rps = router.getPoint();
                Shape rrs = router.getRangeShape();

                if(showRoutersPlacement)
                {
                    drawCircle(
                            g2D,
                            (int)rps.getX(),
                            (int)rps.getY(),
                            10);
                }

                if(showRoutersRanges)
                {
                    if(clickedRouter == null || (clickedRouter != null && !clickedRouter.equals(router)))
                    {
                        g2D.setPaint(new Color(255, 200, 50, 128));
                        g2D.fill(rrs);
                    }
                }
            }
    }


    public void setRouterMoveMode(boolean value)
    {
        routerMoveMode = value;
    }

    public void setCaseRotateMode(boolean value) {
        caseRotateMode = value;
    }

    public boolean getCaseRotateMode() {
        return caseRotateMode;
    }

    /**
     *
     */
    public void showPlacement(boolean value)
    {
        showRoutersPlacement = value;
    }

    /**
     *
     */
    public void showRanges(boolean value)
    {
        showRoutersRanges = value;
    }

    public void setDatabase(Database db)
    {
        database = db;
    }
    public Database getDatabase()
    {
        return database;
    }
}
