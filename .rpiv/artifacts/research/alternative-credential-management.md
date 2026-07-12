---
date: 2026-07-09T23:30:00+0200
author: Jan Esser
commit: 668ae30
branch: main
repository: git-executor
topic: Alternative credential management approaches for git-executor
tags: [research, git, credentials, alternative-approaches]
status: ready
last_updated: 2026-07-09T23:30:00+0200
last_updated_by: Jan Esser
---

# Research: alternative credential management approaches

## Research Question

Can git credentials in the shell be managed in other ways than the current `git credential approve/reject` implementation?

## Summary

The current `ImbecileCredentials` implementation uses `git credential approve/reject` to write credentials to stdout and read them from stdin. The alternative approaches explored are:

1. **SSH_ASKPASS** — handles credentials at the SSH layer, not Git's layer
2. **git-credential-osx / git-credential-win32** — in-memory credential stores (session-only)
3. **Desktop credential managers** — `gtk-credential`, `git-credential-manager` via D-Bus

## Detailed Findings

### Current Implementation

**File:** `src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt:20-48`

The current flow uses `git credential approve <protocol> <host> <username> <password>` and `git credential reject <protocol> <host> <username>`. Credentials are written to stdout and the user types them on stdin.

**File:** `src/main/kotlin/de/esserjan/edu/imbecile/util/SubProcessBuilder.kt:8-22`

The `outputWriter()` call writes credentials to stdout, and the process waits for input.

### SSH_ASKPASS Alternative

**File:** `src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:59-78`

The existing `gitCommandExec` already checks for `SSH_ASK_PASS` environment variable, but this is not currently used for credential management.

**How it works:**
- SSH client prompts for password (keyboard-interactive mode)
- User pipes password: `echo password | git clone ssh://user@host/repo.git`
- SSH uses the password to authenticate
- Git proceeds normally

**Advantages:**
- Handled at the SSH layer, not Git's layer
- Can be configured per-SSH connection
- Uses SSH's native keyboard-interactive mode

**Disadvantages:**
- Only works with SSH connections
- Requires SSH client to be properly configured
- SSH_ASKPASS can be tricky to set up correctly (needs `setsid`)

### git-credential-osx / git-credential-win32

**Current uses `git-credential-store`** (file-based persistence):
```
~/.git-credentials
protocol=https
host=github.com
username=user
password=pass
```

**git-credential-osx:**
- Stores credentials in an in-memory SQLite database
- Credentials expire after session (never written to disk)
- API: `git credential osx <action> <protocol> <host> <username>`

**git-credential-win32:**
- Stores credentials in an in-memory file database (`credential-cache`)
- Credentials expire after session
- API: `git credential win32 <action> <protocol> <host> <username>`

**Comparison:**
| Feature | git-credential-store | git-credential-osx/win32 |
|---------|---------------------|---------------------------|
| Persistence | File on disk | **In-memory only** (no file) |
| Lifetime | Permanent | **Session only** (expires on logout/reboot) |
| Security | File permissions matter | Memory-only — never on disk |
| Recovery | Delete file to reset | **Cannot recover** — credentials lost permanently |

### Desktop Credential Managers

**gtk-credential:**
- Linux (GTK-based) — GNOME, X11, Wayland
- Uses D-Bus interface on `org.gtk.CredentialManager`
- Set `credentialProcess = git-credential-store` in git config

**git-credential-manager:**
- Windows — Python + tkinter
- Cross-platform alternative

### Required Changes

To migrate from `git credential approve/reject` to `git credential osx/win32`:

1. Add `helper = osx` or `helper = win32` to git config
2. Change `git credential approve` to `git credential osx` or `win32`
3. Accept that credentials are session-only — not recoverable after logout
4. Add error handling for cases where the credential helper isn't available

### Security Implications

| Aspect | Current (store) | With osx/win32 |
|--------|----------------|----------------|
| Persistence | File on disk | Memory only — never written to disk |
| Recovery | Delete file to reset | **Cannot recover** — credentials lost permanently |
| Forensics | File can be examined in tools | No file artifacts — harder to trace |
| Backup | Can backup `~/.git-credentials` | No backup mechanism available |

## Code References

- `src/main/kotlin/de/esserjan/edu/imbecile/ImbecileCredentials.kt:20-48` — `fill()` / `reject()` methods
- `src/main/kotlin/de/esserjan/edu/imbecile/util/SubProcessBuilder.kt:8-22` — `outputWriter()` call
- `src/main/kotlin/de/esserjan/edu/imbecile/Imbecile.kt:59-78` — `extraEnvVars["SSH_ASK_PASS"]` check
- `~/.git/config:12` — `credential.helper store --file <file>`

## Integration Points

**Inbound:** `git credential` command, `~/.git/config` file
**Outbound:** Git's credential helper interface (stdin/stdout)
**Dependencies:** SSH client (for SSH credentials), `git-credential-osx` or `git-credential-win32` (for OS-specific)

## Precedents & Lessons

No similar past changes found in git history.

## Developer Context

**Q (ImbecileCredentials.kt:20-48):** How does the current `git credential approve/reject` flow work?
A: `git credential approve` writes credentials to stdout. The user types them on stdin. The process collects output and returns it.

**Q (SubProcessBuilder.kt:8-22):** What does the `outputWriter()` call do?
A: It writes credentials to stdout, flushes, and waits for the process to exit. The user's stdin input is read by git's credential helper.

## Related Research

- SSH authentication alternatives (SSH_ASKPASS)
- Cross-platform credential management
- Git credential helper specifications

---

*Research artifact created on 2026-07-09 at 23:30:00+0200 by Jan Esser.*