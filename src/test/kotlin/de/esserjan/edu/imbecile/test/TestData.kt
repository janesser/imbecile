package de.esserjan.edu.imbecile.test

import java.io.File

interface TestData {
    companion object {
        val GIT_PATH = File("/usr/bin/git")

        val GIT_REPO = File("/tmp/egit.git")
        const val GIT_REPO_REMOTE = "https://github.com/eclipse-egit/egit.git"
        val GIT_FILE = File(GIT_REPO, "pom.xml")
        val GIT_FOLDER = File(GIT_REPO, "icons")
        const val GIT_HISTORICAL_COMMIT_ID = "e90d864edca6eb34d0b7a1f0dcc767bcd4970bb5"
        const val GIT_HISTORICAL_ONTO_COMMIT_ID = "cd8c66d521371cbd1163b136f991a9598055d84a"

        const val GIT_MOCK_REMOTE = "mockRemote"
        const val GIT_REMOTE_PROJECT = "egit.git"

        const val GIT_USER_NAME = "Test User Name"
        const val GIT_USER_EMAIL = "Test.User@Email"
    }

    interface SshData {
        companion object {
            const val SSH_TEST_KEY = "testKey"
            const val SSH_TEST_KEY_PWD = "" // "testKeyPassword"
            const val SSH_TEST_ASKPASS = "testAskPass.sh"
            const val SSH_TEST_PRINCIPAL = "testPrincipal"
            const val SSH_MOCK_PORT = 61333 // auto select
            const val SSH_SERVER = "localhost"
            val GIT_REMOTE_URL = String.format("ssh://%s@%s:%d/%s",
                SSH_TEST_PRINCIPAL, SSH_SERVER, SSH_MOCK_PORT, GIT_REMOTE_PROJECT)
        }
    }

    interface HttpsData {
        companion object {
            const val HTTPS_PROTOCOL = "https"
            const val HTTPS_PORT = 62345
            const val HTTPS_HOST = "localhost"
            const val HTTPS_USERNAME = "user"
            const val HTTPS_PASSWORD = "pass"
            val GIT_REMOTE_URL = String.format("https://%s@%s:%d/%s",
                HTTPS_USERNAME, HTTPS_HOST, HTTPS_PORT, GIT_REMOTE_PROJECT)
            const val HTTPS_GIT_FOLDER = "test_git_https"
            const val HTTPS_CERT_PASSWORD = "mockedSecret"
        }
    }
}