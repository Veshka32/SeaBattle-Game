import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;

import static javax.swing.JOptionPane.showMessageDialog;

public class GameWindow extends JFrame { //GameWindow is a top-level container
    private Container cp;
    private JPanel messagePanel = new JPanel(new BorderLayout());
    private JPanel buttonPanel = new JPanel();
    private JLabel message = new JLabel("Welcome to SeaBattle");
    private JLabel message1 = new JLabel("Message 1");
    private MyPanel leftBoard = new MyPanel();
    private MyPanel rightBoard = new MyPanel();
    private State state;
    private PlayerAction handler;

    public GameWindow() {
        cp = getContentPane(); //top-level container
        cp.setLayout(new BorderLayout()); //default arrange components
        //set messagePanel
        messagePanel.setPreferredSize(new Dimension(GameConstant.DRAW_FIELD_DIMENSION*2+GameConstant.CELL_SIZE, 100));
        messagePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        rightBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == State.MAKE_MOVE)
                    handler.onMouseClicked(e.getX(), e.getY(),state);
                repaint();
            }
        });

        leftBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == State.BUILD_HORIZONTAL_SHIP || state == State.BUILD_VERTICAL_SHIP) {
                    handler.onMouseClicked(e.getX(), e.getY(), state);
                    repaint();
                    state = State.CHOOSE_ORIENT;
                    handler.onPassState(State.CHOOSE_ORIENT);
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

    public void showException(){
        showMessageDialog(this, "You may not place ship here");
    }

    public void updateState(State s) {
        state = s;
        revalidate();
    }

    public class MyPanel extends JPanel {
        public ArrayDeque<Drawable> objectsForDraw = new ArrayDeque<>();

        public MyPanel() {
            setBorder(BorderFactory.createStrokeBorder(new BasicStroke(5.0f)));
            setBackground(Color.WHITE);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Drawable d : objectsForDraw) {
                d.draw(g);
            }
        }

        public void addDrawable(Drawable d) {
            objectsForDraw.add(d);
        }

        public void eraseAll() {
            objectsForDraw.clear();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(GameConstant.DRAW_FIELD_DIMENSION, GameConstant.DRAW_FIELD_DIMENSION);
        }
    }

    private void refresh() {
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

    private void chooseGameMode() {
        messagePanel.removeAll();
        JButton autoBuildButton = new JButton("Auto");
        autoBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.onPassState(State.AUTO_BUILD_SHIPS);
                repaint();
                startShooting();
                revalidate();
            }
        });

        JButton manuallyBuildButton = new JButton("Manually");
        manuallyBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.onPassState(State.CHOOSE_ORIENT);
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
        Object[] options = {"Horizontal",
                "Vertical"};
        int n = JOptionPane.showOptionDialog(this,
                "Choose orientation of ship",
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,     //do not use a custom Icon
                options,  //the titles of buttons
                options[0]); //default button title
        if (n == 0) {
            message1.setText("Build horizontal ship");
            state = State.BUILD_HORIZONTAL_SHIP;
        } else {message1.setText("Build vertical ship");
            state = State.BUILD_VERTICAL_SHIP;};
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
                handler.onPassState(State.NEW_GAME);
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
}
