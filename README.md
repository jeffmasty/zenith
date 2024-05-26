# Zenith Intelligence Tracker

A demonstration of Spring.io's OpenAi support for Java. (https://spring.io/projects/spring-ai)

**Note:** The key to your OpenAI API needs to be exported into the runtime environment as: spring.ai.openai.api-key 

Create an account and generate an API key here: https://platform.openai.com/api-keys

Text chat, image generation and document embedding (RAG) is wrapped in a Swing GUI managed by Spring Boot. Vocal input through a microphone is transcribed by the AI, responses may be read back in various AI voices and settings such as model type, temperature, audio speed and image size are available.

## Build 
Built with Java 21, Maven and Lombok (https://projectlombok.org/)
This project was built against the development branch of the Spring AI API (1.0.0-SNAPSHOT), which changes frequently.

### Screenshot
![JudahZone logo](/screenshot.png)

TODO:

- tokens info
- buffer voice request and response
- voice response volume and replay
- microphone accelerator keys
- rag session info and CRUD
- fine-tune vectorDB settings
- dall-e-3 style setting
- REST app
- linux shell AI assistant

