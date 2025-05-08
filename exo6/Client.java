package exo6;

import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int serverPort = 9000;
        
        int[] numbers = {1, 2, 3, 4, 5};

        // Sérialisation du tableau
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteStream);
        out.writeObject(numbers);
        out.flush();
        byte[] data = byteStream.toByteArray();

        // Envoi du tableau au serveur
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, serverPort);
        socket.send(sendPacket);
        System.out.println("Tableau envoyé au serveur.");

        // Réception de la réponse (somme des nombres)
        byte[] buffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivePacket);

        // Lecture de la somme reçue
        ByteArrayInputStream byteInput = new ByteArrayInputStream(receivePacket.getData());
        DataInputStream dataInput = new DataInputStream(byteInput);
        int sum = dataInput.readInt();

        System.out.println("Somme reçue du serveur : " + sum);

        // Fermeture du socket
        socket.close();
    }
}

