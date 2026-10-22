package br.com.outsera.razzies.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "producer", uniqueConstraints = @UniqueConstraint(name = "uk_producer_name", columnNames = "name"))
public class Producer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	protected Producer() {
	}

	public Producer(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Producer name must not be blank");
		}
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	// O nome é a chave natural e imutável; getName() mantém a comparação correta com proxies do Hibernate.
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return o instanceof Producer other && Objects.equals(name, other.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		return "Producer[id=" + id + ", name=" + name + "]";
	}

}
