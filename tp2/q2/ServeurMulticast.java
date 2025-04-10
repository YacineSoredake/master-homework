package tp2.q2;
import java.net.*;
import java.util.Random;

public class ServeurMulticast {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(1234);
        InetAddress groupe = InetAddress.getByName("230.0.0.0");
        int portMulticast = 4446;

        int secret = new Random().nextInt(100) + 1;
        boolean trouvé = false;
        byte[] buffer = new byte[1024];

        System.out.println("Serveur lancé. Nombre à deviner : " + secret);

        while (!trouvé) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength()).trim();
            int nombre = Integer.parseInt(msg);
            System.out.println("Reçu " + nombre + " de " + packet.getAddress() + ":" + packet.getPort());

            String réponse;
            if (nombre == secret) {
                réponse = "Gagné ! Le nombre était " + secret + ". Joueur: " + packet.getAddress() + ":" + packet.getPort();
                trouvé = true;
            } else if (nombre < secret) {
                réponse = "Trop petit de " + packet.getAddress() + ":" + packet.getPort();
            } else {
                réponse = "Trop grand de " + packet.getAddress() + ":" + packet.getPort();
            }

            byte[] data = réponse.getBytes();
            DatagramPacket multicast = new DatagramPacket(data, data.length, groupe, portMulticast);
            socket.send(multicast);
        }

        System.out.println("Fin du jeu !");
        socket.close();
    }
}
