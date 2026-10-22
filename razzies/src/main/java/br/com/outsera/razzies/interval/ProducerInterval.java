package br.com.outsera.razzies.interval;

/**
 * Intervalo entre duas vitórias consecutivas de um produtor.
 */
public record ProducerInterval(String producer, int interval, int previousWin, int followingWin) {
}
