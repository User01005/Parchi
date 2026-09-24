// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

val keystoreFile = file("debug.keystore")
val keystoreBase64File = file("debug.keystore.base64")
if (!keystoreFile.exists() && keystoreBase64File.exists()) {
  keystoreFile.writeBytes(java.util.Base64.getDecoder().decode(keystoreBase64File.readText().trim()))
}

