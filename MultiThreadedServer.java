import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MultiThreadedServer {
    private static boolean sectionLibre = true;
    private static final int tempsAcces = 5000;
    private static final Object lock = new Object();
    private static final BlockingQueue<Socket> fileAttente = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        int port = 5000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur prêt sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                fileAttente.add(clientSocket);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("En attente d'accès à la section critique...");
                while (fileAttente.peek() != socket) {
                    Thread.sleep(500); // Attente active jusqu'à ce que ce soit son tour
                }

                synchronized (lock) {
                    fileAttente.poll();
                    sectionLibre = false;
                }

                out.println("Accès autorisé à la section critique.");
                System.out.println("Client " + socket.getInetAddress() + ":" + socket.getPort() + " accède à la section critique.");

                Thread.sleep(tempsAcces);

                synchronized (lock) {
                    sectionLibre = true;
                }

                out.println("Fin de la section critique.");
                System.out.println("Client " + socket.getInetAddress() + ":" + socket.getPort() + " a libéré la section critique.");
            } catch (IOException | InterruptedException e) {
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
}
