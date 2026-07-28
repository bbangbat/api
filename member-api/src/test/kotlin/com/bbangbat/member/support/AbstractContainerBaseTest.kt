package com.bbangbat.member.support

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
abstract class AbstractContainerBaseTest {
    companion object {
        @Container
        @ServiceConnection
        @JvmField
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.4")

        @Container
        @ServiceConnection
        @JvmField
        val redis: GenericContainer<*> = GenericContainer("redis:7-alpine").withExposedPorts(6379)
    }
}
