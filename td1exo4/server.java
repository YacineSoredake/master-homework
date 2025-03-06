package td1exo4;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class server {
    private static final int PORT = 12345;
    
        public static void main(String[] args) {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Serveur en écoute sur le port " + PORT);
                while (true) {
                    Socket s = serverSocket.accept();   
                    ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
    
                    int [] tab = (int [])in.readObject();

                Arrays.sort(tab);  

                out.writeObject(tab);
                System.out.println("tableau trié envoyé au client");
                s.close();
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}