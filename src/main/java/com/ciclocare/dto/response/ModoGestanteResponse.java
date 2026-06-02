package com.ciclocare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModoGestanteResponse {
	private boolean modoGestante;
	private LocalDate ultimaMenstruacao;
	private LocalDate previsaoParto;

	private Integer semanaAtual;
	private Integer semanasCompletas;
	private Integer diasSemana;

	private Integer diasGravidez;
	private Integer meses;
	private Integer semanasMes;

	private Integer diasRestantes;
	private Integer semanasRestantes;
	private Integer diasRestantesSemana;

	private String mensagemPrincipal;
	private String mensagemSecundaria;
}
