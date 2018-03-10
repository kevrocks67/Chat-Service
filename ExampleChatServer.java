import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ChatServer extends Thread{
    final InetAddress host = InetAddress.getByName("127.0.0.1");
    final int backlog = 2;
    final int port = 5000;
    private ServerSocket sSocket;

    public ChatServer() throws IOException{
        sSocket = new ServerSocket(port, backlog, host);
        //sSocket.setSoTimeout(10000);
    }

    public void run(){
        while(true) {
            try{
                System.out.println("Waiting for client on port "+port+
                                    "...");
                Socket server = sSocket.accept();

                System.out.println("Connected to "+host);
                DataInputStream in = new DataInputStream(server
                                         .getInputStream());
                System.out.println(in.readUTF());
                DataOutputStream out = new DataOutputStream(server
                                            .getOutputStream());
                out.writeUTF("Thank you for connecting to "+host
                            +"\nGoodbye!");
                server.close();
            }
            catch (SocketTimeoutException e){
                System.out.println("Socket timed out!");
                break;
            }
            catch (IOException e){
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args){
        try{
            Thread t = new ChatServer();
            t.start();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
