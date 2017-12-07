import edu.princeton.cs.algs4.Bag;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StartWindow extends JFrame { //StartWindow is a top-level container
    Container cp;
    JPanel messagePanel = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel();
    JLabel message = new JLabel("Welcome to SeaBattle");
    JLabel message1 = new JLabel("Message 1");
    MyPanel leftBoard = new MyPanel();
    MyPanel rightBoard = new MyPanel();
    State state;
    PlayerAction handler;
    int scale;

    public StartWindow() {
        cp = getContentPane(); //top-level container
        cp.setLayout(new BorderLayout()); //default arrange components
        //set messagePanel
        messagePanel.setPreferredSize(new Dimension(1050, 100));
        messagePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        messagePanel.add(message, BorderLayout.NORTH);
        messagePanel.add(message1, BorderLayout.CENTER);
        rightBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == State.MAKE_MOVE)
                    handler.passCoordinates(e.getX(), e.getY(),state,scale);
                repaint();
            }
        });

        leftBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == State.BUILD_HORIZONTAL_SHIP || state == State.BUILD_VERTICAL_SHIP) {
                    handler.passCoordinates(e.getX(), e.getY(), state,scale);
                    repaint();
                    state = State.CHOOSE_ORIENT;
                    handler.passState(State.CHOOSE_ORIENT);
                }
            }
        });

        leftBoard.addDrawable(new ShipBoard());
        rightBoard.addDrawable(new ShipBoard());

        setJMenuBar(createMenuBar());
        cp.add(messagePanel, BorderLayout.NORTH);
        chooseGameMode();
        cp.add(leftBoard, BorderLayout.WEST);
        cp.add(rightBoard, BorderLayout.EAST);
        setResizable(false);
        setTitle("Sea Battle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //stop execute code when user close frame window; without this app would be still running;
        pack(); //organize panels, sizes
        setVisible(true);
    }

    public void setHandler(PlayerAction p) {
        handler = p;
        revalidate();
    }

    public void updateMessage(String s, String s1) {
        if (!s.equals("")) message.setText(s);
        if (!s1.equals("")) message1.setText(s1);
        revalidate();
    }

    public void updateState(State s) {
        state = s;
        revalidate();
    }

    public class MyPanel extends JPanel {
        public Bag<Drawable> objectsForDraw = new Bag<>();

        public MyPanel() {
            setBorder(BorderFactory.createLineBorder(Color.black));
            setBorder(BorderFactory.createStrokeBorder(new BasicStroke(5.0f)));
            setBackground(Color.WHITE);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            scale=getPreferredSize().height/10;
            for (Drawable d : objectsForDraw) {
                d.draw(g,scale);
            }
        }

        public void addDrawable(Drawable d) {
            objectsForDraw.add(d);
        }

        public void eraseAll() {
            objectsForDraw = new Bag<>();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }
    }

    public void refresh() {
        leftBoard.eraseAll();
        leftBoard.addDrawable(new ShipBoard());
        rightBoard.eraseAll();
        rightBoard.addDrawable(new ShipBoard());
        chooseGameMode();
        repaint();
        revalidate();
    }

    public void drawOnLeft(Drawable object) {
        leftBoard.addDrawable(object);
        revalidate();
    }

    public void drawOnRight(Drawable object) {
        rightBoard.addDrawable(object);
        revalidate();
    }

    public void chooseGameMode() {
        messagePanel.removeAll();
        JButton autoBuildButton = new JButton("Auto");
        autoBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.passState(State.AUTO_BUILD_SHIPS);
                repaint();
                startShooting();
                revalidate();
            }
        });

        JButton manuallyBuildButton = new JButton("Manually");
        manuallyBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.passState(State.CHOOSE_ORIENT);
                revalidate();
                repaint();
            }
        });
        buttonPanel = new JPanel();
        buttonPanel.add(autoBuildButton);
        buttonPanel.add(manuallyBuildButton);

        message.setText("Welcome to SeaBattle");
        message1.setText("Choose method for ship building");
        messagePanel.add(buttonPanel, BorderLayout.SOUTH);
        messagePanel.add(message, BorderLayout.NORTH);
        messagePanel.add(message1, BorderLayout.CENTER);
    }

    public void chooseOrientation() {
        state = State.DO_NOTHING;
        message1.setText("Choose orientation of ship");
        buttonPanel.removeAll();
        JButton horizontal = new JButton("Horizontal");
        horizontal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                message1.setText("Build horizontal ship");
                buttonPanel.removeAll();
                state = State.BUILD_HORIZONTAL_SHIP;
            }
        });
        JButton vertical = new JButton("Vertical");
        vertical.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                message1.setText("Build vertical ship");
                buttonPanel.removeAll();
                state = State.BUILD_VERTICAL_SHIP;
            }
        });
        buttonPanel.add(horizontal);
        buttonPanel.add(vertical);
    }

    public void startShooting() {
        messagePanel.remove(buttonPanel);
        message.setText("Done!");
        message1.setText("Player, make a shot");
        state = State.MAKE_MOVE;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem newGame = new JMenuItem("New game");

        newGame.addActionListener(new ActionListener() { //anonymous inner class to implement methos specific to this source
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.passState(State.NEW_GAME);
                refresh();
            }
        });

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); //close JFrame
            }
        });
        menu.add(newGame);
        menu.add(exit);
        return menuBar;
    }

    public static void main(String[] args) {
        //To run the constructor on the event-dispatching thread,
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StartWindow();  // Let the constructor do the job
            }
        });
    }
}
