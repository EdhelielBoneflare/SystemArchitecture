package uni.architect.SystemArchitect;

import uni.architect.SystemArchitect.model.Buffer;
import uni.architect.SystemArchitect.model.Generator;
import uni.architect.SystemArchitect.model.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║     Система массового обслуживания - Симулятор            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Get simulation time
        System.out.print("Введите время моделирования: ");
        double simulationTime = scanner.nextDouble();

        // Get number of generators
        System.out.print("Введите количество источников заявок: ");
        int numGenerators = scanner.nextInt();

        // Get number of workers
        System.out.print("Введите количество приборов: ");
        int numWorkers = scanner.nextInt();

        // Get buffer capacity
        System.out.print("Введите размер буфера: ");
        int bufferCapacity = scanner.nextInt();

        System.out.println();

        // Create generators
        List<Generator> generators = new ArrayList<>();
        for (int i = 0; i < numGenerators; i++) {
            System.out.printf("Источник %d:%n", i + 1);
            System.out.print("  Интервал генерации: ");
            double genInterval = scanner.nextDouble();
            generators.add(new Generator(i, genInterval, 0));
        }

        System.out.println();

        // Create workers
        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < numWorkers; i++) {
            System.out.printf("Прибор %d:%n", i + 1);
            System.out.print("  Максимальное время обработки: ");
            double maxProcessingTime = scanner.nextDouble();
            workers.add(new Worker(i, false, 0.0, maxProcessingTime, null));
        }

        System.out.println();

        // Create buffer
        Buffer buffer = new Buffer(bufferCapacity);

        // Create and run simulator
        Simulator simulator = new Simulator(generators, workers, buffer);

        System.out.println("╔═════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         Начало моделирования...                             ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        simulator.runSimulation(simulationTime);

        System.out.println();
        System.out.println("╔═════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         Моделирование завершено!                            ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════════════╝");

        scanner.close();
    }
}
