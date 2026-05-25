Experimental JAVA-27 For Android™

An advanced, experimental engineering framework designed to explore the boundaries of the Java Virtual Machine architecture. This repository hosts isolated, customized structural overrides for foundational java.lang and jdk.internal components, establishing a sandboxed research environment for low-level runtime experimentation. The primary focus of this engineering initiative is to dissect the bootstrap sequence of the modern virtual machine, investigate the direct performance impacts of bypassing or altering standard API contracts, and construct a hardened, modified environment where traditional runtime expectations can be safely subverted or reconfigured for academic evaluation. By modifying foundational behaviors directly within the primary packages, this project serves as an infrastructure baseline for deep runtime inspection and diagnostic innovation.

Project Status and Legal Disclaimer

Experimental Nature
This repository is an ongoing, highly experimental academic proof of concept. It is not intended, designed, or stable enough for production environments. The code modifies fundamental behaviors of core Java classes and standard API contracts, which may result in runtime instability, thread deadlock, memory corruption, or unpredictable JVM behavior if executed outside of an isolated test harness. Due to the destructive potential of breaking standard system layer invariants, all compilation and execution should take place within virtualized or heavily isolated sandboxes.

Open Source and Intellectual Property Notice
* Upstream Packages: This project interacts with and implements structures derived from official, publicly available OpenJDK distributions, including standard source trees utilized by Google and the wider Java maintainer ecosystem.
* Fair Use & Non-Infringement: All references to standard corporate entities, package naming conventions, and upstream OpenJDK structures are used strictly for technical compatibility, reference, and educational evaluation under Fair Use principles. 
* Zero Commercial Intent: This software is published with no commercial intent, provides no financial gain, and is intended solely to understand internal JVM mechanics. It does not distribute proprietary binaries or closed-source modifications belonging to any single entity.
* Copyright Status: This project is explicitly released into the public domain with no copyright claimed by the author. It is a community-facing, open research asset designed to foster legal, open educational collaboration without infringing on corporate trademarks or proprietary distribution models.

Key Components Under Evaluation

* java.lang.System / Runtime / Class: Intercepts vital platform workflows, permitting custom logic injection into memory management, reflection arrays, and basic native bindings.
* java.lang.ClassLoader: Facilitates experimental class-loading strategies and alternative bytecode parsing behaviors.
* java.lang.Thread / ThreadGroup: Explores custom threading mechanics and alternative concurrency management configurations within the base layer.
* jdk.internal.main.runtime.classes.Shared: Manages underlying environment configurations and boot-strapping mechanics.

Technical Specifications and Tooling

The environment relies on modern build automation and declarative dependency management to assemble the experimental runtime targets.

Build Tooling Configuration
* Automation Engine: Gradle via Kotlin DSL wrappers.
* Dependency Locking: Managed dynamically via centralized Version Catalogs inside gradle/libs.versions.toml for complete build reproducibility.
* Compilation Model: Highly isolated Java compilation tasks tailored to override internal packages without contaminating standard local SDK paths.

Getting Started
<img width="4000" height="2500" alt="Image" src="https://github.com/user-attachments/assets/08a06d4a-3187-4d37-aff0-8822b2ddef09" />

Prerequisites
* A validated Java Development Kit (JDK 21 or newer recommended for modern experimental features).
* A terminal environment supporting execution of standard shell or batch scripts.

Compiling the Platform
To compile the custom runtime targets and execute the experimental test suites, run the following commands in the root directory:

For Unix/Linux/macOS platforms:
./gradlew build

For Windows environments:
gradlew.bat build

License

This project is completely dedicated to the public domain. It is provided "as-is", without warranty of any kind, explicit or implied. Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.
