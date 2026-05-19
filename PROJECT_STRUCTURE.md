# MoodRest 프로젝트 파일 구조

## 프로젝트 개요

MoodRest는 사용자가 하루 컨디션 데이터를 입력하면 권장 수면 시간과 힐링 루틴을 추천하고, 날씨 API, Gemini API, YouTube Data API를 활용해 힐링 장소와 영상을 추천하는 Android 앱이다.

평가 기준상 Activity 3개 이상, Intent 데이터 전달, Coroutine, Retrofit/Glide/Volley, Jetpack Library, 외부 API, 머신러닝/API 활용, 안정성 등을 명확히 보여줄 수 있도록 구성한다.

---

## 단순화된 파일 구조

```text
com/example/moodrest/
├── MainActivity.kt
├── ResultActivity.kt
├── VideoRecommendActivity.kt
├── DetailActivity.kt
│
├── data/
│   ├── UserCondition.kt
│   ├── WeatherInfo.kt
│   ├── VideoItem.kt
│   └── PlaceRecommendation.kt
│
├── remote/
│   ├── RetrofitClient.kt
│   ├── WeatherApiService.kt
│   ├── GeminiApiService.kt
│   └── YouTubeApiService.kt
│
├── repository/
│   ├── WeatherRepository.kt
│   ├── GeminiRepository.kt
│   └── YouTubeRepository.kt
│
├── domain/
│   ├── SleepAdvisor.kt
│   ├── RoutineAdvisor.kt
│   └── InputValidator.kt
│
├── ui/
│   ├── VideoAdapter.kt
│   └── PlaceAdapter.kt
│
└── util/
    ├── Constants.kt
    ├── ExternalIntentHelper.kt
    └── SampleDataProvider.kt
```

---

# 루트 Activity 파일

## `MainActivity.kt`

사용자가 하루 컨디션 데이터를 입력하는 첫 화면이다.

### 주요 역할

- 수면 시간 입력
- 공부/작업 시간 입력
- 스트레스 정도 선택
- 기분 상태 선택
- 지역 입력
- 원하는 힐링 방식 선택
- 시연용 샘플 데이터 자동 입력
- 입력값 검증
- `UserCondition` 객체 생성
- `Intent`를 사용해 `ResultActivity`로 데이터 전달

### 평가 기준 연결

- Activity 사용
- Intent 데이터 전달
- 안정성: 입력값 검증
- 시연 편의성: 샘플 데이터 버튼

---

## `ResultActivity.kt`

사용자의 입력값을 바탕으로 권장 수면 시간, 기본 루틴, 날씨 기반 추천 방향을 보여주는 화면이다.

### 주요 역할

- `MainActivity`에서 전달받은 `UserCondition` 데이터 수신
- `SleepAdvisor`를 통해 권장 수면 시간 계산
- `RoutineAdvisor`를 통해 기본 힐링 루틴 생성
- `WeatherRepository`를 통해 OpenWeather API 호출
- 날씨 결과를 바탕으로 추천 메시지 보정
- Gemini API로 받은 힐링 장소 추천 결과 표시
- 장소 추천 목록을 `PlaceAdapter`로 표시
- 영상 추천 화면으로 이동

### 평가 기준 연결

- Activity 사용
- Intent 데이터 전달
- Coroutine 사용
- Retrofit 사용
- 외부 API 사용
- RecyclerView 사용 가능
- 안정성: API 실패 시 기본 추천 표시

---

## `VideoRecommendActivity.kt`

YouTube Data API를 사용해 사용자 상태에 맞는 힐링 영상 목록을 보여주는 화면이다.

### 주요 역할

- 사용자 상태에 따라 YouTube 검색 키워드 생성
- `YouTubeRepository`를 통해 YouTube Data API 호출
- 영상 목록을 RecyclerView로 표시
- `VideoAdapter` 사용
- Glide를 사용해 영상 썸네일 이미지 표시
- 영상 클릭 시 YouTube 앱 또는 브라우저로 이동

### 평가 기준 연결

