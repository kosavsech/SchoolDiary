plugins {
	val kotlinVersion = "1.8.21"
	id("com.android.application") version "8.0.2" apply false
	id("com.android.library") version "8.0.2" apply false
	id("org.jetbrains.kotlin.android") version kotlinVersion apply false
	id("org.jetbrains.kotlin.jvm") version kotlinVersion apply false
	id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false
	id("com.google.dagger.hilt.android") version "2.46.1" apply false
	id("com.google.devtools.ksp") version "1.8.21-1.0.11" apply false
}

buildscript {
	repositories {
		google()
		mavenCentral()
		maven(url = "https://jitpack.io")
	}
	dependencies {
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
		classpath("com.google.dagger:hilt-android-gradle-plugin:2.46.1")
	}
}