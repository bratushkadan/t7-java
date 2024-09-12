package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> arr = randArr(10000);

        System.out.println("=== Задание 1");
        System.out.println("\tВариант 2 – поиск максимального элемента в массиве");
        task1Seq(arr);
        task1Multithreading(arr);
        task1ForkJoin(arr);
        findMaxNumberFork(arr);
        System.out.println();
        System.out.println("=== Задание 2");
        task2();

        System.out.println();
        System.out.println("=== Задание 3");
        task3();
    }

    private static List<Integer> randArr(int size) {
        List<Integer> arr = new ArrayList<>(size);

        Random random = new Random();

        for (int i = 0; i < size; i++) {
            int randomNumber = random.nextInt(0, 10000 * size);
            arr.add(randomNumber);
        }

        return arr;
    }

    public static void task1Seq(List<Integer> arr) {
        System.out.println("\t1. Последовательно");

        int max = -1;
        long startTime = System.currentTimeMillis();
        MemoryMonitor mm = new MemoryMonitor(25);
        mm.start();
        for (int integer : arr) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (integer > max) {
                max = integer;
            }
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        mm.stopWait();
        long maxUsedMem = mm.getMaxUsedMem();


        System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMem / Math.pow(2, 20)));
        System.out.println("\tМаксимальное число в массиве - " + max);
        System.out.println("\tВремя выполнения: " + duration + " миллисекунд");
    }

    public static void task1Multithreading(List<Integer> arr) {
        int nThreads = Runtime.getRuntime().availableProcessors();
        System.out.printf("\t2. Многопоточно (%d потоков)\n", nThreads);

        List<Future<Integer>> futures = new ArrayList<>();
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        int chunkSize = arr.size() / nThreads;

        for (int i = 0; i < nThreads; i++) {
            int start = i * chunkSize;
            int end = (i == nThreads - 1) ? arr.size() : start + chunkSize;
            Callable<Integer> task = () -> {
                int maxInChunk = -1;
                for (int j = start; j < end; j++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (arr.get(j) > maxInChunk) {
                        maxInChunk = arr.get(j);
                    }
                }
                return maxInChunk;
            };
            futures.add(exec.submit(task));
        }

        int max = -1;
        long startTime = System.currentTimeMillis();
        MemoryMonitor mm = new MemoryMonitor(25);
        mm.start();
        for (Future<Integer> fut : futures) {
            try {
                int localMax = fut.get();
                if (localMax > max) {
                    max = localMax;
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        exec.shutdown();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        mm.stopWait();
        long maxUsedMem = mm.getMaxUsedMem();

        System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMem / Math.pow(2, 20)));
        System.out.println("\tМаксимальное число в массиве - " + max);
        System.out.println("\tВремя выполнения: " + duration + " миллисекунд");
    }

    public static void task1ForkJoin(List<Integer> arr) {
        int nThreads = Runtime.getRuntime().availableProcessors();
        System.out.printf("\t3. ForkJoin (%d потоков)\n", nThreads);

        List<Future<Integer>> futures = new ArrayList<>();
        // Внутри WorkStealingPool используется технология ForkJoinPool или fork/join framework.
        ExecutorService exec = Executors.newWorkStealingPool(nThreads);
        int chunkSize = arr.size() / nThreads;

        for (int i = 0; i < nThreads; i++) {
            int start = i * chunkSize;
            int end = (i == nThreads - 1) ? arr.size() : start + chunkSize;
            Callable<Integer> task = () -> {
                int maxInChunk = -1;
                for (int j = start; j < end; j++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (arr.get(j) > maxInChunk) {
                        maxInChunk = arr.get(j);
                    }
                }
                return maxInChunk;
            };
            futures.add(exec.submit(task));
        }

        int max = -1;
        long startTime = System.currentTimeMillis();
        MemoryMonitor mm = new MemoryMonitor(25);
        mm.start();
        for (Future<Integer> fut : futures) {
            try {
                int localMax = fut.get();
                if (localMax > max) {
                    max = localMax;
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        exec.shutdown();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        mm.stopWait();
        long maxUsedMem = mm.getMaxUsedMem();

        System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMem / Math.pow(2, 20)));
        System.out.println("\tМаксимальное число в массиве - " + max);
        System.out.println("\tВремя выполнения: " + duration + " миллисекунд");
    }

    private static int findMaxInRange(List<Integer> sublist) throws
            InterruptedException {
        int max = Integer.MIN_VALUE;
        for (int number : sublist) {
            Thread.sleep(1);
            if (number > max) {
                max = number;
            }
        }
        return max;
    }

    public static void findMaxNumberFork(List<Integer> list) {
        int nThreads = Runtime.getRuntime().availableProcessors();
        System.out.printf("\t4. Образцовая функция findMaxNumberFork (%d потоков)\n", nThreads);
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Список пуст или равен null");
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(nThreads);
        MaxFinderTask task = new MaxFinderTask(list, 0, list.size());

        long startTime = System.currentTimeMillis();
        MemoryMonitor mm = new MemoryMonitor(25);
        mm.start();
        int max = forkJoinPool.invoke(task);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        mm.stopWait();
        long maxUsedMem = mm.getMaxUsedMem();

        System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMem / Math.pow(2, 20)));

        System.out.println("\tМаксимальное число в массиве - " + max);
        System.out.println("\tВремя выполнения: " + duration + " миллисекунд");
    }

    static class MaxFinderTask extends RecursiveTask<Integer> {
        private List<Integer> list;
        private int start;
        private int end;

        // Конструктор MaxFinderTask для создания задачи для подсписка
        MaxFinderTask(List<Integer> list, int start, int end) {
            this.list = list;
            this.start = start;
            this.end = end;
        }

        // Метод compute(), выполняющий вычисления для задачи
        @Override
        protected Integer compute() {
// Если в подсписке только один элемент, вернем его
            if (end - start <= 1000) {
                try {
                    return findMaxInRange(list.subList(start, end));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
// Найдем середину подсписка
            int middle = start + (end - start) / 2;
// Создаем две подзадачи для левой и правой половин подсписка
            MaxFinderTask leftTask = new MaxFinderTask(list, start, middle);
            MaxFinderTask rightTask = new MaxFinderTask(list, middle, end);
// Запускаем подзадачу для правой половины параллельно
            leftTask.fork();
// Вычисляем максимальные значения в левой и правой половинах

            int rightResult = rightTask.compute();
            int leftResult = leftTask.join();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
// Возвращаем максимальное значение из левой и правой половин
            return Math.max(leftResult, rightResult);
        }
    }

    public static void task2() {
        System.out.println("Введите числа");

        Random rand = new Random();

        ExecutorService executor = Executors.newCachedThreadPool();
        Scanner scanner = new Scanner(System.in);

        try {
            while (true) {
                String input = scanner.nextLine();
                if (input.equals("")) {
                    break;
                }

                int number;
                try {
                    number = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    continue;
                }

                Future<Integer> future = executor.submit(() -> {
                    int t = rand.nextInt(1, 6);
                    Thread.sleep(1000 * t);

                    return number * number;
                });

                executor.submit(() -> {
                    try {
                        System.out.println("Результат: " + future.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            }
        } finally {
            executor.shutdown();
            scanner.close();
        }
    }

    public static void task3() {
        // схема:
        //   Очередь --> Генератор файлов
        //      ^
        //      |
        //      |
        //   Обработчик файлов

        BlockingQueue<File> bq = new LinkedBlockingQueue<File>(5);

        Thread genTh = new Thread(new FileGenerator(bq));
        Thread jsonTh = new Thread(new FileProcessor(bq, "XML"));
        Thread xmlTh = new Thread(new FileProcessor(bq, "JSON"));
        Thread xlsTh = new Thread(new FileProcessor(bq, "XLS"));

        genTh.start();
        jsonTh.start();
        xmlTh.start();
        xlsTh.start();
    }
}

class MemoryMonitor {
    private Thread t;
    private final int ivlMs;
    private long maxUsedMem = Long.MIN_VALUE;

    public MemoryMonitor(int ivlMs) {
        this.ivlMs = ivlMs;
    }

    public void start() {
        t = new Thread(() -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                    }

                    maxUsedMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
                    Thread.sleep(ivlMs);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        t.start();
    }

    public long getMaxUsedMem() {
        return maxUsedMem;
    }
    public void stop() {
        t.interrupt();
    }
    public void stopWait() {
        stop();
        try {
            t.join();
        } catch (InterruptedException ignored) {}
    }
}

record File(String type, int size) {
}

class FileGenerator implements Runnable {
    private final BlockingQueue<File> queue;

    public FileGenerator(BlockingQueue<File> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        Random rand = new Random();
        String[] fileTypes = {"JSON", "XML", "XLS"};

        while (true) {
            try {
                Thread.sleep(rand.nextInt(100, 1001));
                String ft = fileTypes[rand.nextInt(fileTypes.length)];
                int fsize = rand.nextInt(10, 101);
                queue.put(new File(ft, fsize));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class FileProcessor implements Runnable {
    private final BlockingQueue<File> queue;
    private final String type;
    public FileProcessor(BlockingQueue<File> queue, String type) {
        this.queue = queue;
        this.type = type;
    }
    @Override
    public void run() {
        while (true) {
            try {
                File file = queue.take();
                if (file.type().equals(type)) {
                    int time = file.size() * 7;
                    Thread.sleep(time);
                    System.out.printf("File type = %s of size %d was processed in %d ms.\n", file.type(), file.size(), time);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}