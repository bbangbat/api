package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.Connection

/**
 * TiDB의 AUTO_INCREMENT는 노드별 캐시 할당이라 단조 증가를 보장하지 않는다.
 * 나중에 넣은 행이 더 작은 ID를 받을 수 있어, ID 순서에 의존하는 조회가 조용히 틀어진다.
 * 해당 테이블만 MySQL 호환 모드(AUTO_ID_CACHE 1)로 돌린다.
 *
 * AUTO_ID_CACHE는 TiDB 전용 문법이라 MySQL에서는 문법 오류가 나므로 버전을 보고 건너뛴다.
 * (dev는 MySQL로 남기고 운영만 TiDB로 옮기는 구성)
 *
 * 주의: TiDB로 이전할 때 RDS의 flyway_schema_history를 복사하면 MySQL에서 no-op으로 기록된
 * 이 버전이 적용된 것으로 처리돼 영영 실행되지 않는다. 반드시 빈 DB에 Flyway를 새로 돌릴 것.
 */
@Suppress("ktlint:standard:class-naming", "ClassName")
class V7__Apply_tidb_auto_id_cache : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val connection = context.connection

        if (!isTiDb(connection)) {
            return
        }

        connection.createStatement().use { statement ->
            TARGET_TABLES.forEach { table -> statement.execute("ALTER TABLE $table AUTO_ID_CACHE 1") }
        }
    }

    private fun isTiDb(connection: Connection): Boolean =
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT VERSION()").use { rows ->
                rows.next() && rows.getString(1).contains("TiDB", ignoreCase = true)
            }
        }

    companion object {
        /**
         * ID 순서에 의존하는 조회가 있는 테이블만 대상으로 한다.
         * - live_talk_messages: afterId 폴링(id > ?), ORDER BY id, max(id). 순서가 깨지면 메시지가 누락된다
         * - reviews, favorites: 최신순 목록(ORDER BY id DESC)
         *
         * members는 TSID라 시간 정렬이 보장되고,
         * congestion_votes/review_images/review_menus/store_talk_summaries/stores는
         * ID 순서에 의존하는 조회가 없어 제외한다.
         */
        private val TARGET_TABLES = listOf("live_talk_messages", "reviews", "favorites")
    }
}
