# Chat Service API

Este é o backend de uma aplicação de chat em tempo real, desenvolvido com **Java** e **Spring Boot**. A API gerencia
usuários, autenticação, criação de grupos e troca de mensagens.

## Tecnologias Utilizadas

* **Java 17**
* **Spring Boot 3.5.9**
* **Spring Security + JWT** (Autenticação e Autorização)
* **Spring Data JPA** (Persistência de dados)
* **WebSocket (STOMP)** (Comunicação em tempo real)
* **H2 Database** (Banco em memória para testes)
* **PostgreSQL** (Banco de dados de produção)
* **JUnit 5 & Mockito** (Testes Unitários e de Integração)
* **Maven** (Gerenciador de dependências)

## Funcionalidades

- [x] **Autenticação:** Cadastro e Login de usuários via Token JWT.
- [x] **Gestão de Usuários:** Perfil de usuário.
- [x] **Gestão de Grupos:**
    - Criar novos grupos.
    - Adicionar membros (Apenas administradores).
    - Remover membros (Apenas administradores).
    - Listar grupos.
- [x] **Chat Real-time:** Suporte a WebSocket para envio de mensagens.

## Como Rodar o Projeto

### Pré-requisitos

* Java 17 instalado.
* Maven instalado.

### Passo a passo

1. **Clone o repositório:**
   Abra o terminal e rode o comando abaixo para baixar o código:
   ```bash
   git clone [https://github.com/juniorSousa14/chat-backend-api.git](https://github.com/juniorSousa14/chat-backend-api.git)
   cd chat-service