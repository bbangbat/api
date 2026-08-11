-- 운영자 역할. 부여는 DB에서 직접 UPDATE 한다.
ALTER TABLE members
    ADD COLUMN role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER';

-- 실시간 톡 소프트 삭제. NULL이면 살아 있는 메시지.
ALTER TABLE live_talk_messages
    ADD COLUMN deleted_at DATETIME NULL;

-- 가게별 조회와 내 톡 조회 모두 살아 있는 메시지만 대상으로 한다.
CREATE INDEX idx_talk_store_deleted_created ON live_talk_messages (store_id, deleted_at, created_at);
CREATE INDEX idx_talk_author_deleted_id ON live_talk_messages (author_id, deleted_at, id);
