package br.com.juniorjvsousa.chat_service.domain.dto;

import java.util.List;
import java.util.UUID;

public record CriarGrupoDTO(String nome,
                            UUID criadorId,
                            List<UUID> outrosAdminsIds) {
}
