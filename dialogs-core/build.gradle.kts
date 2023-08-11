plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
}

android {
	
	defaultConfig {
		minSdk = 21
		compileSdk = 33
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}
	
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android.txt"),
				"proguard-rules.pro"
			)
		}
	}
	
	buildFeatures {
		buildConfig = false
		compose = true
	}
	
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	composeOptions {
		kotlinCompilerExtensionVersion = "1.4.7"
	}
	namespace = "com.vanpra.composematerialdialogs"
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	
	implementation("com.google.android.material:material:1.6.1")
	implementation("androidx.core:core-ktx:1.9.0")
	
	val version = "1.2.1"
	implementation("androidx.compose.ui:ui:$version")
	implementation("androidx.compose.material3:material3:1.1.1")
	implementation("androidx.compose.material:material:$version")
	implementation("androidx.compose.material:material-icons-extended:$version")
	implementation("androidx.compose.animation:animation:$version")
	implementation("androidx.compose.foundation:foundation-layout:$version")
	
	implementation("androidx.activity:activity-compose:1.6.0-rc02")
	implementation("androidx.navigation:navigation-compose:2.5.2")
}