- Activity 사용
- Coroutine 사용
- Retrofit 사용
- Glide 사용
- 외부 API 사용
- RecyclerView 사용
- 외부 APP 연동

---

## `DetailActivity.kt`

추천 장소나 추천 영상의 상세 정보를 보여주는 화면이다.

### 주요 역할

- 선택한 추천 장소 또는 영상 정보 수신
- 상세 설명 표시
- YouTube 앱 열기 버튼 제공
- 지도 앱에서 장소 검색 버튼 제공
- 필요 시 캘린더 앱으로 루틴 등록 화면 열기

### 평가 기준 연결

- Activity 3개 이상 조건 보강
- Intent 데이터 전달
- 외부 APP 연동
- 완성도 향상

---

# `data/` 폴더

앱에서 사용하는 데이터 모델 클래스를 모아두는 폴더이다.

---

## `UserCondition.kt`

사용자가 입력한 하루 컨디션 정보를 담는 데이터 클래스이다.

### 포함 데이터 예시

- 수면 시간
- 공부/작업 시간
- 스트레스 정도
- 기분 상태
- 지역
- 원하는 힐링 방식

### 사용 위치

- `MainActivity`
- `ResultActivity`
- `GeminiRepository`
- `RoutineAdvisor`

### 권장 구현

Activity 간 전달을 위해 `Parcelable`로 구현한다.

```kotlin
@Parcelize
data class UserCondition(
    val sleepHours: Double,
    val workHours: Double,
    val stressLevel: String,
    val mood: String,
    val location: String,
    val preferredActivity: String
) : Parcelable
```

---

## `WeatherInfo.kt`

OpenWeather API 응답 중 앱에서 필요한 날씨 정보만 정리한 데이터 클래스이다.

### 포함 데이터 예시

- 도시명
- 현재 온도
- 날씨 상태
- 날씨 설명

### 사용 위치

- `WeatherRepository`
- `ResultActivity`
- `GeminiRepository`
- `RoutineAdvisor`

### 예시

```kotlin
data class WeatherInfo(
    val cityName: String,
    val temperature: Double,
    val weatherMain: String,
    val description: String
)
```

---

## `VideoItem.kt`

YouTube 추천 영상 하나의 정보를 담는 데이터 클래스이다.

### 포함 데이터 예시

- 영상 제목
- 채널명
- 썸네일 URL
- 영상 ID

### 사용 위치

- `YouTubeRepository`
- `VideoRecommendActivity`
- `VideoAdapter`
- `DetailActivity`

### 예시

```kotlin
data class VideoItem(
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val videoId: String
)
```

---

## `PlaceRecommendation.kt`

Gemini API가 추천한 힐링 장소 정보를 담는 데이터 클래스이다.

### 포함 데이터 예시

- 장소 유형
- 추천 이유
- 검색 키워드

### 사용 위치

- `GeminiRepository`
- `ResultActivity`
- `PlaceAdapter`
- `DetailActivity`

### 예시

```kotlin
data class PlaceRecommendation(
    val title: String,
    val reason: String,
    val keyword: String
)
```

---

# `remote/` 폴더

Retrofit을 이용한 외부 API 통신 인터페이스와 설정을 모아두는 폴더이다.

---

## `RetrofitClient.kt`

Retrofit 객체를 생성하고 관리하는 파일이다.

### 주요 역할

- OpenWeather API용 Retrofit 객체 생성
- Gemini API용 Retrofit 객체 생성
- YouTube Data API용 Retrofit 객체 생성
- 공통 OkHttpClient 설정
- JSON 변환기 설정

### 사용 위치

- `WeatherRepository`
- `GeminiRepository`
- `YouTubeRepository`

### 평가 기준 연결

- Retrofit 사용
- 다운로드 매니저 항목 대응
- 외부 API 사용

---

## `WeatherApiService.kt`

OpenWeather API 요청을 정의하는 Retrofit 인터페이스이다.

### 주요 역할

- 현재 날씨 조회 API 정의
- 지역명, API Key, 단위, 언어 파라미터 전달

### 예상 기능

