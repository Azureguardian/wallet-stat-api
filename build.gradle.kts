import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.SchemaMappingType

val springMockkVersion: String by project

plugins {
	id("org.springframework.boot") version "2.6.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
	id("nu.studer.jooq") version "6.0.1"
//	id("nu.studer.jooq") version "7.1.1"
}

group = "com.anymindgroup"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11


jooq {
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)
    configurations {
        create("xml") {
            generateSchemaSourceOnCompilation.set(false) // default (can be omitted)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = project.properties.getOrDefault("dbUrl", "jdbc:postgresql://localhost:5432/epsr").toString()
                    user = project.properties.getOrDefault("dbUser", "imurometsv").toString()
                    password = project.properties.getOrDefault("dbPassword", "password").toString()
                    properties.add(Property().withKey("ssl").withValue("false"))
                }
                generator.apply {
                    name = "org.jooq.codegen.XMLGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        schemata.addAll(
                            listOf(
                                SchemaMappingType().apply {
                                    inputSchema = "public"
                                },
                                SchemaMappingType().apply {
                                    inputSchema = "aggregates"
                                }
                            )
                        )
                        includes = "aggregates.balance_hourly | public.transactions"
                    }
                    generate.apply {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isFluentSetters = true
                        isPojos = true
                    }
                    target.apply {
                        packageName = "generated"
                        directory = "src/main/resources/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }

        create("java") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.xml.XMLDatabase"
                        schemata = listOf(
                                SchemaMappingType().apply {
                                    inputSchema = "public"
                                },
                                SchemaMappingType().apply {
                                    inputSchema = "aggregates"
                                }
                            )
                        properties.add(Property().withKey("dialect").withValue("POSTGRES"))
                        properties.add(
                            Property().withKey("xmlFile")
                                .withValue("src/main/resources/jooq/generated/information_schema.xml")
                        )
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isFluentSetters = true
                        isPojos = true
                        isComments = true
                        isPojosEqualsAndHashCode = true
                    }
                    target.apply {
                        packageName = "com.anymindgroup.web.server.task"
                        directory = "$buildDir/generated/sources/jooq/src/main/java"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

// Generate jooq classes before compilation
tasks.named("compileKotlin") {
    dependsOn("generateJavaJooq")
}

repositories {
	mavenCentral()
}

dependencies {
//	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
//    implementation("org.springframework.data:spring-data-r2dbc:1.4.3")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    // https://mvnrepository.com/artifact/jakarta.xml.bind/jakarta.xml.bind-api
//     jooqGenerator("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")

    runtimeOnly("io.r2dbc:r2dbc-postgresql")
	jooqGenerator("org.postgresql:postgresql:42.3.3")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core:8.5.4")
//    implementation("org.jooq:jooq:3.16.5")

    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:postgresql:1.16.3")
	testImplementation("org.testcontainers:junit-jupiter:1.16.3")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sourceSets {
	main {
		java.srcDir(file("$buildDir/generated/sources/jooq/src/main/java"))
	}
}