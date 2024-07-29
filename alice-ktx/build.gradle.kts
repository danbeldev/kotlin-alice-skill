import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish") version "0.28.0"
}

group = "com.github.alice.ktx"
version = "0.0.2"

project.setProperty("mainClassName", "com.github.alice.ktx.Skill")

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:1.4.12")
}

kotlin {
    jvmToolchain(11)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.danbeldev",
        artifactId = "alice-ktx",
        version = "0.0.2"
    )

    pom {
        name.set("kotlin-alice-skill")
        description.set("Асинхронный фреймворк для разработки навыков Алисы из Яндекс.Диалогов")
        inceptionYear.set("2024")
        url.set("https://github.com/danbeldev/kotlin-alice-skill")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("danbel")
                name.set("DanBel")
                email.set("dan.bel.89@bk.ru")
            }
        }

        scm {
            url.set("https://github.com/danbeldev/kotlin-alice-skill")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}