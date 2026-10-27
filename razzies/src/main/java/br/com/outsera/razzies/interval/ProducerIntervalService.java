package br.com.outsera.razzies.interval;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.outsera.razzies.repository.MovieRepository;
import br.com.outsera.razzies.repository.ProducerWin;

/**
 * Calcula os produtores com menor e maior intervalo entre duas vitórias consecutivas.
 */
@Service
public class ProducerIntervalService {

	private final MovieRepository movieRepository;

	public ProducerIntervalService(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	// Passagem única: a consulta já agrupa as vitórias por produtor em ordem de ano, então cada vitória só se
	// compara com a anterior. Empates entram na ordem da consulta, que é conferida no próprio laço.
	@Transactional(readOnly = true)
	public ProducerIntervals findMinAndMaxIntervals() {
		List<ProducerInterval> min = new ArrayList<>();
		List<ProducerInterval> max = new ArrayList<>();
		String previousProducer = null;
		int previousYear = 0;

		for (ProducerWin win : movieRepository.findProducerWinsOrderedByProducerAndYear()) {
			if (win.producer().equals(previousProducer)) {
				if (win.year() < previousYear) {
					throw new IllegalStateException("Producer wins must be ordered by year");
				}
			} else if (previousProducer != null && win.producer().compareTo(previousProducer) < 0) {
				// Um produtor que reaparece depois de outro teria vitórias fora do grupo e perderia intervalos.
				throw new IllegalStateException("Producer wins must be grouped by producer");
			}

			// Vitórias no mesmo ano não geram intervalo zero.
			if (win.producer().equals(previousProducer) && win.year() > previousYear) {
				ProducerInterval interval = new ProducerInterval(win.producer(), win.year() - previousYear,
						previousYear, win.year());

				if (min.isEmpty() || interval.interval() < min.getFirst().interval()) {
					min.clear();
				}
				if (min.isEmpty() || interval.interval() == min.getFirst().interval()) {
					min.add(interval);
				}

				if (max.isEmpty() || interval.interval() > max.getFirst().interval()) {
					max.clear();
				}
				if (max.isEmpty() || interval.interval() == max.getFirst().interval()) {
					max.add(interval);
				}
			}
			previousProducer = win.producer();
			previousYear = win.year();
		}
		return new ProducerIntervals(min, max);
	}

}
