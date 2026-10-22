package br.com.outsera.razzies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RazziesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RazziesApplication.class, args);
	}

}
