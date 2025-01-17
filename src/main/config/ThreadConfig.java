package main.config;


/**
 * @param frequency частота работы потока в секундах
 * @param delay длительность работы одной итерации - имитирует выполнение полезной нагрузки
 */
public record ThreadConfig(int frequency, int delay) {}
