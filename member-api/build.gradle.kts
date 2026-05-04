plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.tsid.creator)
//    implementation(libs.jdsl.jpql.dsl)
//    implementation(libs.jdsl.jpql.render)
//    implementation(libs.jdsl.spring.data.jpa.support)

    compileOnly(libs.springdoc.openapi.webmvc.ui)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.junit.jupiter)
}
