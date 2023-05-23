import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.java

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.example.reaperutility"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.9")
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20230227")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.0.1")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        manifest {
            attributes(
                    "Main-Class" to "com.github.redreaperlp.reaperutility.Main"
            )
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}

tasks {
	withType<Jar> {
		manifest {
			attributes(
					"Main-Class" to "com.github.redreaperlp.reaperutility.Main"
			)
		}
	}
}

application {
    mainClass.set("com.github.redreaperlp.reaperutility.Main")
}

