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
    implementation(project(":review-api"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.jackson.module.kotlin)
    runtimeOnly(libs.jackson.dataformat.yaml)

    // 프로필 이미지 presigned URL 발급 어댑터에서 사용
    implementation(libs.aws.s3)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.mysql.connector)
    // Boot 4는 flyway-core만으로 자동설정 안 됨 → starter로 FlywayAutoConfiguration 모듈까지 포함
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.flyway.mysql)

    testImplementation(libs.spring.boot.starter.test)
}