```kotlin
@GET("weather")
suspend fun getCurrentWeather(
    @Query("q") city: String,
    @Query("appid") apiKey: String,
    @Query("units") units: String = "metric",
    @Query("lang") lang: String = "kr"
): WeatherResponse
```

### 사용 위치

- `WeatherRepository`

---

## `GeminiApiService.kt`

Gemini API 요청을 정의하는 Retrofit 인터페이스이다.

### 주요 역할

- 사용자 컨디션과 날씨 정보를 바탕으로 힐링 장소 추천 요청
- Gemini API 응답 수신

### 사용 위치

- `GeminiRepository`

### 평가 기준 연결

- 외부 API 사용
- 머신러닝/API 사용 근거

---

## `YouTubeApiService.kt`

YouTube Data API 요청을 정의하는 Retrofit 인터페이스이다.

### 주요 역할

- 검색 키워드를 바탕으로 영상 목록 요청
- 영상 제목, 채널명, 썸네일, 영상 ID 수신

### 사용 위치

- `YouTubeRepository`

### 평가 기준 연결

- 외부 API 사용
- Retrofit 사용
- RecyclerView와 연계 가능

---

# `repository/` 폴더

외부 API 호출과 데이터 변환 로직을 Activity에서 분리하는 폴더이다.

Activity가 API를 직접 호출하지 않도록 만들어 코드 안정성과 유지보수성을 높인다.

---

## `WeatherRepository.kt`

OpenWeather API 호출을 담당하는 클래스이다.

### 주요 역할

- `WeatherApiService` 호출
- Coroutine 기반 비동기 처리
- API 응답을 `WeatherInfo`로 변환
- API 실패 시 기본 날씨 정보 또는 에러 결과 반환
- try-catch를 통한 앱 비정상 종료 방지

### 사용 위치

- `ResultActivity`

### 안정성 처리 예시

- 인터넷 연결 실패
- 잘못된 지역명 입력
- API Key 오류
- 응답값 누락

---

## `GeminiRepository.kt`

Gemini API 호출을 담당하는 클래스이다.

### 주요 역할

- 사용자 상태와 날씨 정보를 프롬프트로 변환
- Gemini API 호출
- 응답 문자열을 `PlaceRecommendation` 목록으로 변환
- API 실패 시 기본 추천 장소 제공

### 기본 추천 예시

- 조용한 카페
- 실내 스트레칭
- 도서관 라운지
- 집에서 휴식

### 사용 위치

- `ResultActivity`

---

## `YouTubeRepository.kt`

YouTube Data API 호출을 담당하는 클래스이다.

### 주요 역할

- 사용자 상태에 맞는 검색 키워드 생성
- `YouTubeApiService` 호출
- API 응답을 `VideoItem` 목록으로 변환
- 실패 시 빈 리스트 또는 기본 메시지 반환

### 검색 키워드 예시

- 스트레스 높음: `10분 명상`
- 피곤함: `수면 음악`
- 집중 필요: `pomodoro study with me`
- 기분 전환: `힐링 브이로그`

### 사용 위치

- `VideoRecommendActivity`

---

# `domain/` 폴더

앱의 핵심 판단 로직을 모아두는 폴더이다.

외부 API와 직접 관련 없는 순수 Kotlin 로직을 이곳에 둔다.

---

## `SleepAdvisor.kt`

사용자의 수면 시간과 스트레스 상태를 바탕으로 권장 수면 시간을 계산하는 클래스이다.

### 주요 역할

- 수면 시간이 부족한지 판단
- 스트레스가 높은지 판단
- 권장 수면 시간 계산
- 수면 관련 안내 문장 생성

### 예상 규칙

```text
수면 5시간 미만 → 권장 수면 8.5시간
수면 5~6시간 → 권장 수면 8시간
수면 6~7시간 → 권장 수면 7.5시간
수면 7시간 이상 → 권장 수면 7시간 유지
스트레스 높음 → 권장 수면 시간 0.5시간 추가
```

### 사용 위치

- `ResultActivity`
- `RoutineAdvisor`

---

## `RoutineAdvisor.kt`

