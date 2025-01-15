package main;

import main.config.WorkConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class ThreadManager {

    private final DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Random random = new Random();
    private final WorkConfig workConfig;
    private final List<ReentrantLock> locks;
    private final ScheduledExecutorService executorService;

    public ThreadManager(WorkConfig workConfig) {
        this.workConfig = workConfig;
        this.locks = Stream.generate(ReentrantLock::new)
                           .limit(workConfig.lockListSize())
                           .toList();
        this.executorService = Executors.newScheduledThreadPool(workConfig.threadConfigs().size());
    }

    public void doWork() {
        workConfig.threadConfigs()
                  .forEach(config -> {
                      Runnable runnable = prepareRunnable(config.delay(), config.frequency());
                      executorService.scheduleAtFixedRate(runnable, 0, config.frequency(), TimeUnit.SECONDS);
                  });
    }

    private Runnable prepareRunnable(int delay, int frequency) {
        return () -> {
            var targetLocks = getRandomLocks();
            System.out.printf("%s пытается получить блокировки %s %n", getPrefix(delay, frequency), targetLocks);
            if (lockAllIfPossible(targetLocks)) {
                System.out.printf("%s получил блокировки %s %n", getPrefix(delay, frequency), targetLocks);
                try {
                    Thread.sleep(delay * 1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    unlockByList(targetLocks);
                }
                System.out.printf("%s закончил работу %n", getPrefix(delay, frequency));
            } else {
                //все нужные блокировки получить не смогли
                //пропускаем итерацию, попробуем в следующей
                System.out.printf("%s не удалось получить блокировки, попробуем в следующую итерацию %s %n", getPrefix(delay, frequency), targetLocks);
            }
        };
    }

    private boolean lockAllIfPossible(List<ReentrantLock> targetLocks) {
        var successLocked = targetLocks.stream()
                                       .takeWhile(Lock::tryLock)
                                       .toList();
        boolean allSuccess = successLocked.size() == targetLocks.size();
        if (!allSuccess) {
            //освободим, если что то успели захватить
            unlockByList(successLocked);
        }
        return allSuccess;
    }

    private List<ReentrantLock> getRandomLocks() {
        return random.ints(workConfig.lockToHold(), 0, locks.size() - 1)
                     .mapToObj(locks::get)
                     .toList();
    }

    private void unlockByList(List<ReentrantLock> locks) {
        locks.forEach(ReentrantLock::unlock);
    }

    private String getPrefix(int delay, int frequency) {
        return "[%s; Поток с ид %s; частота %s; длительность %s]".formatted(dateTimeFormatter.format(LocalDateTime.now()), Thread.currentThread().threadId(), frequency, delay);
    }

}
