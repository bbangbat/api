plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.aws.s3)

    compileOnly(libs.springdoc.openapi.webmvc.ui)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.kotlin)
}
