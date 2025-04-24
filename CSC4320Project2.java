import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

class Buffer {
    private final Queue<Integer> queue = new LinkedList<>();
    private final Semaphore emptySlots;
    private final Semaphore fullSlots;
    private final ReentrantLock mutex = new ReentrantLock();

    public Buffer(int capacity) {
        this.emptySlots = new Semaphore(capacity);
        this.fullSlots = new Semaphore(0);
    }

    public void produce(int item, int producerId) throws InterruptedException {
        System.out.println("[Producer " + producerId + "] Waiting to produce...");
        emptySlots.acquire();
        mutex.lock();
        try {
            queue.add(item);
            System.out.println("[Producer " + producerId + "] Produced item " + item);
        } finally {
            mutex.unlock();
            fullSlots.release();
        }
    }

    public int consume(int consumerId) throws InterruptedException {
        System.out.println("[Consumer " + consumerId + "] Waiting to consume...");
        fullSlots.acquire();
        mutex.lock();
        try {
            int item = queue.remove();
            System.out.println("[Consumer " + consumerId + "] Consumed item " + item);
            return item;
        } finally {
            mutex.unlock();
            emptySlots.release();
        }
    }
}

class Producer extends Thread {
    private final int pid;
    private final int arrivalTime;
    private final int burstTime;
    private final Buffer buffer;

    public Producer(int pid, int arrivalTime, int burstTime, Buffer buffer) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.buffer = buffer;
    }

    public void run() {
        try {
            Thread.sleep(arrivalTime * 1000L);
            System.out.println("[Process " + pid + "] Arrived at time " + arrivalTime);
            Thread.sleep(burstTime * 1000L);
            buffer.produce(pid, pid);
            System.out.println("[Process " + pid + "] Finished after burst " + burstTime + "s");
        } catch (InterruptedException e) {
            System.out.println("[Process " + pid + "] Interrupted.");
        }
    }
}

class Consumer extends Thread {
    private final Buffer buffer;
    private final int consumerId;

    public Consumer(Buffer buffer, int consumerId) {
        this.buffer = buffer;
        this.consumerId = consumerId;
    }

    public void run() {
        try {
            for (int i = 0; i < 3; i++) {
                buffer.consume(consumerId);
                Thread.sleep(1500);
            }
        } catch (InterruptedException e) {
            System.out.println("[Consumer " + consumerId + "] Interrupted.");
        }
    }
}

public class CSC4320Project2 {
    public static void main(String[] args) {
        Buffer buffer = new Buffer(3); 

        List<Producer> producers = new ArrayList<>();
        Consumer consumer = new Consumer(buffer, 1); 

        // Load processes from file
        try (Scanner scanner = new Scanner(new File("processes.txt"))) {
            if (scanner.hasNextLine()) scanner.nextLine(); 
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().trim().split("\\s+");
                int pid = Integer.parseInt(parts[0]);
                int arrival = Integer.parseInt(parts[1]);
                int burst = Integer.parseInt(parts[2]);
                producers.add(new Producer(pid, arrival, burst, buffer));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: processes.txt not found.");
            return;
        }

        for (Producer p : producers) {
            p.start();
        }
        consumer.start();

        for (Producer p : producers) {
            try {
                p.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted.");
            }
        }

        try {
            consumer.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted while waiting for consumer.");
        }

        System.out.println("All processes and consumer completed.");
    }
}
