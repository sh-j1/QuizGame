import java.io.*;
import java.net.*;
import java.util.*;

public class QuizServer {
    private static final int DEFAULT_PORT = 1234; // 기본 포트 번호
    private static final String QUIZ_FILE = "src/quiz.txt"; // 퀴즈 파일 경로

    public static void main(String[] args) {
        int port = DEFAULT_PORT; // 포트 설정
        try (ServerSocket serverSocket = new ServerSocket(port)) { // 서버 소켓 생성
            System.out.println("Quiz Server is running on port " + port);

            while (true) { // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 수락
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start(); // 새로운 스레드에서 클라이언트 처리
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    // 클라이언트를 처리하는 Runnable 클래스
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket; // 클라이언트와의 소켓 연결 저장
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                List<String[]> quizData = loadQuizData(); // 퀴즈 데이터 로드
                int score = 0; // 클라이언트의 점수 초기화

                // 퀴즈 질문-응답 처리
                for (int i = 0; i < quizData.size(); i++) {
                    String[] questionAnswer = quizData.get(i);
                    String question = questionAnswer[0];
                    String answer = questionAnswer[1];

                    // 질문 전송
                    out.println("QUESTION#" + (i + 1) + "#" + question);
                    String clientResponse = in.readLine();

                    // 클라이언트 응답 처리
                    if (clientResponse != null && clientResponse.startsWith("ANSWER#")) {
                        String[] parts = clientResponse.split("#");
                        if (parts.length == 3) {
                            String clientAnswer = parts[2].trim().toLowerCase();
                            if (clientAnswer.equals(answer.toLowerCase())) {
                                out.println("RESULT#" + (i + 1) + "#CORRECT");
                                score++;
                            } else {
                                out.println("RESULT#" + (i + 1) + "#INCORRECT");
                            }
                        } else {
                            out.println("ERROR#Invalid format.");
                        }
                    } else {
                        out.println("ERROR#Unexpected message.");
                    }
                }

                // 최종 점수 전송
                out.println("SCORE#" + score);

                // 종료
                System.out.println("Client disconnected: " + socket.getInetAddress());
            } catch (IOException e) {
                System.out.println("Connection error with client: " + e.getMessage());
            }
        }

        // 퀴즈 데이터를 파일에서 읽어오는 메서드
        private List<String[]> loadQuizData() {
            List<String[]> quizList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(QUIZ_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\?");
                    if (parts.length == 2) {
                        quizList.add(new String[]{parts[0].trim(), parts[1].trim()});
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading quiz data: " + e.getMessage());
            }
            return quizList;
        }
    }
}
