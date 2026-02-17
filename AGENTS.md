# AGENTS.md - grails-server-timing

## Project Overview

This is a **Grails Plugin** that injects Server Timing HTTP headers into responses, implementing
the [W3C Server Timing specification](https://w3c.github.io/server-timing/). It automatically tracks action time, view
rendering time, and total request time, surfacing them in browser DevTools.

- **Language:** Groovy 4.0.30 on Java 17
- **Framework:** Grails 7.0.7
- **Build System:** Gradle 8.14.4 (with wrapper)
- **Current Version:** 0.0.1-SNAPSHOT
- **License:** Apache 2.0

## Skill Files (Best Practices)

Detailed best practices are documented in `.skills/`:

| Skill File                                                                     | Purpose                                               |
|--------------------------------------------------------------------------------|-------------------------------------------------------|
| [`.skills/repository-structure.md`](.skills/repository-structure.md)           | Canonical directory layout and architectural rules    |
| [`.skills/gradle-convention-plugins.md`](.skills/gradle-convention-plugins.md) | Convention plugin patterns, naming, and anti-patterns |
| [`.skills/plugin-project.md`](.skills/plugin-project.md)                       | Plugin project scope: source code + unit tests only   |
| [`.skills/example-apps.md`](.skills/example-apps.md)                           | Example app patterns: integration & functional tests  |

**Read these skill files before making structural changes to the repository.**

## Critical Rules

1. **NEVER add code to the root `build.gradle` to configure subprojects.** No `subprojects {}`, `allprojects {}`, or
   `configure()` blocks. All shared configuration goes through convention plugins in `build-logic/`.
2. **The plugin project contains ONLY plugin code and unit tests.** No integration tests, no functional tests, no
   example controllers or views.
3. **Example apps under `examples/` host all integration and functional tests.** They depend on the plugin via
   `implementation project(':grails-server-timing')` and test it as a real consumer would.
4. **Use Gradle convention plugins to deduplicate.** If two or more subprojects share build logic, extract it into a
   convention plugin in `build-logic/`.
5. **Always use lazy Gradle APIs** to avoid eager initialization (`tasks.register()`, `tasks.named()`, `configureEach`,
   `provider {}`).

## Repository Structure

```
grails-server-timing/
├── .skills/             # Best practice skill files
├── plugin/              # Core Grails plugin (artifact: grails-server-timing)
│   ├── src/main/        #   Plugin source code
│   └── src/test/        #   Unit tests ONLY
├── examples/app1/       # Example Grails app
│   └── src/integration-test/  # Integration & functional tests
├── coverage/            # JaCoCo coverage aggregation
├── docs/                # Asciidoctor documentation
├── build-logic/         # Gradle convention plugins (composite build)
│   └── config/          #   Code style configs (checkstyle, codenarc)
├── .github/workflows/   # CI, release, and release-notes workflows
├── build.gradle         # Root build file (docs + root-publish ONLY)
├── settings.gradle      # Multi-project settings
└── gradle.properties    # Version properties
```

## Build and Test Commands

```bash
# Full build (compile + test)
./gradlew build

# Run only unit tests (plugin module)
./gradlew :grails-server-timing:test

# Run integration tests (example app)
./gradlew :app1:integrationTest

# Aggregated coverage report (unit + integration)
./gradlew :coverage:jacocoAggregatedReport

# Skip tests
./gradlew build -PskipTests

# Run the example app
./gradlew :app1:bootRun

# Generate documentation
./gradlew docs

# Clean build
./gradlew clean build

# Run code style checks only
./gradlew codeStyle

# Skip code style checks
./gradlew build -PskipCodeStyle
```

## SDK Requirements

Use SDKMAN to install the correct tool versions (see `.sdkmanrc`):

- Java: `17.0.17-librca`
- Gradle: `8.14.4`
- Groovy: `4.0.30`

Run `sdk env install` to set up the environment.

## Architecture

The plugin intercepts HTTP requests via a servlet filter and Grails interceptor:

1. **`ServerTimingFilter`** (servlet filter, highest precedence + 100) wraps every request, starts `total` and `other`
   timers.
2. **`ServerTimingInterceptor`** (Grails interceptor) starts an `action` timer in `before()`, stops it and starts a
   `view` timer in `after()`.
3. **`ServerTimingResponseWrapper`** intercepts response commit to inject the `Server-Timing` header before the first
   byte is written.
4. For non-controller requests (e.g., static assets), only `total` and `other` metrics appear.

### Core Classes (plugin/src/main/groovy/org/grails/plugins/servertiming/)

| Class                            | Purpose                                                            |
|----------------------------------|--------------------------------------------------------------------|
| `GrailsServerTimingGrailsPlugin` | Plugin descriptor; registers the filter bean when enabled          |
| `ServerTimingFilter`             | Servlet filter; creates `TimingMetric` per request, wraps response |
| `ServerTimingResponseWrapper`    | Response wrapper; injects `Server-Timing` header on commit         |
| `ServerTimingInterceptor`        | Grails interceptor; tracks action and view timing                  |
| `ServerTimingUtils`              | Reads plugin configuration; auto-enables in DEV/TEST environments  |
| `core/Metric`                    | Single timing metric model with RFC 7230 name validation           |
| `core/TimingMetric`              | Collection of metrics; generates header value                      |

## Configuration

Set in `application.yml`:

| Property                                | Default                                    | Description                               |
|-----------------------------------------|--------------------------------------------|-------------------------------------------|
| `grails.plugins.serverTiming.enabled`   | `null` (auto: on in DEV/TEST, off in PROD) | Explicitly enable/disable the plugin      |
| `grails.plugins.serverTiming.metricKey` | `GrailsServerTiming`                       | Request attribute key for storing metrics |

**Security note:** The plugin is disabled in production by default because timing data could facilitate timing attacks.

## Testing

### Unit Tests (`plugin/src/test/`)

- **`MetricSpec`** — Tests RFC 7230 name validation, start/stop lifecycle, header value formatting, serialization,
  equals/hashCode
- **`TimingMetricSpec`** — Tests create/get/has/remove, validation, insertion
  order, serialization

### Integration Tests (`examples/app1/src/integration-test/`)

- **`ServerTimingIntegrationSpec`** — 11 tests verifying header presence and correctness across fast/slow actions, view
  rendering, JSON/text responses, static assets

Tests use the **Spock Framework** and run on JUnit Platform.

## Build-Logic Convention Plugins

Convention plugins in `build-logic/src/main/groovy/` standardize build configuration:

| Plugin                        | Purpose                                                                              |
|-------------------------------|--------------------------------------------------------------------------------------|
| `compile.gradle`              | Java/Groovy compilation settings (UTF-8, incremental, Java release from `.sdkmanrc`) |
| `testing.gradle`              | Test framework config (Spock, JUnit Platform, test-logger)                           |
| `plugin.gradle`               | Grails plugin application                                                            |
| `example.gradle`              | Example app config (grails-web, GSP, assets)                                         |
| `project-publish.gradle`      | Per-project Maven publishing metadata                                                |
| `root-publish.gradle`         | Root-level Nexus publishing workaround                                               |
| `docs.gradle`                 | Documentation aggregation (Groovydoc + Asciidoctor)                                  |
| `assets.gradle`               | Asset pipeline with Bootstrap/jQuery WebJars                                         |
| `run.gradle`                  | Debug flags for `bootRun`                                                            |
| `coverage-aggregation.gradle` | JaCoCo coverage aggregation across subprojects (XML + HTML reports)                  |
| `style.gradle`                | Checkstyle + CodeNarc code style checking (configs in `build-logic/config/`)         |

## CI/CD

- **CI** (`.github/workflows/ci.yml`): Builds and tests on push/PR; publishes snapshots to Maven Central Snapshots on
  push to release branches.
- **Coverage** (`.github/workflows/coverage.yml`): Runs the full build and posts an aggregated JaCoCo coverage summary
  (instructions, branches, lines, methods, classes) to the GitHub Actions job summary.
- **Release** (`.github/workflows/release.yml`): 4-stage pipeline triggered by GitHub release — stage artifacts, release
  to Maven Central, publish docs to GitHub Pages, bump version.
- **Release Notes** (`.github/workflows/release-notes.yml`): Auto-drafts release notes using release-drafter with
  category labels.

## Code Conventions

- Groovy source files use standard Grails conventions (domain classes, controllers, interceptors, services in
  `grails-app/`, other classes in `src/main/groovy/`).
- Metric names must conform to RFC 7230 token rules (alphanumeric plus `!#$%&'*+-.^_`|~`).
- Description strings follow HTTP quoted-string escaping rules.
- The plugin uses `System.nanoTime()` for timing precision.
- When writing Gradle, always use the latest best practices to avoid eager initialization.
