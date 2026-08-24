plugins {
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.slf4j.api)

    // 시간순 PK 생성 (com.bbangbat.common.id.Tsid). 각 모듈의 JPA 엔티티가 사용한다.
    api(libs.tsid.creator)
    api(libs.spring.boot.starter.data.jpa)

    testImplementation(libs.spring.boot.starter.test)
}
