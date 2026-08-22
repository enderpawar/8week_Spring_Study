# Day11 (8/22, Week B D4) 용어

주제: 영속성 컨텍스트 · 1차 캐시 · 동일성

## 1. 영속성 컨텍스트와 1차 캐시

| 용어 | 한줄뜻 | 오늘 코드/관찰 |
|---|---|---|
| 영속성 컨텍스트(Persistence Context) | `EntityManager`가 현재 트랜잭션 동안 "관리 중(managed)"인 Entity들을 붙잡아두는 공간 | `@Transactional` 메서드 하나 = 컨텍스트 하나 |
| 1차 캐시 | 영속성 컨텍스트 안에서 (Entity 타입, id)를 키로 관리 중인 Entity를 찾는 식별자 맵 | `save()`나 `findById()`로 한 번 올라온 Entity는 재조회 시 SQL 없이 여기서 바로 반환됨 |
| 관리 상태(managed) | `save()`/`find` 등으로 영속성 컨텍스트에 들어가 추적되고 있는 Entity의 상태 | `repository.save()`가 반환한 객체, `findById()`가 반환한 객체 |
| `entityManager.clear()` | 영속성 컨텍스트(1차 캐시 포함)를 통째로 비움 | 호출 뒤 같은 id를 다시 조회하면 캐시에 없으니 SQL이 다시 나감 |

## 2. 실험으로 확인한 것

| 조건 | 첫 번째 `findById()` | 두 번째 `findById()` | `first == second` |
|---|---|---|---|
| `save()`+`flush()` 직후, `clear()` 없이 바로 2회 조회 | SELECT 0번(이미 캐시에 있음) | SELECT 0번 | `true` |
| `save()`+`flush()` 뒤 `clear()`로 캐시를 비우고 2회 조회 | SELECT 1번(캐시가 비어 DB에서 로드) | SELECT 0번(방금 로드된 게 캐시에 있음) | `true` |

두 조건 모두 `first == second`는 `true`다 — 값이 같아서가 아니라 **같은 트랜잭션·같은 id면 1차 캐시가 항상 같은 참조를 돌려주기 때문**이다. `clear()`는 이 캐시를 비워 "처음 조회하는 상태"로 되돌리는 도구다.

## 3. 다음 Day로 넘기는 경계

1차 캐시가 "같은 객체를 돌려준다"는 걸 확인했다면, 그 객체의 필드를 바꿨을 때 `save()` 없이도 DB에 반영되는지는 Day12(변경 감지·flush 시점)에서 다룬다.
