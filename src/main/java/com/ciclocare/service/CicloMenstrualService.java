package com.ciclocare.service;

import com.ciclocare.dto.request.CicloMenstrualRequest;
import com.ciclocare.dto.response.CicloMenstrualResponse;
import com.ciclocare.dto.response.DashboardCicloResponse;
import com.ciclocare.dto.response.ModoGestanteResponse;
import com.ciclocare.dto.response.UsuarioResponse;
import com.ciclocare.entity.CicloMenstrual;
import com.ciclocare.entity.Usuario;
import com.ciclocare.enums.FaseCiclo;
import com.ciclocare.exception.ResourceNotFoundException;
import com.ciclocare.repository.CicloMenstrualRepository;
import com.ciclocare.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CicloMenstrualService {

    private final CicloMenstrualRepository cicloRepository;
	private final UsuarioRepository usuarioRepository;


    @Transactional
    public CicloMenstrualResponse criar(UUID usuarioId, CicloMenstrualRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		int mediaDuracaoCiclo;
		int mediaDuracaoMenstruacao;
		int duracaoCiclo = request.getDuracaoCiclo() != null
				? request.getDuracaoCiclo()
				: 28;
		int duracaoMenstruacao = request.getDuracaoMenstruacao() != null
				? request.getDuracaoMenstruacao()
				: 5;

		List<CicloMenstrual> ultimosCiclos =
				cicloRepository.findTop3ByUsuarioOrderByDataInicioDesc(usuario);

		LocalDate dataInicio = request.getDataInicio();

		if (dataInicio == null) {
			dataInicio = request.getUltimaMenstruacao();
		}

		if (dataInicio == null) {
			throw new RuntimeException("Data de início ou última menstruação é obrigatória");
		}

		LocalDate dataFim = request.getDataFim();

		if (dataFim == null) {
			dataFim = dataInicio.plusDays(request.getDuracaoMenstruacao() - 1);
		}

		if (ultimosCiclos.isEmpty()) {
			mediaDuracaoCiclo = request.getDuracaoCiclo();
		} else {
			mediaDuracaoCiclo = Math.round(
					(float) ultimosCiclos.stream()
							.mapToInt(CicloMenstrual::getDuracaoCiclo)
							.average()
							.orElse(request.getDuracaoCiclo()));
		}

		if (ultimosCiclos.isEmpty()) {
			mediaDuracaoMenstruacao = request.getDuracaoMenstruacao();
		} else {
			mediaDuracaoMenstruacao = Math.round(
					(float) ultimosCiclos.stream()
							.mapToInt(CicloMenstrual::getDuracaoMenstruacao)
							.average()
							.orElse(request.getDuracaoMenstruacao())
			);
		}
		LocalDate proximaPrevisao = dataInicio.plusDays(mediaDuracaoCiclo);

		LocalDate previsaoOvulacao = proximaPrevisao.minusDays(14);

		LocalDate janelaFertilInicio = previsaoOvulacao.minusDays(5);

		LocalDate janelaFertilFim = previsaoOvulacao.plusDays(1);

        CicloMenstrual ciclo = CicloMenstrual.builder()
                .usuario(usuario)
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .ultimaMenstruacao(dataInicio)
				.duracaoCiclo(mediaDuracaoCiclo)
				.duracaoMenstruacao(mediaDuracaoMenstruacao)
                .proximaPrevisao(proximaPrevisao)
				.janelaFertilInicio(janelaFertilInicio)
				.janelaFertilFim(janelaFertilFim)
				.previsaoOvulacao(previsaoOvulacao)
                .intensidadeFluxo(request.getIntensidadeFluxo())
                .build();

        CicloMenstrual cicloSalvo = cicloRepository.save(ciclo);
        return mapToResponse(cicloSalvo);
    }

    public CicloMenstrualResponse buscarPorId(UUID id) {
        CicloMenstrual ciclo = cicloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo menstrual não encontrado"));
        return mapToResponse(ciclo);
    }

    public List<CicloMenstrualResponse> buscarTodosPorUsuario(UUID usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return cicloRepository.findAllByUsuario(usuario)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CicloMenstrualResponse buscarUltimo(UUID usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        CicloMenstrual ciclo = cicloRepository.findUltimoByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum ciclo registrado"));
        return mapToResponse(ciclo);
    }

    @Transactional
    public CicloMenstrualResponse atualizar(UUID id, CicloMenstrualRequest request) {
        CicloMenstrual ciclo = cicloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo menstrual não encontrado"));

        ciclo.setDataInicio(request.getDataInicio());
        ciclo.setDataFim(request.getDataFim());
        ciclo.setDuracaoCiclo(request.getDuracaoCiclo());
        ciclo.setDuracaoMenstruacao(request.getDuracaoMenstruacao());
        ciclo.setUltimaMenstruacao(request.getUltimaMenstruacao());
        ciclo.setProximaPrevisao(request.getUltimaMenstruacao().plusDays(request.getDuracaoCiclo()));
        ciclo.setIntensidadeFluxo(request.getIntensidadeFluxo());

        CicloMenstrual cicloAtualizado = cicloRepository.save(ciclo);
        return mapToResponse(cicloAtualizado);
    }

    @Transactional
    public void deletar(UUID id) {
        CicloMenstrual ciclo = cicloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo menstrual não encontrado"));
        cicloRepository.delete(ciclo);
    }

    private CicloMenstrualResponse mapToResponse(CicloMenstrual ciclo) {
        return CicloMenstrualResponse.builder()
                .id(ciclo.getId())
                .usuarioId(ciclo.getUsuario().getId())
                .dataInicio(ciclo.getDataInicio())
                .dataFim(ciclo.getDataFim())
                .duracaoCiclo(ciclo.getDuracaoCiclo())
                .duracaoMenstruacao(ciclo.getDuracaoMenstruacao())
                .ultimaMenstruacao(ciclo.getUltimaMenstruacao())
                .proximaPrevisao(ciclo.getProximaPrevisao())
                .intensidadeFluxo(ciclo.getIntensidadeFluxo())
                .criadoEm(ciclo.getCriadoEm())
                .build();
    }

	public String gerarMensagem(FaseCiclo faseCiclo) {
		return switch(faseCiclo) {
			case MENSTRUAL -> "Seu corpo está em fase de renovação.";
			case FOLICULAR -> "Mais energia e disposição hoje.";
			case OVULACAO -> "Alta fertilidade no momento.";
			case LUTEA -> "Momento ideal para desacelerar.";
		};
	}

	public DashboardCicloResponse exibirDashboard(UUID idUsuaria) {
		Usuario usuaria = usuarioRepository.findById(idUsuaria)
				.orElseThrow(() ->
						new ResourceNotFoundException("Usuária não encontrada."));

		Long quantidadeCiclos = cicloRepository.countByUsuario(usuaria);
		boolean menosDe3Ciclos = quantidadeCiclos < 3;

		List<CicloMenstrual> ultimosCiclos =
				cicloRepository.findTop3ByUsuarioOrderByDataInicioDesc(usuaria);

		if (ultimosCiclos.isEmpty()) {
			throw new ResourceNotFoundException("Nenhum ciclo encontrado.");
		}

		CicloMenstrual cicloAtual = ultimosCiclos.get(0);

		int mediaDuracaoCiclo = Math.round(
				(float) ultimosCiclos.stream()
						.mapToInt(CicloMenstrual::getDuracaoCiclo)
						.average()
						.orElse(28)
		);

		int mediaDuracaoMenstruacao = Math.round(
				(float) ultimosCiclos.stream()
						.mapToInt(CicloMenstrual::getDuracaoMenstruacao)
						.average()
						.orElse(5)
		);

		LocalDate ultimaMenstruacao = cicloAtual.getDataInicio();
		LocalDate proximaPrevisao = ultimaMenstruacao.plusDays(mediaDuracaoCiclo);
		LocalDate previsaoOvulacao = proximaPrevisao.minusDays(14);
		LocalDate janelaFertilInicio = previsaoOvulacao.minusDays(5);
		LocalDate janelaFertilFim = previsaoOvulacao.plusDays(1);


		Integer diaCiclo = calcularDiaCiclo(
				ultimaMenstruacao,
				mediaDuracaoCiclo
		);

		FaseCiclo faseCiclo = calcularFaseAtual(
				diaCiclo,
				mediaDuracaoCiclo,
				mediaDuracaoMenstruacao
		);

		return DashboardCicloResponse.builder()
				.diaCiclo(diaCiclo)
				.faseCiclo(faseCiclo)
				.mensagem(gerarMensagem(faseCiclo))
				.ultimaMenstruacao(ultimaMenstruacao)
				.duracaoCiclo(mediaDuracaoCiclo)
				.duracaoMenstruacao(mediaDuracaoMenstruacao)
				.proximaPrevisao(proximaPrevisao)
				.previsaoOvulacao(previsaoOvulacao)
				.janelaFertilInicio(janelaFertilInicio)
				.janelaFertilFim(janelaFertilFim)
				.quantidadeCiclos(quantidadeCiclos)
				.menosDe3Ciclos(menosDe3Ciclos)
				.build();
	}

	public FaseCiclo calcularFaseAtual(
			Integer diaCiclo,
			Integer duracaoCiclo,
			Integer duracaoMenstruacao) {
		if (diaCiclo <= duracaoMenstruacao) {
			return FaseCiclo.MENSTRUAL;
		}

		int ovulacao = duracaoCiclo - 14;

		if (diaCiclo < ovulacao) {
			return FaseCiclo.FOLICULAR;
		}

		if (diaCiclo == ovulacao) {
			return FaseCiclo.OVULACAO;
		}

		return FaseCiclo.LUTEA;
	}

	public Integer calcularDiaCiclo(LocalDate ultimaMenstruacao, Integer duracaoCiclo) {
		Long dias = ChronoUnit.DAYS.between(
				ultimaMenstruacao,
				LocalDate.now()
		);

		return (int) (dias % duracaoCiclo) + 1;
	}

	public ModoGestanteResponse exibirModoGestante(UUID idUsuaria) {
		// buscando a usuária e o último ciclo para ter como base
		Usuario usuaria = usuarioRepository.findById(idUsuaria)
				.orElseThrow(() ->
						new ResourceNotFoundException("Usuária não encontrada"));

		List<CicloMenstrual> ultimosCiclos =
				cicloRepository.findTop3ByUsuarioOrderByDataInicioDesc(usuaria);

		if (ultimosCiclos.isEmpty()) {
			throw new ResourceNotFoundException("Nenhum ciclo encontrado.");
		}

		CicloMenstrual cicloAtual = ultimosCiclos.get(0);

		// cálculos abaixo
		LocalDate ultimaMenstruacao = cicloAtual.getDataInicio();
		LocalDate hoje = LocalDate.now();

		long diasGravidez = ChronoUnit.DAYS.between(ultimaMenstruacao, hoje);

		int semanasCompletas = (int) diasGravidez / 7;
		int meses = semanasCompletas / 4;
		int semanasMes = semanasCompletas % 4;
		int diasSemana = (int) diasGravidez % 7;

		int semanaAtual = semanasCompletas + 1;

		int diasGravidezTotal = 280;
		int diasRestantes = Math.max(0, diasGravidezTotal - (int) diasGravidez);

		int semanasRestantes = diasRestantes / 7;
		int diasRestantesSemana = diasRestantes % 7;

		LocalDate previsaoParto = ultimaMenstruacao.plusDays(280);

		String textoMeses = meses == 1
				? "1 mês"
				: meses + " meses";

		String textoSemanas = semanasMes == 1
				? "1 semana"
				: semanasMes + " semanas";
		String mensagemSecundaria = textoMeses + " e " + textoSemanas;

		return ModoGestanteResponse.builder()
				.semanaAtual(semanaAtual)
				.semanasCompletas(semanasCompletas)
				.diasSemana(diasSemana)
				.diasGravidez((int) diasGravidez)
				.diasRestantes(diasRestantes)
				.previsaoParto(previsaoParto)
				.mensagemPrincipal("Você está na " + semanaAtual + "ª semana de gestação")
				.meses(meses)
				.semanasMes(semanasMes)
				.semanasRestantes(semanasRestantes)
				.diasRestantesSemana(diasRestantesSemana)
				.mensagemSecundaria(mensagemSecundaria)
				.build();
	}

	public String gerarMensagemDetalhada(FaseCiclo faseCiclo, Integer diaCiclo) {
		return switch (faseCiclo) {
			case MENSTRUAL -> switch (diaCiclo) {
				case 1 -> "Hoje pode ser o início do fluxo menstrual. É comum sentir mais cólicas, cansaço ou sensibilidade. Tente respeitar seu ritmo e evitar se cobrar demais.";
				case 2 -> "O fluxo ainda pode estar mais intenso. Seu corpo está em processo de renovação, então priorize descanso, hidratação e atividades mais leves.";
				case 3 -> "A fase menstrual continua, mas algumas mulheres já começam a sentir uma leve melhora na energia. Observe seus sinais e cuide do seu conforto.";
				case 4 -> "Seu fluxo pode começar a diminuir. Aos poucos, o corpo se prepara para retomar mais disposição nos próximos dias.";
				case 5 -> "Você pode estar chegando ao fim da menstruação. É um bom momento para voltar devagar à rotina, sem ignorar os sinais do corpo.";
				default -> "Você ainda está na fase menstrual. Continue respeitando seu ritmo, mantendo-se hidratada e priorizando autocuidado.";
			};

			case FOLICULAR -> switch (diaCiclo % 3) {
				case 1 -> "Na fase folicular, sua energia tende a aumentar. Pode ser um bom momento para organizar tarefas, estudar e iniciar novos planos.";
				case 2 -> "Seu corpo está se preparando para a ovulação. Você pode perceber mais disposição, clareza mental e vontade de se movimentar.";
				default -> "A fase folicular costuma trazer mais vitalidade. Aproveite esse período para avançar em atividades que exigem foco e energia.";
			};

			case OVULACAO -> "Você está no período de ovulação, quando a fertilidade tende a estar mais alta. Observe sinais como aumento da libido, muco cervical mais elástico e mais energia.";

			case LUTEA -> switch (diaCiclo % 3) {
				case 1 -> "Na fase lútea, o corpo começa a se preparar para uma possível menstruação. Pode ser interessante desacelerar um pouco.";
				case 2 -> "É comum sentir mais fome, retenção de líquido ou alterações de humor nessa fase. Observe seus padrões com gentileza.";
				default -> "A fase lútea pode pedir mais cuidado emocional e físico. Priorize rotina, alimentação equilibrada e descanso.";
			};
		};
	}



	public List<CicloMenstrualResponse> buscarCalendario(
			UUID usuarioId,
			LocalDate inicio,
			LocalDate fim
	) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		return cicloRepository.buscarCiclosNoPeriodo(usuario, inicio, fim)
				.stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}
}
