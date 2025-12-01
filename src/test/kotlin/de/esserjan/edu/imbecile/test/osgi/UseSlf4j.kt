package de.esserjan.edu.imbecile.test.osgi

import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@WithBundle("slf4j.api")
@WithBundle(value = "slf4j.simple", start = true)
annotation class UseSlf4j