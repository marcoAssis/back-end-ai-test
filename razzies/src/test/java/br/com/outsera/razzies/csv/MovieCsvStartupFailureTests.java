package br.com.outsera.razzies.csv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.SpringApplication;

import br.com.outsera.razzies.RazziesApplication;

/**
 * Testes de integração da inicialização: um CSV inválido deve impedir a aplicação de subir.
 */
class MovieCsvStartupFailureTests {

	@ParameterizedTest
	// Aspas duplas como delimitador de texto, porque as mensagens esperadas contêm aspas simples.
	@CsvSource(delimiter = '|', quoteCharacter = '"', textBlock = """
			csv/invalid/invalid-winner.csv    | Invalid CSV record 2: winner 'true' must be 'yes' or empty
			csv/invalid/non-positive-year.csv | Invalid CSV record 2: year '0' must be positive
			csv/invalid/non-numeric-year.csv  | Invalid CSV record 2: year 'abc' is not a number
			csv/invalid/missing-column.csv    | Invalid CSV record 2: expected 5 columns but found 4
			csv/invalid/empty-title.csv       | Invalid CSV record 1: title is empty
			csv/invalid/missing-header.csv    | is missing columns [studios]
			csv/invalid/duplicate-header.csv  | The header contains a duplicate name
			csv/invalid/unclosed-quote.csv    | EOF reached before encapsulated token finished
			csv/invalid/does-not-exist.csv    | CSV file not found
			""")
	void failsStartupWithInvalidCsv(String csv, String expectedMessage) {
		SpringApplication application = new SpringApplication(RazziesApplication.class);

		// try-with-resources fecha o contexto se uma regressão deixar a aplicação subir.
		assertThatThrownBy(() -> {
			try (var context = application.run("--spring.main.web-application-type=none",
					"--razzies.csv.path=classpath:" + csv)) {
			}
		})
				.isInstanceOf(MovieCsvException.class)
				.hasMessageContaining(expectedMessage);
	}

}
