package br.com.outsera.razzies.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.outsera.razzies.domain.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

	@Query("""
			select new br.com.outsera.razzies.repository.ProducerWin(p.name, m.releaseYear)
			from Movie m join m.producers p
			where m.winner = true
			order by p.name, m.releaseYear""")
	List<ProducerWin> findProducerWins();

}
