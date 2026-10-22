package br.com.outsera.razzies.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.outsera.razzies.domain.Producer;

public interface ProducerRepository extends JpaRepository<Producer, Long> {

}
