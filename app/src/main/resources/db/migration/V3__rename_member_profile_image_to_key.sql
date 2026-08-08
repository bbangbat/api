-- 프로필 이미지를 전체 URL 대신 S3 key로 저장한다.
-- (버킷이 환경별로 분리돼 있어 URL을 저장하면 환경 이동 시 값이 깨진다)
ALTER TABLE members CHANGE COLUMN profile_image_url profile_image_key VARCHAR(500) NULL;

-- 기존에 전체 URL로 저장된 값은 key로 해석할 수 없으므로 비운다.
UPDATE members SET profile_image_key = NULL WHERE profile_image_key LIKE 'http%';
