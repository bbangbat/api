plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))
    implementation(project(":member-api"))

    implementation(libs.spring.boot.starter.web)

    runtimeOnly(libs.mysql.connector)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    testImplementation(libs.spring.boot.starter.test)
}
