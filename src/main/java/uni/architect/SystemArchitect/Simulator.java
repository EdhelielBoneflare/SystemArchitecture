package uni.architect.SystemArchitect;

import uni.architect.SystemArchitect.model.Buffer;
import uni.architect.SystemArchitect.model.Event;
import uni.architect.SystemArchitect.model.Generator;
import uni.architect.SystemArchitect.model.Worker;
import uni.architect.SystemArchitect.model.Request;

import java.util.List;
import java.util.PriorityQueue;


public class Simulator {

    private final List<Generator> generators;
    private final List<Worker> workers;
    private final Buffer buffer;

    private double currentTime = 0.0;
    private int requestCounter = 0;
    private int declinedRequests = 0;
    private int workerPointer = 0;

    // Track the current request for each event
    private Request currentRequest = null;
    private int affectedWorker = -1;

    private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();

    public Simulator(List<Generator> generators, List<Worker> workers, Buffer buffer) {
        this.generators = generators;
        this.workers = workers;
        this.buffer = buffer;
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
        eventQueue.add(new Event(Event.EventType.END, simulationTime, -1));

        printTableHeader();

        for (Generator generator : generators) {
            eventQueue.add(new Event(Event.EventType.GENERATION, generator.getNextGenTime(), generator.getNumber()));
        }

        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            currentTime = event.getTime();
            if (currentTime >= simulationTime) {
                break;
            }

            switch (event.getType()) {
                case Event.EventType.GENERATION -> handleGenerationEvent(event.getObjectNumber());
                case Event.EventType.COMPLETION -> handleCompletionEvent(event.getObjectNumber());
            }

            printEventRow(event);
        }
        printEndRow();
        printTableFooter();
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
            // original simple buffer handling: count decline if buffer already full, then add (buffer decides replacement)
            if (buffer.isFull()) {
                declinedRequests++;
            }
            buffer.addRequest(request);
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
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
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

}
