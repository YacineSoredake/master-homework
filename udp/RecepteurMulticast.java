package udp;
import java.net.*;
import java.io.*;

public class RecepteurMulticast {
    public static void main(String[] args) {
        String multicastAddress = "224.0.0.1"; // Adresse de multidiffusion
        int port = 1234;

        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress group = InetAddress.getByName(multicastAddress);
            NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

            // Nouvelle méthode pour rejoindre un groupe multicast
            socket.joinGroup(new InetSocketAddress(group, port), netIf);

            System.out.println("Récepteur en attente de messages multicast...");

            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Message reçu : " + message);
            }
        } catch (Exception e) {
            System.out.println("Problème à la réception : " + e.getMessage());
        }
    }
}
