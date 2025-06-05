# LandmarksDomain

This component defines the core domain models and configuration used by the Landmarks application ecosystem.

## Features

- Contains domain classes representing photos, analysis metadata, landmarks, and related entities.
- Provides configuration constants for integration with Google Cloud services.
- Shared dependency for other components (e.g., LandmarksServer, LandmarksStorage).

## Requirements

- Java 21+
- Maven

## Usage

This module is intended to be a common access point for domain models and configuration used across the Landmarks application components. 
It is included as a dependency in other modules such as `LandmarksServer` and `LandmarksStorage`.

## Build 

To build this module, run the following command in the root directory of the component:

```bash
mvn clean install
```

## Example Domain Model

```java
public record AnalysisMetadata(
    String photoId,
    String photoName,
    Status status,
    List<LandmarkMetadata> landmarks
) {}
```