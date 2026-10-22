package br.com.outsera.razzies.interval;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Testes de integração do endpoint de intervalos: contexto completo, importação real do CSV e H2 em memória.
 * Cada classe aninhada com {@link TestPropertySource} carrega outro CSV em um contexto próprio.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProducerIntervalControllerTests {

	private static final String URI = "/producers/award-intervals";

	private static void assertIntervals(MockMvcTester mvc, String expectedJson) {
		assertThat(mvc.get().uri(URI))
				.hasStatusOk()
				.hasContentType(MediaType.APPLICATION_JSON)
				.bodyJson()
				.isStrictlyEqualTo(expectedJson);
	}

	@Nested
	class OriginalCsv {

		@Autowired
		MockMvcTester mvc;

		@Test
		void returnsMinAndMaxIntervalsFromProvidedCsv() {
			assertIntervals(mvc, """
					{
					  "min": [
					    { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
					  ],
					  "max": [
					    { "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }
					  ]
					}
					""");
		}

		@Test
		void rejectsMethodsOtherThanGet() {
			assertThat(mvc.post().uri(URI))
					.hasStatus(HttpStatus.METHOD_NOT_ALLOWED)
					.hasHeader("Allow", "GET");
		}

		@Test
		void rejectsNonJsonAcceptHeader() {
			assertThat(mvc.get().uri(URI).accept(MediaType.APPLICATION_XML))
					.hasStatus(HttpStatus.NOT_ACCEPTABLE);
		}

	}

	@Nested
	@TestPropertySource(properties = "razzies.csv.path=classpath:csv/ties.csv")
	class TiedIntervals {

		@Autowired
		MockMvcTester mvc;

		@Test
		void returnsEveryProducerTiedOnMinAndMax() {
			assertIntervals(mvc, """
					{
					  "min": [
					    { "producer": "Producer A", "interval": 1, "previousWin": 2000, "followingWin": 2001 },
					    { "producer": "Producer B", "interval": 1, "previousWin": 2010, "followingWin": 2011 }
					  ],
					  "max": [
					    { "producer": "Producer C", "interval": 10, "previousWin": 1990, "followingWin": 2000 },
					    { "producer": "Producer D", "interval": 10, "previousWin": 2005, "followingWin": 2015 }
					  ]
					}
					""");
		}

	}

	@Nested
	@TestPropertySource(properties = "razzies.csv.path=classpath:csv/same-year.csv")
	class WinsInSameYear {

		@Autowired
		MockMvcTester mvc;

		@Test
		void ignoresRepeatedYearsInsteadOfReturningZeroInterval() {
			assertIntervals(mvc, """
					{
					  "min": [
					    { "producer": "Producer X", "interval": 3, "previousWin": 2000, "followingWin": 2003 }
					  ],
					  "max": [
					    { "producer": "Producer X", "interval": 3, "previousWin": 2000, "followingWin": 2003 }
					  ]
					}
					""");
		}

	}

	@Nested
	@TestPropertySource(properties = "razzies.csv.path=classpath:csv/three-wins.csv")
	class MoreThanTwoWins {

		@Autowired
		MockMvcTester mvc;

		@Test
		void usesOnlyConsecutiveWins() {
			assertIntervals(mvc, """
					{
					  "min": [
					    { "producer": "Producer W", "interval": 1, "previousWin": 2001, "followingWin": 2002 }
					  ],
					  "max": [
					    { "producer": "Producer Z", "interval": 8, "previousWin": 2002, "followingWin": 2010 }
					  ]
					}
					""");
		}

	}

	@Nested
	@TestPropertySource(properties = "razzies.csv.path=classpath:csv/no-intervals.csv")
	class NoProducerWithTwoWins {

		@Autowired
		MockMvcTester mvc;

		@Test
		void returnsEmptyLists() {
			assertIntervals(mvc, """
					{ "min": [], "max": [] }
					""");
		}

	}

	@Nested
	@TestPropertySource(properties = "razzies.csv.path=classpath:csv/normalized-names.csv")
	class ProducerNameVariations {

		@Autowired
		MockMvcTester mvc;

		@Test
		void groupsNamesIgnoringCaseAndSpacesAndReturnsFirstSpelling() {
			assertIntervals(mvc, """
					{
					  "min": [
					    { "producer": "Ann", "interval": 1, "previousWin": 2000, "followingWin": 2001 },
					    { "producer": "Joel Silver", "interval": 1, "previousWin": 2000, "followingWin": 2001 }
					  ],
					  "max": [
					    { "producer": "Bob", "interval": 3, "previousWin": 2000, "followingWin": 2003 }
					  ]
					}
					""");
		}

	}

	@Nested
	@TestPropertySource(properties = "razzies.csv.path=classpath:csv/bom-crlf.csv")
	class BomAndWindowsLineEndings {

		@Autowired
		MockMvcTester mvc;

		@Test
		void readsFileWithBomCrlfBlankLinesAndUppercaseValues() {
			assertIntervals(mvc, """
					{
					  "min": [
					    { "producer": "Producer A", "interval": 4, "previousWin": 2000, "followingWin": 2004 }
					  ],
					  "max": [
					    { "producer": "Producer A", "interval": 4, "previousWin": 2000, "followingWin": 2004 }
					  ]
					}
					""");
		}

	}

}
