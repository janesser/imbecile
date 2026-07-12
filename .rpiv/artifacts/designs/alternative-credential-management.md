---
date: 2026-07-12T10:40:54+0200
author: Jan Esser
commit: 668ae30
branch: main
repository: git-executor
topic: Alternative Credential Management
tags: [research, git, credentials, architecture, design, gcm]
status: in-progress
last_updated: 2026-07-12T10:40:54+0200
last_updated_by: Jan Esser
parent: Initial Decomposition
---

# Design: Alternative Credential Management

## Summary
The architecture is shifting away from writing git credentials to disk via a file-based store (`git-credential-store`). We are introducing a `CredentialStore` abstraction to decouple the core `Imbecile` API from specific credential implementations, allowing us to seamlessly support both legacy file storage and modern, in-memory solutions like Git Credential Manager (GCM).

## Requirements
- Replace file-based credential storage with a dynamic, abstract store mechanism.
- Support legacy `git-credential-store` for backward compatibility.
- Implement GCM/OS-native credential handling for session-only, memory-safe storage.
- Ensure the core `Imbecile` API remains fluent and unchanged by the underlying credential mechanism.

## Current State Analysis
The current implementation relies heavily on `ImbecileCredentials.kt` using `git credential approve/reject` (lines 20-48) and a subprocess utility that flushes credentials to stdout (`SubProcessBuilder.kt`). This is simple but non-secure as it involves writing credentials to a disk file (`~/.git-credentials`).

### Key Discoveries
- Existing flow uses `git credential approve/reject`.
- `ImbecileCredentials.kt:20-48` holds the core logic for credential handling.
- `SubProcessBuilder.kt:8-22` facilitates the credential output.
- `Imbecile.kt:59-78` already includes logic to check for `SSH_ASK_PASS` in the environment, which can be leveraged by the new architecture.

## Scope
### Building
- Implementing the `CredentialStore` interface.
- Implementing the legacy `FileCredentialStore` using the existing `ImbecileCredentials.kt` logic.
- Implementing the modern `GcmCredentialStore` using OS-native helpers.
- Refactoring `Imbecile.kt` to use the selected `CredentialStore` abstraction.

### Not Building
- Dedicated support for specific Desktop Manager solutions (e.g., `gtk-credential`), unless explicitly requested by a change in core requirements.
- A complete rewrite of the `SubProcessBuilder` utility beyond necessary minimal interface changes.

## Decisions
### Credential Strategy
The chosen strategy is a hybrid approach, prioritizing security and modern standards. The core architecture will adopt an abstract `CredentialStore`.
- **Decision**: Adopt a `CredentialStore` abstraction to decouple storage implementation from core logic.
- **Decision**: Include a `FileCredentialStore` implementation to maintain compatibility with existing `~/.git-credentials` workflows.
- **Decision**: Implement `GcmCredentialStore` as the preferred, modern implementation, favoring session-only, memory-safe storage (as per user input).

## Architecture
### src/main/kotlin/de/esserjan/edu/imbecile/CredentialStore.kt — NEW
// Full implementation of CredentialStore interface will go here

### src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt:20-48 — MODIFY
// Code replacement/addition to adapt existing class to CredentialStore implementation will go here

### src/main/kotlin/de/esserjan/edu/imbecile/GcmCredentialStore.kt — NEW
// Full implementation of GCM-based credential storage will go here

### src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:59-78 — MODIFY
// Code changes to inject CredentialStore selection logic and API usage will go here

## Slices
### Slice 1: CredentialStore Interface
**Files**: `src/main/kotlin/de/esserjan/edu/imbecile/CredentialStore.kt`

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "CredentialStore" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] `CredentialStore` interface defines all necessary methods (e.g., `get`, `set`, `isAvailable`).

### Slice 2: Legacy Store Implementation
**Files**: `src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt`

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "ImbecileCredentials" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] Legacy file-based logic successfully implements the `CredentialStore` contract without side effects.

### Slice 3: Modern Store Implementation
**Files**: `src/main/kotlin/de/esserjan/edu/imbecile/GcmCredentialStore.kt`

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "GcmCredentialStore" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] GCM implementation successfully handles credential caching and expiration.

### Slice 4: Executor Wiring & Selection
**Files**: `src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt`

#### Automated Verification:
- [ ] Type checking passes: `npm run check`
- [ ] Tests pass: `npm test`
- [ ] Grep pattern from Verification Note: `grep -r "Imbecile.CredentialStore" src/main/kotlin/de/esserjan/edu/imbecile/` returns >= 1

#### Manual Verification:
- [ ] The `Imbecile` core class correctly selects and delegates credential operations to the active `CredentialStore`.

## Desired End State
When running `gitCommandExec("pull", "--ff-only", "origin")`, the process will transparently use the configured credential store (e.g., GCM) to handle the authentication handshake, completely abstracting the credential retrieval process from the OSGi bundle's execution logic.

## File Map
- `src/main/kotlin/de/esserjan/edu/imbecile/CredentialStore.kt # NEW - Defines the contract for credential handling.`
- `src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt:20-48 # MODIFY - Implements the legacy file-based store.`
- `src/main/kotlin/de/esserjan/edu/imbecile/GcmCredentialStore.kt # NEW - Implements the modern Git Credential Manager store.`
- `src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:59-78 # MODIFY - Updates core logic to select and utilize the CredentialStore.`

## Ordering Constraints
- Slice 1 must execute before Slices 2, 3, and 4.
- Slice 2 and Slice 3 are independent and can be generated in parallel (though Slice 4 needs both).
- Slice 4 must execute after Slices 2 and 3.

## Verification Notes
- **Risk**: GCM availability requires specific OS setup/dependencies.
- **Risk**: Credentials stored in memory (GCM) are volatile and cannot be recovered after system reboot or session termination.
- **Precedent**: The existing logic in `Imbecile.kt:59-78` provides an existing hook for credential flow that needs to be maintained/refactored.

## Performance Considerations
- No significant performance impacts are expected from abstraction, though GCM interaction might have a slightly higher latency than file-based lookups for initial startup.

## Migration Notes
The primary migration path involves transitioning from relying on the `git credential approve/reject` command output stream to a programmatic interface (`CredentialStore`). A fallback to the legacy file-based store ensures compatibility during this transition.

## Pattern References
- `src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt:20-48`: Existing pattern for command-line credential handling via standard Git helper invocation.

## Developer Context
**Q (ImbecileCredentials.kt:20-48):** How does the current `git credential approve/reject` flow work?
A: `git credential approve` writes credentials to stdout. The user types them on stdin. The process collects output and returns it.

**Q (SubProcessBuilder.kt:8-22):** What does the `outputWriter()` call do?
A: It writes credentials to stdout, flushes, and waits for the process to exit. The user's stdin input is read by git's credential helper.

## Design History
- Decomposed based on research and user preference for OS-native solutions.
- Slices defined to prioritize abstraction (1), legacy support (2), modern implementation (3), and final integration (4).

## References
- Research artifact: `/home/jan/projs/git-executor/.rpiv/artifacts/research/alternative-credential-management.md`