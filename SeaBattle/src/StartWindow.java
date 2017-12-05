import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

public class StartWindow extends JFrame{ //StartWindow is a top-level container
    Container cp;
    JPanel helpPanel;

    public StartWindow(){
        cp=getContentPane(); //top-level container
        cp.setLayout(new BorderLayout()); //default arrange components
        helpPanel=new JPanel();
        helpPanel.setPreferredSize(new Dimension(800,100));
        helpPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        setTitle("Sea Battle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //stop execute code when user close frame window; without this app would be still running;
        setJMenuBar(createMenuBar());
        cp.add(helpPanel,BorderLayout.NORTH);
        cp.add(new LeftPanel(),BorderLayout.WEST);
        cp.add(new LeftPanel(),BorderLayout.EAST);
        pack();
        setVisible(true);
    }

    public class LeftPanel extends JPanel{

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawString("This is left Panel!",10,20);
            setBorder(BorderFactory.createLineBorder(Color.black));
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
