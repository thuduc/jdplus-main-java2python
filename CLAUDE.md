# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JDemetra+ is a seasonal adjustment and time series analysis tool developed by the National Bank of Belgium. This is a Java 17+ Maven project implementing TRAMO/SEATS and X-12ARIMA methods.

## Build and Development Commands

### Building the project
```bash
# Full build
mvn clean install

# Fast build (skips enforcer, modernizer, and heylogs checks)
mvn clean install -Pfast-build

# Build specific module
mvn clean install -pl jdplus-main-base -am
```

### Running tests
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl jdplus-main-base/jdplus-toolkit-base-parent/jdplus-toolkit-base-core

# Run a specific test class
mvn test -Dtest=QRSolverTest

# Skip tests during build
mvn clean install -DskipTests
```

### Common Maven goals
```bash
# Check dependencies
mvn dependency:tree
mvn dependency:analyze

# Update versions
mvn versions:set -DnewVersion=3.5.2
mvn versions:commit
```

## Architecture and Structure

The project follows a modular structure organized by:
- **Topics**: `toolkit`, `tramoseats`, `x13`, `sa`, `spreadsheet`, `sql`, `text`
- **Layers**: `base` (core libraries), `cli` (command-line), `desktop` (GUI)

Module naming convention: `jdplus-TOPIC-LAYER-STEREOTYPE`

Key structural patterns:
- Each topic has API, core implementation, and format-specific modules (xml, protobuf, etc.)
- Desktop modules use NetBeans Platform
- Uses Java modules (module-info.java) with JPMS
- Annotation processors: Lombok, java-service-util, java-design-util

## Key Technologies and Patterns

- **Testing**: JUnit 5 (Jupiter and Vintage) with AssertJ
- **Build**: Maven 3.6+ with enforcer rules for quality
- **Java**: Requires Java 17+, targets Java 17 bytecode
- **Serialization**: Protocol Buffers for data exchange
- **Annotations**: Lombok for boilerplate reduction, JSpecify for null-safety
- **Desktop**: NetBeans Platform for GUI modules