사용자 상태, 권장 수면 시간, 날씨 정보를 바탕으로 기본 힐링 루틴을 생성하는 클래스이다.

### 주요 역할

- 오늘의 회복 루틴 생성
- 공부/작업 강도 조절 추천
- 날씨에 따른 실내/실외 활동 분기
- Gemini API 실패 시 사용할 기본 추천 문장 생성

### 추천 예시

```text
수면 부족 + 스트레스 높음:
- 25분 가벼운 복습
- 10분 스트레칭
- 따뜻한 음료 마시기
- 평소보다 30분 일찍 취침 준비

날씨 맑음:
- 가까운 공원 산책 추천

비 오는 날:
- 실내 카페 또는 조용한 공간 추천
```

### 사용 위치

- `ResultActivity`
- `GeminiRepository`

---

## `InputValidator.kt`

사용자 입력값을 검증하는 클래스이다.

### 주요 역할

- 빈 입력값 검사
- 숫자 범위 검사
- 잘못된 값 입력 방지
- 다음 화면 이동 전 에러 메시지 반환

### 검증 예시

```text
수면 시간 미입력 → "수면 시간을 입력해주세요."
수면 시간 0 미만 → "수면 시간은 0보다 작을 수 없습니다."
수면 시간 24 초과 → "수면 시간은 24시간을 초과할 수 없습니다."
공부/작업 시간 24 초과 → "공부/작업 시간은 24시간을 초과할 수 없습니다."
지역 미입력 → "지역을 입력해주세요."
기분 미선택 → "기분 상태를 선택해주세요."
```

### 사용 위치

- `MainActivity`

---

# `ui/` 폴더

RecyclerView Adapter와 UI 관련 클래스를 모아두는 폴더이다.

---

## `VideoAdapter.kt`

YouTube 추천 영상 목록을 RecyclerView에 표시하는 Adapter이다.

### 주요 역할

- `VideoItem` 리스트 표시
- 영상 썸네일 표시
- 영상 제목 표시
- 채널명 표시
- 아이템 클릭 이벤트 처리
- 클릭 시 YouTube 앱 또는 브라우저 열기

### 사용 기술

- RecyclerView
- Glide

### 사용 위치

- `VideoRecommendActivity`

---

## `PlaceAdapter.kt`

Gemini API가 추천한 힐링 장소 목록을 RecyclerView에 표시하는 Adapter이다.

### 주요 역할

- `PlaceRecommendation` 리스트 표시
- 장소 유형 표시
- 추천 이유 표시
- 장소 검색 키워드 표시
- 상세 화면 이동 또는 지도 앱 검색 이벤트 처리

### 사용 기술

- RecyclerView

### 사용 위치

- `ResultActivity`
- `DetailActivity`

---

# `util/` 폴더

앱 전체에서 공통으로 사용하는 유틸리티 클래스를 모아두는 폴더이다.

---

## `Constants.kt`

앱 전체에서 사용하는 상수를 모아두는 파일이다.

### 주요 역할

- Intent Extra Key 관리
- API 관련 기본값 관리
- 요청 코드 관리
- 기본 검색 키워드 관리

### 예시

```kotlin
object Constants {
    const val EXTRA_USER_CONDITION = "extra_user_condition"
    const val EXTRA_WEATHER_INFO = "extra_weather_info"
    const val EXTRA_VIDEO_ITEM = "extra_video_item"
    const val EXTRA_PLACE_RECOMMENDATION = "extra_place_recommendation"

    const val DEFAULT_CITY = "Busan"
    const val YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v="
}
```

### 사용 위치

- 모든 Activity
- Repository
- ExternalIntentHelper

---

## `ExternalIntentHelper.kt`

외부 앱 연동 기능을 모아두는 파일이다.

### 주요 역할

- YouTube 앱 또는 브라우저 열기
- 지도 앱에서 장소 키워드 검색
- 캘린더 앱에 추천 루틴 등록
- 외부 앱이 없을 때 예외 처리

### 예상 함수

```kotlin
fun openYoutube(context: Context, videoId: String)

fun openMapSearch(context: Context, keyword: String)

fun openCalendarInsert(
    context: Context,
    title: String,
    description: String
)
```

