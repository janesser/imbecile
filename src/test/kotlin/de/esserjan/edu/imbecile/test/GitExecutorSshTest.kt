package de.esserjan.edu.imbecile.test

import com.sshtools.common.publickey.SshKeyPairGenerator
import com.sshtools.common.publickey.SshKeyUtils
import com.sshtools.common.ssh.SshException
import de.esserjan.edu.imbecile.Imbecile
import de.esserjan.edu.imbecile.test.TestData.SshData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.sham.git.MockGitServer
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.security.GeneralSecurityException
import java.util.stream.Stream

class GitExecutorSshTest : GitTestSupport() {

    private val underTest: Imbecile = Imbecile()

    init {
        if (TestData.GIT_PATH.exists()) {
            underTest.executable = TestData.GIT_PATH
        }
        underTest.repositoryDirectory = TestData.GIT_REPO
    }

    companion object {
        private lateinit var keyFilePriv: File
        private lateinit var keyFilePub: File
        private lateinit var askPassFile: File

        @JvmStatic
        @BeforeAll
        @Throws(IOException::class, SshException::class)
        fun generateClientKeypair() {
            keyFilePriv = File.createTempFile(SshData.SSH_TEST_KEY, "")
            Files.setPosixFilePermissions(keyFilePriv.toPath(), PosixFilePermissions.fromString("rw-------"))
            keyFilePriv.deleteOnExit()

            keyFilePub = File(keyFilePriv.absolutePath + ".pub")
            keyFilePub.deleteOnExit()

            val keyPair = SshKeyPairGenerator.generateKeyPair(SshKeyPairGenerator.SSH2_RSA)
            SshKeyUtils.savePrivateKey(keyPair, SshData.SSH_TEST_KEY_PWD, "", keyFilePriv)
            SshKeyUtils.createPublicKeyFile(keyPair.publicKey, "", keyFilePub)
        }

        @JvmStatic
        @BeforeAll
        @Throws(IOException::class)
        fun setupAskpass() {
            askPassFile = File.createTempFile(SshData.SSH_TEST_ASKPASS, "")
            Files.setPosixFilePermissions(askPassFile.toPath(), PosixFilePermissions.fromString("rwx------"))
            askPassFile.deleteOnExit()

            // FIXME on github-actions | debug1: read_passphrase: can't open /dev/tty: No such device or address
            FileWriter(askPassFile).use { writer ->
                writer.write("#!/bin/sh")
                writer.write(System.lineSeparator())
                writer.write("echo ")
                writer.write(SshData.SSH_TEST_KEY_PWD)
                writer.write(System.lineSeparator())
                writer.flush()
            }
        }
    }

    private var server: MockGitServer? = null

    @BeforeEach
    @Throws(IOException::class, InterruptedException::class)
    fun startSshMock() {
        val knownHosts = File(System.getProperty("user.home") + "/.ssh", "known_hosts")
        if (knownHosts.exists()) {
            val sshkeygenCleanup = Runtime.getRuntime().exec(
                arrayOf(
                    "/usr/bin/ssh-keygen", //
                    "-f", knownHosts.absolutePath, //
                    "-R", "[${SshData.SSH_SERVER}]:${SshData.SSH_MOCK_PORT}" //
                )
            )
            assertEquals(0, sshkeygenCleanup.waitFor(), sshkeygenCleanup.errorReader().readLine())
        }

        server = MockGitServer(SshData.SSH_MOCK_PORT)
        server?.allowPublicKey(SshKeyUtils.getPublicKey(keyFilePub).jcePublicKey)?.enableShell()

        server?.prepareGitProject(TestData.GIT_REMOTE_PROJECT)

        server?.start()
    }

    @AfterEach
    @Throws(IOException::class)
    fun stopSshMock() {
        server?.stop()
    }

    @Test
    @Throws(IOException::class, GeneralSecurityException::class, InterruptedException::class)
    fun canGitFetchSsh() {
        // arrange 3: re-register mock remote
        underTest.removeRemote(TestData.GIT_MOCK_REMOTE)
        underTest.addRemote(TestData.GIT_MOCK_REMOTE, SshData.GIT_REMOTE_URL)

        // arrange 4: set GIT_SSH_COMMAND with specific key
        underTest.extraEnvVars["GIT_SSH_COMMAND"] = sshCommand(null).reduce { a, b -> "$a $b" }
        underTest.extraEnvVars[Imbecile.ENV_SSH_ASK_PASS] = askPassFile.absoluteFile.toString()

        // act
        val res = underTest.fetch(TestData.GIT_MOCK_REMOTE)

        // assert
        assertExitCodeZero(res)
    }

    private fun sshCommand(serverUrl: String?): Array<String> {
        return sshCommand(serverUrl, null)
    }

    private fun sshCommand(serverUrl: String?, command: String?): Array<String> {
        return arrayOf( //
            "/usr/bin/ssh", "-v", //
            "-i", keyFilePriv.absolutePath, //
            "-o", "IdentitiesOnly=yes", //
            "-o", "StrictHostKeyChecking=no", //
            (serverUrl ?: ""), //
            (command ?: "") //
        )
    }
}