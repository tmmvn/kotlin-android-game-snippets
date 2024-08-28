// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	kotlin("jvm") version "2.0.0"
	alias(libs.plugins.androidApplication) apply false
	alias(libs.plugins.kotlinAndroid) apply false
	alias(libs.plugins.compose.compiler) apply false
}
true // Needed to make the Suppress annotation work for the plugins block
