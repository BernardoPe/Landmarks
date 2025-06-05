# Landmarks gRPC Client

Este módulo implementa um cliente em Java para comunicação com um serviço gRPC responsável por identificar marcos geográficos (landmarks) em imagens submetidas.

---
## Requisitos

- **Java 21**
- **Maven** (ou outro sistema de build com suporte a gRPC e Protobuf)
- Acesso à **internet** (para descoberta dinâmica de IPs via Google Cloud Function)
- Servidor gRPC ativo com o serviço `LandmarksService` definido


## Execução

### Build
Para compilar o projeto, execute o seguinte comando:

```bash
mvn clean package
```

### Execução
Para executar o cliente, utilize o comando:

```bash
java -jar target/LandmarksClient-1.0-jar-with-dependencies.jar
```

## Interface de linha de comando
A interface de linha de comando do cliente permite interagir com o serviço gRPC. As opções disponíveis são:

```plaintext
1 - Submit photo
2 - Lookup results
3 - Get photos
99 - Exit
Choose an Option?
```

### Opções
- **1 - Submit photo**: Envia uma foto para o serviço gRPC para identificação de marcos.
- **2 - Lookup results**: Consulta os resultados de identificação de marcos previamente submetidos.
- **3 - Get photos**: Recupera as fotos associadas aos resultados de identificação.
- **99 - Exit**: Encerra o cliente.

## Pressupostos e regras de execução
O serviço gRPC é disponibilizado por instâncias no Google Cloud pertencentes ao grupo instance-group-landmarks-server, na zona europe-west1-b.

O cliente utiliza uma Cloud Function pública para descobrir dinamicamente o IP das instâncias disponíveis:

pgsql
```plaintext
https://europe-west1-cn2425-t1-g06.cloudfunctions.net/funcIPLookup?zone=europe-west1-b&groupName=instance-group-landmarks-server
```

As instâncias devem estar expostas na porta 8000 (configurada em SERVICE_PORT).
O cliente tenta repetidamente conectar-se até obter um IP válido e um canal READY.
