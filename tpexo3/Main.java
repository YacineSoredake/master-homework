import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> ids = Arrays.asList(1, 2, 3);

        List<LamportProcess> processes = new ArrayList<>();
        for (int id : ids) {
            List<Integer> others = new ArrayList<>(ids);
            others.remove((Integer) id);
            processes.add(new LamportProcess(id, others));
        }

        new Timer().schedule(new TimerTask() {
            public void run() {
                processes.get(0).requestCriticalSection(); // P1
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            public void run() {
                processes.get(1).requestCriticalSection(); // P2
            }
        }, 5000);

        new Timer().schedule(new TimerTask() {
            public void run() {
                processes.get(2).requestCriticalSection(); // P3
            }
        }, 8000);
    }
}
