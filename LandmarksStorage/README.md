# LandmarksStorage

Este componente é responsável por armazenar e recuperar fotos de pontos de referência (landmarks) e os seus metadados associados nos serviços Google Cloud.

## Funcionalidades

- Armazena fotos no Google Cloud Storage.
- Armazena e recupera metadados no Firestore.
- Fornece uma interface para outros componentes interagirem com o armazenamento.

## Requisitos

- Java 21+
- Maven
- Projeto Google Cloud com as seguintes APIs ativadas:
  - Cloud Storage
  - Firestore
- Conta de serviço com as seguintes permissões:
  - Cloud Datastore Owner
  - Storage Admin
- Variáveis de ambiente:
  - `GOOGLE_APPLICATION_CREDENTIALS`: Caminho para o ficheiro da conta de serviço do Google Cloud.
- Configurar a classe `Config` no componente `LandmarksDomain` com o ID do projeto Google Cloud e os nomes do bucket/coleção.

## Build e Execução

```bash
# Compilar o projeto
mvn clean install