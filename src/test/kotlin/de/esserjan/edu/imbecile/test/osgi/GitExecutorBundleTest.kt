package de.esserjan.edu.imbecile.test.osgi

import de.esserjan.edu.imbecile.Imbecile
import de.laeubisoft.osgi.junit5.framework.annotations.EmbeddedFramework
import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle
import de.laeubisoft.osgi.junit5.framework.extension.FrameworkExtension
import de.laeubisoft.osgi.junit5.framework.services.FrameworkEvents
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.osgi.framework.launch.Framework
import org.osgi.test.common.annotation.InjectService

@ExtendWith(FrameworkExtension::class)
@UseFelixServiceComponentRuntime
@UseDynamicBundle
@UseSlf4j
@WithBundle("org.jetbrains.kotlin.osgi-bundle")
@WithBundle(value = "imbecile", start = true)
class GitExecutorBundleTest {
    @InjectService
    lateinit var frameworkEvents: FrameworkEvents

    @BeforeEach
    fun checkService() {
        frameworkEvents.assertErrorFree()
    }

    @BeforeEach
    fun printFrameworkInfo(@EmbeddedFramework framework: Framework) {
        FrameworkExtension.printBundles(framework) { line -> println(line) }
        FrameworkExtension.printServices(framework) { line -> println(line) }
    }

    @InjectService
    lateinit var underTest: Imbecile

    @Test
    fun gotInjected() {
        assertNotNull(underTest)
    }
}