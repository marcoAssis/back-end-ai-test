package br.com.outsera.razzies.interval;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/producers", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProducerIntervalController {

	private final ProducerIntervalService service;

	public ProducerIntervalController(ProducerIntervalService service) {
		this.service = service;
	}

	@GetMapping("/award-intervals")
	public ProducerIntervals awardIntervals() {
		return service.findMinAndMaxIntervals();
	}

}