### 평가 기준 연결

- 외부 APP 연동
- 안정성: 외부 앱 실행 실패 처리

---

## `SampleDataProvider.kt`

발표 시연용 샘플 데이터를 제공하는 파일이다.

### 주요 역할

- 피곤한 학생 시나리오 데이터 제공
- 스트레스 높은 사용자 시나리오 데이터 제공
- 컨디션 좋은 사용자 시나리오 데이터 제공
- MainActivity의 샘플 입력 버튼에서 사용

### 예시

```kotlin
object SampleDataProvider {
    fun tiredStudent(): UserCondition {
        return UserCondition(
            sleepHours = 5.0,
            workHours = 7.0,
            stressLevel = "높음",
            mood = "피곤함",
            location = "Busan",
            preferredActivity = "조용한 곳"
        )
    }

    fun goodConditionStudent(): UserCondition {
        return UserCondition(
            sleepHours = 7.5,
            workHours = 3.0,
            stressLevel = "낮음",
            mood = "좋음",
            location = "Busan",
            preferredActivity = "산책"
        )
    }
}
```

### 사용 위치

- `MainActivity`

---

# 구현 시 우선순위

## 1단계: 필수 구조 구현

- `MainActivity`
- `ResultActivity`
- `VideoRecommendActivity`
- `DetailActivity`
- `UserCondition`
- Intent 데이터 전달

## 2단계: 기본 추천 로직 구현

- `InputValidator`
- `SleepAdvisor`
- `RoutineAdvisor`
- `SampleDataProvider`

## 3단계: API 연결

- `RetrofitClient`
- `WeatherApiService`
- `WeatherRepository`
- `GeminiApiService`
- `GeminiRepository`
- `YouTubeApiService`
- `YouTubeRepository`

## 4단계: UI 목록 구현

- `VideoAdapter`
- `PlaceAdapter`
- RecyclerView 적용
- Glide로 썸네일 표시

## 5단계: 외부 앱 연동

- YouTube 앱 열기
- 지도 앱 검색
- 캘린더 앱 일정 추가

## 6단계: 안정성 보강

- API 실패 처리
- 빈 응답 처리
- 입력값 검증
- 외부 앱 실행 실패 처리
- 네트워크 오류 메시지 표시

---

# 평가 기준 대응표

| 평가 항목 | 구현 내용 |
|---|---|
| Activity 3개 이상 | MainActivity, ResultActivity, VideoRecommendActivity, DetailActivity |
| Intent | MainActivity에서 ResultActivity로 UserCondition 전달 |
| Coroutine | OpenWeather, Gemini, YouTube API 비동기 호출 |
| 다운로드 매니저 | Retrofit으로 외부 API 호출, Glide로 영상 썸네일 로딩 |
| Jetpack Library | RecyclerView 사용, 필요 시 Fragment/ViewPager2 추가 |
| 외부 APP 연동 | YouTube 앱 열기, 지도 앱 검색, 캘린더 앱 열기 |
| API | OpenWeather API, Gemini API, YouTube Data API |
| 머신러닝/API | Gemini API를 활용한 사용자 상태 기반 장소 추천 |
| 안정성 | 입력값 검증, API 실패 처리, 기본 추천 제공 |
| 디자인 | DESIGN.md 기반 카드형 UI, 색상/여백/버튼 스타일 통일 |

---

# 추가 권장 사항

## `DESIGN.md`

디자인 일관성을 위해 별도 파일로 관리한다.

포함하면 좋은 내용:

- 앱 전체 색상
- 배경색
- 카드 모양
- 버튼 스타일
- 입력창 스타일
- 폰트 크기
- 여백 기준
- 화면별 UI 분위기

## `README.md`

발표와 제출을 위해 프로젝트 설명을 정리한다.

포함하면 좋은 내용:

- 앱 개요
- 주요 기능
- 사용 API
- 사용 기술
- 평가 기준 충족표
- 실행 방법
- API Key 설정 방법
- 시연 흐름
