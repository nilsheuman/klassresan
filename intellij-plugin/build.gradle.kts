plugins {
  kotlin("jvm") version "2.1.0"
  id("org.jetbrains.intellij.platform") version "2.8.0"
}

group = "org.intellij.sdk"
version = "0.3.1"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2024.3.6")
    // for PsiJavaFile
    bundledPlugin("com.intellij.java")
//    bundledPlugin("org.jetbrains.kotlin")
  }

  implementation("org.java-websocket:Java-WebSocket:1.5.6")
  implementation(kotlin("stdlib"))

  testImplementation("junit:junit:4.13.2")
  testImplementation(kotlin("test"))
}

intellijPlatform {
  buildSearchableOptions = false

  pluginConfiguration {
    ideaVersion {
      sinceBuild = "243"
    }
  }
  pluginVerification {
    ides {
      recommended()
    }
  }
}

tasks.buildPlugin {
  archiveBaseName.set("klassresan")
}
tasks.test {
  useJUnitPlatform()
}