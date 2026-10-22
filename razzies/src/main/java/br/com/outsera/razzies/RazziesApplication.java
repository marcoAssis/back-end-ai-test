package br.com.outsera.razzies;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(info = @Info(title = "Razzies API", version = "1.0",
		description = "Intervalos entre prêmios de Pior Filme do Golden Raspberry Awards"))
public class RazziesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RazziesApplication.class, args);
	}

}
