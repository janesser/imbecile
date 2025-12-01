package de.esserjan.edu.imbecile.test.osgi

import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@WithBundle("org.objectweb.asm")
@WithBundle("org.objectweb.asm.commons")
@WithBundle("org.objectweb.asm.tree")
@WithBundle("org.objectweb.asm.tree.analysis")
@WithBundle("org.objectweb.asm.util")
@WithBundle(value = "org.apache.aries.spifly.dynamic.bundle", start = true)
annotation class UseDynamicBundle