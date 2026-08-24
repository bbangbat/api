package com.bbangbat.common.id

import org.hibernate.annotations.IdGeneratorType

/**
 * 시간순으로 증가하는 PK를 앱에서 생성한다. (TSID)
 *
 * DB 채번(AUTO_INCREMENT)에 의존하지 않으므로 벤더에 중립적이다.
 * 특히 TiDB의 AUTO_INCREMENT는 노드별 캐시 할당이라 단조 증가를 보장하지 않는데,
 * ID 순서로 정렬하거나 커서를 잡는 조회(톡 afterId 폴링, 최신순 목록)는 순서가 깨지면
 * 결과가 조용히 틀어진다. 그런 테이블은 이 방식을 쓴다.
 *
 * 상위 42비트가 타임스탬프라 기존 순번 ID(수천 단위)보다 항상 큰 값이 나온다.
 * 그래서 AUTO_INCREMENT에서 전환해도 기존 데이터와의 정렬 순서가 유지된다.
 *
 * 주의: 인스턴스를 2개 이상 띄우면 노드 ID를 인스턴스별로 지정해야 한다
 * (환경변수 TSID_NODE). 지정하지 않으면 JVM마다 노드 ID를 무작위로 골라
 * 같은 밀리초에 ID가 겹칠 수 있다. 단일 인스턴스에서는 해당 없음.
 */
@IdGeneratorType(TsidGenerator::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class Tsid
