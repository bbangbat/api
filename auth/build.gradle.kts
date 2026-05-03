plugins {
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(project(":common"))

    api(libs.spring.boot.starter.security)
    api(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.data.redis)

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    compileOnly(libs.springdoc.openapi.webmvc.ui)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
}
