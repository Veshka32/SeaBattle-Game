
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TreeSet;

public final class StdDrawForSeaBattle implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    public static final Color BLACK = Color.BLACK;

    /**
     * The color red.
     */
    public static final Color RED = Color.RED;

    /**
     * The color white.
     */
    public static final Color WHITE = Color.WHITE;

    // default colors
    private static final Color DEFAULT_PEN_COLOR = BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = WHITE;
    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    private static final int DEFAULT_SIZE = 512;
    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;
    // boundary of drawing canvas, 0% border
    // private static final double BORDER = 0.05;
    private static final double BORDER = 0.00;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;
    // default font
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);
    // current pen color
    private static Color penColor;
    private static int width = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;
    // current pen radius
    private static double penRadius;
    // show we draw immediately or wait until next show?
    private static boolean defer = false;
    private static double xmin, ymin, xmax, ymax;
    // for synchronization
    private static Object mouseLock = new Object();
    private static Object keyLock = new Object();
    // current font
    private static Font font;
    // double buffered graphics
    private static BufferedImage offscreenImage, onscreenImage;
    private static Graphics2D offscreen, onscreen;

    // singleton for callbacks: avoids generation of extra .class files
    private static StdDrawForSeaBattle std = new StdDrawForSeaBattle();

    // the frame for drawing to the screen
    private static JFrame frame;

    // mouse state
    private static double mouseX = 0;
    private static double mouseY = 0;
    // transfer mouse and key event to another class
    private static PlayerAction handler;

    // static initializer
    static {
        init();
    }

    // singleton pattern: client can't instantiate
    private StdDrawForSeaBattle() {
    }

    public static void addPlayerActions(SeaBattle seabattle) {
        handler = seabattle;
    }

