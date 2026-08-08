-- 성별/연령대를 소셜에서도 못 받고 사용자도 입력하지 않은 경우를 표현한다.
-- NOT NULL은 유지하고 UNKNOWN 값을 추가한다.
ALTER TABLE members
    MODIFY COLUMN gender ENUM('MALE','FEMALE','UNKNOWN') NOT NULL,
    MODIFY COLUMN age_group ENUM('TEENS','TWENTIES','THIRTIES','FORTIES','FIFTIES','SIXTIES_PLUS','UNKNOWN') NOT NULL;
