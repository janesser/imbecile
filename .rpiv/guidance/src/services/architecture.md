# Service Layer

The `src/services` layer does not exist in the current codebase. The actual codebase uses a clean architecture without a services layer.

## Current Architecture

The codebase is organized around the `src/core` layer, which contains the core domain logic for git execution. The actual module structure is:

```kotlin
src/main/kotlin/de/esserjan/edu/imbecile/
├── Imbecile.kt                     # Core API class
├── ImbecileResult.kt               # Result wrapper (exit code + output)
├── ImbecileCredentials.kt          # SSH/HTTPS credential management
├── ImbecileActivator.kt             # OSGi bundle activator
└── util/
    └── SubProcessBuilder.kt        # Process execution utility
```

## OSGi Registration Pattern

The `Imbecile` instance is registered via `ImbecileActivator` using the `ServiceFactory` pattern:

```kotlin
@Component
class ImbecileActivator : BundleActivator {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    object ImbecileServiceFactory : ServiceFactory<Imbecile> {
        override fun getService(bundle: Bundle, registration: ServiceRegistration<Imbecile>): Imbecile = Imbecile()
        override fun ungetService(bundle: Bundle, registration: ServiceRegistration<Imbecile>, service: Imbecile?) {}
    }

    override fun start(context: BundleContext) {
        context.registerService(Imbecile::class.java, ImbecileServiceFactory, Hashtable<String, Any>(0))
    }

    override fun stop(context: BundleContext) {
        context.ungetService(registration?.reference)
    }
}