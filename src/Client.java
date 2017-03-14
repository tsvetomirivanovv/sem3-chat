import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {
    public DataInputStream inputStream;
    public DataOutputStream outputStream;
    private Socket socket = null;
    private Scanner console = new Scanner(System.in);
    private String username,ip,write,read;
    private int port;
    

    public Client(String username, String ip, int port){
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    public void run(){ // RUN THREAD
        startRunning();
    }

    public void startRunning(){ // MAIN METHOD IN THREAD
        try {
            while (true){
                connectToServer(); // CONNECT TO SERVER
                setupStreams(); // SETUP STREAMS
                writingThread.start(); // THREAD FOR USER INPUT IN THE CONSOEL
                alive.start(); // THREAD FOR SENDING ALVIE MESSAGE
                whileConnected(); // LISTENING FOR MESSAGES
            }
        }catch (Exception e){
        }
    }


    public void connectToServer()throws IOException{ // CONNECT TO THE SERVER
        socket = new Socket(InetAddress.getByName(ip),port);
        System.out.println("You are connected to Server: " + ip + " with Port number: " + port);
    }


    public void setupStreams()throws IOException{ // SETUP STREAMS AND J_OK AND J_ERR RESPONSE
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(socket.getInputStream());

        String join = "JOIN " + username +", " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        outputStream.writeUTF(join);
        outputStream.flush();
        String serverResponse = inputStream.readUTF();
        if (serverResponse.equalsIgnoreCase("J_OK")) {
            System.out.println(serverResponse);
        }
        else if (serverResponse.equalsIgnoreCase("J_ERR")){
            System.out.println("Possible duplicate name! Please try again with a new name");
            closeClient("QUIT");
        }
    }


    public void whileConnected() throws IOException{ // LISTEN FOR DATA AND DISPLAY IT
        do {
            try {
                read=inputStream.readUTF();
                System.out.println(read);
            }catch (IOException e){
            }
        }while (!read.equals("J_ERR") );
    }


    public void closeClient(String trigger) throws IOException { // METHOD FOR DISCONNECTING
        if (trigger.equalsIgnoreCase("QUIT")){
            System.out.println("You disconnected from the chat!");
            sendMessage("QUIT");
            outputStream.close();
            inputStream.close();
            socket.close();
            System.exit(1);
        }
    }


    Thread writingThread = new Thread(new Runnable() { // THREAD FOR USER INPUT
        @Override
        public void run() {
            while (true) {
                try {
                        write = console.nextLine();
                        String msg = ("DATA " + username + ": " + write);
                        closeClient(write);
                    if (write.length()<=250) {
                        outputStream.writeUTF(msg);
                    } else if (write.length()>250){
                        System.out.println("Message longer than 250 characters " + "\n try with a shorter message");
                        sendMessage(write);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    Thread alive = new Thread(new Runnable() { // THREAD TO SEND ALVE MESSAGE EVERY 60 SECONDS
        @Override
        public void run() {
            while (true) {
                try {
                    sendMessage("ALVE");
                    Thread.currentThread().sleep(6*10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    public void sendMessage(String message) { // SEND MESSAGE METHOD
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
