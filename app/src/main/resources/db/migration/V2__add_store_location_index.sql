-- 지도 영역(bounds) 조회용 좌표 인덱스
-- 위도로 먼저 범위를 좁히고 경도로 필터링한다.
CREATE INDEX idx_stores_latitude_longitude ON stores (latitude, longitude);
