# Moody
## 역할

- 이미지 자동 추천
  - 자연어 감정 분석 : 박찬희
  - 이미지 감정 분석 : 박예지
- 데이터베이스
  - Firebase : 박찬희, 홍수희
  - SQLite : 김민영
- 안드로이드 스튜디오 : 김민영, 홍수희, 박예지, 박찬희

박찬희 본인 역할
=> 이미지 자동 추천에서 자연어 감정 분석 모델을 훈련. 훈련된 모델을 안드로이드 스튜디오에 적재. 적재된 모델을 채팅창에서 활용할 수 있도록 구현
=> Friebase database에 사용자 정보를 저장하고, 사용자의 친구 관리(즐겨찾기, 추가), 프로필 범위 설정 구현

## 제작 기간
- 2020.04 ~ 2020.09

## About
- 실시간 대화 중, 이모티콘이나 이미지를 찾다 대화 흐름이 끊기게 되는 경우가 발생한다.
- 흐름이 끊기지 않고, 텍스트를 전송했을 때 바로 관련 이미지를 추천하는 기능이 있으면 편리할 것이다.
- 채팅화면에서 이미지를 자동으로 추천해주는 앱을 구상했다.

## UI/UX
<img width="577" alt="Moody1" src="https://user-images.githubusercontent.com/108286046/190906969-b15e7c74-d7ca-4a85-91f7-1de4a7fcfa2b.png">
<img width="572" alt="Moody2" src="https://user-images.githubusercontent.com/108286046/190907158-0c29ed2a-a6d2-4fd2-a31e-372ed376bf0e.png">
<img width="568" alt="Moody3" src="https://user-images.githubusercontent.com/108286046/190907203-702a5b57-1e33-428b-ae06-60f28f1850af.png">
<img width="428" alt="Moody4" src="https://user-images.githubusercontent.com/108286046/190907272-24afe663-e50b-4857-9cbd-a17d55e206fe.png">


## Tool
- Android Studio
- Firebase
- Java
- Python
- TensorFlow

## Implementation
- Firebase의 realtime Database를 활용하여 사용자들이 실시간으로 데이터를 공유할 수 있도록 했다.
- 텐서플로우, 파이썬을 활용해 LSTM 자연어 감정 분석 모델을 훈련 시켰다.

## Preprocess
- 트위터 API를 활용해 일상 게시글을 크롤링
- 수집된 데이터를 감정단어사전을 참고해 데이터를 분류
- 데이터 전처리, 정제 과정을 통해 불필요한 어미, 특수문자를 제거
<img width="469" alt="화면 캡처 2022-09-18 225423" src="https://user-images.githubusercontent.com/108286046/190910517-787c2607-bf60-49d8-8459-e6c832050f34.png">
- 감정 분석 모델 구성
<img width="308" alt="화면 캡처 2022-09-18 225541" src="https://user-images.githubusercontent.com/108286046/190910596-0455251d-e573-4389-a0e7-5ad87382c396.png">
- 훈련된 모델 정확도
<img width="251" alt="화면 캡처 2022-09-18 225559" src="https://user-images.githubusercontent.com/108286046/190910601-7eaf42a0-5933-46b0-841d-e273951f623d.png">
