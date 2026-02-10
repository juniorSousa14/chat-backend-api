package br.com.juniorjvsousa.chat_service.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_grupos")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private String nome;

    @ManyToMany
    @JoinTable(
            name = "tb_grupo_membros",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> membros;

    @ManyToMany
    @JoinTable(
            name = "grupo_admins",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> administradores = new HashSet<>();
}

