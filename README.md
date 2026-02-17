# Grails Server Timing Plugin

[![CI](https://github.com/grails-plugins/grails-server-timing/actions/workflows/ci.yml/badge.svg)](https://github.com/grails-plugins/grails-server-timing/actions/workflows/ci.yml)
[![Coverage](https://github.com/grails-plugins/grails-server-timing/actions/workflows/coverage.yml/badge.svg)](https://github.com/grails-plugins/grails-server-timing/actions/workflows/coverage.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.grails.plugins/grails-server-timing)](https://central.sonatype.com/artifact/org.grails.plugins/grails-server-timing)
[![License](https://img.shields.io/github/license/grails-plugins/grails-server-timing)](https://www.apache.org/licenses/LICENSE-2.0)

A Grails plugin that injects [Server Timing](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Server-Timing) HTTP headers into
responses, implementing the [W3C Server Timing specification](https://w3c.github.io/server-timing/). It automatically
tracks controller action time, view rendering time, and total request time -- surfacing them directly in your browser's
DevTools.

## Quick Start

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'org.grails.plugins:grails-server-timing:0.0.1-SNAPSHOT'
}
```

That's it. The plugin is **automatically enabled** in `development` and `test` environments. No additional configuration
is required.

> **Note:** The plugin is disabled by default in production to prevent exposing timing data that could
> facilitate [timing attacks](https://w3c.github.io/server-timing/#security-considerations).

### Using Snapshot Builds

Snapshot builds are published to Maven Central Snapshots on every push to `main` and release branches. To use a snapshot
version, add the Maven Central Snapshots repository to your `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = 'https://central.sonatype.com/repository/maven-snapshots/'
            mavenContent {
                snapshotsOnly()
            }
            content {
                includeModule('org.grails.plugins', 'grails-server-timing')
            }
        }
    }
}
```

Then reference the snapshot version in your `build.gradle`:

```groovy
dependencies {
    implementation 'org.grails.plugins:grails-server-timing:0.0.1-SNAPSHOT'
}
```

> **Note:** Snapshot versions are unstable and may change without notice. They are intended for testing
> upcoming changes before a release.

## How It Works

The plugin intercepts HTTP requests using a servlet filter and a Grails interceptor:

1. **`ServerTimingFilter`** wraps every request, starting `total` and `other` timers.
2. **`ServerTimingInterceptor`** starts an `action` timer before the controller executes, then swaps to a `view` timer
   after.
3. **`ServerTimingResponseWrapper`** injects the `Server-Timing` header just before the first byte is written.

A typical response header looks like:

```
Server-Timing: total;dur=156.3;desc="Total", action;dur=45.2;desc="Action", view;dur=98.7;desc="View"
```

| Request Type                        | Metrics Captured          |
|-------------------------------------|---------------------------|
| Controller with view                | `total`, `action`, `view` |
| Controller with render (JSON, text) | `total`, `action`         |
| Static assets / other resources     | `total`, `other`          |

## Viewing in Browser DevTools

Open DevTools (F12), go to the **Network** tab, click a request, and select the **Timing** tab. Metrics
appear under "Server Timing":

- **Chrome** 65+ / **Edge** 79+ / **Opera** 52+
- **Firefox** 61+
- **Safari** 16.4+

## Configuration

Configure in `application.yml` under `grails.plugins.servertiming`:

| Property    | Type      | Default              | Description                                                                           |
|-------------|-----------|----------------------|---------------------------------------------------------------------------------------|
| `enabled`   | `Boolean` | `null` (auto)        | `null` = enabled in dev/test only. Set `true` or `false` to override.                 |
| `metricKey` | `String`  | `GrailsServerTiming` | Request attribute key for storing metrics. Change only if you have a naming conflict. |

### Environment-Specific Example

```yaml
environments:
    development:
        grails:
            plugins:
                servertiming:
                    enabled: true
    production:
        grails:
            plugins:
                servertiming:
                    enabled: false
```

## Compatibility

| Plugin Version | Grails | Java | Groovy |
|----------------|--------|------|--------|
| 0.x            | 7.0.x  | 17+  | 4.0.x  |

## Documentation

Full documentation is available at
the [project documentation site](https://grails-plugins.github.io/grails-server-timing/). This includes architecture
details, the W3C specification, security considerations, and browser DevTools usage guides.

## Building from Source

Prerequisites: [SDKMAN!](https://sdkman.io/)

```bash
sdk env install    # Install Java 17, Gradle 8.14, Groovy 4.0
./gradlew build    # Compile and run all tests
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full development setup.

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
