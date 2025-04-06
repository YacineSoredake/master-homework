import java.io.*;
import java.net.*;
import java.util.*;

public class LamportProcess {
    private int id;
    private int clock = 0;
    private List<Integer> otherProcesses;
    private List<Request> queue = new ArrayList<>();
    private int ackCount = 0;

    public LamportProcess(int id, List<Integer> others) {
        this.id = id;
        this.otherProcesses = others;
        new Thread(this::startServer).start();
    }

    public void requestCriticalSection() {
        clock++;
        queue.add(new Request(clock, id));
        sendToAll("REQ " + clock + " " + id);
    }

    private void handleMessage(String msg) {
        String[] parts = msg.split(" ");
        String type = parts[0];
        int receivedClock = Integer.parseInt(parts[1]);
        int senderId = Integer.parseInt(parts[2]);
    
        System.out.println("P" + id + " received from P" + senderId + ": " + msg);
    
        clock = Math.max(clock, receivedClock) + 1;
    
        if (type.equals("REQ")) {
            queue.add(new Request(receivedClock, senderId));
            sendToOne(senderId, "ACK " + clock + " " + id);
        } else if (type.equals("ACK")) {
            ackCount++;
            if (ackCount == otherProcesses.size() && isMyRequestFirst()) {
                enterCriticalSection();
            }
        } else if (type.equals("REL")) {
            queue.removeIf(r -> r.id == senderId);
        }
    }
    

    private boolean isMyRequestFirst() {
        queue.sort(null);
        return !queue.isEmpty() && queue.get(0).id == id;
    }

    private void enterCriticalSection() {
        System.out.println("P" + id + " ENTERS critical section");
        try { Thread.sleep(2000); } catch (Exception ignored) {}
        exitCriticalSection();
    }

    private void exitCriticalSection() {
        queue.removeIf(r -> r.id == id);
        ackCount = 0;
        sendToAll("REL " + clock + " " + id);
        System.out.println("P" + id + " EXITS critical section");
    }

    private void sendToAll(String msg) {
        for (int other : otherProcesses) {
            sendToOne(other, msg);
        }
    }

    private void sendToOne(int toId, String msg) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 4000 + toId);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(msg);
                System.out.println("P" + id + " sent to P" + toId + ": " + msg);
            } catch (Exception e) {
                System.out.println("Send failed from P" + id + " to P" + toId);
            }
        }).start();
    }
    

    private void startServer() {
        try (ServerSocket server = new ServerSocket(4000 + id)) {
            while (true) {
                Socket client = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String msg = in.readLine();
                if (msg != null) handleMessage(msg);
            }
        } catch (IOException e) {
            System.out.println("Server error P" + id);
        }
    }

    static class Request implements Comparable<Request> {
        int clock, id;
        Request(int c, int i) { clock = c; id = i; }

        public int compareTo(Request r) {
            if (clock != r.clock) return clock - r.clock;
            return id - r.id;
        }
    }
}
