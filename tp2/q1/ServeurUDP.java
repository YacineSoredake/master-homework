package tp2.q1;
import java.net.*;
// import java.util.Random;

public class ServeurUDP {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(1234);
        byte[] buffer = new byte[1024];
        int secret = 86;
        System.out.println("Serveur lancé. Nombre à deviner : " + secret);
        boolean match = false;
        while (!match) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            int nombre = Integer.parseInt(message.trim());
            System.out.println("Le client (" + packet.getAddress() + ":" + packet.getPort() + ") a dit : " + nombre);
            String reponse;
            if (nombre < secret) {
                reponse = "Trop petit";
            } else if (nombre > secret) {
                reponse = "Trop grand";
            } else {
                reponse = "Gagne";
                match = true;
            }
            byte[] repBytes = reponse.getBytes();
            DatagramPacket rep = new DatagramPacket(repBytes, repBytes.length, packet.getAddress(), packet.getPort());
            socket.send(rep);
        }
        System.out.println("termine");
        socket.close();
    }
}
