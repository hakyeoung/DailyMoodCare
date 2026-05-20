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
