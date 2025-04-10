import java.io.*;
import java.net.*;
import java.util.*;

public class LamportProcess {
    private int id;
    private int clock = 0;
    private List<Integer> peers;
    private List<Request> queue = new ArrayList<>();
    private int ackCount = 0;

    public LamportProcess(int id, List<Integer> peers) {
        this.id = id;
        this.peers = peers;
        new Thread(this::startServer).start();
    }

    public void requestAccess() {
        clock++;
        queue.add(new Request(clock, id));
        broadcast("REQ " + clock + " " + id);
    }

    private void handleMessage(String message) {
        String[] parts = message.split(" ");
        String type = parts[0];
        int timestamp = Integer.parseInt(parts[1]);
        int senderId = Integer.parseInt(parts[2]);

        clock = Math.max(clock, timestamp) + 1;

        switch (type) {
            case "REQ":
                queue.add(new Request(timestamp, senderId));
                sendTo(senderId, "ACK " + clock + " " + id);
                break;
            case "ACK":
                ackCount++;
                if (ackCount == peers.size() && isMyRequestFirst()) {
                    enterCriticalSection();
                }
                break;
            case "REL":
                queue.removeIf(r -> r.processId == senderId);
                break;
        }
    }

    private boolean isMyRequestFirst() {
        queue.sort(Comparator.naturalOrder());
        return !queue.isEmpty() && queue.get(0).processId == id;
    }

    private void enterCriticalSection() {
        System.out.println("P" + id + " ENTERS critical section");
        try { Thread.sleep(2000); } catch (Exception ignored) {}
        exitCriticalSection();
    }

    private void exitCriticalSection() {
        queue.removeIf(r -> r.processId == id);
        ackCount = 0;
        broadcast("REL " + clock + " " + id);
        System.out.println("P" + id + " EXITS critical section");
    }

    private void broadcast(String msg) {
        for (int peerId : peers) {
            sendTo(peerId, msg);
        }
    }

    private void sendTo(int peerId, String msg) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5000 + peerId);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(msg);
                System.out.println("P" + id + " sent to P" + peerId + ": " + msg);
            } catch (IOException e) {
                System.out.println("P" + id + " failed to send to P" + peerId);
            }
        }).start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(5000 + id)) {
            while (true) {
                try (Socket client = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                    String msg = in.readLine();
                    if (msg != null) {
                        System.out.println("P" + id + " received: " + msg);
                        handleMessage(msg);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("P" + id + " server error.");
        }
    }

    static class Request implements Comparable<Request> {
        int timestamp, processId;
        Request(int t, int p) { timestamp = t; processId = p; }

        public int compareTo(Request r) {
            if (timestamp != r.timestamp) return Integer.compare(timestamp, r.timestamp);
            return Integer.compare(processId, r.processId);
        }
    }

    public static void main(String[] args) {
        int myId = Integer.parseInt(args[0]);
        List<Integer> allIds = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            allIds.add(Integer.parseInt(args[i]));
        }
        LamportProcess process = new LamportProcess(myId, allIds);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("P" + myId + ": Press Enter to request access to CS...");
            scanner.nextLine();
            process.requestAccess();
        }
    }
}
