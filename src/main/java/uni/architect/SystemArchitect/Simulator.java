package uni.architect.SystemArchitect;

import uni.architect.SystemArchitect.model.Buffer;
import uni.architect.SystemArchitect.model.Event;
import uni.architect.SystemArchitect.model.Generator;
import uni.architect.SystemArchitect.model.Worker;
import uni.architect.SystemArchitect.model.Request;

import java.util.*;


public class Simulator {
    private final Scanner scanner = new Scanner(System.in);

    private final List<Generator> generators;
    private final List<Worker> workers;
    private final Buffer buffer;

    private double currentTime = 0.0;
    private int requestCounter = 0;
    private int declinedRequests = 0;
    private int workerPointer = 0;
    private boolean auto = true;
    private boolean needPrint = true;

    // Track the current request for each event
    private Request currentRequest = null;
    private int affectedWorker = -1;

    private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();
    private final Map<Integer, Integer> declineTypeCounter = new HashMap<>();

    public Simulator(List<Generator> generators, List<Worker> workers, Buffer buffer, boolean auto, boolean needPrint) {
        this.generators = generators;
        this.workers = workers;
        this.buffer = buffer;
        this.auto = auto;
        this.needPrint = needPrint;

        generators.forEach(gen -> declineTypeCounter.put(gen.getNumber(), 0));
    }

    private boolean chooseWorker() {
        int tries = 0;
        while (workers.get(workerPointer).isBusy()) {
            tries++;
            workerPointer = (workerPointer + 1) % workers.size();
            if (tries >= workers.size()) {
                return false;
            }
        }
        return true;
    }

    public void runSimulation(double simulationTime) {
        if (simulationTime >= 0) {
            eventQueue.add(new Event(Event.EventType.END, simulationTime, -1));
        }

        if (needPrint) {
            printTableHeader();
        }

        Event startSimulation = new Event(Event.EventType.START, 0.0, -1);
        eventQueue.add(startSimulation);
        if (needPrint) {
            printStartRow();
        }

        for (Generator generator : generators) {
            eventQueue.add(new Event(Event.EventType.GENERATION, generator.getNextGenTime(), generator.getNumber()));
        }

        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            currentTime = event.getTime();
            if ((simulationTime >= 0 && currentTime >= simulationTime)
                    ||  (simulationTime < 0 && requestCounter >= 1000)) {
                break;
            }

            switch (event.getType()) {
                case Event.EventType.GENERATION -> handleGenerationEvent(event.getObjectNumber());
                case Event.EventType.COMPLETION -> handleCompletionEvent(event.getObjectNumber());
            }

            if (needPrint) {
                printEventRow(event);
                if (!auto) {
                    scanner.nextLine();  // add pause before each step
                }
            }
        }

