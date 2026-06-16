package com.bbangbat.live.support

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdslTestConfiguration::class)
@Testcontainers
abstract class AbstractContainerBaseTest {
    companion object {
        @Container
        @ServiceConnection
        @JvmField
        val mysql: MySQLContainer<*> =
            MySQLContainer("mysql:8.4")
                // WSL2 등 느린 디스크 환경에서 init이 지연되어 기동 실패하는 것을 방지 (데이터 디렉터리를 RAM tmpfs로)
                .withTmpFs(mapOf("/var/lib/mysql" to "rw"))
                .withUrlParam("allowPublicKeyRetrieval", "true")
                .withUrlParam("useSSL", "false")
    }
}
