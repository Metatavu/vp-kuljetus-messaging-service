plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation("com.rabbitmq:amqp-client:5.21.0")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}