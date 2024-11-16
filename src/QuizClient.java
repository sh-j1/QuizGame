import java.io.*;
import java.net.*;

public class QuizClient {
    // 서버 설정 파일 경로 
    private static final String CONFIG_FILE = "src/server_info.txt"; // 서버의 IP와 포트 정보를 저장한 파일
    private static final String DEFAULT_HOST = "localhost"; // 기본 호스트 이름 (로컬 호스트)
    private static final int DEFAULT_PORT = 1234; // 기본 포트 번호

    public static void main(String[] args) {
        // 서버 연결 설정
        String host = DEFAULT_HOST; // 서버 호스트 초기화
        int port = DEFAULT_PORT; // 서버 포트 초기화

        // 서버 설정 파일에서 IP와 포트 정보를 읽어오기
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            host = br.readLine().trim(); // 첫 번째 줄: 호스트 이름
            port = Integer.parseInt(br.readLine().trim()); // 두 번째 줄: 포트 번호
        } catch (IOException e) { // 파일 읽기 실패 시 기본 설정 사용
            System.out.println("Using default server settings: " + DEFAULT_HOST + ":" + DEFAULT_PORT);
        }

        Socket socket = null; // 소켓 선언
        try (
            // 서버와 연결
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)) // 사용자 입력받는 스트림
        ) {
            socket = new Socket(host, port); // 서버와 소켓 연결
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 서버로부터 입력받는 스트림
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // 서버로 출력하는 스트림

            System.out.println("Connected to the Quiz Server."); // 서버 연결 성공 메시지
            String serverMessage; // 서버로부터 받은 메시지 저장 변수

            // 서버와의 통신 반복
            while ((serverMessage = in.readLine()) != null) { // 서버 메시지를 읽음
                // 서버 메시지가 질문일 경우 처리
                if (serverMessage.startsWith("QUESTION#")) {
                    // 질문 번호와 내용을 분리
                    String[] parts = serverMessage.split("#", 3);
                    System.out.println(parts[2]); // 질문 출력
                    System.out.print("Your answer: ");
                    String answer = userInput.readLine(); // 사용자 입력 읽기
                    // 서버로 사용자의 답변 전송
                    out.println("ANSWER#" + parts[1] + "#" + answer);
                } 
                // 서버가 결과 메시지를 보낸 경우
                else if (serverMessage.startsWith("RESULT#")) {
                    System.out.println(serverMessage.split("#")[2]); // 결과 출력 (CORRECT 또는 INCORRECT)
                } 
                // 서버가 최종 점수 메시지를 보낸 경우
                else if (serverMessage.startsWith("SCORE#")) {
                    // 최종 점수 출력
                    System.out.println("Final Score: " + serverMessage.split("#")[1]);
                    // 서버에 종료 명령 전송
                    out.println("QUIT");
                    break; // 반복문 종료
                } 
                // 서버가 오류 메시지를 보낸 경우
                else if (serverMessage.startsWith("ERROR#")) {
                    System.out.println("Error: " + serverMessage.split("#", 2)[1]); // 오류 내용 출력
                }
            }

        } catch (IOException e) { // 서버 연결 실패 또는 통신 중 오류 발생
            System.out.println("Error connecting to server: " + e.getMessage());
        } finally {
            // 소켓 명시적으로 닫기
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                    System.out.println("Socket closed.");
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}
