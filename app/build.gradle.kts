plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
    archiveBaseName.set("java-27")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf(
        "--patch-module", "java.base=${project.file("src/main/java")}"
    ))
}
