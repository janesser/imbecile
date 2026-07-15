# Core Domain
The `src/core` layer houses the core domain logic for git execution, comprising:
- `Imbecile.kt` – main API class
- `ImbecileResult.kt` – result wrapper (exit code + output)
- `ImbecileCredentials.kt` – SSH/HTTPS credential management
- `ImbecileActivator.kt` – OSGi activation logic
- `util/` – shared utilities

## Guiding Principles

- **Separation of Concerns** – Each module has a single responsibility.
- **Explicit Error Propagation** – `ImbecileResult` surfaces exit codes and output for consistent diagnostics.
- **OSGi Lifecycle Management** – Services are registered via `ServiceFactory` and activated through `ImbecileActivator`.
- **Abstraction of Process Execution** – `SubProcessBuilder` centralizes command handling, reducing boiler‑plate across APIs.
- **Cross‑Layer Consistency** – Path normalization, URL→path translation, and consistent error handling are applied uniformly.

These principles ensure maintainability, testability, and extensibility of the git-executor framework.
