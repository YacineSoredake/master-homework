package tpuno;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerThread {
    private static final int PORT = 3000;
    private static boolean sectionCritiqueOccupee = false;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("\nServeur en écoute sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);

                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void setSectionCritique(boolean etat) {
        sectionCritiqueOccupee = etat;
        envoyerEtatATous();
    }

    private static void envoyerEtatATous() {
        for (ClientHandler client : clients) {
            client.envoyerMessage("État de la section critique : " + (sectionCritiqueOccupee ? "Occupée" : "Libre"));
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                envoyerMessage("Connexion sucess. Attente de l'accès à la section critique...");
                Thread.sleep(10000);
                while (true) {
                    if (!sectionCritiqueOccupee) {
                        setSectionCritique(true);
                        envoyerMessage("Accès accordé à la section critique.");
                        System.out.println("Client " + socket.getInetAddress() + " utilise la section critique.");
                        Thread.sleep(5000); 
                        setSectionCritique(false);
                        envoyerMessage("Fin de l'accès à la section critique.");
                        System.out.println("Client " + socket.getInetAddress() + " a libéré la section critique.");
                        break; 
                    } else {
                        Thread.sleep(1000); 
                    }
                }

                socket.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void envoyerMessage(String msg) {
            if (out != null) {
                out.println(msg);
            }
        }
    }
}
