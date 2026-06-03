package com.ciclocare.controller;

import com.ciclocare.dto.request.RegisterRequest;
import com.ciclocare.dto.request.UpdateProfileRequest;
import com.ciclocare.dto.response.ApiResponse;
import com.ciclocare.dto.response.DashboardCicloResponse;
import com.ciclocare.dto.response.ModoGestanteResponse;
import com.ciclocare.entity.Usuario;
import com.ciclocare.service.CicloMenstrualService;
import com.ciclocare.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
	private final CicloMenstrualService cicloMenstrualService;

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse> registrar(
            @Valid @RequestBody RegisterRequest request) {
        try {
            var usuarioCriado = usuarioService.registrar(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.sucesso("Usuário registrado com sucesso", usuarioCriado));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> buscarPorId(@PathVariable UUID id) {
        try {
            var usuario = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(ApiResponse.sucesso("Usuário encontrado", usuario));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

    @GetMapping("/perfil/meu")
    public ResponseEntity<ApiResponse> meuPerfil() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            var usuario = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(ApiResponse.sucesso("Perfil recuperado", usuarioService.mapToResponse(usuario)));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            var usuarioAtualizado = usuarioService.atualizarPerfil(id, request);
            return ResponseEntity.ok(ApiResponse.sucesso("Perfil atualizado com sucesso", usuarioAtualizado));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletar(@PathVariable UUID id) {
        try {
            usuarioService.deletarConta(id);
            return ResponseEntity.ok(ApiResponse.sucesso("Conta deletada com sucesso"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

	@GetMapping("/{id}/dashboard")
	public DashboardCicloResponse exibirDashboard(
			@PathVariable UUID id
	) { return cicloMenstrualService.exibirDashboard(id); }

	@GetMapping("/{id}/modo-gestante")
	public ModoGestanteResponse exibirModoGestante(@PathVariable UUID id) {
		return cicloMenstrualService.exibirModoGestante(id);
	}

}
