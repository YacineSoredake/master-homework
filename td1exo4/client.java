package td1exo4;
import java.io.*;
import java.net.*;
import java.util.Arrays;
class client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            int[] array = {5, 3, 8, 1, 2};
            out.writeObject(array);
            System.out.println("Tableau envoyé.");

            int[] sortedArray = (int[]) in.readObject();
            System.out.println("Tableau trié reçu : " + Arrays.toString(sortedArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}