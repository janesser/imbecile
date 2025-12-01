package de.esserjan.edu.imbecile.test.osgi

import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@WithBundle("org.osgi.util.promise")
@WithBundle("org.osgi.util.function")
@WithBundle("org.osgi.service.component")
@WithBundle("org.osgi.dto")
@WithBundle(value = "org.apache.felix.scr", start = true)
annotation class UseFelixServiceComponentRuntime