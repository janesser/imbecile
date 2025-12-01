package de.esserjan.edu.imbecile.test

import de.esserjan.edu.imbecile.ImbecileResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.junit.jupiter.api.Assertions.assertEquals

abstract class GitTestSupport {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ORIGIN = "origin"
    }

    fun assertExitCodeZero(result: ImbecileResult) {
        log.debug(result.outputText)
        assertEquals(0, result.exitCode, result.outputText)
    }
}