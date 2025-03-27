package udp;

import java.net.*;
import java.io.*;

class Emetteur {
    public static void main(String[] arg) {
        int port = 1234;
        String adr = "localhost";

        try {
            DatagramSocket soc = new DatagramSocket();
            System.out.println("Port local : " + soc.getLocalPort());
            InetAddress adIP = InetAddress.getByName(adr);
            BufferedReader entree = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("Message à envoyer : ");
                String ligne = entree.readLine();
                byte[] tampon = ligne.getBytes();
                int longueur = tampon.length;

                DatagramPacket message = new DatagramPacket(tampon, longueur, adIP, port);
                soc.send(message);

                tampon = new byte[256];
                message = new DatagramPacket(tampon, tampon.length);
                soc.receive(message);

                ligne = new String(message.getData(), 0, message.getLength());
                System.out.println("Réception du port " + message.getPort() + " de la machine " + message.getAddress() + " : " + ligne);
            }
        } catch (Exception exc) {
            System.out.println("Problème à l'envoi");
            exc.printStackTrace();
        }
    }
}
