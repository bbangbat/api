package com.bbangbat.auth.resolver

/** 현재 로그인에 사용한 소셜 제공자를 주입한다. 예전 토큰이면 null일 수 있다. */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthProvider
