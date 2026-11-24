# N1 합격노트

JLPT N1 공부용 안드로이드 앱 (단어·문법·기출유형·독해·청해·모의고사).

- 앱 화면: `web/index.html` (한 파일, 브라우저에서도 열림)
- `main`에 push하면 GitHub Actions가 APK를 빌드해서 Releases의 `latest`에 올립니다.
- 듣기는 기기의 TTS를 사용합니다. 설정 › 텍스트 음성 변환에서 Google 일본어 음성을 설치하세요.

## 데이터 출처
- 단어 목록(N5~N1, 약 8,100개): [open-anki-jlpt-decks](https://github.com/jamsinclair/open-anki-jlpt-decks) (MIT 라이선스, tanos.co.uk 목록 기반). 라이선스 전문: `web/js/LICENSE-open-anki-jlpt-decks.txt`
- N1·N2 한국어 뜻, N1 예문·해석, 문법·기출유형 문제·독해·청해는 이 앱용으로 새로 작성
- 한자의 한국 한자음: Python `hanja` 라이브러리로 생성
