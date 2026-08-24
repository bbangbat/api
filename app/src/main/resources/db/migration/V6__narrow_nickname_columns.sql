-- 닉네임 정책은 2~10자인데(NicknamePolicy) 컬럼은 VARCHAR(20)으로 남아 있었다.
-- 정책이 2~20자에서 2~10자로 좁혀질 때(485c251) 컬럼과 JPA 매핑이 따라가지 않은 잔재다.
--
-- 주의: 정책 변경(2026-08-09) 이전에 가입한 회원은 11~20자 닉네임을 가질 수 있다.
-- MySQL strict mode에서는 그런 행이 남아 있으면 아래 ALTER가 "Data too long"으로 실패한다.
-- 적용 전에 아래 쿼리로 대상이 없는지 확인할 것.
--   SELECT id, nickname FROM members WHERE CHAR_LENGTH(nickname) > 10;
--   SELECT id, author_nickname FROM live_talk_messages WHERE CHAR_LENGTH(author_nickname) > 10;

ALTER TABLE members
    MODIFY COLUMN nickname VARCHAR(10) NOT NULL;

-- 작성 시점 닉네임 스냅샷. 같은 정책을 따르므로 함께 좁힌다.
ALTER TABLE live_talk_messages
    MODIFY COLUMN author_nickname VARCHAR(10) NOT NULL;
