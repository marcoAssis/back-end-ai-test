package br.com.outsera.razzies.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.outsera.razzies.domain.Movie;
import br.com.outsera.razzies.domain.Producer;
import br.com.outsera.razzies.repository.MovieRepository;
import br.com.outsera.razzies.repository.ProducerRepository;

/**
 * Importa o CSV configurado em {@code razzies.csv.path} na inicialização. Qualquer falha interrompe o startup
 * e a transação desfaz a importação parcial.
 * <p>
 * Roda em {@link SmartInitializingSingleton}, depois que todos os beans foram criados e antes de o servidor web
 * aceitar requisições; um {@code ApplicationRunner} rodaria com a porta já aberta e a API responderia sem dados.
 */
@Component
public class MovieCsvLoader implements SmartInitializingSingleton {

	private static final Logger log = LoggerFactory.getLogger(MovieCsvLoader.class);

	private final CsvProperties properties;
	private final MovieCsvParser parser;
	private final MovieRepository movieRepository;
	private final ProducerRepository producerRepository;
	private final TransactionTemplate transactionTemplate;

	public MovieCsvLoader(CsvProperties properties, MovieCsvParser parser, MovieRepository movieRepository,
			ProducerRepository producerRepository, PlatformTransactionManager transactionManager) {
		this.properties = properties;
		this.parser = parser;
		this.movieRepository = movieRepository;
		this.producerRepository = producerRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	// Transação explícita: este callback não passa pelo proxy de @Transactional.
	@Override
	public void afterSingletonsInstantiated() {
		transactionTemplate.executeWithoutResult(status -> importMovies());
	}

	private void importMovies() {
		List<MovieCsvRecord> records = parser.parse(properties.path());

		// Um único Producer por nome normalizado, compartilhado entre os filmes; mantém a grafia da primeira ocorrência.
		Map<String, Producer> producersByName = new HashMap<>();
		List<Movie> movies = new ArrayList<>(records.size());
		for (MovieCsvRecord record : records) {
			movies.add(toMovie(record, producersByName));
		}

		producerRepository.saveAll(producersByName.values());
		movieRepository.saveAll(movies);
		log.info("Imported {} movies and {} producers from {}", movies.size(), producersByName.size(),
				properties.path().getDescription());
	}

	private static Movie toMovie(MovieCsvRecord record, Map<String, Producer> producersByName) {
		Set<Producer> producers = new HashSet<>();
		for (String name : record.producers()) {
			producers.add(producersByName.computeIfAbsent(Producer.normalize(name), key -> new Producer(name)));
		}
		return new Movie(record.year(), record.title(), record.studios(), record.winner(), producers);
	}

}
