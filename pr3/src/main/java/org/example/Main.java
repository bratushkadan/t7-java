package org.example;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Main {
    public static void main(String[] args) {
        // 1.
//        new Sensors().run();

        // 2.
        //      2.1.2
//        Streams.run212();
        //      2.2.2
//        Streams.run222();
        ///     2.3.2
//        Streams.run232();

        // 3.
//        UsFr.run();

        // 4.
//        FileProccessingSystem.run();
    }
}

// Task 1

class Sensors {
    public static void run() {
        Observable<Integer> temperatureSensor = Observable.interval(1, TimeUnit.SECONDS)
                .map(tick -> 15 + (int)(Math.random() * 16))
                .doOnNext(temp -> System.out.printf("Temperature: %d ºC\n", temp));

        Observable<Integer> co2Sensor = Observable.interval(1, TimeUnit.SECONDS)
                .map(tick -> 30 + (int)(Math.random() * 71))
                .doOnNext(co2 -> System.out.printf("CO2: %d ppm\n", co2));

        Observable.combineLatest(temperatureSensor, co2Sensor, (temp, co2) -> {
            if (temp > 25 && co2 > 70) {
                return "ALARM!!!";
            } else if (temp > 25) {
                return "Warning! Temperature is: " + temp + "°C";
            } else if (co2 > 70) {
                return "Warning! CO2 is: " + co2 + " ppm";
            }
            return "Everything's normal.";
        }).subscribe(System.out::println);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Task 2
class Streams {
    public static void run212() {
        Observable.range(0, 1000)
                .map(i -> ThreadLocalRandom.current().nextInt(0, 1001))
                .filter(number -> number > 500)
                .subscribe(System.out::println);
    }

    public static void run222() {
        Observable<Integer> s1 = Observable.range(0, 3); // 1 2 3
        Observable<Integer> s2 = Observable.range(4, 3); // 4 5 6
        Observable<Integer> mergedStream = Observable.concat(s1, s2);
        mergedStream.subscribe(System.out::println);
    }

    public static void run232() {
        Observable<Integer> randStream = Observable
                .range(0, 10)
                .map(i -> ThreadLocalRandom.current().nextInt(1, 501));
        Observable<Integer> cutRandStream = randStream.take(5);
        cutRandStream.subscribe(System.out::println);
    }
}

// Task 3
record UserFriend(int userId, int friendId) {
}

class UsFr {
    public static Observable<UserFriend> getFriends(int userId) {
        return Observable.fromArray(
                new UserFriend(userId, (int) (ThreadLocalRandom.current().nextInt(1, 50))),
                new UserFriend(userId, (int) (ThreadLocalRandom.current().nextInt(1, 50))),
                new UserFriend(userId, (int) (ThreadLocalRandom.current().nextInt(1, 50)))
        );
    }
    public static void run() {
        Observable<UserFriend> userFriendStream = Observable
                .range(1, 5)
                .flatMap(UsFr::getFriends);

        userFriendStream.subscribe(userFriend -> {
            System.out.println("User: " + userFriend.userId() + ", Friend: " + userFriend.friendId());
        });
    }
}

// Task 4
record File(String type, int size) {
}

class FileGenerator {
    private final String[] fileTypes = {"JSON", "XML", "XLS"};

    private final Random random = new Random();

    public Observable<File> generate() {
        return Observable
                .interval(
                        random.nextInt(100, 1001),
                        TimeUnit.MILLISECONDS)
                .map(
                        t -> new File(
                                fileTypes[random.nextInt(fileTypes.length)],
                                random.nextInt(10, 101))
                )
                // Returns a default, shared Scheduler instance intended for IO-bound work.
                // This can be used for asynchronously performing blocking IO.
                .subscribeOn(Schedulers.io())
                // Modifies an ObservableSource to perform its emissions and notifications on a
                // specified Scheduler, asynchronously with an unbounded buffer with Flowable.bufferSize() "island size".
                .observeOn(Schedulers.io());
    }
}

class FileProcessor {
    private final String type;
    public FileProcessor(String type) {
        this.type = type;
    }

    public void process(File file) {
            try {
                if (file.type().equals(type)) {
                    int time = file.size() * 7;
                    Thread.sleep(time);
                    System.out.printf("File type = %s of size %d was processed in %d ms.\n", file.type(), file.size(), time);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
}

class FileProccessingSystem {
    public static void run() {
        FileGenerator gen = new FileGenerator();
        BlockingQueue<File> queue = new LinkedBlockingQueue<>(5);

        FileProcessor jsonProcessor = new FileProcessor("JSON");
        FileProcessor xmlProcessor = new FileProcessor("XML");
        FileProcessor xlsProcessor = new FileProcessor("XLS");

        gen.generate()
                .subscribe(file -> {
                    try {
                        queue.put(file);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        // можно было бы за'pipe'ить с предыдущим потоком,
        // но условие требует наличие очереди для контроля пропускной способности
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .subscribe(tick -> {
                    try {
                        File file = queue.take();
                        xmlProcessor.process(file);
                        jsonProcessor.process(file);
                        xlsProcessor.process(file);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}
    }
}