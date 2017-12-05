import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StartWindow extends JFrame{ //StartWindow is a top-level container
    Container cp;
    JPanel helpPanel;

    Ship currentShip=new Ship(true,1,1,1);
    Graphics gg;


    public StartWindow(){
        cp=getContentPane(); //top-level container
        cp.setLayout(new BorderLayout()); //default arrange components
        helpPanel=new JPanel();
        helpPanel.setPreferredSize(new Dimension(1000,100));
        helpPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        setTitle("Sea Battle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //stop execute code when user close frame window; without this app would be still running;
        setJMenuBar(createMenuBar());
        cp.add(helpPanel,BorderLayout.NORTH);
        cp.add(new MyPanel(),BorderLayout.WEST);
        pack();
        setVisible(true);
    }

    public class MyPanel extends JPanel{
        int newX;
        int newY;

        public MyPanel(){
            setBorder(BorderFactory.createLineBorder(Color.black));
            setBackground(Color.WHITE);
            setBounds(10,10,300,300);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    newX=e.getX();
                    newY=e.getY();
                    repaint(newX,newY,50,50);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawOval(newX,newY,50,50);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400,400);
        }


    }



    public JPanel chooseGameMode(){
        JPanel panel=new JPanel();
        panel.setLayout(new FlowLayout());
        JLabel label=new JLabel("Choose method for ship building",JLabel.CENTER);
        JButton autoBuildButton=new JButton("Auto");
        autoBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cp.remove(panel);
                revalidate();
                repaint();
            }
        });
        JButton manuallyBuildButton=new JButton("Manually");
        manuallyBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cp.remove(panel);
                cp.add(chooseOrientation());
                revalidate();
                repaint();
            }
        });
        panel.add(label);
        panel.add(autoBuildButton);
        panel.add(manuallyBuildButton);
        return panel;
    }

    private JPanel chooseOrientation(){
        JPanel panel=new JPanel();
        panel.setLayout(new FlowLayout());
        JLabel label=new JLabel("Choose orientation of ship",JLabel.CENTER);
        JButton horizontal=new JButton("Horizontal");
        horizontal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cp.remove(panel);
                revalidate();
                repaint();
            }
        });
        JButton vertical=new JButton("Vertical");
        vertical.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cp.remove(panel);
                revalidate();
                repaint();
            }
        });
        panel.add(label);
        panel.add(horizontal);
        panel.add(vertical);
        return panel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem newGame = new JMenuItem("New game");
        JMenuItem exit=new JMenuItem("Exit");
        newGame.addActionListener(new ActionListener() { //anonymous inner class to implement methos specific to this source
            @Override
            public void actionPerformed(ActionEvent e) {
                cp.add(chooseGameMode()); //add panel to JFrame to the north
                revalidate();
            }
        });
        newGame.setActionCommand("New game");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); //close JFrame
            }
        });
        exit.setActionCommand("Exit");
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
