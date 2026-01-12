package br.com.juniorjvsousa.chat_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_grupos")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;

}
