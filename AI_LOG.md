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

---

## Interação 5 — Avaliação da importação do CSV

**Data:** 2026-10-22

### Prompt

> Depois vamos partir para a importação do CSV. Utilize as skills spring-boot e java-implement para avaliar a melhor forma de carregar o arquivo na inicialização, quero utilizar Apache Commons CSV e deixar o caminho do arquivo configurável.

### Skills e ferramentas

- `java-backend-skills:spring-boot`: composição dos beans, momento da carga e configuração tipada.
- `java-backend-skills:java-implement`: reaproveitamento de APIs existentes (Spring `Resource`, Commons CSV) e responsabilidades das classes.
- Consulta ao Maven Central: Commons CSV mais recente é 1.14.1, e a versão não é gerenciada pelo Spring Boot 4.1.1.

### Alternativas avaliadas para o momento da carga

| Opção | Avaliação |
|---|---|
| `ApplicationRunner` | Escolhida: idiomática, roda com o contexto pronto e uma falha aborta o startup. |
| `@EventListener(ApplicationReadyEvent)` | Descartada: roda depois que a aplicação já está pronta para tráfego e uma falha não impede a subida. |
| `@PostConstruct` | Descartada: roda no meio da inicialização dos beans, com ordem e transações menos previsíveis. |
| `data.sql` / `CSVREAD` do H2 | Descartada: não usa Commons CSV e não separa os produtores. |

### Proposta

- Dependência `org.apache.commons:commons-csv:1.14.1` com versão explícita no `pom.xml`.
- `@ConfigurationProperties("razzies.csv")` com um `Resource path`, padrão `classpath:Movielist.csv`; aceita também `file:/caminho/arquivo.csv`. Arquivo inexistente falha o startup com mensagem clara.
- `MovieCsvParser`: lê o `Resource` com Commons CSV (delimitador `;`, cabeçalho por nome, trim, linhas vazias ignoradas, BOM removido) e separa os produtores com um único regex.
- `MovieCsvLoader` (`ApplicationRunner`, `@Transactional`): deduplica produtores por nome, salva produtores e filmes.
- Testes poderão apontar outros CSVs com `razzies.csv.path`.

### Status

Avaliação apresentada e aprovada na interação 6.

---

## Interação 6 — Implementação da importação do CSV

**Data:** 2026-10-22

### Prompts

> Pode implementar falhando a inicialização em linha inválida

> Mantenha a separação entre parser e loader, com a importação transacional.

### Skills e ferramentas

- `java-backend-skills:spring-boot` e `java-backend-skills:java-implement` (carregadas na interação 5).

### Decisões tomadas

- **Linha inválida falha a inicialização**, com o número da linha na mensagem. Carga parcial daria resultados errados sem aviso.
- Parser e loader separados: `MovieCsvParser` não acessa o banco; `MovieCsvLoader` (`ApplicationRunner`, `@Transactional`) só converte e persiste.
- Validações do parser: arquivo existente, colunas obrigatórias no cabeçalho (sem diferenciar maiúsculas), mesma quantidade de colunas em cada linha, ano numérico e título não vazio. `studios`, `producers` e `winner` podem ser vazios.
- `winner` verdadeiro apenas para `yes`, sem diferenciar maiúsculas.
- BOM UTF-8 removido na abertura do arquivo; CRLF, espaços extras e linhas em branco tolerados.
- Produtores deduplicados por nome com `Map<String, Producer>`; nomes repetidos no mesmo campo viram um só pelo `Set`.
- Caminho configurável em `razzies.csv.path` (padrão `classpath:Movielist.csv`, aceita `file:`), ligado ao record `CsvProperties` via `@ConfigurationPropertiesScan`.
- Commons CSV 1.14.1 com versão explícita no `pom.xml`.

### O que a IA fez

- Criou o pacote `br.com.outsera.razzies.csv` com `CsvProperties`, `MovieCsvParser`, `MovieCsvRecord`, `MovieCsvLoader` e `MovieCsvException`.
- Adicionou a dependência no `pom.xml` e a propriedade em `application.properties`.
- Validou o startup com o jar (`--spring.main.web-application-type=none`) em CSVs temporários:

