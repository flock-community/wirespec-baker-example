# Wirespec Baker Example

This project demonstrates how to extend [Wirespec](https://github.com/flock-community/wirespec) with custom preprocessing and code generation capabilities.

## Project Overview

The Wirespec Baker Example is a multi-module Maven project that showcases:

1. A custom **Wirespec Preprocessor** that modifies OpenAPI specifications before they're processed
2. A custom **BakerKotlinEmitter** that extends the standard Wirespec code generation

The project uses the standard Swagger Petstore OpenAPI specification as an example and demonstrates how to customize the generated code.

## Project Structure

- **app** - Main application module containing:
  - OpenAPI specification
  - Custom preprocessor
  - Spring Boot application
- **emitter** - Custom emitter module containing:
  - BakerKotlinEmitter implementation

## Wirespec Preprocessor

The Wirespec Preprocessor (`PreProcessor.kt`) is responsible for modifying the OpenAPI specification before it's processed by Wirespec. In this example, the preprocessor:

1. Filters the paths to only include those with "/pet" in them
2. Adds "Baker" suffix to the operationId of GET, PUT, and POST operations

```kotlin
class PreProcessor : (String) -> String {
    override fun invoke(input: String) =
        OpenAPI.decodeFromString(input)
            .run { copy(paths = paths
                .filterKeys { it.value == "/pet" }
                .mapValues { it.value.copy(
                    get = it.value.get?.copy(operationId = it.value.get?.operationId?.plus("Baker")),
                    put = it.value.put?.copy(operationId = it.value.put?.operationId?.plus("Baker")),
                    post = it.value.post?.copy(operationId = it.value.post?.operationId?.plus("Baker")),
                ) }
            ) }
            .let(OpenAPI::encodeToString)
}
```

For example, the "updatePet" operation ID in the original OpenAPI spec becomes "updatePetBaker" after preprocessing.

## BakerKotlinEmitter

The BakerKotlinEmitter (`BakerKotlinEmitter.kt`) extends the standard KotlinEmitter from Wirespec to generate additional code. In this example, the emitter:

1. Generates all the standard Wirespec code (models, endpoints, etc.)
2. Additionally generates an "Interaction" object for each endpoint

```kotlin
class BakerKotlinEmitter(val packageName: PackageName, emitShared: EmitShared): KotlinEmitter(packageName, emitShared) {

    override fun emit(module: Module, logger: Logger): NonEmptyList<Emitted> {
        return super.emit(module, logger)
            .plus(module.statements.filterIsInstance<Endpoint>()
            .map {
                println("Baking endpoint ${it.identifier}")
                val name = emit(it.identifier) + "Interaction"
                Emitted("${packageName.toDir()}/interaction/${name}", """
                    package ${packageName.value}
                    
                    object ${name}
                """.trimIndent()) }
            )
    }
}
```

For each endpoint, it creates a simple object with the name of the endpoint plus "Interaction" (e.g., "AddPetBakerInteraction").

## Configuration

The custom preprocessor and emitter are configured in the Maven build using the Wirespec Maven plugin:

```xml

<plugin>
  <groupId>community.flock.wirespec.plugin.maven</groupId>
  <artifactId>wirespec-maven-plugin</artifactId>
  <version>${wirespec.version}</version>
  <dependencies>
    <dependency>
      <groupId>kotlin.baker</groupId>
      <artifactId>wirespec-baker-example-emitter</artifactId>
      <version>${project.version}</version>
    </dependency>
  </dependencies>
  <executions>
    <execution>
      <id>openapi</id>
      <goals>
        <goal>convert</goal>
      </goals>
      <configuration>
        <input>${project.basedir}/src/main/openapi/petstorev3.json</input>
        <output>${project.build.directory}/generated-sources</output>
        <preProcessor>${project.basedir}/src/main/wirespec/community/flock/wirespec/baker/preprocessor/PreProcessor.kt
        </preProcessor>
        <format>OpenAPIV3</format>
        <emitterClass>kotlin.baker.emitter.BakerKotlinEmitter</emitterClass>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Usage

To use this example:

1. Clone the repository
2. Build the project with Maven: `mvn clean install`
3. Examine the generated code in `app/target/generated-sources`

## Extending for Your Own Project

To create your own custom preprocessor and emitter:

1. Create a preprocessor class that implements the `(String) -> String` function interface
2. Create an emitter class that extends `KotlinEmitter` and overrides the `emit` method
3. Configure the Wirespec Maven plugin to use your custom classes

## Requirements

- Java 17 or higher
- Maven 3.6 or higher