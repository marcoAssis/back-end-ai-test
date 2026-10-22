# AI_LOG — Registro de interações com IA

Registro das interações com o agente de IA durante o desenvolvimento da avaliação técnica Outsera (API do Golden Raspberry Awards), conforme exigido no item 5 do enunciado.

- **Agente:** Claude Code (modelo Claude Opus 5.5)
- **Stack:** Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA, H2 em memória, Maven Wrapper
- **Formato:** cada entrada traz o prompt (resumido), as skills/ferramentas usadas, o que a IA produziu e as decisões tomadas pelo desenvolvedor.

---

## Interação 1 — Análise inicial dos requisitos e do CSV

**Data:** 2026-10-22

### Prompt

> Vamos iniciar essa avaliação técnica em Java. Tenho aqui o PDF com os requisitos e o CSV que será utilizado. Dê uma analisada nos dois arquivos e verifique quais skills disponíveis podem nos ajudar nesse desenvolvimento, principalmente as relacionadas a Java. Me aponte possíveis dificuldades e pontos que merecem atenção na implementação.

### O que a IA fez

1. Extraiu o texto do PDF `Avaliação Outsera - Back-End.pdf` (`pdftotext`).
2. Inspecionou o `Movielist.csv` com comandos de shell: encoding, separador, número de colunas por linha, aspas, valores da coluna `winner`, separadores de produtores e final de arquivo.
3. Calculou o resultado esperado com um script Python independente da implementação, para servir de oráculo nos testes.
4. Inspecionou o projeto gerado pelo Spring Initializr em `razzies/`.
5. Levantou as skills disponíveis no ambiente relevantes para Java/Spring.

### Resumo dos requisitos

- Ler o CSV e inserir os dados em banco na inicialização da aplicação.
- Endpoint que retorna o produtor com maior intervalo entre dois prêmios consecutivos e o que ganhou dois prêmios mais rápido, no formato `{"min": [...], "max": [...]}` com `producer`, `interval`, `previousWin`, `followingWin`.
- API REST no nível 2 de maturidade de Richardson.
- Somente testes de integração, garantindo que os dados retornados estão de acordo com os dados fornecidos.
- Banco em memória, SGBD embarcado, sem instalação externa.
- README com instruções para rodar o projeto e os testes.
- Código em repositório git.
- Uso de IA obrigatório e avaliado, com registro das interações (este arquivo).
- A avaliação usará outros conjuntos de dados, então o resultado precisa ser correto independentemente da entrada.

### Características do CSV fornecido

- 206 linhas de dados mais cabeçalho `year;title;studios;producers;winner`.
- Separador `;`, UTF-8, quebra de linha `\n`, sem aspas, todas as linhas com 5 colunas.
- 42 filmes vencedores (`winner = yes`); nos demais a coluna está vazia.
- Produtores múltiplos no mesmo campo, separados por `", "`, `" and "` e `", and "` (5 ocorrências deste último).
- Anos com mais de um vencedor: 1986, 1990 e 2015.

### Resultado esperado com o CSV original (oráculo)

| Lista | Produtor       | Intervalo | Prêmio anterior | Prêmio seguinte |
|-------|----------------|-----------|-----------------|-----------------|
| min   | Joel Silver    | 1         | 1990            | 1991            |
| max   | Matthew Vaughn | 13        | 2002            | 2015            |

Existem apenas 4 intervalos no arquivo: Joel Silver (1), Bo Derek (6), Buzz Feitshans (9) e Matthew Vaughn (13).

### Skills consultadas / identificadas como úteis

Plugin `java-backend-skills`:

