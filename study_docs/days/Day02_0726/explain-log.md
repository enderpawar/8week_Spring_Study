# Day02 (7/26, Week A D2) 예측→실행→차이설명

형식: 개념 | 예측 | 실제 결과 | 차이 설명(왜 이렇게 동작하나)

| 개념 | 예측 | 실제 결과 | 차이 설명 |
|---|---|---|---|
| `record` 접근자 호출 (`request.getRoomName()`) | 잘 동작할 것(Domain의 getter처럼) | 컴파일 에러: cannot find symbol | record는 필드명 그대로(`roomName()`)가 접근자, JavaBean `get` 접두사 관례를 안 따름 |
| 필드명 `roomname` vs 호출 `roomName()` | 대소문자 상관없이 동작할 것 | 컴파일 에러: cannot find symbol | Java는 대소문자를 구분하므로 record 컴포넌트명과 정확히 일치해야 접근자가 생성됨 |
| 한글 JSON을 `curl.exe -d`로 전송 | 정상 파싱될 것 | 400 Bad Request, 이어서 curl 자체 에러(Malformed URL) | Windows 콘솔 기본 인코딩이 UTF-8이 아니고, 수동 이스케이프 따옴표가 PowerShell에서 한 번 더 해석되며 깨짐 → `Invoke-RestMethod`+해시테이블로 전환해 해결 |
| `/reservations/cancel` 오타(`rerservations`, `cancle`, `cancled`) | 컴파일도 실행도 문제없을 것(정의·호출이 일관되므로) | 실제로 컴파일 성공, 정상 동작 | 컴파일러는 이름의 "일치 여부"만 보지 "철자가 맞는 영어인가"는 검사하지 않음 — 오타 방지는 직접 호출/테스트의 영역 |
| `/reservations/cancel` 최종 호출 | `confirmed: false` 응답 | "김민준님이301호 예약을 취소하셨습니다 (확정 : false)" | 예측대로 동작. DTO→Domain 생성→`cancel()`로 상태 변경까지 흐름 확인 |
