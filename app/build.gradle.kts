plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))
    implementation(project(":member-api"))
    implementation(project(":store-api"))
    implementation(project(":live-api"))
    implementation(project(":search-api"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation(libs.spring.boot.starter.log4j2)
    runtimeOnly(libs.jackson.dataformat.yaml)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.mysql.connector)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    testImplementation(libs.spring.boot.starter.test)
}
