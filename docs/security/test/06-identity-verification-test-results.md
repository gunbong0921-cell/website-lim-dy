# Hexaq 가입 전 본인인증 테스트 결과

| 항목 | 내용 |
|---|---|
| 목적 | 이메일 DI·6자리 소유 확인의 단위 테스트 실행 기록 |
| 실행일 | 2026-09-11 |
| 결과 | 27건 전부 통과. 실패 0 |
| 관련 문서 | [본인인증](../06-identity-verification.md), [보안종합](../overview.md), [기능 01](../../features/01-signup-verification.md) |

Spring 컨텍스트·Oracle·Solapi·SMTP 실호출은 하지 않았다. Domain·Application·Infrastructure·프론트 형식 힌트만 검증한다.

---

## 1. 요약

| 구분 | 건수 | 실패 | 스킵 | 소요 |
|---|---|---|---|---|
| Java (JUnit 5) | 25 | 0 | 0 | 1.916s |
| 프론트 (Node `node:test`) | 2 | 0 | 0 | 0.114s |
| **합계** | **27** | **0** | **0** | — |

성공률 100%. Gradle `BUILD SUCCESSFUL`.

DI는 `Member.email` unique다. 휴대폰은 unique가 아니다. 소유 확인은 이메일 또는 휴대폰 6자리 중 하나다.

---

## 2. 실행 명령

Java:

```
.\gradlew.bat test --tests "com.edu.springboot.domain.member.PhoneVerificationPolicyTest" --tests "com.edu.springboot.domain.member.MemberIdentityTest" --tests "com.edu.springboot.infrastructure.persistence.memory.InMemoryPhoneVerificationStoreTest" --tests "com.edu.springboot.infrastructure.persistence.redis.RedisPhoneVerificationStoreTest" --tests "com.edu.springboot.application.member.VerifyCodeServiceTest" --tests "com.edu.springboot.application.member.SendPhoneVerificationServiceTest" --tests "com.edu.springboot.application.member.SignUpServiceIdentityTest" --tests "com.edu.springboot.application.member.LoginServiceIdentityTest" -x npmBuild
```

프론트 (`frontend-react`에서):

```
node --test src/utils/memberForm.phone.test.js
```

---

## 3. 클래스별 결과

| 계층 | 테스트 클래스 | 건수 | 결과 |
|---|---|---|---|
| Domain | `PhoneVerificationPolicyTest` | 3 | 통과 |
| Domain | `MemberIdentityTest` | 3 | 통과 |
| Infrastructure | `InMemoryPhoneVerificationStoreTest` | 4 | 통과 |
| Infrastructure | `RedisPhoneVerificationStoreTest` | 3 | 통과 |
| Application | `VerifyCodeServiceTest` | 3 | 통과 |
| Application | `SendPhoneVerificationServiceTest` | 4 | 통과 |
| Application | `SignUpServiceIdentityTest` | 3 | 통과 |
| Application | `LoginServiceIdentityTest` | 2 | 통과 |
| Utils | `memberForm.phone.test.js` | 2 | 통과 |

---

## 4. 케이스

### 4.1 Domain — `PhoneVerificationPolicy`

TTL 180초, 재전송 60초, 일 10회, 토큰 30분.

| 케이스 | 결과 |
|---|---|
| TTL·재전송·일일 한도는 문서와 같다 | 통과 |
| 휴대폰은 숫자만 남기고 01x 10~11자리만 통과한다 | 통과 |
| 인증번호는 6자리이고 문자에 포함된다 | 통과 |

### 4.2 Domain — `Member` / `VerificationChannel`

| 케이스 | 결과 |
|---|---|
| email은 unique이고 phone은 unique가 아니다 | 통과 |
| 이메일 또는 휴대폰 중 하나만 확인되면 가입 인증이다 | 통과 |
| 채널 기본값은 EMAIL이다 | 통과 |

### 4.3 Infrastructure — `VerificationStore`

| 케이스 | 결과 |
|---|---|
| 코드를 저장하고 찾을 수 있다 | 통과 |
| 토큰은 한 번만 소비된다 | 통과 |
| 쿨다운이 있으면 남은 초를 준다 | 통과 |
| 일일 카운터를 올린다 | 통과 |
| 코드 저장은 PHONE_VERIFY 접두와 TTL을 쓴다 | 통과 |
| 토큰 소비는 GETDEL에 해당한다 | 통과 |
| 쿨다운 TTL이 없으면 0초다 | 통과 |

### 4.4 Application — 6자리 확인·발송

| 케이스 | 결과 |
|---|---|
| 휴대폰 6자리가 맞으면 토큰을 주고 코드를 지운다 | 통과 |
| 이메일 6자리가 맞으면 MAIL 키로 토큰을 준다 | 통과 |
| 코드가 틀리거나 6자리가 아니면 거절한다 | 통과 |
| 잘못된 번호면 SMS를 보내지 않는다 | 통과 |
| 쿨다운 중이면 SMS를 보내지 않는다 | 통과 |
| 같은 번호 일 10회면 SMS를 보내지 않는다 | 통과 |
| SMS 실패면 저장한 코드를 지운다 | 통과 |

### 4.5 Application — 가입·로그인

| 케이스 | 결과 |
|---|---|
| 이미 있는 이메일이면 토큰을 소비하지 않는다 | 통과 |
| 토큰이 없으면 가입하지 않는다 | 통과 |
| 휴대폰 토큰을 한 번 쓰고 가입하면 phoneVerified다 | 통과 |
| 이메일·휴대폰 모두 미확인이면 로그인할 수 없다 | 통과 |
| 휴대폰 확인이면 로그인할 수 있다 | 통과 |

### 4.6 Utils — `isMobilePhone`

화면 힌트다. 6자리 확인의 정본은 서버다.

| 케이스 | 결과 |
|---|---|
| 010 계열 10~11자리를 통과한다 | 통과 |
| 유선·빈 값은 거절한다 | 통과 |

---

## 5. 이번에 보지 않은 것

| 항목 | 이유 |
|---|---|
| Solapi·SMTP 실발송 | 포트 모의. SMS 실패 시 코드 삭제만 확인 |
| 실 Redis | `StringRedisTemplate` 모의. 메모리는 실제 `InMemoryPhoneVerificationStore` |
| PASS/NICE 통신사 DI | 문서대로 현재 구현이 아님 |
| 다른 실메일 다계정 | 문서의 남은 한계. 이상 탐지와 겹침 |
| 일회용 메일 DB 전 거절 | [05 테스트](./05-disposable-email-test-results.md)에서 이미 확인 |
| `WebsiteLimDyApplicationTests` 컨텍스트 기동 | Oracle·Redis·메일 등 로컬 비밀값이 필요 |

---

## 6. 테스트 파일

| 경로 |
|---|
| `src/test/java/com/edu/springboot/domain/member/PhoneVerificationPolicyTest.java` |
| `src/test/java/com/edu/springboot/domain/member/MemberIdentityTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/persistence/memory/InMemoryPhoneVerificationStoreTest.java` |
| `src/test/java/com/edu/springboot/infrastructure/persistence/redis/RedisPhoneVerificationStoreTest.java` |
| `src/test/java/com/edu/springboot/application/member/VerifyCodeServiceTest.java` |
| `src/test/java/com/edu/springboot/application/member/SendPhoneVerificationServiceTest.java` |
| `src/test/java/com/edu/springboot/application/member/SignUpServiceIdentityTest.java` |
| `src/test/java/com/edu/springboot/application/member/LoginServiceIdentityTest.java` |
| `frontend-react/src/utils/memberForm.phone.test.js` |
