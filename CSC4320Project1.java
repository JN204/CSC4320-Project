import java.io.*;
import java.util.*;

class Process {
    int pid, arrivalTime, burstTime, priority, waitingTime, turnaroundTime;
    boolean completed;

    public Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completed = false;
    }

    @Override
    public String toString() {
        return "PID: " + pid + ", Arrival Time: " + arrivalTime + ", Burst Time: " + burstTime + ", Priority: " + priority;
    }
}

class ProcessScheduler {
    public static void main(String[] args) {
        List<Process> processes = readProcessesFromFile("processes.txt");
        
        System.out.println("Processes Loaded:");
        for (Process p : processes) {
            System.out.println(p);
        }

        System.out.println("\nFirst-Come, First-Served (FCFS) Scheduling:");
        fcfsScheduling(new ArrayList<>(processes));
        
        System.out.println("\nPriority Scheduling:");
        priorityScheduling(new ArrayList<>(processes));
    }

    public static List<Process> readProcessesFromFile(String filename) {
        List<Process> processList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 4) {
                    int pid = Integer.parseInt(parts[0]);
                    int arrivalTime = Integer.parseInt(parts[1]);
                    int burstTime = Integer.parseInt(parts[2]);
                    int priority = Integer.parseInt(parts[3]);
                    processList.add(new Process(pid, arrivalTime, burstTime, priority));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return processList;
    }

    public static void fcfsScheduling(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        int totalWT = 0, totalTAT = 0;
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: | ");
        List<Integer> timeMarkers = new ArrayList<>();
        timeMarkers.add(0);
        
        for (Process p : processes) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            p.waitingTime = currentTime - p.arrivalTime;
            p.turnaroundTime = p.waitingTime + p.burstTime;
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
            currentTime += p.burstTime;
            
            ganttChart.append("P").append(p.pid).append(" | ");
            timeMarkers.add(currentTime);
        }
        
        System.out.println(ganttChart.toString());
        for (Integer time : timeMarkers) {
            System.out.print(time + "\t");
        }
        System.out.println();
        
        printStatistics(processes, totalWT, totalTAT);
    }

    public static void priorityScheduling(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.priority));
        int currentTime = 0, completed = 0;
        int totalWT = 0, totalTAT = 0;
        boolean[] visited = new boolean[processes.size()];
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: | ");
        List<Integer> timeMarkers = new ArrayList<>();
        timeMarkers.add(0);

        while (completed < processes.size()) {
            for (int i = 0; i < processes.size(); i++) {
                Process p = processes.get(i);
                if (p.arrivalTime <= currentTime && !p.completed && !visited[i]) {
                    readyQueue.add(p);
                    visited[i] = true;
                }
            }
            if (!readyQueue.isEmpty()) {
                Process p = readyQueue.poll();
                if (currentTime < p.arrivalTime) {
                    currentTime = p.arrivalTime;
                }
                p.waitingTime = currentTime - p.arrivalTime;
                p.turnaroundTime = p.waitingTime + p.burstTime;
                totalWT += p.waitingTime;
                totalTAT += p.turnaroundTime;
                currentTime += p.burstTime;
                p.completed = true;
                completed++;
                
                ganttChart.append("P").append(p.pid).append(" | ");
                timeMarkers.add(currentTime);
            } else {
                currentTime++;
            }
        }
        
        System.out.println(ganttChart.toString());
        for (Integer time : timeMarkers) {
            System.out.print(time + "\t");
        }
        System.out.println();
        
        printStatistics(processes, totalWT, totalTAT);
    }
    
    public static void printStatistics(List<Process> processes, int totalWT, int totalTAT) {
        System.out.println("\nProcesses Stats:");
        for (Process p : processes) {
            System.out.println("PID: " + p.pid + " | WT: " + p.waitingTime + " | TAT: " + p.turnaroundTime);
        }
        System.out.println("Average WT: " + (double) totalWT / processes.size());
        System.out.println("Average TAT: " + (double) totalTAT / processes.size());
    }
}
