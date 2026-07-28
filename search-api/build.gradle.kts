plugins {
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":store-api"))

    compileOnly(libs.springdoc.openapi.webmvc.ui)

    testImplementation(libs.spring.boot.starter.test)
}