| Cenário | Resultado |
|---|---|
| CSV padrão | 206 filmes e 359 produtores (conferido com script independente) |
| BOM, CRLF, cabeçalho em maiúsculas, linha em branco, `YES` | 2 filmes e 3 produtores |
| Ano não numérico | `Invalid CSV line 3: year 'abc' is not a number` |
| Coluna faltando na linha | `Invalid CSV line 3: expected 5 columns but found 4` |
| Coluna faltando no cabeçalho | `... is missing columns [studios]` |
| Arquivo inexistente | `CSV file not found: ...` |

- `./mvnw test` (`contextLoads`): sucesso, com a carga do CSV padrão no log.
- Testes de integração automatizados desses cenários ficam para a etapa de testes.

---

## Interação 7 — Revisão da importação e validação dos dados importados

**Data:** 2026-10-22

### Prompts

> Antes de avançarmos, utilize a skill code-review para revisar a implementação da importação do CSV, verifique se tem problemas no parser, na transação e na persistência dos relacionamentos.

> Também confira se os 359 produtores informados são realmente distintos e valide a consulta findProducerWins() com os dados importados.

### Skills e ferramentas

- `code-review`, executada em segundo plano sobre o pacote `csv`, `domain` e `repository`.
- Teste JUnit temporário rodado numa cópia do projeto fora do repositório: extraiu do H2 os produtores, os pares de `findProducerWins()` e contagens via `JdbcTemplate`. Os dados foram comparados com um script Python independente.

### Resultado da validação

| Verificação | Resultado |
|---|---|
| Linhas em `producer` / `count(distinct name)` / `count(distinct lower(trim(name)))` | 359 / 359 / 359 |
| Nomes iguais aos do oráculo Python | sim |
| Filmes / vencedores / linhas em `movie_producer` | 206 / 42 / 472 |
| Filmes sem produtor / produtores sem filme | 0 / 0 |
| `findProducerWins()` igual ao oráculo | sim, 98 pares, ordenados por produtor e ano |
| Produtores com 2+ vitórias | Bo Derek (1984, 1990), Buzz Feitshans (1985, 1994), Joel Silver (1990, 1991), Matthew Vaughn (2002, 2015) |
| Resíduos de separador nos nomes (`and`, `,`, `;`, espaços duplos) | nenhum |

### Inconsistências do próprio CSV

- Grafias diferentes da mesma pessoa: `Michael De Luca` (2015, vencedor) e `Michael DeLuca` (2008, vencedor); `Steve Perry` / `Steven Perry`; `Norm Golightly` / `Norman Golightly`; `Rick Alvares` / `Rick Alvarez`.
- Separador sem espaço na linha 171: `Brian Robbinsand Sharla Sumpter Bridgett` vira um único produtor.
- Nenhuma afeta o resultado com este CSV: unificar `Michael De Luca` geraria intervalo 7, entre o min (1) e o max (13); a linha 171 não é vencedora.
- Decisão proposta: não aplicar normalização aproximada de nomes. O sistema trata o texto do CSV como fonte da verdade, e heurísticas de similaridade arriscariam unir pessoas diferentes nos CSVs da avaliação.

### Resultado da code-review e triagem

A revisão retornou 10 achados. Triagem feita pela IA, aguardando decisão do desenvolvedor:

| # | Achado | Triagem |
|---|---|---|
| 1 | `winner` diferente de `yes` vira `false` sem aviso | Procede: falhar em valor desconhecido (só `yes` ou vazio). |
| 2 | `isConsistent()` rejeita linha sem o `;` final | Procede em parte: aceitar `winner` ausente no fim; manter falha com colunas a mais ou faltando antes. |
| 3 | Linha duplicada gera duas vitórias no mesmo ano (intervalo 0) | Procede, mas é regra do cálculo: tratar no serviço de intervalos (decisão em aberto desde a interação 1). |
| 4 | Espaço duplo ou maiúsculas diferentes separam o mesmo produtor | Procede em parte: colapsar espaços internos; comparação sem diferenciar maiúsculas a decidir. |
| 5 | `ApplicationRunner` roda com a porta HTTP já aberta | Confirmado, impacto baixo: já registrado na interação 5; documentar. |
| 6 | Importação não idempotente | Descartado: H2 em memória com `create-drop`, recriado a cada startup. |
| 7 | Separador sensível a maiúsculas; `&` e `Jr.` não tratados | Procede só `AND` maiúsculo; `&` e vírgula em `Jr.` são especulativos e conflitam com o formato do enunciado. |
| 8 | Número de linha errado na última linha sem `\n`; "expected 5 columns" fixo | Procede: usar número do registro e quantidade real do cabeçalho. |
| 9 | Ano zero/negativo aceito; vencedor sem produtor sem aviso | Procede em parte: rejeitar ano não positivo; logar aviso para vencedor sem produtor. |
| 10 | `IDENTITY` desliga batch insert; `computeIfAbsent` em `stream().map()` | Batch descartado (206 linhas, YAGNI); trocar o stream com efeito colateral por laço procede. |

