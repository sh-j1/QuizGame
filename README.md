# QuizGame

QuizGame은 **Java 소켓 프로그래밍**을 기반으로 한 멀티스레드 퀴즈 게임 서버 및 클라이언트 프로젝트입니다. 다수의 클라이언트가 동시에 서버에 접속하여 퀴즈를 풀고 결과를 확인할 수 있습니다. **텍스트 기반 클라이언트** 또는 **GUI 기반 클라이언트**를 통해 퀴즈를 풀 수 있습니다.

---

## 주요 기능
- **멀티스레드 서버**:
  - 다중 클라이언트의 동시 접속 및 퀴즈 진행 지원.
- **클라이언트**:
  - 텍스트 기반 클라이언트와 GUI 기반 클라이언트 구현.
- **퀴즈 데이터 관리**:
  - 텍스트 파일(`quiz.txt`)에서 퀴즈 데이터를 불러와 게임 진행.
- **애플리케이션 프로토콜**:
  - 서버와 클라이언트 간 메시지 형식을 정의하여 통신.

---

## 기술 스택
- **언어**:
	- Java
- **기술**:
  - Socket Programming
  - Multithreading (Thread, ExecutorService)
- **도구**:
  - IDE: IntelliJ IDEA, VSCode
  - Git & GitHub

---

## 프로젝트 구조
```
QuizGame/
├── bin/                    # 컴파일된 .class 파일
│   ├── QuizClient.class
│   ├── QuizClientGUI.class
│   ├── QuizServer.class
├── src/                    # 소스 코드 및 텍스트 파일
│   ├── QuizClient.java     # 텍스트 기반 클라이언트 소스 코드
│   ├── QuizClientGUI.java  # GUI 기반 클라이언트 소스 코드
│   ├── QuizServer.java     # 서버 소스 코드
│   ├── quiz.txt            # 퀴즈 데이터 파일
│   ├── server_info.txt     # 서버 설정 파일 (IP 및 포트 정보)
├── README.md               # 프로젝트 설명 파일
├── .gitignore              # Git 제외 파일 설정
```
---

## 실행 요구사항
- **Java JDK 17 이상** 설치
- **텍스트 파일**:
  - `quiz.txt`: 퀴즈 데이터가 포함된 파일
  - `server_info.txt`: 서버 설정 파일
    ```
    localhost
    1234
    ```
- **운영 체제**: Windows, macOS, Linux

---

## 실행 방법

### 1. 소스 코드 컴파일
```bash
javac -d bin src/*.java
```
### 2. 서버 실행
```bash
java -cp bin QuizServer
```
### 3. 클라이언트 실행
- 텍스트 기반 클라이언트:

```bash
java -cp bin QuizClient
```
- GUI 기반 클라이언트:
```bash
java -cp bin QuizClientGUI
```
---
## 파일 설명

### QuizServer.java:
- 서버 역할을 수행하며 클라이언트와의 소켓 통신을 통해 퀴즈를 진행.
- 멀티스레드 방식으로 다수의 클라이언트를 동시에 처리.
### QuizClient.java:
- 텍스트 기반 클라이언트로, 터미널에서 서버와 통신하며 퀴즈에 참여.
### QuizClientGUI.java:
- GUI 기반 클라이언트로, 사용자 친화적인 인터페이스 제공.
### quiz.txt:
- 퀴즈 데이터 파일 (질문과 정답 포함).
- 예:

```
What is the capital of ROK? Seoul
What is the capital of Japan? Tokyo
```
### server_info.txt:
- 서버의 IP와 포트 정보를 저장.
- 형식:
```
localhost
1234
```