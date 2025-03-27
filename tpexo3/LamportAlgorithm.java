import java.io.*;
import java.net.*;
import java.util.*;

class LamportProcess {
    private int processId, clock = 0, ackCount = 0;
    private List<Integer> peers;
    private Queue<Integer> queue = new PriorityQueue<>();

    public LamportProcess(int processId, List<Integer> peers) {
        this.processId = processId;
        this.peers = peers;
        new Thread(this::startServer).start();
    }

    public void requestAccess() {
        clock++;
        queue.add(processId);
        broadcast("REQ " + clock + " " + processId);
    }

    private void receiveMessage(String message) {
        String[] parts = message.split(" ");
        int senderClock = Integer.parseInt(parts[1]);
        int senderId = Integer.parseInt(parts[2]);
        clock = Math.max(clock, senderClock) + 1;

        if (message.startsWith("REQ")) {
            queue.add(senderId);
            sendMessage(senderId, "ACK " + clock + " " + processId);
        } else if (message.startsWith("ACK")) {
            ackCount++;
            if (ackCount == peers.size() && queue.peek() == processId) enterCriticalSection();
        } else if (message.startsWith("REL")) {
            queue.poll();
        }
    }

    private void enterCriticalSection() {
        System.out.println("Process " + processId + " in critical section");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        exitCriticalSection();
    }

    private void exitCriticalSection() {
        queue.poll();
        broadcast("REL " + clock + " " + processId);
    }

    private void broadcast(String message) { peers.forEach(peer -> sendMessage(peer, message)); }

    private void sendMessage(int peer, String message) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5000 + peer);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(message);
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(5000 + processId)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    receiveMessage(in.readLine());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}

public class LamportAlgorithm {
    public static void main(String[] args) {
        new LamportProcess(Integer.parseInt(args[0]), Arrays.asList(1, 2, 3));
    }
}
