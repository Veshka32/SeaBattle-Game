import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChooseGameMode extends JFrame implements ActionListener{
    JPanel panel;
    JLabel label;
    JButton autoBuildButton,manuallyBuildButton;

    public ChooseGameMode(){
        setSize(300,150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //stop execute code when user close frame window; without this app would be still running;
        setVisible(true);

        panel=new JPanel();
        panel.setBounds(10,10,50,20); //xy-left top corner
        panel.setLayout(null);

        label=new JLabel("Choose method for ship building");
        label.setBounds(10,20,400,20); // same as panel

        autoBuildButton=new JButton("Auto");
        autoBuildButton.setBounds(10,50,100,30);
        autoBuildButton.addActionListener(this); //so now object of MainScreen must implement ActionListener Interface (override method actionPerformed)
        autoBuildButton.setActionCommand("auto");

        manuallyBuildButton=new JButton("Manually");
        manuallyBuildButton.setBounds(120,50,100,30);
        manuallyBuildButton.addActionListener(this);
        manuallyBuildButton.setActionCommand("manually");

        panel.add(label);
        panel.add(autoBuildButton);
        panel.add(manuallyBuildButton);
        add(panel); //add panel to JFrame
    }

    public void actionPerformed(ActionEvent e) {

        if ("auto".equals(e.getActionCommand())){
            System.out.println("auto");
        } else {
            System.out.println("manually");
        }
        this.setVisible(false);
    }

    public static void main(String[] args) {
        //create new thread - for what?
        SwingUtilities.invokeLater(() -> new ChooseGameMode());
    }
}
