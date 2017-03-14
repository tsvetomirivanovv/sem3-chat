import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientMain {

    public static void main(String[] args) {
        Client client;
        Scanner console = new Scanner(System.in);
        System.out.println("\nType your username: ");
        String username = console.nextLine();


        System.out.println("\n Type the IP of the server you wish to connect!");
        String ip = console.nextLine();

        System.out.println("\n Type the Port of the server you wish to connect!");
        int port = console.nextInt();

        client = new Client(username, ip, port);
        client.start();

    }
}
