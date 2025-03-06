package tpuno;
import java.io.*;
import java.net.*;

public class ClientThread {  
    private static final String HOST = "localhost";
    private static final int PORT = 3000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connecté au serveur. En attente d'accès à la section critique...");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Serveur message : " + message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
