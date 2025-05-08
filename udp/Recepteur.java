package udp;

import java.net.*;

class Recepteur {
    public static void main(String[] args) {
        int port = 1234;
        
        try {
            DatagramSocket soc = new DatagramSocket(port);
            System.out.println("Récepteur en attente sur le port " + port + "...");

            byte[] data = new byte[256];

            while (true) {
                DatagramPacket message = new DatagramPacket(data, data.length);
                soc.receive(message);

                String ligne = new String(message.getData(), 0, message.getLength());
                System.out.println("Message reçu de " + message.getAddress() + " : " + ligne);

                String reponse = "Message reçu: " + ligne;
                byte[] repdata = reponse.getBytes();
                DatagramPacket reponsePacket = new DatagramPacket(
                    repdata, repdata.length, message.getAddress(), message.getPort());
                soc.send(reponsePacket);
                
            }
            
        } catch (Exception e) {
            System.out.println("Problème à la réception");
            e.printStackTrace();
        }
    }
}
