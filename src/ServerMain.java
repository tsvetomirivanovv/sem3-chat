import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerMain {
    public static Scanner console = new Scanner(System.in);
    public static Map<String,Socket> clients = new HashMap<>();
    public static List<DataOutputStream> broadcastArray = new ArrayList<>();
    public static ArrayList<Server> serverArrayList = new ArrayList<>();

    public static void main(String[] args) throws Exception{
        System.out.println("Your server IP address is: " + getServerIP());
        System.out.println("\nType your server port");
        int port = console.nextInt();
        System.out.println("\nYour Server with port: " + port + " + IP address: " + getServerIP() + "/ has started!"  );

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while (true){
                Server c  = new Server(serverSocket.accept());
                serverArrayList.add(c);
                c.start();
            }
        }
    }


    public static InetAddress getServerIP() throws Exception {
        InetAddress ipAdr = null;
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
            if (netint.getName().equalsIgnoreCase("wlan1")) {
                InetAddress in = netint.getInetAddresses().nextElement();
                ipAdr = in;
            }
        return ipAdr;
    }


   public static Thread serverScanner = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                String serverInput = console.nextLine();

                if (serverInput.equalsIgnoreCase("list")) {
                    System.out.println(clients.keySet());
                    System.out.println(serverArrayList.toString());
                }
                if (serverInput.equalsIgnoreCase("listfull")){
                    System.out.println(clients);
                }
            }

        }
    });

}
