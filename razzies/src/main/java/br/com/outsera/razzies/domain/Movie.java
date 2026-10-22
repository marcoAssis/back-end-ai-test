package br.com.outsera.razzies.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "movie")
public class Movie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// "year" é palavra reservada no H2.
	@Column(name = "release_year", nullable = false)
	private int releaseYear;

	@Column(nullable = false)
	private String title;

	private String studios;

	@Column(nullable = false)
	private boolean winner;

	@ManyToMany
	@JoinTable(name = "movie_producer",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "producer_id"))
	private Set<Producer> producers = new HashSet<>();

	protected Movie() {
	}

	public Movie(int releaseYear, String title, String studios, boolean winner, Set<Producer> producers) {
		this.releaseYear = releaseYear;
		this.title = title;
		this.studios = studios;
		this.winner = winner;
		this.producers = new HashSet<>(producers);
	}

	public Long getId() {
		return id;
	}

	public int getReleaseYear() {
		return releaseYear;
	}

	public String getTitle() {
		return title;
	}

	public String getStudios() {
		return studios;
	}

	public boolean isWinner() {
		return winner;
	}

	public Set<Producer> getProducers() {
		return Collections.unmodifiableSet(producers);
	}

	// Sem chave natural: identidade pelo id gerado e hashCode constante, estável antes e depois do persist.
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return o instanceof Movie other && id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Movie.class.hashCode();
	}

	@Override
	public String toString() {
		return "Movie[id=" + id + ", releaseYear=" + releaseYear + ", title=" + title + ", winner=" + winner + "]";
	}

}
