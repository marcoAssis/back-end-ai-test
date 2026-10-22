package br.com.outsera.razzies.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "producer",
		uniqueConstraints = @UniqueConstraint(name = "uk_producer_normalized_name", columnNames = "normalized_name"))
public class Producer {

	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Grafia original com espaços normalizados, usada no retorno da API.
	@Column(nullable = false)
	private String name;

	// Identidade do produtor: ignora maiúsculas e espaços repetidos.
	@Column(name = "normalized_name", nullable = false)
	private String normalizedName;

	protected Producer() {
	}

	public Producer(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Producer name must not be blank");
		}
		this.name = collapseSpaces(name);
		this.normalizedName = normalize(name);
	}

	/**
	 * Chave de comparação de nomes: sem espaços nas pontas, espaços internos colapsados e minúsculas.
	 */
	public static String normalize(String name) {
		return collapseSpaces(name).toLowerCase(Locale.ROOT);
	}

	private static String collapseSpaces(String name) {
		return WHITESPACE.matcher(name.strip()).replaceAll(" ");
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	// O nome normalizado é a chave natural e imutável; o getter mantém a comparação correta com proxies do Hibernate.
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return o instanceof Producer other && Objects.equals(normalizedName, other.getNormalizedName());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(normalizedName);
	}

	@Override
	public String toString() {
		return "Producer[id=" + id + ", name=" + name + "]";
	}

}
