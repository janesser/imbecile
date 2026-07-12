---
date: 2026-07-12T10:40:54+0200
author: Jan Esser
commit: 668ae30
branch: main
repository: git-executor
topic: "Alternative Credential Management"
tags: [research, git, credentials, architecture, design, gcm]
status: in-review
parent: "Initial Decomposition"
phase_count: 4
phases:
  - n: 1, title: "CredentialStore Interface"
  - n: 2, title: "Legacy Store Implementation"
  - n: 3, title: "Modern Store Implementation"
  - n: 4, title: "Executor Wiring & Selection"
last_updated: 2026-07-12T10:40:54+0200
last_updated_by: Jan Esser
---

# Alternative Credential Management Implementation Plan

## Overview
This implementation plan transitions `git-executor` from a file-based credential store to a dynamic, abstract `CredentialStore` abstraction. This allows the system to seamlessly support both legacy `git-credential-store` and modern, memory-safe solutions like Git Credential Manager (GCM), decoupling the core `Imbecile` API from specific storage implementations.

## Desired End State
When running `gitCommandExec("pull", "--ff-only", "origin")`, the process will transparently use the configured credential store (e.g., GCM) to handle the authentication handshake, completely abstracting the credential retrieval process from the OSGi bundle's execution logic.

## What We're NOT Doing
- Dedicated support for specific Desktop Manager solutions (e.g., `gtk-credential`), unless explicitly requested by a change in core requirements.
- A complete rewrite of the `SubProcessBuilder` utility beyond necessary minimal interface changes.

## Phase 1: CredentialStore Interface

### Overview
This phase involves creating the `CredentialStore` abstraction, defining the contract for all future credential storage mechanisms.

### Changes Required:

#### 1. CredentialStore.kt
**File**: `src/main/kotlin/de/esserjan/edu/imbecile/CredentialStore.kt`
**Changes**: Full implementation of `CredentialStore` interface.

\`\`\`kotlin
// Code from design artifact's Architecture section (CredentialStore.kt)
// Full implementation of CredentialStore interface will go here
```

### Success Criteria:

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "CredentialStore" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] `CredentialStore` interface defines all necessary methods (e.g., `get`, `set`, `isAvailable`).

---

## Phase 2: Legacy Store Implementation

### Overview
Implement the `FileCredentialStore` using the existing `ImbecileCredentials.kt` logic to maintain compatibility with the `~/.git-credentials` workflow.

### Changes Required:

#### 1. ImbecileCredentials.kt
**File**: `src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt:20-48`
**Changes**: Adaptation of existing class logic to implement the `CredentialStore` contract.

\`\`\`kotlin
// Code from design artifact's Architecture section (ImbecileCredentials.kt:20-48)
// Code replacement/addition to adapt existing class to CredentialStore implementation will go here
```

### Success Criteria:

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "ImbecileCredentials" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] Legacy file-based logic successfully implements the `CredentialStore` contract without side effects.

---

## Phase 3: Modern Store Implementation

### Overview
Implement the `GcmCredentialStore` using OS-native helpers for modern, memory-safe, session-only credential storage.

### Changes Required:

#### 1. GcmCredentialStore.kt
**File**: `src/main/kotlin/de/esserjan/edu/imbecile/GcmCredentialStore.kt`
**Changes**: Full implementation of GCM-based credential storage.

\`\`\`kotlin
// Code from design artifact's Architecture section (GcmCredentialStore.kt)
// Full implementation of GCM-based credential storage will go here
```

### Success Criteria:

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "GcmCredentialStore" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] GCM implementation successfully handles credential caching and expiration.

---

## Phase 4: Executor Wiring & Selection

### Overview
Refactor `Imbecile.kt` to correctly select the active `CredentialStore` (preferably GCM) and delegate all credential operations to it, ensuring the core API remains fluent.

### Changes Required:

#### 1. Imbecile.kt
**File**: `src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:59-78`
**Changes**: Injection of `CredentialStore` selection logic and refactoring API usage.

\`\`\`kotlin
// Code from design artifact's Architecture section (Imbecile.kt:59-78)
// Code changes to inject CredentialStore selection logic and API usage will go here
```

### Success Criteria:

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "Imbecile.CredentialStore" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] The `Imbecile` core class correctly selects and delegates credential operations to the active `CredentialStore`.

---

## Plan Review (Step 4)

_Independent post-finalization review by artifact-code-reviewer and artifact-coverage-reviewer subagents. Findings triaged at Step 5._

| source | plan-loc | codebase-loc | severity | dimension | finding | recommendation | resolution |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| code | Phase 4 §1 (Imbecile.kt) | src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:22-23 | Blocker | actionability | `Imbecile.kt` does not define a mechanism for injecting or accessing the `CredentialStore` abstraction. | Modify the `Imbecile` constructor to accept a `CredentialStore` dependency. | (pending) |
| code | Phase 2 §1 (ImbecileCredentials.kt) | src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt:17-34 | Concern | code-quality | The methods `fill` and `reject` are tightly coupled to the shell's `git credential approve/reject` workflow, relying on specific stdout/stdin formats. | Replace these methods with `CredentialStore` interface methods (e.g., `get/delete`) to adhere to the abstraction. | (pending) |
| code | Phase 4 §3 (Imbecile.kt) | src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:147 | Concern | code-quality | The `pull` method executes `gitCommandExec` without any awareness of the newly injected credential store, meaning credential handling remains reliant on legacy shell helpers. | Refactor `pull` to implement logic that queries the injected `CredentialStore` to provide credentials to the execution environment. | (pending) |
| code | Phase 4 §2 (Imbecile.kt) | src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:59 | Concern | code-quality | The `gitCommandExec` method acts as a monolithic wrapper, preventing granular interception of credential operations. | Refactor `gitCommandExec` to accept an optional `CredentialStore` argument and allow credential resolution logic to run before process start. | (pending) |

---

## Testing Strategy

### Automated:
- Standard project checks (type checking, unit tests) defined in each slice's automated verification.

### Manual Testing Steps:
1. Verify GCM functionality in a test session.
2. Verify legacy file store compatibility in a test session.
3. Ensure `gitCommandExec` remains fluent and unchanged after wiring.

## Performance Considerations
- No significant performance impacts are expected from abstraction, though GCM interaction might have a slightly higher latency than file-based lookups for initial startup.

## Migration Notes
The primary migration path involves transitioning from relying on the `git credential approve/reject` command output stream to a programmatic interface (`CredentialStore`). A fallback to the legacy file-based store ensures compatibility during this transition.

## Developer Context
**Q (ImbecileCredentials.kt:20-48):** How does the current `git credential approve/reject` flow work?
A: `git credential approve` writes credentials to stdout. The user types them on stdin. The process collects output and returns it.

**Q (SubProcessBuilder.kt:8-22):** What does the `outputWriter()` call do?
A: It writes credentials to stdout, flushes, and waits for the process to exit. The user's stdin input is read by git's credential helper.

## References
- Design: `.rpiv/artifacts/designs/alternative-credential-management.md`
- Research: `/home/jan/projs/git-executor/.rpiv/artifacts/research/alternative-credential-management.md`