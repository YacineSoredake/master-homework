package tp2.q1;
import java.net.*;
import java.util.Scanner;

public class ClientUDP {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress adresse = InetAddress.getByName("localhost");
        Scanner scanner = new Scanner(System.in);
        byte[] buffer = new byte[1024];

        while (true) {
            System.out.print("Devinez un nombre (1-100) : ");
            String saisie = scanner.nextLine();
            byte[] msg = saisie.getBytes();

            DatagramPacket packet = new DatagramPacket(msg, msg.length, adresse, 1234);
            socket.send(packet);

            DatagramPacket rep = new DatagramPacket(buffer, buffer.length);
            socket.receive(rep);

            String reponse = new String(rep.getData(), 0, rep.getLength());
            System.out.println("Réponse du serveur : " + reponse);

            if (reponse.equals("Gagné")) {
                break;
            }
        }

        socket.close();
    }
}
