plugins {
    id("java")
}

group = "org.dooit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google() // Add Google repository to resolve Firebase Admin SDK dependencies
}

dependencies {
    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.1.1")

    implementation ("org.json:json:20231013")

    // Google Cloud Firestore SDK
    implementation("com.google.cloud:google-cloud-firestore:3.5.0")

    // Testing dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("org.slf4j:slf4j-simple:2.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.dooit.Main" // Replace with your actual main class
        )
    }

    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

