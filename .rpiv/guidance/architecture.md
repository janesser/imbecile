# Git Executor

A CLI tool that executes commands inside git repositories. A Kotlin-based OSGi bundle.

## Overview

`git-executor` wraps the `git` CLI as an OSGi bundle, exposing a fluent API for git operations. It implements clean architecture with a clear separation of concerns.

## Project map

```
src/main/kotlin/de/esserjan/edu/imbecile/
├── Imbecile.kt                     # Core API class
├── ImbecileResult.kt               # Result wrapper (exit code + output)
├── ImbecileCredentials.kt          # SSH/HTTPS credential management
├── ImbecileActivator.kt             # OSGi bundle activator
└── util/
    └── SubProcessBuilder.kt        # Process execution utility
```

## Commands

- `gitCommandExec("--version")` — print git version
- `gitCommandExec("reset", "--hard", commitId)` — reset to commit
- `gitCommandExec("commit", "-m", message, "--amend")` — amend commit
- `gitCommandExec("fetch", "origin")` — fetch from origin
- `gitCommandExec("pull", "--ff-only", "origin")` — pull with fast-forward
- `gitCommandExec("push", "-f", "origin")` — force push

## Cross-layer considerations

- **Path normalization**: Git stores paths with forward slashes; normalize all paths internally.
- **URL → path mapping**: translate SSH, HTTPS, and bare git URLs to local directories.
- **Command context**: commands always run inside a repo; no command is executed in the executor's own directory.
- **Process execution**: `SubProcessBuilder` pipes stdin, stdout, and stderr together for simple output parsing.
- **OSGi registration**: `Imbecile` is instantiated and registered in `ImbecileActivator` via `ServiceFactory` pattern.
- **Result wrapper**: All operations return `ImbecileResult` with `exitCode` and `outputText` for debugging.
- **Credential management**: SSH/HTTPS credentials are managed through the OS credential cache.
