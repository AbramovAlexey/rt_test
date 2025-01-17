package main.config;

import java.util.List;
import java.util.Objects;

/**
 * @param lockListSize  общее количество ReentrantLock
 * @param lockToHold    количество блокировок, которое попытается захватить каждый поток
 * @param threadConfigs настройки потоков - частота работы, и "длительность", размер списка по сути задает количество будущих потоков
 */
public record WorkConfig(int lockListSize, int lockToHold, List<ThreadConfig> threadConfigs) {

    public WorkConfig {
        if (lockListSize < lockToHold) {
            throw new IllegalArgumentException("Общее количество блокировок меньше, чем попытается захватить один поток");
        }
        if (Objects.isNull(threadConfigs) || threadConfigs.isEmpty()) {
            throw new IllegalArgumentException("Нужна настройка хотя бы для одного потока");
        }
    }

}
