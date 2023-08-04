plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:4.11.0")
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
    options.compilerArgs.addAll(listOf("-Werror", "-Xlint:all"))
}

tasks.withType<Copy>().all {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}