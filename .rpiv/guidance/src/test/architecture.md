# Testing Layer

The `src/test` layer contains unit and integration tests for the git executor.

## Module structure

- **GitExecutorTest.kt** — Main test class with unit tests
- **GitHttpsTestSupport.kt** — Support for HTTPS test cases
- **GitTestSupport.kt** — Shared test utilities
- **TestData.kt** — Test data and fixtures
- **osgi/** — OSGi bundle tests
- **git_https_backend/** — Mocked git/ssh/https endpoints

## Patterns

### Unit test structure

Unit tests are organized under `src/test/kotlin/de/esserjan/edu/imbecile/test/`:

```kotlin
class GitExecutorTest {
    @Test
    fun "can commit"() {
        // test implementation
    }

    @Test
    fun "cannot commit without changes"() {
        // test implementation
    }
}
```

### HTTPS test support

HTTPS tests use `GitHttpsTestSupport` for common functionality:

```kotlin
@ExtendWith(GitHttpsTestSupport::class)
class ImbecileHttpsTest {
    @Test
    fun "can fetch"() {
        // test implementation
    }

    @Test
    fun "can push"() {
        // test implementation
    }
}
```

### OSGi bundle tests

OSGi bundle tests are under `osgi/`:

```kotlin
class GitExecutorBundleTest {
    @Test
    fun "bundle starts and stops"() {
        // test implementation
    }
}
```

### Mocked git endpoints

Mocked git endpoints are under `git_https_backend/`:

```kotlin
class GitHttpBackendHandler {
    // implements git HTTP backend
}

class GitAuthenticationConstraint {
    // implements git authentication constraint
}
```

### Test data

Test data is shared under `TestData.kt`:

```kotlin
class TestData {
    val gitRepoUrl = "https://github.com/user/repo.git"
    val localPath = "src/"
}
```

## Workflows

### Adding a new unit test

1. Create a new test class under `src/test/kotlin/de/esserjan/edu/imbecile/test/`
2. Add relevant test methods
3. Add the test to the test suite
4. Run the tests to verify
5. Document the test in README.md

### Adding a new test case

1. Create a new test method with a descriptive name
2. Use `@ExtendWith` to add test support
3. Write test implementation
4. Run the test to verify
5. Add the test to the test suite

### Adding a new mocked endpoint

1. Create a new endpoint class under `git_https_backend/`
2. Implement the endpoint interface
3. Add the endpoint to the test suite
4. Run the tests to verify
5. Document the endpoint in README.md