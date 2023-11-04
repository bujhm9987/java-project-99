import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	java
	application
	checkstyle
	id("org.springframework.boot") version "3.1.5"
	id("io.spring.dependency-management") version "1.1.3"
	id("io.freefair.lombok") version "8.4"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

application { mainClass.set("hexlet.code.AppApplication") }

repositories { mavenCentral() }

dependencies {
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	//annotationProcessor("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	//implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("org.springframework.boot:spring-boot-starter-security")
	//implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	//implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:3.1.1.RELEASE")
	//implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")


	//testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
	//implementation("net.datafaker:datafaker:2.0.2")
	//implementation("org.instancio:instancio-junit:3.3.1")

	//compileOnly ("org.projectlombok:lombok")

}

checkstyle {
	toolVersion = "10.3.3"
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
		events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
		showStandardStreams = true
	}
}

tasks {
	val stage by registering {
		dependsOn(installDist, clean)
	}
	installDist {
		mustRunAfter(clean)
	}
}