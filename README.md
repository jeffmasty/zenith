# Zenith Intelligence Tracker

A Java client that communicates with OpenAI through Spring.io's library. (https://spring.io/projects/spring-ai)  

**Note:** The key to your OpenAI API needs to be exported into the runtime environment as: spring.ai.openai.api-key 

Create an account and generate an API key here: https://platform.openai.com/api-keys

Text chat, image generation and document embeddings (RAG) are wrapped in a Swing GUI managed by Spring Boot. Settings such as model type, temperature, audio speed and image size are available.  Vocal input through a microphone is transcribed by the AI, responses may be read back in various AI voices. 

## Build 
Built with Java 21, Maven and Lombok (https://projectlombok.org/).
This project was built against the development branch of the Spring AI API (1.0.0-SNAPSHOT) which changes frequently.

mvn clean install -DskipTests

### Screenshot
![JudahZone logo](/screenshot.png)

TODO:
- dall-e-3 style setting
- upload images into context
- default.properties
- delete vector docs via chunk Ids (bug)
- vector similarities info
- import pdfs/ChunkMaker 
- logging, exception handling
- tokens info
- buffer voice request and response
- microphone accelerator keys
- ChatCompletionRequest.name for multi-user history
- REST app
- Assistant: AI linux shell (aish)

Recent updates:
- settings and auto-save
- send chat history
- vectorDB model and dimension settings
- Rag Session CRUD DocID -> FilesModel (w/ bug)
- voice response logarithmic volume
- audio player feedback and replay

Icons from iconscout.com and iconfinder.com

