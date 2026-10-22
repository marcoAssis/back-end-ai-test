package br.com.outsera.razzies.interval;

import java.util.List;

/**
 * Menores e maiores intervalos entre vitórias consecutivas, com todos os empatados de cada lista.
 */
public record ProducerIntervals(List<ProducerInterval> min, List<ProducerInterval> max) {

	public ProducerIntervals {
		min = List.copyOf(min);
		max = List.copyOf(max);
	}

}
