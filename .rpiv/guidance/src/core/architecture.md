# Core Domain

The `src/core` layer contains the core domain logic for git execution. It defines the public API for interacting with git repositories.

## Module structure

- **Imbecile.kt** — Main API class exposed to consumers
- **ImbecileResult.kt** — Data class wrapping git operation results
- **ImbecileCredentials.kt** — SSH/HTTPS credential management
- **ImbecileActivator.kt** — OSGi bundle activation
- **util/** — Utility classes and interfaces

## Patterns

### Command execution

Git commands are executed via `SubProcessBuilder`, which pipes stdin, stdout, and stderr together. This simplifies output parsing:

```kotlin
gitCommandExec("commit", "-m", message, "--amend")
```

### Result wrapper

All operations return `ImbecileResult` with exit code and output text:

```kotlin
data class ImbecileResult(val exitCode: Int, val outputText: String)
```

### OSGi component registration

The `Imbecile` class is registered as a declarative service:

```kotlin
@Component
class Imbecile(
    var executable: File = File("git"),
    var repositoryDirectory: File? = null,
    val extraEnvVars: MutableMap<String, String> = mutableMapOf()
)
```
