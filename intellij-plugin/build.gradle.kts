plugins {
  kotlin("jvm") version "1.9.23"
  id("org.jetbrains.intellij.platform") version "2.8.0"
}

group = "org.intellij.sdk"
version = "0.1.0"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2024.3.6")
  }

  implementation("org.java-websocket:Java-WebSocket:1.5.6")
  implementation(kotlin("stdlib"))
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