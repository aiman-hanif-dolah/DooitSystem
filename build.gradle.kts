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

    implementation ("org.json:json:20210307")

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
