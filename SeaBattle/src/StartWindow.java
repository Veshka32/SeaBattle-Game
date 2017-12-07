import edu.princeton.cs.algs4.Bag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StartWindow extends JFrame { //StartWindow is a top-level container
    Container cp;
    JPanel helpPanel=helpPanel = new JPanel();
    JLabel message = new JLabel();
    MyPanel leftBoard = new MyPanel();
    MyPanel rightBoard = new MyPanel();
    State state;
    PlayerAction handler;

    public StartWindow() {
        cp = getContentPane(); //top-level container
        cp.setLayout(new BorderLayout()); //default arrange components
        //set helpPanel
        helpPanel.setPreferredSize(new Dimension(1050, 100));
        helpPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        helpPanel.add(message,BorderLayout.WEST);
        rightBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state==State.MAKE_MOVE){
                    handler.passCoordinates(e.getX(),e.getY(),State.MAKE_MOVE);
                }
                repaint();
            }
        });
        chooseGameMode(); //add panel to JFrame to the north
        leftBoard.addDrawable(new ShipBoard());
        rightBoard.addDrawable(new ShipBoard());

        setJMenuBar(createMenuBar());
        cp.add(helpPanel, BorderLayout.NORTH);
        cp.add(leftBoard, BorderLayout.WEST);
        cp.add(rightBoard, BorderLayout.EAST);
        setResizable(false);
        setTitle("Sea Battle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //stop execute code when user close frame window; without this app would be still running;
        pack();
        setVisible(true);
    }

    public void setHandler(PlayerAction p) {
        handler = p;
        revalidate();
    }

    public void updateMessage(String s) {
        message.setText(s);
        revalidate();
    }

    public void updateState(State s){
        state=s;
        revalidate();
    }

    public class MyPanel extends JPanel {
        public Bag<Drawable> objectsForDraw=new Bag<>();

        public MyPanel() {
            setBorder(BorderFactory.createLineBorder(Color.black));
            setBackground(Color.WHITE);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Drawable d:objectsForDraw){
                d.draw(g);
            }
        }

        public void addDrawable(Drawable d){
            objectsForDraw.add(d);
        }

        public void eraseAll(){
            objectsForDraw=new Bag<>();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }
    }

    public void refresh(){
        leftBoard.eraseAll();
        leftBoard.addDrawable(new ShipBoard());
        rightBoard.eraseAll();
        rightBoard.addDrawable(new ShipBoard());
        helpPanel.removeAll();
        chooseGameMode();
        repaint();
        revalidate();
    }

    public void drawOnLeft(Drawable object){
        leftBoard.addDrawable(object);
        revalidate();
    }

    public void drawOnRight(Drawable object){
        rightBoard.addDrawable(object);
        revalidate();
    }

    public void chooseGameMode() {
        JLabel label = new JLabel("Choose method for ship building", JLabel.CENTER);
        JButton autoBuildButton = new JButton("Auto");
        autoBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.passAction(State.AUTO_BUILD_SHIPS);
                repaint();
                startShooting();
                revalidate();
            }
        });

        JButton manuallyBuildButton = new JButton("Manually");
        manuallyBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpPanel.removeAll();
                chooseOrientation();
                revalidate();
                repaint();
            }
        });
        helpPanel.add(label);
        helpPanel.add(autoBuildButton);
        helpPanel.add(manuallyBuildButton);
    }

    private void chooseOrientation() {
        JLabel label = new JLabel("Choose orientation of ship", JLabel.CENTER);
        JButton horizontal = new JButton("Horizontal");
        horizontal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.passAction(State.BUILD_SHIP);
                revalidate();
                repaint();
            }
        });
        JButton vertical = new JButton("Vertical");
        vertical.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.passAction(State.BUILD_SHIP);
                revalidate();
                repaint();
            }
        });
        helpPanel.add(label);
        helpPanel.add(horizontal);
        helpPanel.add(vertical);
    }

    public void startShooting(){
        helpPanel.removeAll();
        message.setText("Player, make a shot!");
        helpPanel.add(message);
        state=State.MAKE_MOVE;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem newGame = new JMenuItem("New game");

        newGame.addActionListener(new ActionListener() { //anonymous inner class to implement methos specific to this source
            @Override
            public void actionPerformed(ActionEvent e) {
                handler.passAction(State.NEW_GAME);
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
