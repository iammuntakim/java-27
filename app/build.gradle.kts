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