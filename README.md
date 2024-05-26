# Zenith Intelligence Tracker


This project demonstrates a portion of Spring.io's OpenAi support. (https://spring.io/projects/spring-ai)

**Note:** The key to your OpenAI API needs to be exported into the runtime environment as SPRING_AI_OPENAI_API_KEY. Create an account and generate an API key here: https://platform.openai.com/api-keys

Text chat, image generation and document embedding (RAG) functionality is wrapped in a Swing GUI managed by Spring Boot. Vocal input through a microphone is transcribed by the AI, responses may be read back in various AI voices and appropriate settings are made available (model type, temperature, audio speed, etc).

## Build 
Built with Java 21, Maven and Lombok (https://projectlombok.org/)
This project was built against the development branch of the Spring AI API (1.0.0-SNAPSHOT), which changes frequently.

### Screenshot
![JudahZone logo](/screenshot.png)

TODO:
- question history = up/down cursor, AIText exe
- Microphone spacebar stops record, record Accelerator key,  documents & image mic hookup
- dirty rag
- WavPlayer bean

- tokens info
- buffer voice request and response
- voice volume
- rag session info and CRUD
- vectorDB settings and fine-tune
- REST app
- linux shell AI assistant

