package br.com.juniorjvsousa.chat_service.domain.entity;


import br.com.juniorjvsousa.chat_service.enums.PapelDoUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_membros_grupo")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private ChatGroup grupo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private PapelDoUsuario papel;

}
