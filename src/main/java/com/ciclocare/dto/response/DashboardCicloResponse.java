package com.ciclocare.dto.response;

import com.ciclocare.enums.FaseCiclo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCicloResponse {
	private Integer diaCiclo;
	private FaseCiclo faseCiclo;
	private String mensagem; // remover
	private LocalDate ultimaMenstruacao;
	private Integer duracaoCiclo;
	private Integer duracaoMenstruacao;
	private LocalDate proximaPrevisao;
	private LocalDate previsaoOvulacao;
	private LocalDate janelaFertilInicio;
	private LocalDate janelaFertilFim;
	private Long quantidadeCiclos;
	private boolean menosDe3Ciclos;
}
