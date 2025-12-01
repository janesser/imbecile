package de.esserjan.edu.imbecile.test

import de.esserjan.edu.imbecile.Imbecile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class GitExecutorTest : GitTestSupport() {

    private val underTest: Imbecile

    init {
        underTest = Imbecile()
        if (TestData.GIT_PATH.exists()) {
            underTest.executable = TestData.GIT_PATH
        }
        underTest.repositoryDirectory = TestData.GIT_REPO
    }

    @Test
    @Order(1)
    fun `can find git on path`() {
        val process = Runtime.getRuntime().exec(arrayOf("which", "git"))
        assertEquals(0, process.waitFor())
    }

    @Test
    @Order(2)
    fun `can git version`() {
        val res = underTest.version()
        assertExitCodeZero(res)
    }

    @Test
    @Order(3)
    fun `can git clone`() {
        val deleteExitCode = Runtime.getRuntime().exec(arrayOf("rm", "-fR", TestData.GIT_REPO.path)).waitFor()
        assertEquals(0, deleteExitCode)
        assertFalse(TestData.GIT_REPO.exists())

        val res = underTest.clone(
            TestData.GIT_REPO_REMOTE,
            TestData.GIT_REPO,
            null,
            null
        )
        assertExitCodeZero(res)
        assertTrue(TestData.GIT_REPO.exists())
    }

    @Test
    @Order(4)
    fun `can git reset`() {
        val res = underTest.reset(true, TestData.GIT_HISTORICAL_COMMIT_ID)
        assertExitCodeZero(res)
    }

    @Test
    @Order(4)
    fun `can git clean`() {
        val f = File(TestData.GIT_FOLDER, "cleanMe")
        f.createNewFile()
        assertTrue(f.exists())

        val res = underTest.clean()
        assertExitCodeZero(res)

        assertFalse(f.exists())
    }

    private fun resetTestScenario() {
        try {
            `can git reset`()
            `can git clean`()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Test
    @Order(5)
    fun `can git fetch https`() {
        val res = underTest.fetch(ORIGIN)
        assertExitCodeZero(res)
    }

    @Test
    @Order(6)
    fun `can git commit`() {
        resetTestScenario() // arrange

        val res = underTest.commit("empty commit", false, true, false)
        assertExitCodeZero(res)
    }

    @Test
    @Order(6)
    fun `can detect uncommitted file`() {
        resetTestScenario() // arrange

        assertFalse(underTest.hasChanges())

        Files.move(
            TestData.GIT_FILE.toPath(),
            Paths.get(TestData.GIT_FOLDER.path, TestData.GIT_FILE.name),
            StandardCopyOption.REPLACE_EXISTING
        )

        assertTrue(underTest.hasChanges())
    }

    @Test
    @Order(6)
    fun `can stage all files`() {
        resetTestScenario() // arrange

        Files.move(
            TestData.GIT_FILE.toPath(),
            Paths.get(TestData.GIT_FOLDER.path, TestData.GIT_FILE.name),
            StandardCopyOption.REPLACE_EXISTING
        )

        val res = underTest.addAll()
        assertExitCodeZero(res)
    }

    @Test
    @Order(6)
    fun `can stage file new file`() {
        resetTestScenario() // arrange

        val targetF = Paths.get(TestData.GIT_FOLDER.path, TestData.GIT_FILE.name).toFile()
        assertTrue(targetF.createNewFile())

        val res = underTest.add(targetF)
        assertExitCodeZero(res)
    }

    @Test
    @Order(6)
    fun `cannot stage nonexistent file`() {
        resetTestScenario() // arrange

        val nonExistentF = Paths.get(TestData.GIT_FOLDER.path, "nonExistentFile").toFile()
        assertFalse(nonExistentF.exists())

        val res = underTest.add(nonExistentF)
        assertEquals(128, res.exitCode, res.outputText)
    }

    @Test
    @Order(7)
    fun `can git pull`() {
        resetTestScenario() // arrange

        val res = underTest.pull(ORIGIN, Imbecile.PullMode.FF_ONLY)
        log.debug(res.outputText)
        assertExitCodeZero(res)
    }

    @Test
    @Order(8)
    fun `can git pull detect conflict`() {
        resetTestScenario() // arrange

        val resCommit = underTest.commit("empty commit", false, true, false)
        assertExitCodeZero(resCommit)

        val res = underTest.pull(ORIGIN, Imbecile.PullMode.FF_ONLY)
        log.debug(res.outputText)
        assertEquals(128, res.exitCode, res.outputText)
    }

    @Test
    @Order(8)
    fun `can git pull rebase`() {
        resetTestScenario() // arrange

        val resCommit = underTest.commit("empty commit", false, true, false)
        assertExitCodeZero(resCommit)

        val res = underTest.pull(ORIGIN, Imbecile.PullMode.REBASE_MERGE)
        assertExitCodeZero(res)
    }

    @Test
    @Order(8)
    fun `can git rebase abort`() {
        resetTestScenario() // arrange

        val targetF = Paths.get(TestData.GIT_FOLDER.path, TestData.GIT_FILE.name).toFile()
        Files.move(TestData.GIT_FILE.toPath(), targetF.toPath(), StandardCopyOption.REPLACE_EXISTING)

        val resAdd = underTest.add(TestData.GIT_FILE)
        assertExitCodeZero(resAdd)

        val resCommit = underTest.commit("file deleted accidentally oops", false, false, false)
        assertExitCodeZero(resCommit)

        val res = underTest.rebase(TestData.GIT_HISTORICAL_ONTO_COMMIT_ID)
        log.debug(res.outputText)
        assertEquals(1, res.exitCode, res.outputText)

        val abortResult = underTest.rebaseAbort()
        assertExitCodeZero(abortResult)
    }
}