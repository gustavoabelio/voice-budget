# Voice Budget

A voice-driven expense tracking API built with Java, Spring Boot, and Spring AI.

The original workflow receives an audio file, transcribes speech, interprets the command with a language model, calls Java tools to access financial transactions, and returns an audio response.

## Added feature

This version adds **expense totals and transaction counts by category**, available through both a REST endpoint and an AI tool.

- Endpoint: `GET /transactions/{category}/total`.
- Tool: `get-category-total`.
- Java retrieves the transactions and calculates the total; the AI uses that result in its response.
- Amounts are stored as integer cents and converted to Brazilian reais with `BigDecimal` in the summary.
- An empty category returns a transaction count of zero and a total of `0.00`.
- The calculation fails explicitly if the sum exceeds the capacity of `long`, rather than returning an incorrect total.

For example, two `GROCERIES` transactions worth 2550 and 1025 cents produce:

```json
{
  "category": "GROCERIES",
  "transactionCount": 2,
  "totalInReais": 35.75
}
```

The categories inherited from the original project are `GROCERIES`, `PHARMA` (pharmacy), and `AUTO` (vehicle expenses).

## Technologies

Java 25, Spring Boot 4.0.5, Spring AI 2.0.0-M4, Spring Data JPA, MySQL, Docker Compose, Gradle Wrapper, JUnit, and Mockito. The framework versions from the instructor's project were retained.

## Build and test without OpenAI

Requirement: JDK 25. In PowerShell, inside the `voice-budget` directory:

```powershell
.\gradlew.bat test
```

This command runs the tests added in this version using Mockito and MockMvc. They use a mocked repository and **do not start the Spring Boot application, a database, Docker, or real AI clients**. No API key, payment card, or purchased credits are required. The first Gradle run needs an internet connection to download dependencies.

The tests cover totals in reais, transaction counts, an empty category, cent precision, numeric overflow, the endpoint's JSON response, and rejection of an unknown category.

On Linux/macOS, use `./gradlew test`.

## Run the application with the real integration

In addition to JDK 25, you need Docker Desktop running and access to the OpenAI API with a valid key and available quota or credits. Real model requests may incur charges on the account associated with that key.

In PowerShell, configure the key in your local environment:

```powershell
$env:OPENAI_API_KEY = "YOUR_API_KEY"
.\gradlew.bat bootRun
```

Spring Boot uses `compose.yml` to start MySQL. The API runs on port 8080, and the database is exposed on port 3307.

The repository contains only the `${OPENAI_API_KEY}` placeholder, not an actual key. Each person supplies their own credentials when running the project. Storing the source code on GitHub does not make API requests by itself.

## Test the REST workflow

With the application running, open another PowerShell window. The examples below use `irm`, the alias for `Invoke-RestMethod`.

```powershell
$body = @{ description = "Grocery store"; category = "GROCERIES"; amount = 2550 } | ConvertTo-Json
irm -Method Post -Uri "http://localhost:8080/transactions" -ContentType "application/json" -Body $body

$body = @{ description = "Bakery"; category = "GROCERIES"; amount = 1025 } | ConvertTo-Json
irm -Method Post -Uri "http://localhost:8080/transactions" -ContentType "application/json" -Body $body

irm "http://localhost:8080/transactions/GROCERIES/total"
```

If the category had no previous records, the last request returns two transactions totaling BRL 35.75. Input amounts in the `amount` field are expressed in cents. These endpoints access the database without calling OpenAI; however, the current application still requires a key to initialize all of its components.

## Test the voice workflow

Speech transcription is configured for Brazilian Portuguese. Record a command such as "Quanto gastei no total com mercado?" ("How much have I spent on groceries in total?"). Upload the audio file:

```powershell
curl.exe --fail-with-body -X POST "http://localhost:8080/transactions/ai" -F "file=@C:/path/command.m4a" --output response.mp3
```

The instructor's sample audio files are also available in `src/test/resources/audio`. An error response is not a valid MP3 file; check the application logs and HTTP status if the request fails.

This workflow calls OpenAI's real transcription, chat, and speech generation services. The new tool is intended to retrieve the category total and provide that result to the assistant's response.

## Original integration tests

The instructor's integration tests and Spring context test were preserved but separated from the default `test` task. To run them explicitly, with Docker and an API key configured:

```powershell
.\gradlew.bat integrationTest
```

These tests may consume the OpenAI API. They are not run automatically by `test` or `build`.

## Validation status

Six tests were run successfully with JDK 25: four use case tests and two endpoint tests using MockMvc. They ran without an OpenAI key or a running database. Mockito is configured as a test agent so that it does not need to attach dynamically to the Java 25 JVM.

Passing these tests validates the exercised business logic and HTTP contract, not the external services. The real voice workflow and MySQL persistence still need validation with the running application; they were not tested with real credentials during this implementation.

## Project structure and learning focus

- `domain`: the transaction model and repository contract inherited from the original project.
- `application`: use cases, including `GetCategoryTotalUseCase`, and their outputs.
- `infrastructure`: HTTP endpoints and JPA persistence.

The added feature demonstrates how a use case queries a repository, sums integer cents without delegating arithmetic to the AI, and exposes the same behavior through HTTP and `@Tool`. It also illustrates the difference between testing with a mocked repository and integrating with a real database and AI models.

## Credits

The voice integration, transaction models, persistence, and initial architecture are based on the [Spring AI budgeting example developed by instructor Rodrigo Poiani](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai).

This version adds category totals, the corresponding endpoint and tool, prompt instructions, unit and HTTP tests, separate test tasks, and documentation. The project name was changed to `voice-budget`; the original Java packages were retained.
