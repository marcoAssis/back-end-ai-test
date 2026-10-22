package br.com.outsera.razzies.interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

	@Transactional(readOnly = true)
	public ProducerIntervals findMinAndMaxIntervals() {
		List<ProducerInterval> intervals = consecutiveIntervals(movieRepository.findProducerWins());
		if (intervals.isEmpty()) {
			return new ProducerIntervals(List.of(), List.of());
		}
		int min = intervals.stream().mapToInt(ProducerInterval::interval).min().getAsInt();
		int max = intervals.stream().mapToInt(ProducerInterval::interval).max().getAsInt();
		return new ProducerIntervals(withInterval(intervals, min), withInterval(intervals, max));
	}

	// Resultado ordenado por produtor e ano da vitória anterior, porque o TreeMap e o TreeSet já iteram nessa ordem.
	private static List<ProducerInterval> consecutiveIntervals(List<ProducerWin> wins) {
		// Anos distintos por produtor: vitórias no mesmo ano não geram intervalo zero.
		Map<String, TreeSet<Integer>> yearsByProducer = wins.stream()
				.collect(Collectors.groupingBy(ProducerWin::producer, TreeMap::new,
						Collectors.mapping(ProducerWin::year, Collectors.toCollection(TreeSet::new))));

		List<ProducerInterval> intervals = new ArrayList<>();
		yearsByProducer.forEach((producer, years) -> {
			Integer previous = null;
			for (int year : years) {
				if (previous != null) {
					intervals.add(new ProducerInterval(producer, year - previous, previous, year));
				}
				previous = year;
			}
		});
		return intervals;
	}

	private static List<ProducerInterval> withInterval(List<ProducerInterval> intervals, int interval) {
		return intervals.stream()
				.filter(candidate -> candidate.interval() == interval)
				.toList();
	}

}
