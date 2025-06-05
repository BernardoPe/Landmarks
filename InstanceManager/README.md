# InstanceManager

Este componente é responsável por gerir o ciclo de vida das instâncias dos componentes `LandmarksApp` e `LandmarksServer` em execução na infraestrutura do Google Cloud.

Facilita o arranque, monitorização e paragem dos serviços necessários, apoiando a operação dos componentes distribuídos.

## Requisitos

- Java 21+
- Maven
- Projeto Google Cloud com as seguintes APIs ativadas: Firestore, Cloud Storage, Pub/Sub.
- Conta de serviço com permissões adequadas:
    - Compute Admin (para gerir instâncias de VM, se aplicável)
- Variáveis de ambiente:
    - `GOOGLE_APPLICATION_CREDENTIALS`: Caminho para o ficheiro da conta de serviço do Google Cloud.
- Configurar a classe `Config` no componente `LandmarksDomain` com o ID do projeto Google Cloud e os nomes dos recursos.

## Build e Execução

```bash
# Compilar o projeto
mvn clean install

# Executar o gestor de instâncias
java -jar target/InstanceManager-1.0-jar-with-dependencies.jar
```