---

## Interação 8 — Correções da code-review e simplificação

**Data:** 2026-10-22

### Prompt

> pode aplicar as melhorias identificadas no code-review, use as skills java-implement e simplify para essas correções. vamos manter os nomes diferenciando maiúsculas e minúsculas, preservando como está no arquivo, depois vejo se alteramos isso, prefiro cotinuar exigindo as cinco colunas CSV, mesmo se winner estiver vazio. Depois compile e valide novamente a importação, sem avançar para o cálculo dos intervalos.

### Decisões do desenvolvedor

- Nomes de produtores diferenciam maiúsculas e minúsculas, preservados como no arquivo (pode ser revisto depois).
- Toda linha precisa ter a mesma quantidade de colunas do cabeçalho (cinco), mesmo com `winner` vazio. O item 2 da code-review não foi aplicado.

### Correções aplicadas (`java-implement`)

- `winner`: aceita só `yes` (sem diferenciar maiúsculas) ou vazio; outro valor falha o startup.
- Ano precisa ser inteiro positivo.
- Espaços repetidos dentro do nome do produtor viram um só.
- Separador ` and ` sem diferenciar maiúsculas.
- Mensagens usam o número do registro (linha de dados, sem o cabeçalho) e a quantidade real de colunas do cabeçalho.
- Aviso no log para filme vencedor sem produtores.
- `computeIfAbsent` dentro de streams trocado por laços no `MovieCsvLoader`.

### Simplificação (`simplify`, 4 revisores em paralelo: reuso, simplificação, eficiência, altitude)

Aplicado:
- `CSVRecord.isConsistent()` em vez de comparar tamanhos manualmente; a quantidade de colunas sai de `row.getParser()` só na mensagem de erro, sem parâmetro extra por linha.
- `parser.getHeaderMap()`, que já ignora maiúsculas com `setIgnoreHeaderCase(true)`, em vez de montar um `Set` em minúsculas.
- `Collectors.toUnmodifiableSet()` em `splitProducers`, evitando a segunda cópia em `MovieCsvRecord`.

Não aplicado:
- `BOMInputStream` do commons-io: só viria como dependência transitiva; o tratamento manual tem 5 linhas e não justifica uma dependência nova.
- Colapsar espaços também em `title` e `studios`: esses campos não entram no cálculo e ficam como no arquivo.
- Número da linha física nas mensagens: `getCurrentLineNumber()` é impreciso na última linha sem quebra; o número do registro é exato e está documentado no Javadoc.
- Remover a checagem `resource.exists()`: mantida pela mensagem de erro mais clara.
- Reescrever `parseYear` sem a variável declarada fora do `try`: puramente cosmético.

### Validação

Execução do jar com `--spring.main.web-application-type=none`:

| Cenário | Resultado |
|---|---|
| CSV padrão | 206 filmes e 359 produtores |
| BOM, CRLF, cabeçalho em maiúsculas, linha em branco, `YES` | 2 filmes e 3 produtores |
| `Joel  Silver AND Ann, and Bob`, ` Joel Silver `, `joel silver`, vencedor sem produtor | 4 filmes, 4 produtores (`Ann`, `Bob`, `Joel Silver`, `joel silver`) e aviso no log |
| Cabeçalho com 6 colunas e linha com 5 | `Invalid CSV record 2: expected 6 columns but found 5` |
| `winner` igual a `true` | `Invalid CSV record 2: winner 'true' must be 'yes' or empty` |
| Ano `0` | `Invalid CSV record 2: year '0' must be positive` |
| Ano `abc` | `Invalid CSV record 2: year 'abc' is not a number` |
| Linha com 4 colunas, com e sem quebra no fim do arquivo | `Invalid CSV record 2: expected 5 columns but found 4` |
| Coluna faltando no cabeçalho / arquivo inexistente | `missing columns [studios]` / `CSV file not found` |

