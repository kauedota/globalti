package br.com.globalti.prova.domain;

/**
 * Essa enum serve para definir os níveis de acesso do sistema.
 * ADMIN gerencia todo mundo; USER mexe só nos próprios dados.
 */
public enum Role {
    USER,
    ADMIN
}
