package td1exo4;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

// Classe du serveur multi-threadé
class server {
    public static final AtomicInteger clientCount = new AtomicInteger(0);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            serverSocket.setSoTimeout(600000); // Fermer après 10 minutes sans client
            System.out.println("Serveur en attente de clients...");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(socket).start();
                } catch (SocketTimeoutException e) {
                    System.out.println("Aucun client n'est arrivé en 10 minutes. Fermeture du serveur...");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Classe pour gérer chaque client dans un thread séparé
class ClientHandler extends Thread {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            socket.setSoTimeout(300000);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            int clientNumber = server.clientCount.incrementAndGet();
            System.out.println("Client connecté #" + clientNumber + " : " + socket.getInetAddress());

            int[] array = (int[]) in.readObject();
            Arrays.sort(array);
            out.writeObject(array);
            System.out.println("Tableau trié envoyé au client #" + clientNumber);

        } catch (SocketTimeoutException e) {
            System.out.println("Client inactif pendant 5 minutes. Fermeture de la connexion...");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}