//    public static void stopPlayerActions(){
//        handler=null;
//    }

    /**
     * Sets the canvas (drawing area) to be 512-by-512 pixels.
     * This also erases the current drawing and resets the coordinate system,
     * pen radius, pen color, and font back to their default values.
     * Ordinarly, this method is called once, at the very beginning
     * of a program.
     */
    public static void setCanvasSize() {
        setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Sets the canvas (drawing area) to be <em>width</em>-by-<em>height</em> pixels.
     * This also erases the current drawing and resets the coordinate system,
     * pen radius, pen color, and font back to their default values.
     * Ordinarly, this method is called once, at the very beginning
     * of a program.
     *
     * @param canvasWidth  the width as a number of pixels
     * @param canvasHeight the height as a number of pixels
     * @throws IllegalArgumentException unless both {@code canvasWidth} and
     *                                  {@code canvasHeight} are positive
     */
    public static void setCanvasSize(int canvasWidth, int canvasHeight) {
        if (canvasWidth <= 0 || canvasHeight <= 0)
            throw new IllegalArgumentException("width and height must be positive");
        width = canvasWidth;
        height = canvasHeight;
        init();
    }

    // init
    private static void init() {
        if (frame != null) frame.setVisible(false);
        frame = new JFrame();
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics();
        onscreen = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();

        // add antialiasing
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);

        // frame stuff
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);

        draw.addMouseListener(std);
        draw.addMouseMotionListener(std);

        frame.setContentPane(draw);
        frame.addKeyListener(std);    // JLabel cannot get keyboard focus
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Standard Draw");
        frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    // create the menu bar (changed to private)
    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem menuItem1 = new JMenuItem(" Save...   ");
        menuItem1.addActionListener(std);
        menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItem1);
        return menuBar;
    }


    /***************************************************************************
     *  User and screen coordinate systems.
     ***************************************************************************/

    /**
     * Sets the <em>x</em>-scale to be the default (between 0.0 and 1.0).
     */
    public static void setXscale() {
        setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
    }

    /**
     * Sets the <em>y</em>-scale to be the default (between 0.0 and 1.0).
     */
    public static void setYscale() {
        setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
    }

    /**
     * Sets the <em>x</em>-scale and <em>y</em>-scale to be the default
     * (between 0.0 and 1.0).
     */
    public static void setScale() {
        setXscale();
        setYscale();
    }

    /**
     * Sets the <em>x</em>-scale to the specified range.
     *
     * @param min the minimum value of the <em>x</em>-scale
     * @param max the maximum value of the <em>x</em>-scale
     * @throws IllegalArgumentException if {@code (max == min)}
     */
    public static void setXscale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
        }
    }

    /**
     * Sets the <em>y</em>-scale to the specified range.
     *
     * @param min the minimum value of the <em>y</em>-scale
     * @param max the maximum value of the <em>y</em>-scale
     * @throws IllegalArgumentException if {@code (max == min)}
     */
    public static void setYscale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        synchronized (mouseLock) {
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
        }
    }

    /**
     * Sets both the <em>x</em>-scale and <em>y</em>-scale to the (same) specified range.
     *
     * @param min the minimum value of the <em>x</em>- and <em>y</em>-scales
     * @param max the maximum value of the <em>x</em>- and <em>y</em>-scales
     * @throws IllegalArgumentException if {@code (max == min)}
     */
    public static void setScale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
        }
    }

    // helper functions that scale from user coordinates to screen coordinates and back
    private static double scaleX(double x) {
        return width * (x - xmin) / (xmax - xmin);
    }

    private static double scaleY(double y) {
        return height * (ymax - y) / (ymax - ymin);
    }

    private static double factorX(double w) {
        return w * width / Math.abs(xmax - xmin);
    }

    private static double factorY(double h) {
        return h * height / Math.abs(ymax - ymin);
    }

    private static double userX(double x) {
        return xmin + x * (xmax - xmin) / width;
    }

    private static double userY(double y) {
        return ymax - y * (ymax - ymin) / height;
    }


    /**
     * Clears the screen to the default color (white).
     */
    public static void clear() {
        clear(DEFAULT_CLEAR_COLOR);
    }

    /**
     * Clears the screen to the specified color.
     *
     * @param color the color to make the background
     */
    public static void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
        draw();
    }

    /**
     * Returns the current pen radius.
     *
     * @return the current value of the pen radius
     */
    public static double getPenRadius() {
        return penRadius;
    }

    /**
     * Sets the radius of the pen to the specified size.
     * The pen is circular, so that lines have rounded ends, and when you set the
     * pen radius and draw a point, you get a circle of the specified radius.
     * The pen radius is not affected by coordinate scaling.
     *
     * @param radius the radius of the pen
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public static void setPenRadius(double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("pen radius must be nonnegative");
        penRadius = radius;
        float scaledPenRadius = (float) (radius * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }

    /**
     * Sets the pen size to the default size (0.002).
     * The pen is circular, so that lines have rounded ends, and when you set the
     * pen radius and draw a point, you get a circle of the specified radius.
     * The pen radius is not affected by coordinate scaling.
     */
    public static void setPenRadius() {
        setPenRadius(DEFAULT_PEN_RADIUS);
    }

    /**
     * Returns the current pen color.
     *
     * @return the current pen color
     */
    public static Color getPenColor() {
        return penColor;
    }

    /**
     * Sets the pen color to the specified color.
     * <p>
     * The predefined pen colors are
     * {@code StdDraw.BLACK}, {@code StdDraw.BLUE}, {@code StdDraw.CYAN},
     * {@code StdDraw.DARK_GRAY}, {@code StdDraw.GRAY}, {@code StdDraw.GREEN},
     * {@code StdDraw.LIGHT_GRAY}, {@code StdDraw.MAGENTA}, {@code StdDraw.ORANGE},
     * {@code StdDraw.PINK}, {@code StdDraw.RED}, {@code StdDraw.WHITE}, and
     * {@code StdDraw.YELLOW}.
     *
     * @param color the color to make the pen
     */
    public static void setPenColor(Color color) {
        if (color == null) throw new IllegalArgumentException();
        penColor = color;
        offscreen.setColor(penColor);
    }

    /**
     * Set the pen color to the default color (black).
     */
    public static void setPenColor() {
        setPenColor(DEFAULT_PEN_COLOR);
    }

    /**
     * Sets the pen color to the specified RGB color.
     *
     * @param red   the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue  the amount of blue (between 0 and 255)
     * @throws IllegalArgumentException if {@code red}, {@code green},
     *                                  or {@code blue} is outside its prescribed range
     */
    public static void setPenColor(int red, int green, int blue) {
        if (red < 0 || red >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue < 0 || blue >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        setPenColor(new Color(red, green, blue));
    }

    /**
     * Sets the font to the specified value.
     *
     * @param font the font
     */
    public static void setFont(Font font) {
        if (font == null) throw new IllegalArgumentException();
        StdDrawForSeaBattle.font = font;
    }

    /**
     * Sets the font to the default font (sans serif, 16 point).
     */
    public static void setFont() {
        setFont(DEFAULT_FONT);
    }


    /***************************************************************************
     *  Drawing geometric shapes.
     ***************************************************************************/

    /**
     * Draws a line segment between (<em>x</em><sub>0</sub>, <em>y</em><sub>0</sub>) and
     * (<em>x</em><sub>1</sub>, <em>y</em><sub>1</sub>).
     *
     * @param x0 the <em>x</em>-coordinate of one endpoint
     * @param y0 the <em>y</em>-coordinate of one endpoint
     * @param x1 the <em>x</em>-coordinate of the other endpoint
     * @param y1 the <em>y</em>-coordinate of the other endpoint
     */
    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    }

    /**
     * Draws one pixel at (<em>x</em>, <em>y</em>).
     * This method is private because pixels depend on the display.
     * To achieve the same effect, set the pen radius to 0 and call {@code point()}.
     *
     * @param x the <em>x</em>-coordinate of the pixel
     * @param y the <em>y</em>-coordinate of the pixel
     */
    private static void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    /**
     * Draws a point centered at (<em>x</em>, <em>y</em>).
     * The point is a filled circle whose radius is equal to the pen radius.
     * To draw a single-pixel point, first set the pen radius to 0.
     *
     * @param x the <em>x</em>-coordinate of the point
     * @param y the <em>y</em>-coordinate of the point
     */
    public static void point(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);

        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (scaledPenRadius <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - scaledPenRadius / 2, ys - scaledPenRadius / 2,
                scaledPenRadius, scaledPenRadius));
        draw();
    }

    /**
     * Draws a circle of the specified radius, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x      the <em>x</em>-coordinate of the center of the circle
     * @param y      the <em>y</em>-coordinate of the center of the circle
     * @param radius the radius of the circle
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public static void circle(double x, double y, double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * radius);
        double hs = factorY(2 * radius);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }

    /**
     * Draws a filled circle of the specified radius, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x      the <em>x</em>-coordinate of the center of the circle
     * @param y      the <em>y</em>-coordinate of the center of the circle
     * @param radius the radius of the circle
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public static void filledCircle(double x, double y, double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * radius);
        double hs = factorY(2 * radius);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }

    /**
     * Draws an ellipse with the specified semimajor and semiminor axes,
     * centered at (<em>x</em>, <em>y</em>).
     *
     * @param x             the <em>x</em>-coordinate of the center of the ellipse
     * @param y             the <em>y</em>-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws IllegalArgumentException if either {@code semiMajorAxis}
     *                                  or {@code semiMinorAxis} is negative
     */
    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (!(semiMajorAxis >= 0)) throw new IllegalArgumentException("ellipse semimajor axis must be nonnegative");
        if (!(semiMinorAxis >= 0)) throw new IllegalArgumentException("ellipse semiminor axis must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * semiMajorAxis);
        double hs = factorY(2 * semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }

    /**
     * Draws a square of side length 2r, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the square
     * @param y          the <em>y</em>-coordinate of the center of the square
     * @param halfLength one half the length of any side of the square
     * @throws IllegalArgumentException if {@code halfLength} is negative
     */
    public static void square(double x, double y, double halfLength) {
        if (!(halfLength >= 0)) throw new IllegalArgumentException("half length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfLength);
        double hs = factorY(2 * halfLength);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }

    /**
     * Draws a filled square of the specified size, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the square
     * @param y          the <em>y</em>-coordinate of the center of the square
     * @param halfLength one half the length of any side of the square
     * @throws IllegalArgumentException if {@code halfLength} is negative
     */
    public static void filledSquare(double x, double y, double halfLength) {
        if (!(halfLength >= 0)) throw new IllegalArgumentException("half length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfLength);
        double hs = factorY(2 * halfLength);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }


    /**
     * Draws a rectangle of the specified size, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the rectangle
     * @param y          the <em>y</em>-coordinate of the center of the rectangle
     * @param halfWidth  one half the width of the rectangle
     * @param halfHeight one half the height of the rectangle
     * @throws IllegalArgumentException if either {@code halfWidth} or {@code halfHeight} is negative
     */
    public static void rectangle(double x, double y, double halfWidth, double halfHeight) {
        if (!(halfWidth >= 0)) throw new IllegalArgumentException("half width must be nonnegative");
        if (!(halfHeight >= 0)) throw new IllegalArgumentException("half height must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfWidth);
        double hs = factorY(2 * halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }

    /**
     * Draws a filled rectangle of the specified size, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the rectangle
     * @param y          the <em>y</em>-coordinate of the center of the rectangle
     * @param halfWidth  one half the width of the rectangle
     * @param halfHeight one half the height of the rectangle
     * @throws IllegalArgumentException if either {@code halfWidth} or {@code halfHeight} is negative
     */
    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
        if (!(halfWidth >= 0)) throw new IllegalArgumentException("half width must be nonnegative");
        if (!(halfHeight >= 0)) throw new IllegalArgumentException("half height must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfWidth);
        double hs = factorY(2 * halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }


    /**
     * Copies offscreen buffer to onscreen buffer. There is no reason to call
     * this method unless double buffering is enabled.
     */
    public static void show() {
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }

    // draw onscreen if defer is false
    private static void draw() {
        if (!defer) show();
    }

    /**
     * Enable double buffering. All subsequent calls to
     * drawing methods such as {@code line()}, {@code circle()},
     * and {@code square()} will be deffered until the next call
     * to show(). Useful for animations.
     */
    public static void enableDoubleBuffering() {
        defer = true;
    }

    /**
     * Disable double buffering. All subsequent calls to
     * drawing methods such as {@code line()}, {@code circle()},
     * and {@code square()} will be displayed on screen when called.
     * This is the default.
     */
    public static void disableDoubleBuffering() {
        defer = false;
    }


    /***************************************************************************
     *  Save drawing to a file.
     ***************************************************************************/

    /**
     * Saves the drawing to using the specified filename.
     * The supported image formats are JPEG and PNG;
     * the filename suffix must be {@code .jpg} or {@code .png}.
     *
     * @param filename the name of the file with one of the required suffixes
     */
    public static void save(String filename) {
        if (filename == null) throw new IllegalArgumentException();
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);

        // png files
        if ("png".equalsIgnoreCase(suffix)) {
            try {
                ImageIO.write(onscreenImage, suffix, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // need to change from ARGB to RGB for JPEG
        // reference: http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
        else if ("jpg".equalsIgnoreCase(suffix)) {
            WritableRaster raster = onscreenImage.getRaster();
            WritableRaster newRaster;
            newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[]{0, 1, 2});
            DirectColorModel cm = (DirectColorModel) onscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
                    cm.getRedMask(),
                    cm.getGreenMask(),
                    cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false, null);
            try {
                ImageIO.write(rgbBuffer, suffix, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid image file type: " + suffix);
        }
    }


    /**
     * This method cannot be called directly.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        FileDialog chooser = new FileDialog(StdDrawForSeaBattle.frame, "Use a .png or .jpg extension", FileDialog.SAVE);
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            StdDrawForSeaBattle.save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = StdDrawForSeaBattle.userX(e.getX());
            mouseY = StdDrawForSeaBattle.userY(e.getY());
            //handler.MouseClicked(mouseX, mouseY);
        }

    }


        @Override
        public void mouseEntered (MouseEvent e){
            // this body is intentionally left empty
        }

        /**
         * This method cannot be called directly.
         */
        @Override
        public void mouseExited (MouseEvent e){
            // this body is intentionally left empty
        }


        @Override
        public void mousePressed (MouseEvent e){

        }

        /**
         * This method cannot be called directly.
         */
        @Override
        public void mouseReleased (MouseEvent e){

        }

        /**
         * This method cannot be called directly.
         */
        @Override
        public void mouseDragged (MouseEvent e){

        }

        /**
         * This method cannot be called directly.
         */
        @Override
        public void mouseMoved (MouseEvent e){

        }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        synchronized (keyLock) {

        }
    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyPressed(KeyEvent e) {

    }

    /**
     * This method cannot be called directly.
     */
    @Override
    public void keyReleased(KeyEvent e) {

    }

}
