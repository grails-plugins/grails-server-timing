# Gradle Best Practices

## Purpose

This skill covers Gradle best practices for this project, including convention plugins, extension configuration,
lazy APIs, and build structure. Convention plugins eliminate duplication across subprojects by centralizing shared
build logic. They live in the `build-logic/` composite build and are applied by ID in each subproject's `build.gradle`.

## Core Rules

### NEVER configure subprojects from the root build.gradle

The root `build.gradle` must NEVER use `subprojects {}`, `allprojects {}`, or `configure(subprojects.matching {...}) {}`
to apply plugins or configure subproject behavior. This is an anti-pattern that causes ordering issues, breaks project
isolation, and makes builds harder to reason about.

```groovy
// BAD - Never do this in root build.gradle
subprojects {
    apply plugin: 'groovy'
    dependencies {
        implementation 'org.example:shared-lib:1.0'
    }
}

// BAD - Never do this either
allprojects {
    repositories {
        mavenCentral()
    }
}
```

Instead, create a convention plugin in `build-logic/` and apply it in each subproject that needs it:

```groovy
// GOOD - build-logic/src/main/groovy/org.grails.plugins.servertiming.compile.gradle
plugins {
    id 'groovy'
}
// shared compilation config here
```

```groovy
// GOOD - plugin/build.gradle
plugins {
    id 'org.grails.plugins.servertiming.compile'
}
```

