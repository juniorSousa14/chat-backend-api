package br.com.juniorjvsousa.chat_service.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private String nome;
    @NotNull
    private String email;
    @NotNull
    private String senha;
    private LocalDateTime dataCriacao;

    @ManyToMany(mappedBy = "membros")
    @JsonIgnore
    private List<Grupo> grupos;

    // para definir a data de criação automaticamente antes de persistir
    @PrePersist
    public void presPersist() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
    }
}
