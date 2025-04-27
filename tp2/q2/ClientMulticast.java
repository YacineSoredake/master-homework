package tp2.q2;

import java.net.*;
import java.util.Scanner;

public class ClientMulticast {
    public static void main(String[] args) throws Exception {
        InetAddress serveur = InetAddress.getByName("localhost");
        int portServeur = 1234;
        InetAddress groupe = InetAddress.getByName("230.0.0.0");
        int portMulticast = 4446;
        DatagramSocket socketEnvoi = new DatagramSocket();
        Thread ecouteThread = new Thread(() -> {
            try {
                MulticastSocket socketMulticast = new MulticastSocket(portMulticast);
                NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                socketMulticast.joinGroup(new InetSocketAddress(groupe, portMulticast), netIf);
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socketMulticast.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("message pour tout" + msg);
                    if (msg.startsWith("Gagné")) break;
                }
                socketMulticast.leaveGroup(new InetSocketAddress(groupe, portMulticast), netIf);
                socketMulticast.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ecouteThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Entrez un nombre (1-100) : ");
            String guess = scanner.nextLine();
            byte[] data = guess.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serveur, portServeur);
            socketEnvoi.send(packet);
        }
    }
}
