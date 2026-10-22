package br.com.outsera.razzies.repository;

/**
 * Vitória de um produtor em um ano: um filme vencedor com N produtores gera N vitórias.
 */
public record ProducerWin(String producer, int year) {
}
