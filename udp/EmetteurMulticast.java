package udp;
import java.net.*;
import java.io.*;

public class EmetteurMulticast {
    public static void main(String[] args) {
        String multicastAddress = "224.0.0.1"; // Adresse de multidiffusion
        int port = 1234;

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            System.out.println(group);
            BufferedReader entree = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("Message à envoyer : ");
                String message = entree.readLine();
                byte[] buffer = message.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                socket.send(packet);

                System.out.println("Message envoyé au groupe multicast.");
            }
        } catch (Exception e) {
            System.out.println("Problème à l'envoi : " + e.getMessage());
        }
    }
}
