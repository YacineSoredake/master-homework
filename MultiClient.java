import java.io.*;
import java.net.*;

public class MultiClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connexion au serveur...");

            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Serveur: " + response);
                if (response.equals("Fin de la section critique.")) {
                    break;
                }
            }

            System.out.println("Déconnexion du serveur.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
