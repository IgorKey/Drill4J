import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
}

tasks {

    val runAgent by registering {
        dependsOn(gradle.includedBuild("spring-petclinic-kotlin").task(":bootRun"))
        group = "application"
    }
    val runIntegrationTests by registering {
        dependsOn(gradle.includedBuild("integration-tests").task(":test"))
        group = "application"
    }
}
allprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.allWarningsAsErrors = true
    }
}

allprojects {

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}