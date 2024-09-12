package org.example;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) {
        // Задание 1
//        task1();

        // Задание 2
//        try {
//            MemoryMonitor mm = new MemoryMonitor(5);
//            mm.start();
//            long startTime = System.nanoTime();
//            task2FisFos("random_bytes.bin", "random_bytes.copy.bin");
//            long endTime = System.nanoTime();
//            mm.stopWait();
//            long maxUsedMemBytes = mm.getMaxUsedMem();
//            new File("random_bytes.copy.bin").delete();
//
//            System.out.println("FileInputStream/FileOutputStream");
//            System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMemBytes / Math.pow(2, 20)));
//            System.out.println("\tВремя выполнения: " + (endTime - startTime) / Math.pow(10, 6) + " миллисекунд");
//
//            mm.start();
//            startTime = System.nanoTime();
//            task2FileChan("random_bytes.bin", "random_bytes.copy.bin");
//            endTime = System.nanoTime();
//            mm.stopWait();
//            maxUsedMemBytes = mm.getMaxUsedMem();
//            new File("random_bytes.copy.bin").delete();
//
//            System.out.println("FileChannel");
//            System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMemBytes / Math.pow(2, 20)));
//            System.out.println("\tВремя выполнения: " + (endTime - startTime) / Math.pow(10, 6) + " миллисекунд");
//
//            mm.start();
//            startTime = System.nanoTime();
//            task2ApacheCommonsIO("random_bytes.bin", "random_bytes.copy.bin");
//            endTime = System.nanoTime();
//            mm.stopWait();
//            maxUsedMemBytes = mm.getMaxUsedMem();
//            new File("random_bytes.copy.bin").delete();
//
//            System.out.println("Apache Commons IO");
//            System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMemBytes / Math.pow(2, 20)));
//            System.out.println("\tВремя выполнения: " + (endTime - startTime) / Math.pow(10, 6) + " миллисекунд");
//
//            mm.start();
//            startTime = System.nanoTime();
//            task2FilesClassNio("random_bytes.bin", "random_bytes.copy.bin");
//            endTime = System.nanoTime();
//            mm.stopWait();
//            maxUsedMemBytes = mm.getMaxUsedMem();
//            new File("random_bytes.copy.bin").delete();
//
//            System.out.println("Files Class nio");
//            System.out.printf("\tMax used mem: %d MiB", (long)(maxUsedMemBytes / Math.pow(2, 20)));
//            System.out.println("\tВремя выполнения: " + (endTime - startTime) / Math.pow(10, 6) + " миллисекунд");
//        } catch (IOException e) {
//            System.err.print(e);
//            System.exit(1);
//        }

        // Задание 3
        try {
            String fileName = "bratushkadan-ns.yaml";
            int hs = calcHashSum(fileName);
            System.out.printf("16-bit hash-sum of \"%s\": %d\n", fileName, hs);
            fileName = "pom.xml";
            hs = calcHashSum(fileName);
            System.out.printf("16-bit hash-sum of \"%s\": %d\n", fileName, hs);

            fileName = "random_bytes.bin";
            hs = calcHashSum(fileName);
            System.out.printf("16-bit hash-sum of \"%s\": %d\n", fileName, hs);
        } catch (IOException e) {
            System.exit(1);
        }

        // Задание 4
//        try {
//            new DirectoryWatcher().run();
//        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
    }

    private static void task1() {
        System.out.println("=== Задание 1");
        String fileName = "bratushkadan-ns.yaml";

        // записываем
        Path filePath = Paths.get(fileName);
        try {
            Files.write(filePath, List.of("apiVersion: v1\nkind: Namespace\nmetadata:\n  name: bratushkadan".split("\n")));
        } catch (IOException e) {
            System.err.printf("Error writing to %s: %s\n:", filePath, e.getMessage());
        }

        // читаем
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String l : lines) {
                System.out.println(l);
            }
        } catch (IOException e) {
            System.err.printf("Error reading file %s: %s\n", filePath, e.getMessage());
        }
    }

    public static void task2FisFos(String source, String destination) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void task2FileChan(String source, String destination) throws IOException {
        try (FileChannel srcChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(destination).getChannel()) {
            srcChannel.transferTo(0, srcChannel.size(), destChannel);
        }
    }

    public static void task2ApacheCommonsIO(String source, String destination) throws IOException {
        FileUtils.copyFile(new File(source), new File(destination));
    }


    public static void task2FilesClassNio(String source, String destination) throws IOException {
        Files.copy(Paths.get(source), Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
    }

    public static int calcHashSum(String file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(2048);
            int checksum = 0;

            // Разбиваем данные на 16-битные значения и суммируем их с проверкой переноса.
            while (fileChannel.read(buffer) != -1 || buffer.position() > 0) {
                buffer.flip();
                while (buffer.remaining() >= 2) {
                    short value = buffer.getShort();
                    checksum += (value & 0xFFFF);
                    if ((checksum & 0x10000) != 0) {
                        checksum = (checksum & 0xFFFF) + 1;
                    }
                }

                if (buffer.remaining() == 1) {
                    byte lastByte = buffer.get();
                    checksum += (lastByte & 0xFF);
                    if ((checksum & 0x10000) != 0) {
                        checksum = (checksum & 0xFFFF) + 1;
                    }
                }
                buffer.clear();
            }

            return (checksum & 0xFFFF);
        }
    }
}

