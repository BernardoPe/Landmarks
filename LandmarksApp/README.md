# LandmarksApp

Este componente atua como um serviço Pub/Sub dedicado à análise automática de fotos de pontos de referência (landmarks).

Recebe mensagens de tópicos Pub/Sub com dados de imagens, processa a análise de forma assíncrona e atualiza os resultados nos serviços integrados.

## Funcionalidades

- Inscreve-se em tópicos Pub/Sub para receber pedidos de análise de imagens.
- Processa e analisa fotos de landmarks de forma assíncrona.
- Integra-se com serviços Google Cloud para armazenamento e atualização de metadados.

## Requisitos

- Java 21+
- Maven
- Projeto Google Cloud com as seguintes APIs ativadas: Firestore, Cloud Storage, Pub/Sub.
- Conta de serviço com permissões adequadas:
  - Cloud Datastore Owner
  - Pub/Sub Admin
  - Storage Admin
- Variáveis de ambiente:
  - `GOOGLE_APPLICATION_CREDENTIALS`: Caminho para o ficheiro da conta de serviço do Google Cloud.
- Configurar a classe `Config` no componente `LandmarksDomain` com o ID do projeto Google Cloud e os nomes dos recursos.

## Build e Execução

```bash
# Compilar o projeto
mvn clean install

# Executar o serviço Pub/Sub de análise de imagens
java -jar target/LandmarksApp-1.0-jar-with-dependencies.jar
```