| Skill               | Uso previsto                                                          |
|---------------------|-----------------------------------------------------------------------|
| `spring-boot`       | Configuração, propriedades e carga do CSV no startup                  |
| `spring-data`       | Entidades JPA, repositórios e consulta dos vencedores                 |
| `spring-web`        | Controller, DTOs de resposta e tratamento de erros                    |
| `spring-testing`    | Testes de integração com `@SpringBootTest` e MockMvc                  |
| `java-implement`    | Implementação das regras de negócio                                   |
| `java-idiomatic`    | Código idiomático (records, streams, imutabilidade)                   |
| `java-design`       | Organização em camadas e responsabilidades                            |
| `java-build`        | Resolução de problemas de Maven e dependências do Boot 4              |

Skills gerais: `code-review` e `simplify` para revisão antes da entrega.

Descartada: `java-unit-testing`, porque o enunciado exige somente testes de integração.

### Pontos de atenção identificados

1. **Separação de produtores:** tratar `", "`, `" and "` e `", and "` com um único regex (`,\s*and\s+|,\s*|\s+and\s+`), com `trim` e descarte de nomes vazios.
2. **Vitórias no mesmo ano:** um produtor premiado duas vezes no mesmo ano geraria intervalo 0. O comportamento precisa ser definido.
3. **Empates:** `min` e `max` são listas e devem conter todos os produtores empatados.
4. **Intervalos consecutivos:** calcular só entre vitórias consecutivas, com os anos ordenados. Um produtor com N vitórias gera N-1 intervalos.
5. **Sem intervalos:** se nenhum produtor tiver duas vitórias, retornar `{"min": [], "max": []}` com HTTP 200.
6. **Robustez do CSV:** caminho configurável por propriedade. Tolerar BOM, `\r\n`, linhas em branco, `yes` com maiúsculas/minúsculas variadas, espaços extras e última linha sem quebra.
7. **CSV inválido:** decidir entre falhar no startup ou ignorar a linha com log, e documentar no README.
8. **Spring Boot 4:** Jackson 3 (pacote `tools.jackson`), anotações de teste em módulos próprios (`spring-boot-webmvc-test`), console H2 como starter separado. Exemplos da internet e sugestões de IA baseados no Boot 3 podem usar imports errados.
9. **Modelo de dados:** normalizar `Movie` e `Producer` em relação N:N em vez de guardar a string de produtores bruta.
10. **Testes:** validar o JSON completo com o CSV original e criar CSVs próprios para empates, ausência de intervalos, vitórias no mesmo ano e separadores mistos, cada um com seu contexto (`@TestPropertySource`).

### Decisões já tomadas

- Recomeçar o projeto do zero, com a base gerada pelo Spring Initializr em `razzies/` (pacote `br.com.outsera.razzies`).
- Stack: Java 21, Spring Boot 4.1.1, starters `webmvc`, `data-jpa` e `h2console`, H2 em memória, Maven Wrapper. Sem Spring Security e sem Lombok.
- Testes de integração com MockMvc.
- Usar o resultado calculado acima como oráculo dos testes com o CSV original.
- Repositório git inicializado na raiz do projeto (branch `main`), com este `AI_LOG.md` mantido atualizado a cada interação.

### Decisões em aberto

- Tratamento de vitórias no mesmo ano (intervalo 0).
- Comportamento diante de linhas inválidas no CSV.

---

## Interação 2 — Inicialização do repositório e do AI_LOG

**Data:** 2026-10-22

### Prompt

> Inicialize o repositório Git, caso ainda não esteja inicializado, e crie um AI_LOG.md para registrar nossas interações com a IA durante o desenvolvimento. Registre a análise inicial que fizemos, incluindo as skills consultadas, os principais pontos identificados e as decisões já tomadas.

### O que a IA fez

- Verificou que não havia repositório git na raiz nem em `razzies/`.
- Executou `git init -b main` na raiz do projeto.
- Criou este `AI_LOG.md` com o registro da interação 1 e desta interação.

---

## Interação 3 — Organização da estrutura e primeiro commit

**Data:** 2026-10-22

### Prompt

> Vamos manter o diretório .claude versionado. Pode mover o Movielist.csv para src/main/resources do projeto. Depois faça o primeiro commit com a estrutura inicial.

