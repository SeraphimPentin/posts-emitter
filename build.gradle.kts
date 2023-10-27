plugins {
    id("java")
    id("checkstyle")
}

group = "polytech"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

subprojects {
    apply(plugin = "checkstyle")
    tasks.withType<Checkstyle>().configureEach {
        configFile = project.rootDir.absoluteFile.resolve("checkstyle.xml")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("checkstyleMainAll") {
    group = "other"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("checkstyleMain") })
}