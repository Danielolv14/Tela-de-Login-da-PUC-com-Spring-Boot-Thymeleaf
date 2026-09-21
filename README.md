# 🔐 SecureAccess — Login, Cadastro e Recuperação de Senha

Atividade 02 (DIW): aplicação web com **Spring Boot + Thymeleaf + Spring Security** que implementa autenticação, cadastro de usuários e recuperação de senha por email (desafio opcional).

## Funcionalidades

- Login por **usuário ou email** (Spring Security, sessão e logout com CSRF).
- Cadastro com validações: campos obrigatórios, email válido, senhas iguais, força mínima da senha (8+ caracteres, letras e números), usuário/email duplicados.
- Senhas armazenadas com **BCrypt**.
- Área protegida (`/`) inacessível sem autenticação.
- **Recuperação de senha por email**: token aleatório de uso único, válido por 30 min, guardado apenas como hash SHA-256; a resposta não revela se o email existe.
- Interface própria, simples e acolhedora: tons quentes, cartão limpo e layout responsivo.

## Endpoints

| Método | Endpoint            | Descrição                                        | Acesso    |
| ------ | ------------------- | ------------------------------------------------ | --------- |
| GET    | `/login`            | Tela de login                                    | Público   |
| POST   | `/login`            | Processado pelo Spring Security                  | Público   |
| GET    | `/register`         | Tela de cadastro                                 | Público   |
| POST   | `/register`         | Processa o cadastro                              | Público   |
| GET    | `/recoverpassword`  | Tela de recuperação de senha                     | Público   |
| POST   | `/recoverpassword`  | Gera o token e envia o email                     | Público   |
| GET    | `/resetpassword?token=…` | Tela para definir a nova senha              | Público   |
| POST   | `/resetpassword`    | Grava a nova senha                               | Público   |
| GET    | `/`                 | Página inicial (área protegida)                  | Logado    |
| POST   | `/logout`           | Encerra a sessão                                 | Logado    |

## Estrutura

```
src/main/java/br/puc/secureaccess/
├── config/       SecurityConfig (regras de acesso, BCrypt)
├── controller/   AuthController, HomeController
├── dto/          RegisterForm, ResetPasswordForm (Bean Validation)
├── model/        AppUser, PasswordResetToken
├── repository/   Spring Data JPA
└── service/      AppUserDetailsService, UserService, PasswordResetService, EmailService
src/main/resources/
├── static/css/style.css
└── templates/    login, register, recoverpassword, resetpassword, home, fragments
```

## Como executar

Requisitos: **Java 8+** e **Maven 3.6+**.

```bash
mvn spring-boot:run
```

Acesse <http://localhost:8080>. O banco é um **H2 em arquivo** (`./data/`), criado automaticamente — não é preciso instalar nada.
Console do H2 não está habilitado por padrão.

## Configuração e credenciais

Nenhuma credencial fica no código. Tudo é lido de **variáveis de ambiente** por meio do `application.properties`:

| Variável        | Padrão                              | Uso                                   |
| --------------- | ----------------------------------- | ------------------------------------- |
| `APP_BASE_URL`  | `http://localhost:8080`             | Base do link enviado por email        |
| `MAIL_HOST`     | *(vazio)*                           | Servidor SMTP (ex.: `smtp.gmail.com`) |
| `MAIL_PORT`     | `587`                               | Porta SMTP                            |
| `MAIL_USERNAME` | *(vazio)*                           | Usuário SMTP                          |
| `MAIL_PASSWORD` | *(vazio)*                           | Senha SMTP / **senha de app** do Gmail |
| `MAIL_FROM`     | `no-reply@secureaccess.local`       | Remetente                             |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | H2 em arquivo    | Trocar de banco, se desejado          |

### Testando a recuperação de senha

- **Sem SMTP** (`MAIL_HOST` vazio): o link de redefinição é impresso no **log do servidor** (`SMTP não configurado. Link de recuperação para …`). Basta copiá-lo para o navegador.
- **Com SMTP** (exemplo Gmail, PowerShell):

```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_USERNAME="seuemail@gmail.com"
$env:MAIL_PASSWORD="senha-de-app-de-16-caracteres"
$env:MAIL_FROM="seuemail@gmail.com"
mvn spring-boot:run
```

> Nunca publique senhas ou tokens reais no GitHub.

## Prints

_Adicione aqui capturas das telas de login, cadastro e recuperação de senha._

## Autores

Daniel Oliveira e Arthur Domingos (Pair Programming)