class DirectoryWatcher {
    private static Map<Path, List<String>> files = new HashMap<>();
    private static Map<Path, String> fileHashSums = new HashMap<>();

    public void run() throws InterruptedException, IOException {
        Path directory = Paths.get("watch-dir");
        // Создаем watchService
        WatchService watchService = FileSystems.getDefault().newWatchService();
        // Регистрируем в watchService каталог с типом наблюдаемых событий CREATE|MODIFY
        directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path filePath = (Path)(event.context());
                    System.out.println(filePath);
                    System.out.printf("A new file was created: %s\n", filePath);
                    files.put(filePath, readlines(directory.resolve(filePath)));
                    try {
                        fileHashSums.put(filePath, Integer.toString(Main.calcHashSum(directory.resolve(filePath).toString())));
                    } catch (IOException ignored) {}
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    Path filePath = (Path)(event.context());
                    System.out.printf("File was modified: %s\n", filePath);
                    processFileModification(directory.resolve(filePath));
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    Path filePath = (Path)(event.context());
                    System.out.println("File was deleted: " + filePath);
                    String hash = fileHashSums.get(directory.resolve(filePath));
                    if (hash != null) {
                        System.out.println("Its hash sum: " + hash);
                    }
                }
            }
            key.reset();
        }
    }

    private void processFileModification(Path filePath) {
        List<String> newFileContents = readlines(filePath);
        final List<String> oldFileContents = files.get(filePath) != null ? files.get(filePath) : new ArrayList<String>();

        List<String> addedLines = newFileContents.stream()
                .filter(line -> !oldFileContents.contains(line))
                .toList();
        List<String> deletedLines = oldFileContents.stream()
                .filter(line -> !newFileContents.contains(line))
                .toList();
        if (!addedLines.isEmpty()) {
            System.out.printf("Lines that were added to the file %s :\n", filePath);
            addedLines.forEach(line -> System.out.println("+ " + line));
        }
        if (!deletedLines.isEmpty()) {
            System.out.printf("Lines that were deleted from the file %s :\n", filePath);
            deletedLines.forEach(line -> System.out.println("- " + line));
        }

        files.put(filePath, newFileContents);
        try {
            fileHashSums.put(filePath, Integer.toString(Main.calcHashSum(filePath.toString())));
        } catch (IOException ignored) {}
    }

    private List<String> readlines(Path filePath) {
        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return new ArrayList<String>();
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