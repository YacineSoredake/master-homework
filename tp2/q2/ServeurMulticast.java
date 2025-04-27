package tp2.q2;

import java.net.*;
import java.util.*;

public class ServeurMulticast {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(1234);
        InetAddress groupe = InetAddress.getByName("230.0.0.0");
        int portMulticast = 4446;
        int secret = new Random().nextInt(100) + 1;
        boolean trouvé = false;
        byte[] buffer = new byte[1024];
        final long TIMEOUT = 2 * 60 * 10; 

        Map<String, Long> joueurs = new HashMap<>();
        Set<String> joueursTimeout = new HashSet<>();
        System.out.println("Serveur started , Le nombre secret est : " + secret);

        while (!trouvé) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String joueur = packet.getAddress().toString() + ":" + packet.getPort();
            long now = System.currentTimeMillis();
            if (joueursTimeout.contains(joueur)) {
                System.out.println("ignore : " + joueur);
                continue;
            }
            if (!joueurs.containsKey(joueur)) {
                joueurs.put(joueur, now);
            } else {
                long last = joueurs.get(joueur);
                if (now - last > TIMEOUT) {
                    joueursTimeout.add(joueur);
                    System.out.println("Joueur " + joueur + " exclu pour inactivité.");
                    continue;
                }
            }

            joueurs.put(joueur, now);
            String msg = new String(packet.getData(), 0, packet.getLength()).trim();
            int nombre = Integer.parseInt(msg);

            System.out.println("Reçu " + nombre + " de " + joueur);

            String réponse;
            if (nombre == secret) {
                réponse = "Gagné ! Nombre = " + secret + ". Joueur: " + joueur;
                trouvé = true;
            } else if (nombre < secret) {
                réponse = "Trop petit de " + joueur;
            } else {
                réponse = "Trop grand de " + joueur;
            }

            byte[] data = réponse.getBytes();
            DatagramPacket multicast = new DatagramPacket(data, data.length, groupe, portMulticast);
            socket.send(multicast);
        }

        System.out.println("Fin du jeu !");
        socket.close();
    }
}
