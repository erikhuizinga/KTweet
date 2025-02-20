import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {

    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
    id("maven-publish")
    id("signing")
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "com.chromasgaming"
version = "1.3.0"

val ktorVersion: String by project

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.junit.jupiter:junit-jupiter:5.9.0")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation(kotlin("stdlib-jdk8"))

}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("ktweet") {
            groupId = "com.chromasgaming"
            artifactId = "ktweet"
            artifact(javadocJar)
            from(components["java"])

            pom {
                name.set("KTweet")
                description.set("KTweet is a Kotlin Library that allows you to consume the Twitter API v2.")
                url.set("https://github.com/ChromasIV/KTweet")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("ChromasIV")
                        name.set("Thomas Carney")
                        email.set("chromasiv@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/ChromasIV/KTweet")
                }
            }
        }
    }
    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/ChromasIV/KTweet")
//            credentials {
//                username = System.getenv("USERNAME")
//                password = System.getenv("TOKEN")
//            }
//        }
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }

        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["ktweet"])
}

tasks.kotlinSourcesJar() {}

detekt {
    config = files("config/detekt/detekt-config.yml")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    systemProperty("consumerKey", System.getProperty("consumerKey"))
    systemProperty("consumerSecret", System.getProperty("consumerSecret"))
    systemProperty("accessToken", System.getProperty("accessToken"))
    systemProperty("accessTokenSecret", System.getProperty("accessTokenSecret"))
    systemProperty("clientId", System.getProperty("clientId"))
    systemProperty("clientSecret", System.getProperty("clientSecret"))

    onlyIf { !project.hasProperty("skipTests")}
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}