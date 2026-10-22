package br.com.outsera.razzies.interval;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Producers")
@RequestMapping(path = "/producers", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProducerIntervalController {

	private final ProducerIntervalService service;

	public ProducerIntervalController(ProducerIntervalService service) {
		this.service = service;
	}

	@Operation(summary = "Produtores com menor e maior intervalo entre duas vitórias consecutivas",
			description = "Considera apenas anos distintos de vitória; empates aparecem todos na mesma lista.")
	@GetMapping("/award-intervals")
	public ProducerIntervals awardIntervals() {
		return service.findMinAndMaxIntervals();
	}

}
