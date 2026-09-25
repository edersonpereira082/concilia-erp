package br.com.conciliacao.domain;

import java.util.Locale;

public final class Texto {
  private static final Locale PT = Locale.forLanguageTag("pt-BR");

  private Texto() {}

  public static String maiusculo(String valor) {
    return valor == null ? null : valor.toUpperCase(PT);
  }
}
