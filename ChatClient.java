import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

//TODO Fix sizing issue
//TODO Implement autoscroll
//TODO Russian text returns "?" for other clients, possible encoding issue
//TODO Figure out communication with python server or build new one
//TODO Encrypt UDP Packets using DTLS(OpenSSL?)
//TODO Set up a better looking top panel
//TODO Add list of people logged in for group chats
//TODO Add document sending function        Use these functions
//TODO Add picture sending function         by uploading and downloading
//TODO Add voice call function              from chat server to avoid port situation

public class ChatClient extends JFrame{
    final JButton logoutButton, sendButton;
    final JLabel peer_name;
    final JScrollPane msg_area_scroll;
    final JTextArea msg_area, msg_box;

    private String alias;
    private String ip;
    private InetAddress externalIP;
    private InetAddress serverIP;
    private int port;

    private Socket tcp_socket; //Currently does nothing
    private DatagramSocket udp_socket;
    
    //Temporary till i figure out direct connection to one person
    //and not everyone on the server
    private String peer = "test person";

    public ChatClient(String alias, String ip, int port){
        this.alias = alias;
        this.ip = ip;
        this.port = port;
        try{
            //Check if running server on same network and set ip based on this
            getExternalIP();
            serverIP = InetAddress.getByName("g1pro757.ddns.net");
            if(externalIP.equals(serverIP)){
                //Using vm-computer name so i can test on other computers
                //on the local network without having to recompile
                //serverIP = InetAddress.getByName("127.0.0.1");
                serverIP = InetAddress.getByName("kali");
            }
            //Start a connection via the UDP Socket
            connect();
        }catch(Exception e){
            System.out.println("Error: Cannot connect to server");
            System.out.println(e);
            System.exit(0);
        }
        
        //Initialize all JComponents with text and listeners
        peer_name = new JLabel(peer);
        logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(udp_socket.isConnected()){
                    udp_socket.disconnect();
                    setVisible(false);
                    new Login();
                }
                else{
                    setVisible(false);
                    new Login();
                }
            }
        });
        msg_area = new JTextArea(20,40);
        msg_area.setEditable(false);
        msg_area.setFocusable(false);
        msg_area.setLineWrap(true);
        msg_area_scroll = new JScrollPane(msg_area);
        msg_area_scroll.setVerticalScrollBarPolicy(
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        msg_area_scroll.setHorizontalScrollBarPolicy(
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        msg_area_scroll.setFocusable(false);
        msg_box = new JTextArea(3,33);
        msg_box.setLineWrap(true);
        msg_box.requestFocusInWindow();
        msg_box.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    send();
                    e.consume();
                }
            }
        });
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                send();
            }
        });
        createWindow();
    }

    //Sets layouts, adds components to panels and packs the app.
    //Also does a setup of the window itself with title, size, etc
    private void createWindow(){
        JPanel top_panel = new JPanel();
        JPanel msg_area_panel = new JPanel();
        JPanel userInput_panel = new JPanel();
        
        top_panel.add(peer_name);
        top_panel.add(logoutButton);
        top_panel.setLayout(new FlowLayout());
        
        //Area for received messages
        msg_area_panel.add(msg_area_scroll);

        //User Input area
        userInput_panel.add(msg_box);
        userInput_panel.add(sendButton);
        userInput_panel.setLayout(new FlowLayout());

        add(top_panel);
        add(msg_area_panel);
        add(userInput_panel);
        pack();

        setTitle("Chat Client v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500,400);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setVisible(false);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                if(udp_socket.isConnected()){
                    udp_socket.disconnect();
                }
                System.exit(0);
            }
       });
    }
   
    //Handles sending of message to server/other clients
    private void send(){
        String raw_msg = msg_box.getText();
        String msg_to_send = alias+": "+raw_msg;
        //Checks if there is a message to send and not just empty spaces
        //if(!raw_msg.isEmpty() && !raw_msg.startsWith(" ")){
        if(raw_msg.trim().length() > 0){
            DatagramPacket dp = new DatagramPacket(msg_to_send.getBytes(),
                                                   msg_to_send.length(),
                                                   serverIP, port);
            try{
                udp_socket.send(dp); 
            }catch(Exception e){
            }
            msg_area.append(msg_to_send+'\n');
            msg_box.setText("");
        }
        else{
            msg_box.setText("");
        }   
    }
    
    //Handles receiving of messages from the server/other clients
    Thread rcv_msg = new Thread(new Runnable(){
        public void run(){
            while(true){
                try{
                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, 1024);
                    udp_socket.receive(dp);
                    String msg = new String(dp.getData(), 0, dp.getLength());
                    msg_area.append(msg+'\n');
                }catch(IOException e){
                    System.out.println("Error receiving packet: "+e);
                }
                catch(Exception e){
                    System.out.println("Real errors Real Shit");
                }
            }
        }
    },"rcv_msg_thread");
    
    //Initializes socket and starts thread to receive messages
    private void connect(){
        try{
            udp_socket = new DatagramSocket();
            rcv_msg.setDaemon(true);
            rcv_msg.start();
        }catch(SocketException e){
            System.out.println("Cannot establish UDP connection");
            System.out.println(e);
            System.exit(0);
        }
       /* try{
            String t = "hello";
            DatagramPacket testPacket = new DatagramPacket(
                                        t.getBytes(),t.length(),
                                        serverIP, port);
            udp_socket.send(testPacket);
        }
        catch(IOException e){
        }*/
    }
    
    //Finds external ip of computer in order to figure out whether being used
    //internal to the network or external to the network. This is mostly for 
    //the comfort of the developer
    public void getExternalIP(){
        try{
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                                                    whatismyip.openStream()));
            externalIP = InetAddress.getByName(in.readLine());
        }catch(Exception e){
            System.out.println("getExternalIP Error: "+e);
        }
    }

    public static void main(String[] args){
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                //ChatClient chat = new ChatClient();
            }
        });
    }
}
