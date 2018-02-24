import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Login extends JFrame{
    final JButton connectButton;
    final JTextField usernameField, portField;

    private String alias;
    private String ip;
    private int port;

    public Login(){
        usernameField = new HintTextField("Username");
        portField = new HintTextField("Port Number");
        portField.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    connect();
                }
            }
        });

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                connect();
            }
        });

        createWindow();
    }

    public void createWindow(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.add(usernameField);
        panel.add(portField);
        panel.add(connectButton);
        add(panel);
        pack();

        setTitle("Chat Client Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,300);
        setLayout(null);
        setFocusable(true);
        setVisible(true);
    }

    public void connect(){
        //Check if username etnered, check if port valid
        //Get ip
        //Send data to ChatClient constructor
        alias = usernameField.getText();
        port = Integer.valueOf(portField.getText());
        System.out.println(alias+'@'+ip+':'+port);
        ChatClient c = new ChatClient(alias, ip, port);
        c.setVisible(true);
        setVisible(false);
    }

    public static void main(String[] args){
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                Login l = new Login();
            }
        });
    }
}
