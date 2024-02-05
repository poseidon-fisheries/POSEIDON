plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    // centralize the dependency on Guava, since that's the
    // one library that we use almost everywhere
    implementation("com.google.guava:guava:33.0.0-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:4.11.0")

    // Google AutoService annotations, for generating ServiceLoader metadata automatically
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "4G" // GitHub actions was crashing on 6GB
    testLogging {
        events("passed", "skipped", "failed")
    }
    maxParallelForks = Runtime
        .getRuntime()
        .availableProcessors()
        .coerceAtMost(8)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Werror", "-Xlint:-processing"))
}

tasks.withType<Copy>().all {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
