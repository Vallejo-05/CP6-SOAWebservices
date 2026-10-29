# CP6 - SOA e WebServices | API Incident Management

API REST em **Java 21 + Spring Boot 4** (lado da publicação) para o sistema de **gestão de ocorrências escolares**. Ela implementa os requisitos e o MER do documento *Requisitos_MER_Telas* e atende exatamente às chamadas do front-end `IncidentManagement` fornecido pelo professor.

## Integrantes

| Nome | RM |
|------|----|
| Fernando Gonzales | RM555045 |
| Gabriel Vallejo | RM554973 |
| Lucas Catroppa | RM555450 |
| Luiz Felipe Coelho | RM555074 |
| Vitor Musolino | RM555012 |

## Perfis e permissões

| Perfil | O que pode fazer |
|--------|------------------|
| **PROFESSOR** | Registrar ocorrências; alterar e excluir **apenas as próprias**; consultar todas as ocorrências |
| **ANALISTA** | Consultar todas as ocorrências e o dashboard |
| **COORDENADOR** | Consultar tudo, acessar o dashboard e alterar o status das ocorrências |
| **ADMINISTRATIVO** | Cadastrar turmas e alunos, vincular alunos às turmas, cadastrar categorias e tipos de ocorrência e gerenciar usuários |

Qualquer usuário autenticado pode alterar a própria senha. Sem token, a API responde **401**; com um perfil sem permissão, responde **403**. Esse é o comportamento que o front-end espera: ele redireciona para o login no 401 e para "acesso negado" no 403.

### Usuários iniciais (criados pela migration `V7`)

| Login | Senha | Perfil/Cargo |
|-------|-------|--------|
| admin@escola.com | 123456 | ADMINISTRATIVO |
| coordenador@escola.com | 123456 | COORDENADOR |
| analista@escola.com | 123456 | ANALISTA |
| professor@escola.com | 123456 | PROFESSOR |

## Endpoints

| Recurso | Endpoints |
|---------|-----------|
| Autenticação | `POST /login` → `{ tokenJWT, id, login, name, role }` |
| Usuários | `POST/GET/PUT /users`, `GET /users/{id}`, `PATCH /users` (perfil), `DELETE /users/{id}` (inativa), `GET /users/roles`, `PATCH /users/password` |
| Alunos (RF02) | `POST/GET/PUT /students`, `GET/DELETE /students/{id}`, `PATCH /students/{id}/image` (foto, multipart), `GET /students/{id}/image`, `GET /students/{id}/incidents` (histórico completo) |
| Turmas (RF03/RF04) | `POST/GET/PUT /schoolclasses`, `GET /schoolclasses/canceled`, `GET/DELETE /schoolclasses/{id}`, `GET /schoolclasses/shifts`, `GET /schoolclasses/semesters`, `GET/POST /schoolclasses/{id}/students`, `DELETE /schoolclasses/{id}/students/{studentId}` |
| Categorias (RF05) | `GET/POST /categories`, `GET/PUT/DELETE /categories/{id}` |
| Tipos de ocorrência (RF06) | `GET/POST /incident-types` (`?category=`), `GET/PUT/DELETE /incident-types/{id}` |
| Ocorrências (RF07–RF09, RF13) | `POST/GET/PUT /incidents`, `GET/DELETE /incidents/{id}`, `PATCH /incidents/status`, `GET /incidents/status`, `GET /incidents/status/{waiting\|progressing\|active\|solved\|unsolved\|closed}`, `GET /incidents/categories`, `GET /incidents/types/{category}` |
| Dashboard | `GET /incidents/summary`: totais por status, por categoria, tipo e turma, ranking de alunos reincidentes e volume por mês |

A documentação completa está no **Swagger UI**: http://localhost:8090/swagger-ui.html. Uma cópia offline da especificação está em `SwaggerOffLine.yaml`.

### Regras de negócio
- A ocorrência só pode ser registrada para um aluno **que pertence à turma** selecionada, e o tipo precisa **pertencer à categoria** escolhida.
- Não é possível registrar ocorrências em turmas canceladas nem para alunos inativos.
- Toda ocorrência nasce com o status *Aguardando atendimento*. O coordenador pode movê-la para *Em atendimento*, *Resolvida*, *Encerrada sem solução* ou *Fechada*. Uma ocorrência fechada não muda mais de status.
- As exclusões são **lógicas**: aluno e usuário são inativados, turma é cancelada e ocorrência é marcada como excluída. Assim o histórico é preservado. Um usuário inativado perde o acesso imediatamente.
- Categoria ou tipo que já tenha ocorrências registradas não pode ser excluído.
- Filtros da consulta (`GET /incidents`): `studentId`, `schoolClassId`, `category`, `type`, `status`, `startDate`, `endDate` (yyyy-MM-dd) e `deleted`, com paginação e ordenação (`page`, `size`, `sort`).
- Formato das datas: `dd/MM/yyyy` para a data de nascimento e `dd/MM/yyyy - HH:mm` para as datas das ocorrências.

## Modelo de dados (MER)

As tabelas seguem o MER do documento: `perfis`, `usuarios`, `alunos` (a foto fica em `alunos_fotos`), `turmas`, `turmas_alunos` (N:M), `categorias`, `tipos_ocorrencia` e `ocorrencias`. Elas são criadas pelas migrations Flyway em `src/main/resources/db/migration`.

```
controller   -> endpoints REST (com documentação Swagger)
dto          -> objetos de entrada e saída da API
domain       -> entidades JPA (User, Role, Student, SchoolClass, Category, IncidentType, Incident)
repository   -> Spring Data JPA
service      -> regras de negócio
infra        -> segurança (JWT), CORS, Swagger e tratamento de erros
```

## Testes automatizados

São **78 testes**, rodando com H2 em memória (modo MySQL) e as mesmas migrations:

```bash
./mvnw test
```

Para cada entidade há testes de controller (permissões e validações), de service (regras de negócio) e de repository. Além deles, o `IncidentFlowIntegrationTest` executa o fluxo completo com login JWT real de cada perfil, e há testes específicos de CORS e de Swagger.

## Como executar

1. Suba o MySQL:
   ```bash
   docker compose up -d
   ```
2. Rode a API (porta **8090**, a mesma usada pelo front-end). As tabelas e os dados iniciais são criados pelo Flyway.
   ```bash
   ./mvnw spring-boot:run
   ```
3. **Front-end:** os arquivos JS do front-end apontam para `http://167.234.235.128:8090`. Para usá-los com a API local, troque esse endereço por `http://localhost:8090` em `auth.js`, `login.js`, `altera-senha.js` e `dashboard-ocorrencias.js`, e sirva a pasta do front-end (por exemplo, com o Live Server na porta 5500). As origens `localhost`, `127.0.0.1` e `167.234.235.128` (em qualquer porta) já estão liberadas no CORS. Para outras origens, use a variável `CORS_ALLOWED_ORIGINS`.

Variáveis de ambiente opcionais: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `CORS_ALLOWED_ORIGINS`.

> O front-end recebido não tem tela para vincular alunos às turmas nem para cadastrar categorias e tipos (RF04, RF05 e RF06). Esses recursos estão disponíveis na API e podem ser usados pelo Swagger.