        if (needPrint) {
            printEndRow();
            printTableFooter();
        }
        printOptimisationResults();
    }

    private void handleGenerationEvent(int genNumber) {
        Generator generator = generators.get(genNumber);
        Request request = generator.generateRequest(currentTime);
        requestCounter++;

        currentRequest = request;

        // schedule next generation
        eventQueue.add(new Event(Event.EventType.GENERATION, generator.getNextGenTime(), genNumber));

        if (chooseWorker()) {
            Worker worker = workers.get(workerPointer);
            affectedWorker = worker.getNumber();
            worker.startProcessing(currentTime, request);
            eventQueue.add(new Event(Event.EventType.COMPLETION, worker.getCompletionTime(), worker.getNumber()));
        } else {
            affectedWorker = -1;
            Request declined = buffer.addRequest(request);
            if (declined != null) {
                declinedRequests++;
                declineTypeCounter.put(declined.getGeneratorNumber(), declineTypeCounter.get(declined.getGeneratorNumber()) + 1);
            }
        }
    }

    private void handleCompletionEvent(int workerNumber) {
        Worker worker = workers.get(workerNumber);
        affectedWorker = worker.getNumber();
        currentRequest = worker.getCurRequest();

        // stop current processing (if any)
        worker.stopProcessing(currentTime);

        Request nextRequest = buffer.getNextRequest();
        if (nextRequest != null) {
            worker.startProcessing(currentTime, nextRequest);
            eventQueue.add(new Event(Event.EventType.COMPLETION, worker.getCompletionTime(), worker.getNumber()));
        }
    }

    private void printTableHeader() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║     Система массового обслуживания - Симулятор            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("║                                                  Календарь событий                                                                       ");
        System.out.println("╠═══════════════╦═══════════╦══════════════╦═══════════════╦═════════════════╦══════════════════════════════════╦════════════════════════════");
        System.out.println("║   Событие     ║   Время   ║ Число заявок ║ Число отказов ║  Номер заявки   ║    Состояние прибора             ║  Состояние буфера          ");
        System.out.println("╠═══════════════╬═══════════╬══════════════╬═══════════════╬═════════════════╬══════════════════════════════════╬════════════════════════════");
    }

    private void printTableFooter() {
        System.out.println("╚═══════════════╩═══════════╩══════════════╩═══════════════╩═════════════════╩══════════════════════════════════╩════════════════════════════");
    }

    private void printEventRow(Event event) {
        String eventType = event.getType() == Event.EventType.GENERATION ? "И" : "П";
        String eventName = String.format("%s%d", eventType, event.getObjectNumber() + 1);

        String requestNum = currentRequest != null ? currentRequest.getNumber() : "-";
        String workerState = getWorkerState();

        System.out.printf("║ %-13s ║   %6.2f  ║    %5d     ║     %5d     ║  %-14s ║  %-31s ║ %-26s %n",
                eventName, event.getTime(), requestCounter, declinedRequests,
                requestNum, workerState, buffer.getState());

        printSysState();
    }

    private void printSysState() {
        StringBuilder deviceStates = new StringBuilder();
        workers.forEach(worker -> {
            if (worker.isBusy() && worker.getCurRequest() != null) {
                deviceStates.append("П").append(worker.getNumber() + 1)
                        .append(": ").append(worker.getCurRequest().getNumber())
                        .append(" до ").append(String.format("%.2f", worker.getCompletionTime())).append(" ");
            } else {
                deviceStates.append("П").append(worker.getNumber() + 1).append(": свободен ");
            }
        });

        StringBuilder closestEventTimes = new StringBuilder();
        eventQueue.forEach(eventQ ->
                closestEventTimes.append(eventQ.getType() == Event.EventType.GENERATION ? "И" : eventQ.getType() == Event.EventType.COMPLETION ? "П" : "Конец").append(eventQ.getObjectNumber() + 1)
                        .append(": ").append(String.format("%.2f", eventQ.getTime())).append(" "));

        System.out.println("╠═══════════════╬═══════════╩══════════════╩═══════════════╩═════════════════╩══════════════════════════════════╩════════════════════════════");
        System.out.println("║ Приборы       ║ " + deviceStates.toString());
        System.out.println("╠═══════════════╬════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("║ События       ║ " + closestEventTimes.toString());
        System.out.println("╠═══════════════╬═══════════╦══════════════╦═══════════════╦═════════════════╦══════════════════════════════════╦════════════════════════════");

    }

    private void printStartRow() {
        System.out.printf("║ %-13s ║   %6.2f  ║    %5d     ║     %5s     ║  %-14s ║  %-31s ║ %-26s %n",
                "Начало мод.", currentTime, requestCounter, "", "", "", "");
        printSysState();
    }

    private void printEndRow() {
        System.out.printf("║ %-13s ║   %6.2f  ║    %5d     ║     %5d     ║  %-14s ║  %-31s ║ %-26s %n",
                "Конец мод.", currentTime, requestCounter, declinedRequests,
                "-", getWorkerState(), buffer.getState());
    }

    private String getWorkerState() {
        if (affectedWorker == -1) {
            return "Все приборы заняты";
        }

        Worker worker = workers.get(affectedWorker);
        String workerName = "П" + (worker.getNumber() + 1);

        if (worker.isBusy() && worker.getCurRequest() != null) {
            return workerName + ": обрабатывает " + worker.getCurRequest().getNumber();
        } else {
            return workerName + ": свободен";
        }
    }

    private void printOptimisationResults() {
        double allWorkTime = 0;
        for (Worker w: workers) {
            allWorkTime += w.getTotalWorkTime() - (w.getCompletionTime() - currentTime);
        }
        double kpd = allWorkTime/(currentTime * workers.size());

        System.out.println("\n╔══════════════════════════════════════════════════════════════");
        System.out.println("║                РЕЗУЛЬТАТЫ");
        System.out.println("╠══════════════════════════════════════════════════════════════");
        System.out.printf("║ Конфигурация системы: %d мест в буфере, %d приборов%n", buffer.getCapacity(), workers.size());
        System.out.printf("║ Общее время моделирования: %.2f сек%n", currentTime);
        System.out.printf("║ Всего заявок сгенерировано: %d%n", requestCounter);
        System.out.printf("║ Всего отказов: %d%n", declinedRequests);
        System.out.printf("║ Коэффициент использования системы: %.3f (требуется >= 0.75)%n", kpd);
        System.out.println("║");
        System.out.println("║ Вероятности отказа по источникам:");
        for (Map.Entry<Integer, Integer> entry : declineTypeCounter.entrySet()) {
            int sourceNum = entry.getKey();
            int declined = entry.getValue();
            double prob = (double) declined / requestCounter;
            String requirement = "";
            switch (sourceNum) {
                case 0 -> requirement = "требуется ≤ 0.005 (0.5%)";
                case 1 -> requirement = "требуется ≤ 0.02 (2%)";
                case 2 -> requirement = "требуется ≤ 0.05 (5%)";
            }
            System.out.printf("║  Источник И%d: %d отказов, P_отк = %.4f %s%n",
                    sourceNum + 1, declined, prob, requirement);
        }
        System.out.println("╚══════════════════════════════════════════════════════════════");
    }

}
