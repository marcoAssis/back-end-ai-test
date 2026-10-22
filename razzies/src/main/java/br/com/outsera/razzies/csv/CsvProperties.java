package br.com.outsera.razzies.csv;

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Configuração do CSV carregado na inicialização.
 *
 * @param path arquivo de filmes, por exemplo {@code classpath:Movielist.csv} ou {@code file:/dados/filmes.csv}
 */
@ConfigurationProperties("razzies.csv")
public record CsvProperties(Resource path) {

	public CsvProperties {
		Objects.requireNonNull(path, "Property razzies.csv.path is required");
	}

}
