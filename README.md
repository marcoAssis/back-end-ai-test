# Razzies API

API REST que lê a lista de indicados e vencedores da categoria Pior Filme do Golden Raspberry Awards a partir de um CSV e retorna os produtores com o menor e o maior intervalo entre duas vitórias consecutivas.

**Stack:** Java 21, Spring Boot 4.1.1, Spring Data JPA, H2 em memória, Apache Commons CSV, springdoc-openapi (Swagger UI).

## Requisitos

- JDK 21 ou superior.
- Nenhuma outra instalação: o Maven Wrapper está no projeto e o banco H2 roda embarcado em memória.

## Executar a aplicação

```bash
cd razzies
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A aplicação sobe em `http://localhost:8080` e importa `src/main/resources/Movielist.csv` antes de aceitar requisições.

### Usar outro CSV

O caminho é configurado pela propriedade `razzies.csv.path`, que aceita `classpath:` ou `file:`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--razzies.csv.path=file:/caminho/para/filmes.csv
```

Ou, com o jar:

```bash
./mvnw package -DskipTests
java -jar target/razzies-0.0.1-SNAPSHOT.jar --razzies.csv.path=file:/caminho/para/filmes.csv
```

O arquivo deve seguir o formato do original: separador `;`, cabeçalho `year;title;studios;producers;winner` e cinco colunas em todas as linhas (`winner` igual a `yes` ou vazio).

## Endpoint

`GET /producers/award-intervals`

```bash
curl http://localhost:8080/producers/award-intervals
```

Resposta com o CSV original:

```json
{
  "min": [
    { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
  ],
  "max": [
    { "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }
  ]
}
```

Swagger UI: `http://localhost:8080/swagger-ui.html` (especificação OpenAPI em `/v3/api-docs`).

## Testes de integração

```bash
cd razzies
./mvnw test
```

Todos os testes sobem o contexto completo do Spring, importam um CSV real no H2 e chamam a API via MockMvc ou consultam o banco:

- `ProducerIntervalControllerTests`: JSON completo da resposta com o CSV original e com CSVs de cenário em `src/test/resources/csv/` (empates, vitórias no mesmo ano, mais de duas vitórias, sem intervalos, variações de nome, BOM/CRLF); status 405 e 406.
- `MovieCsvImportTests`: dados gravados no banco após a importação.
- `MovieCsvStartupFailureTests`: CSVs inválidos em `src/test/resources/csv/invalid/` impedem a inicialização.
- `OpenApiDocsTests`: documentação OpenAPI e Swagger UI.

Cada CSV de cenário roda em um contexto próprio, com um banco H2 próprio: o Spring Boot gera um nome único para cada banco (`spring.datasource.generate-unique-name`, ativo por padrão). Não fixe `spring.datasource.url`, porque isso faria os contextos de teste compartilharem dados.

## Regras de negócio

- **Normalização de produtores:** a coluna `producers` é separada por `,`, ` and ` e `, and `, sem diferenciar maiúsculas. Nomes que diferem só em maiúsculas ou espaços são o mesmo produtor. A resposta usa a grafia da primeira ocorrência no arquivo, com os espaços normalizados. Grafias realmente diferentes (`Michael De Luca` e `Michael DeLuca`) continuam sendo produtores distintos.
- **Anos distintos:** vitórias do mesmo produtor no mesmo ano contam como uma só, então não existe intervalo 0. O intervalo é sempre entre vitórias consecutivas.
- **Empates:** `min` e `max` trazem todos os produtores empatados, ordenados por produtor e ano da vitória anterior. Sem nenhum produtor com duas vitórias, as duas listas vêm vazias.
- **CSV inválido:** arquivo inexistente, coluna faltando, ano não numérico ou não positivo, título vazio ou `winner` diferente de `yes`/vazio impedem a aplicação de subir, com o número do registro na mensagem. Filmes sem produtor são importados com um aviso no log e não entram no cálculo.

## Uso de IA

O desenvolvimento foi feito com o Claude Code, usando as skills `java-backend-skills` (spring-boot, spring-data, spring-web, spring-testing, java-design, java-implement), `code-review` e `simplify`. O registro de prompts, skills, decisões e validações de cada etapa está em [AI_LOG.md](AI_LOG.md).
