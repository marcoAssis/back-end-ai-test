package br.com.outsera.razzies;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Garante que a documentação OpenAPI usada pelo Swagger UI expõe o endpoint de intervalos.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTests {

	@Autowired
	MockMvcTester mvc;

	@Test
	void documentsAwardIntervalsEndpoint() {
		assertThat(mvc.get().uri("/v3/api-docs"))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$.paths['/producers/award-intervals'].get.responses['200']")
				.isNotNull();
	}

	@Test
	void servesSwaggerUi() {
		assertThat(mvc.get().uri("/swagger-ui.html"))
				.hasStatus3xxRedirection()
				.hasRedirectedUrl("/swagger-ui/index.html");
	}

}
