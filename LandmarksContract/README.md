# LandmarksContract

Este componente define os contratos de serviço gRPC e as mensagens Protocol Buffer utilizadas na comunicação entre os componentes da aplicação Landmarks.

## Funcionalidades

- Contém arquivos `.proto` que especificam interfaces de serviço e estruturas de dados.
- Gera classes Java para comunicação via gRPC.
- Dependência partilhada entre componentes servidor e cliente.

## Requisitos

- Java 21+
- Maven

## Build

Para gerar as classes gRPC e protobuf, execute:

```bash
mvn clean install
```