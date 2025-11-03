package uni.architect.SystemArchitect;

import uni.architect.SystemArchitect.model.Buffer;
import uni.architect.SystemArchitect.model.Generator;
import uni.architect.SystemArchitect.model.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Стандартные параметры (true/false):");
        boolean standard = scanner.nextBoolean();

        if (standard) {
            start(false, -1, true, 3,
                    7, 10, List.of(2.0, 1.0, 0.5),
                    2);
        } else {
            readParamsAndStart();
        }

        scanner.close();
    }

    public static void readParamsAndStart() {
        System.out.println("Отображение UI (true/false): ");
        boolean needPrint = scanner.nextBoolean();

        System.out.print("Введите время моделирования: ");
        double simulationTime = scanner.nextDouble();

        System.out.print("Установка автоматического режима (true/false): ");
        boolean auto = scanner.nextBoolean();

        System.out.print("Введите количество источников заявок: ");
        int numGenerators = scanner.nextInt();

        System.out.print("Введите количество приборов: ");
        int numWorkers = scanner.nextInt();

        System.out.print("Введите размер буфера: ");
        int bufferCapacity = scanner.nextInt();

        System.out.println();

        List<Double> genIntervals = new ArrayList<>();
        for (int i = 0; i < numGenerators; i++) {
            System.out.printf("Источник %d:%n", i + 1);
            System.out.print("  Интервал генерации: ");
            genIntervals.add(scanner.nextDouble());
        }

        System.out.println();

        System.out.print("Максимальное время обработки заявки прибором: ");
        double maxProcessingTime = scanner.nextDouble();

        System.out.println();

        start(needPrint, simulationTime, auto, numGenerators, numWorkers, bufferCapacity, genIntervals, maxProcessingTime);
    }

    private static void start(boolean needPrint, double simulationTime, boolean auto, int numGenerators, int numWorkers,
                              int bufferCapacity, List<Double> genIntervals, double maxProcessingTime) {
        List<Generator> generators = new ArrayList<>();

        for (int i = 0; i < numGenerators; i++) {
            generators.add(new Generator(i, genIntervals.get(i), 0));
        }

        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < numWorkers; i++) {
            workers.add(new Worker(i, false, 0.0, maxProcessingTime, null, 0));
        }

        Buffer buffer = new Buffer(bufferCapacity);

        Simulator simulator = new Simulator(generators, workers, buffer, auto, needPrint);

        simulator.runSimulation(simulationTime);
    }
}
