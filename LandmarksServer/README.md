# Landmarks gRPC Server

Este componente é responsável pela submissão, armazenamento, análise e consulta de fotografias com pontos de referência (landmarks).

Utiliza gRPC para comunicação e integra-se com serviços externos para armazenamento de imagens, metadados e consulta de mapas.

## Requisitos

- Java 21+
- Maven
- Projeto Google Cloud configurado com as APIs necessárias: Firestore, Cloud Storage, Maps API, Pub/Sub.
- Conta de serviço com as permissões adequadas para ter acesso aos serviços do Google Cloud:
  - Cloud Datastore Owner;
  - Pub/Sub Admin;
  - Storage Admin;
- Variáveis de ambiente configuradas:
  - `GOOGLE_APPLICATION_CREDENTIALS`: Caminho para o ficheiro da conta de serviço do Google Cloud.
  - `GOOGLE_MAPS_API_KEY`: Chave da API do Google Maps.
- Definir a classe `Config` do componente `LandmarksDomain` com o ID do projeto Google Cloud.

## Build e Execução

```bash
# Compilar o projeto
mvn clean install

# Executar o servidor gRPC
java -jar target/LandmarksServer-1.0-jar-with-dependencies.jar
```
