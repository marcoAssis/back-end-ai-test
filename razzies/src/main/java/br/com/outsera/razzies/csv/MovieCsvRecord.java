package br.com.outsera.razzies.csv;

import java.util.Set;

/**
 * Linha do CSV já validada, com os nomes de produtores separados.
 */
public record MovieCsvRecord(int year, String title, String studios, boolean winner, Set<String> producers) {

	public MovieCsvRecord {
		producers = Set.copyOf(producers);
	}

}
