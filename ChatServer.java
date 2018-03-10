import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

//TODO Fix sending issue, Inetaddress does not work locally
    //Find better way to store clients
/*Use Hash Table, list of hash tables?
*Flag system client side to detect whats desired
*    TODO Add client list for groups
*    TODO Add individual client to client function
*/
public class ChatServer extends Thread {
    List<SocketAddress> clients = new ArrayList<SocketAddress>();
    int alias_length;

    final private int port = 5000;
    private DatagramSocket ds;
    private DatagramPacket dp;
    private byte[] buf = new byte[1024];

    public void run(){
        handleShutdown();
        try{
            ds = new DatagramSocket(port);
            dp = new DatagramPacket(buf, 1024);
            System.out.println("Server started on "
                            +ds.getLocalSocketAddress());

            while(true){
                ds.receive(dp);
                if(!clients.contains(dp.getSocketAddress())){
                    clients.add(dp.getSocketAddress());
                }
                String raw_str = new String(dp.getData(),0,dp.getLength());
                String str = raw_str.substring(0,raw_str.length()-1);
                int alias_length = Integer.valueOf(
                                   raw_str.substring(raw_str.length()-1));
                String alias = raw_str.substring(0,alias_length);
                System.out.println(dp.getAddress()+"::"+str);
                
                for(SocketAddress client:clients){
                    if(client != dp.getSocketAddress()){
                        DatagramPacket distroP = new DatagramPacket(
                                                                 str.getBytes(),
                                                                 str.length(),
                                                                 client, port);
                     ds.send(dp);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void handleShutdown(){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Shutting down...");
                if(!ds.isClosed()){
                    ds.close();
                }
            }
        }, "Shutdown-thread"));
    }

    public static void main(String[] args){
        Thread t = new ChatServer();
        t.start();
    }
}
