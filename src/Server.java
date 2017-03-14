import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server extends Thread {
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket socket = null;
    private Scanner console = new Scanner(System.in);
    public String username,message,realUsername,serverResponse;
    public ServerMain serverMain;


    public Server(Socket socket) throws SocketException {
        this.socket = socket;
        socket.setSoTimeout(63000); // SET FOR HOW MUCH TIME THE SOCKET WILL LISTEN FOR DATA!
    }


    public void run(){ // RUN THREAD
        startRunning();
    }


    public void startRunning(){ // MAIN METHOD IN THREAD
        try {
            while (true){
                setupStreams(); // SETUP THE SOCKET STREAMS
                whileConnected(); // WHILE THE SOCKET IS CONNECTED
            }
        }catch (IOException e){
        }
    }


    public void setupStreams()throws IOException{
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(socket.getInputStream());
        String joinRead = inputStream.readUTF();

        if (joinRead.startsWith("JOIN")) { // IF THE MESSAGE STARTS WITH JOIN
            try {
                String[] usernameSplit = joinRead.split(","); // SPLIT JOIN STRING
                String username = usernameSplit[0]; // GET ONLY THE USERNAME
                String part1 = usernameSplit[1]; // GET THE ONLY THE IP AND PORT
                realUsername = username.replace("JOIN", "");

                if (checkUsernameValid(realUsername) == false | serverMain.clients.containsKey(realUsername)) { // CHECK IF ITS VALID NAME OR ALREADY EXIST
                    outputStream.writeUTF("J_ERR");
                    disconnectAndUpdate();
                }

                else if (checkUsernameValid(realUsername) == true & !serverMain.clients.containsKey(realUsername)) { // IF VALID AND NOT EXISING SEND JOK
                    outputStream.writeUTF("J_OK");
                    System.out.println("\nJOIN" + realUsername + "," +part1);
                    serverMain.clients.put(realUsername, socket);
                    broadcastMessage(list());
                    System.out.println(list());
                }
            } catch (IOException e) {
            }
        } else if (!joinRead.equals("JOIN")){ // IF USER SENDS OTHER THAN JOIN CLOSE CONNECTION
            outputStream.writeUTF("UNKNOWN PROTOCOL MESSAGE");
            disconnectAndUpdate();
        }
    }


    public void whileConnected()throws IOException{
        do {
            try {
                message = inputStream.readUTF();

                if (message.equalsIgnoreCase("ALVE")){
                    System.out.println("ALVE  I am " + realUsername);
                }

                if (message.startsWith("DATA ") & message.length()<=250){
                    System.out.println(message); // DISPLAY MESSAGE IN SERVER
                    broadcastMessage(message); // SEND MESSAGE TO ALL CLIENTS
                }

                if (message.length()>250){
                    sendMessage("J_ERR");
                    disconnectAndUpdate();
                }

                if (message.equalsIgnoreCase("QUIT")){ // REMOVE CLIENT IF TYPES QUIT
                    System.out.println(realUsername + " disconnected from the server with QUIT command!");
                    disconnectAndUpdate();
                    break;
                }


            } catch (SocketTimeoutException p){ // REMOVE CLIENT IF STOP RESPONDING
                System.out.println("ALVE timeout - " + realUsername);
                disconnectAndUpdate();
                break;
            }
            catch (IOException e){ // REMOVE CLIENT
                System.out.println(realUsername+ " disconnected from the server!");
                disconnectAndUpdate();
                break;
            }
        }while (!message.equals("QUIT"));
    }


    public void sendMessage(String message){ // SEND A MESSAGE
        try {
            outputStream.writeUTF(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeClient() throws IOException { // CLOSE CONNECTION
        outputStream.close();
        inputStream.close();
        socket.close();
    }


    public void broadcastMessage(String message)throws IOException{ // SEND TO ALL USERS
      for (int i = 0; i<=serverMain.serverArrayList.size()-1;i++){
          serverMain.serverArrayList.get(i).sendMessage(message);
      }
    }


    public  boolean checkUsernameValid(String s){ // CHECK IF THE USERNAME IS VALID
        String hello = s;
        Pattern p = Pattern.compile("[^a-zA-Z_0-9-_\\s]");
        Matcher m = p.matcher(hello);
        boolean b = m.find();
        if (b==true | hello.length()>12){
        } else if (b==false & hello.length()<=12) {
            return true;
        }
        return  false;
    }

    public void disconnectAndUpdate() throws IOException {
        serverMain.clients.remove(realUsername,socket);
        serverMain.serverArrayList.remove(this);
        closeClient();
        broadcastMessage(list());
        System.out.println(list());
    }

    public String list(){
        String list = "LIST" + serverMain.clients.keySet().toString().replaceAll("[\\[\\],]","");
        return list;
    }

}
