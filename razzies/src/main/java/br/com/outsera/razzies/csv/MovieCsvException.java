package br.com.outsera.razzies.csv;

/**
 * CSV de filmes ausente, ilegível ou com linha inválida. Interrompe a inicialização da aplicação.
 */
public class MovieCsvException extends RuntimeException {

	public MovieCsvException(String message) {
		super(message);
	}

	public MovieCsvException(String message, Throwable cause) {
		super(message, cause);
	}

}