- Banco após importar o CSV padrão (teste temporário fora do repositório): 359 produtores distintos, 472 vínculos, `findProducerWins()` com os mesmos 98 pares validados contra o oráculo na interação 7.
- `./mvnw test`: sucesso.

---

## Interação 9 — Normalização de produtores e filmes sem produtor

**Data:** 2026-10-22

### Prompts

> quero melhorar a normalização dos produtores, vamos considerar nomes iguais mesmo com diferenças de maiúsculas, minúsculas ou espaços, mas vamos preservar o texto original para o retorno.

> podemos aceitar filmes sem produtor, registrando um aviso, já que eles não participam do cálculo dos intervalos, utilize a skill java-implement para esses ajustes e valide se o resultado com o CSV original continua igual.

### Skills e ferramentas

- `java-backend-skills:java-implement`.

### Decisões tomadas

- `Producer` ganhou a coluna `normalized_name` (sem espaços nas pontas, espaços internos colapsados, minúsculas com `Locale.ROOT`), que passa a ser a identidade: `equals`/`hashCode` e a constraint `uk_producer_normalized_name`.
- `name` guarda a grafia original da **primeira ocorrência** no arquivo (só sem espaços nas pontas) e é o que `findProducerWins()` devolve.
- A regra de normalização fica em `Producer.normalize()` (domínio); o parser só separa e apara os nomes, e o loader deduplica pela chave normalizada.
- Grafias que diferem por mais que espaços e maiúsculas (`Michael De Luca` / `Michael DeLuca`) continuam distintas, conforme a decisão da interação 7.
- Filmes sem produtor são importados com aviso no log (antes o aviso era só para vencedores).

### Problema encontrado na validação

- Um campo `, and ` gerava um produtor chamado `and`: o `setTrim` remove o espaço final e o separador `,\s*and\s+` exigia espaço depois de `and`.
- Correção: separador `,\s*and\b\s*|,\s*|\s+and\b\s*`. O limite de palavra (`\b`) mantém nomes como `Anderson`, `Andy` e `Sandra` intactos.

### Validação (teste temporário fora do repositório)

| Cenário | Resultado |
|---|---|
| CSV original | 206 filmes, 359 produtores, 472 vínculos, nenhum aviso; nomes e os 98 pares de `findProducerWins()` idênticos ao oráculo |
| `Joel  Silver AND Ann, and Bob`, ` Joel Silver `, `joel silver`, `BOB and bob` | 3 produtores; vitórias de `Joel  Silver` em 1980, 1981 e 1982 (grafia da primeira ocorrência); `BOB and bob` vira uma vitória de `Bob` |
| Produtores vazios, só espaços e `, and ` | filmes importados, 3 avisos no log, nenhum produtor fantasma |
| `Ann Anderson, Andy and Sandra, and Bob AND` | `Ann Anderson`, `Andy`, `Sandra`, `Bob` |

- `./mvnw test`: sucesso.

### Ajuste final: espaços normalizados no nome armazenado

> armazenar o nome com os espaços normalizados, mantendo a capitalização original,faça esse último ajuste e valide novamente a importação

- `Producer.name` passa a guardar a grafia da primeira ocorrência com espaços nas pontas removidos e espaços internos colapsados, mantendo as maiúsculas originais (`Joel  Silver` vira `Joel Silver`).
- `normalize()` reaproveita o mesmo colapso de espaços (`collapseSpaces`) e só acrescenta a conversão para minúsculas.
- Revalidação: CSV original idêntico ao oráculo (359 produtores, 472 vínculos, 98 pares em `findProducerWins()`); cenário de nomes retorna `Joel Silver` em 1980, 1981 e 1982; cenário sem produtores mantém 3 avisos e nenhum produtor indevido; `./mvnw test` com sucesso.
