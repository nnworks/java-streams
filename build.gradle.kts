/**
 *
 */

plugins {
  id("java-library")
  id("eclipse")
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


dependencies {
  // This dependency is exported to consumers, that is to say found on their compile classpath.
  // api "org.apache.commons:commons-math3:3.6.1"

  // This dependency is used internally, and not exposed to consumers on their own compile classpath.
  // implementation "com.google.guava:guava:23.0"

  // Used test framework
  // testImplementation "junit:junit:4.12"
}

// In this section you declare where to find the dependencies of your project
repositories {
  // Use jcenter for resolving your dependencies.
  // You can declare any Maven/Ivy/file repository here.
  jcenter()
}

sourceSets {
  main {
    java {
      java.srcDir("src/main/java")
    }
    resources {
      java.srcDirs("src/main/resources")
    }
    // output = {
    // }
  }
    
  test {
    java {
      java.srcDirs("src/test/java")
    }
    
    resources {
      java.srcDirs("src/test/resources")
    }
  }
}