### O que a IA fez

- Moveu `Movielist.csv` da raiz para `razzies/src/main/resources/Movielist.csv`.
- Manteve o diretório `.claude/` versionado, como registro da configuração de agentes usada no desenvolvimento.
- Criou o primeiro commit com a estrutura inicial: projeto gerado pelo Spring Initializr, CSV, `.gitignore`, `.claude/` e este `AI_LOG.md`.

### Decisões tomadas

- `.claude/` faz parte do repositório.
- O CSV padrão fica em `src/main/resources`, carregado do classpath. O caminho será configurável por propriedade.

---

## Interação 4 — Modelagem de dados (Movie / Producer)

**Data:** 2026-10-22

### Prompt

> Vamos definir a modelagem. Utilize a skill spring-data para avaliar duas alternativas: manter os produtores como texto na entidade Movie ou criar uma entidade Producer com relacionamento N. Considerando os requisitos da avaliação, qual abordagem você recomenda e por quê? Quero uma solução simples, com boa legibilidade e sem complexidade desnecessária.

Em seguida, após aprovar o plano:

> Vamos seguir com a modelagem N proposta, mantendo o relacionamento unidirecional e a estrutura simples. Tenha atenção ao equals/hashCode das entidades e à unicidade dos produtores. Pode implementar as entidades, repositories e a projeção ProducerWin, utilizando a skill spring-data. Depois execute a compilação para validar. Não avance para a importação do CSV ainda.

### Skills e ferramentas

- `java-backend-skills:spring-data`: análise a partir do resultado necessário (pares produtor/ano dos vencedores) até a consulta e o plano de fetch.
- Modo de planejamento do Claude Code: plano revisado e aprovado pelo desenvolvedor antes da implementação.

### Alternativas avaliadas

| Critério | A: texto em `Movie` | B: `Producer` N:N |
|---|---|---|
| Entidades | 1 | 2 + tabela de junção |
| Onde o split acontece | na consulta, a cada requisição | uma vez, na carga |
| Consulta | vencedores + split + agrupamento em Java | 1 JPQL que já devolve os pares |
| Banco representa o domínio | não | sim, com nome único |
| Risco de erro de split em outro CSV | espalhado entre carga e serviço | isolado no parser da carga |

### Decisões tomadas

- Alternativa B, na forma mínima: `@ManyToMany` unidirecional `Movie` → `Producer`, sem cascade, fetch `LAZY` padrão.
- `studios` permanece como texto, porque nenhum requisito usa estúdios.
- Coluna `release_year` em vez de `year`, que é palavra reservada no H2.
- `Producer`: `equals`/`hashCode` pela chave natural `name`, imutável e sem setter; comparação via `getName()` para funcionar com proxies do Hibernate. O construtor rejeita nome nulo ou em branco.
- Unicidade de produtor garantida no banco pela constraint `uk_producer_name`; a deduplicação na carga fica para a etapa do carregador.
- `Movie`: sem chave natural, então `equals` pelo `id` gerado (não nulo) e `hashCode` constante da classe.
- Entidades sem setters; `getProducers()` devolve visão não modificável.
- Consulta `MovieRepository.findProducerWins()` em JPQL com projeção no record `ProducerWin(producer, year)`, ordenada por produtor e ano.
- Pacotes: `br.com.outsera.razzies.domain` (entidades) e `br.com.outsera.razzies.repository` (repositórios e projeção).

### O que a IA fez

- Criou `Movie`, `Producer`, `MovieRepository`, `ProducerRepository` e `ProducerWin`.
- Executou `./mvnw -q compile`: sucesso.
- Executou `./mvnw test` (somente o `contextLoads` do Initializr) com `show-sql`: contexto sobe, a JPQL é validada no startup e o DDL gerado confere com o modelo (tabelas `movie`, `producer`, `movie_producer`, constraint `uk_producer_name`).
- Não avançou para a importação do CSV, conforme pedido.