The ONLY exception is the `root-publish.gradle` convention plugin, which exists solely as a workaround for a Nexus
publishing bug (https://github.com/gradle-nexus/publish-plugin/issues/310) that requires version/group to be set at the
root level.

### Use the composite build pattern

Convention plugins reside in `build-logic/`, which is included as a composite build via `settings.gradle`:

```groovy
pluginManagement {
    includeBuild('./build-logic') {
        it.name = 'build-logic'
    }
}
```

### Naming convention

Convention plugin files follow the pattern:

```
build-logic/src/main/groovy/org.grails.plugins.servertiming.<purpose>.gradle
```

The plugin ID matches the filename (minus the `.gradle` extension). For example:

- `org.grails.plugins.servertiming.compile.gradle` -> plugin ID `org.grails.plugins.servertiming.compile`

### Declare external plugin dependencies in build-logic/build.gradle

When a convention plugin applies a third-party plugin, that plugin must be declared as an `implementation` dependency in
`build-logic/build.gradle`:

```groovy
// build-logic/build.gradle
plugins {
    id 'groovy-gradle-plugin'
}

dependencies {
    implementation platform("org.apache.grails:grails-bom:${gradleProperties.grailsVersion}")
    implementation 'org.apache.grails:grails-gradle-plugins'
    implementation 'com.adarshr:gradle-test-logger-plugin:4.0.0'
    implementation 'cloud.wondrify:asset-pipeline-gradle'
    implementation 'org.apache.grails.gradle:grails-publish'
}
```

### Share properties from root gradle.properties

The `build-logic/build.gradle` reads the root `gradle.properties` and exposes those values as extra properties so
convention plugins can reference them (e.g., `grailsVersion`):

```groovy
file('../gradle.properties').withInputStream {
    Properties props = new Properties()
    props.load(it)
    project.ext.gradleProperties = props
}

allprojects {
    for (String key : gradleProperties.stringPropertyNames()) {
        ext.set(key, gradleProperties.getProperty(key))
    }
}
```

## Avoid Eager Initialization

Always use lazy/deferred APIs to avoid eagerly resolving tasks or configurations:

```groovy
// GOOD - lazy task configuration
tasks.withType(JavaCompile).configureEach {
    options.encoding = StandardCharsets.UTF_8.name()
}

tasks.named('bootRun', JavaExec).configure {
    doFirst { /* ... */ }
}

tasks.register('docs') {
    dependsOn = [/* ... */]
}

// BAD - eager resolution
tasks.withType(JavaCompile) { // missing .configureEach
    options.encoding = 'UTF-8'
}

task docs { // old task() API is eager
    dependsOn /* ... */
}
```

Key APIs to use:

- `tasks.register()` instead of `task()`
- `tasks.named()` instead of `tasks.getByName()`
- `tasks.withType(X).configureEach {}` instead of `tasks.withType(X) {}`
- `project.provider {}` for lazy values
- `layout.buildDirectory` instead of `buildDir`

## Extension Configuration with Type Hints

When configuring project extensions (like publishing metadata or third-party plugin configurations), use
`extensions.configure(Type)` with explicit parameter hints for better IDE support and type safety:

```groovy
// GOOD - explicit type hint in extension configuration
extensions.configure(GrailsPublishExtension) {
    it.artifactId = project.name
    it.githubSlug = 'grails-plugins/grails-server-timing'
    it.license.name = 'Apache-2.0'
    it.title = 'My Plugin'
    it.developers = [name: 'Developer Name']
}

// GOOD - configuring standard Gradle extensions with type hints
tasks.named('bootRun', JavaExec).configure {
    doFirst {
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
    }
}

// GOOD - property-style DSL extensions without type hints (acceptable for simple cases)
checkstyle {
    toolVersion = checkstyleVersion
    maxWarnings = 0
}
```

**Benefits:**

- IDE auto-completion and type-checking for extension properties
- Clearer intent: code readers immediately see the extension type being configured
- Reduces runtime errors from typos in property names
- Works well with `@GrailsCompileStatic` in Groovy convention plugins

Note: Simple property-style DSL configurations (like `checkstyle {}` or `jacoco {}`) don't require type hints—use
them when you're accessing nested properties or methods where IDE support is most valuable.

## Composition Over Inheritance

Convention plugins should compose by applying other convention plugins rather than duplicating logic:

```groovy
// example.gradle applies other convention plugins
plugins {
    id 'org.apache.grails.gradle.grails-web'
    id 'org.apache.grails.gradle.grails-gsp'
    id 'org.grails.plugins.servertiming.assets'
    id 'org.grails.plugins.servertiming.run'
}
```

## Existing Convention Plugins

| Plugin                        | Purpose                                                                                               |
|-------------------------------|-------------------------------------------------------------------------------------------------------|
| `compile.gradle`              | Java/Groovy compilation: UTF-8, incremental, forked JVM, `-parameters`, Java release from `.sdkmanrc` |
| `testing.gradle`              | Test framework: Spock, JUnit Platform, test-logger (mocha-parallel locally, plain-parallel in CI)     |
| `plugin.gradle`               | Applies `grails-plugin` profile, disables Spring dependency management                                |
| `example.gradle`              | Applies grails-web, grails-gsp, assets, and run plugins for example apps                              |
| `project-publish.gradle`      | Maven publishing metadata (artifact ID, license, developers, GitHub slug)                             |
| `root-publish.gradle`         | Nexus publishing workaround (root-level only)                                                         |
| `docs.gradle`                 | Documentation aggregation (Groovydoc + Asciidoctor + GitHub Pages index)                              |
| `assets.gradle`               | Asset pipeline with Bootstrap/jQuery/Bootstrap-Icons WebJars                                          |
| `run.gradle`                  | Debug/debugWait JVM flags for `bootRun`                                                               |
| `coverage-aggregation.gradle` | JaCoCo coverage aggregation across subprojects (XML + HTML reports)                                   |
| `style.gradle`                | Checkstyle + CodeNarc code style checking; configs in `build-logic/config/`                           |

## When to Create a New Convention Plugin

Create a new convention plugin when:

- Two or more subprojects share the same build configuration
- A subproject's `build.gradle` grows beyond applying plugins and declaring dependencies
- You need to enforce a project-wide standard (e.g., code formatting, static analysis)

Keep each convention plugin focused on a single concern. Prefer small, composable plugins over monolithic ones.

## Repository Management

Repositories are managed centrally in `settings.gradle` via `dependencyResolutionManagement`:

```groovy
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        maven { url = 'https://repo.grails.org/grails/restricted' }
    }
}
```

This prevents subprojects from declaring their own repositories, ensuring consistency. The `FAIL_ON_PROJECT_REPOS` mode
enforces this.
