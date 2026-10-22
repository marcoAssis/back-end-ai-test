package br.com.outsera.razzies.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Lê o CSV de filmes ({@code year;title;studios;producers;winner}) sem acessar o banco.
 * Qualquer registro inválido lança {@link MovieCsvException} indicando o número do registro (linha de dados,
 * sem contar o cabeçalho).
 */
@Component
public class MovieCsvParser {

	private static final Logger log = LoggerFactory.getLogger(MovieCsvParser.class);

	private static final String YEAR = "year";
	private static final String TITLE = "title";
	private static final String STUDIOS = "studios";
	private static final String PRODUCERS = "producers";
	private static final String WINNER = "winner";

	private static final List<String> REQUIRED_HEADERS = List.of(YEAR, TITLE, STUDIOS, PRODUCERS, WINNER);

	private static final String WINNER_VALUE = "yes";

	// Separadores de produtores: ", and ", ", " e " and ", sem diferenciar maiúsculas; o limite de palavra evita cortar nomes como "Anderson".
	private static final Pattern PRODUCER_SEPARATOR = Pattern.compile(",\\s*and\\b\\s*|,\\s*|\\s+and\\b\\s*",
			Pattern.CASE_INSENSITIVE);

	private static final char BOM = '\uFEFF';

	private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
			.setDelimiter(';')
			.setHeader()
			.setSkipHeaderRecord(true)
			.setIgnoreHeaderCase(true)
			.setIgnoreEmptyLines(true)
			.setTrim(true)
			.get();

	public List<MovieCsvRecord> parse(Resource resource) {
		if (!resource.exists()) {
			throw new MovieCsvException("CSV file not found: " + resource.getDescription());
		}
		try (Reader reader = openSkippingBom(resource); CSVParser parser = FORMAT.parse(reader)) {
			validateHeaders(parser, resource);
			List<MovieCsvRecord> records = new ArrayList<>();
			for (CSVRecord row : parser) {
				records.add(toRecord(row));
			}
			return records;
		}
		catch (IOException | UncheckedIOException | IllegalArgumentException ex) {
			throw new MovieCsvException("Could not read CSV file " + resource.getDescription() + ": " + ex.getMessage(), ex);
		}
	}

	private static Reader openSkippingBom(Resource resource) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
		reader.mark(1);
		if (reader.read() != BOM) {
			reader.reset();
		}
		return reader;
	}

	private static void validateHeaders(CSVParser parser, Resource resource) {
		// getHeaderMap() já ignora maiúsculas por causa de setIgnoreHeaderCase(true).
		Map<String, Integer> headers = parser.getHeaderMap();
		List<String> missing = REQUIRED_HEADERS.stream()
				.filter(header -> !headers.containsKey(header))
				.toList();
		if (!missing.isEmpty()) {
			throw new MovieCsvException("CSV file " + resource.getDescription() + " is missing columns " + missing);
		}
	}

	private static MovieCsvRecord toRecord(CSVRecord row) {
		long number = row.getRecordNumber();
		if (!row.isConsistent()) {
			throw invalidRecord(number,
					"expected " + row.getParser().getHeaderNames().size() + " columns but found " + row.size());
		}
		String title = row.get(TITLE);
		if (title.isEmpty()) {
			throw invalidRecord(number, "title is empty");
		}
		boolean winner = parseWinner(row.get(WINNER), number);
		Set<String> producers = splitProducers(row.get(PRODUCERS));
		// Filme sem produtor é importado, mas não participa do cálculo dos intervalos.
		if (producers.isEmpty()) {
			log.warn("CSV record {} ({}) has no producers and will not count for award intervals", number, title);
		}
		return new MovieCsvRecord(parseYear(row.get(YEAR), number), title, row.get(STUDIOS), winner, producers);
	}

	private static int parseYear(String value, long number) {
		int year;
		try {
			year = Integer.parseInt(value);
		}
		catch (NumberFormatException ex) {
			throw invalidRecord(number, "year '" + value + "' is not a number");
		}
		if (year <= 0) {
			throw invalidRecord(number, "year '" + value + "' must be positive");
		}
		return year;
	}

	// Vazio indica filme não vencedor; qualquer valor além de "yes" é erro, para não descartar vencedores em silêncio.
	private static boolean parseWinner(String value, long number) {
		if (value.isEmpty()) {
			return false;
		}
		if (WINNER_VALUE.equalsIgnoreCase(value)) {
			return true;
		}
		throw invalidRecord(number, "winner '" + value + "' must be '" + WINNER_VALUE + "' or empty");
	}

	private static Set<String> splitProducers(String value) {
		return Arrays.stream(PRODUCER_SEPARATOR.split(value))
				.map(String::trim)
				.filter(name -> !name.isEmpty())
				.collect(Collectors.toUnmodifiableSet());
	}

	private static MovieCsvException invalidRecord(long number, String reason) {
		return new MovieCsvException("Invalid CSV record " + number + ": " + reason);
	}

}
