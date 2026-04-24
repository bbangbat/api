plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))

    implementation(libs.spring.boot.starter.data.jpa)
//    implementation(libs.jdsl.jpql.dsl)
//    implementation(libs.jdsl.jpql.render)
//    implementation(libs.jdsl.spring.data.jpa.support)

    testImplementation(libs.spring.boot.starter.test)
}
