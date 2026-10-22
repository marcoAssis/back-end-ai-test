package br.com.outsera.razzies.csv;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Testes de integração da importação: confere no banco o que o carregamento do CSV gravou na inicialização.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MovieCsvImportTests {

	@Nested
	class OriginalCsv {

		@Autowired
		JdbcTemplate jdbc;

		@Test
		void importsEveryMovieAndDistinctProducer() {
			assertThat(count("select count(*) from movie")).isEqualTo(206);
			assertThat(count("select count(*) from movie where winner")).isEqualTo(42);
			assertThat(count("select count(*) from producer")).isEqualTo(359);
			assertThat(count("select count(distinct normalized_name) from producer")).isEqualTo(359);
			assertThat(count("select count(*) from movie_producer")).isEqualTo(472);
		}

		private long count(String sql) {
			return jdbc.queryForObject(sql, Long.class);
		}

	}

	@Nested
	@TestPropertySource(properties = "razzies.csv.path=classpath:csv/normalized-names.csv")
	class ProducerNameVariations {

		@Autowired
		JdbcTemplate jdbc;

		@Test
		void storesOneProducerPerNormalizedNameWithFirstSpellingAndCollapsedSpaces() {
			assertThat(jdbc.queryForList("select name from producer order by name", String.class))
					.containsExactly("Ann", "Bob", "Joel Silver");
		}

		@Test
		void importsMovieWithoutProducers() {
			assertThat(jdbc.queryForObject("select count(*) from movie", Long.class)).isEqualTo(4);
			assertThat(jdbc.queryForObject("""
					select count(*) from movie m
					where not exists (select 1 from movie_producer mp where mp.movie_id = m.id)""", Long.class))
					.isEqualTo(1);
		}

	}

}
