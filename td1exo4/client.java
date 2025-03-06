package td1exo4;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class client {
    private static final int PORT = 12345;
    private static final String HOST = "localhost"; 
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            int[] tab = {5, 3, 8, 1, 2, 9, 4, 7, 6};
            out.writeObject(tab);
            System.out.println("Tableau envoyé au serveur" + Arrays.toString(tab));

            int[] tabTrie = (int[]) in.readObject();
            System.out.println("Tableau trié reçu du serveur : " + Arrays.toString(tabTrie));

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
