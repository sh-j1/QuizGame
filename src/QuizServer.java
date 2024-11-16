import java.io.*;
import java.net.*;
import java.util.*;

public class QuizServer {
    private static final String CONFIG_FILE = "src/server_info.txt"; // 서버 설정 파일 경로
    private static final String QUIZ_FILE = "src/quiz.txt"; // 퀴즈 파일 경로
    private static final String DEFAULT_HOST = "localhost"; // 기본 호스트 이름
    private static final int DEFAULT_PORT = 1234; // 기본 포트 번호
    private static volatile boolean running = true; // 서버 실행 상태 플래그 

    public static void main(String[] args) {
        String host = DEFAULT_HOST; // 초기값으로 기본 호스트 설정
        int port = DEFAULT_PORT;   // 초기값으로 기본 포트 설정

        // server_info.txt 파일에서 호스트와 포트 정보 읽기
        try {
            File configFile = new File(CONFIG_FILE);
            System.out.println("Looking for config file at: " + configFile.getAbsolutePath());

            BufferedReader br = new BufferedReader(new FileReader(configFile));
            host = br.readLine().trim(); // 첫 번째 줄: 호스트 정보
            port = Integer.parseInt(br.readLine().trim()); // 두 번째 줄: 포트 정보
            br.close();

            System.out.println("Config file read successfully. Host: " + host + ", Port: " + port);
        } catch (IOException e) {
            System.out.println("Error reading config file: " + e.getMessage());
            System.out.println("Using default settings: " + DEFAULT_HOST + ":" + DEFAULT_PORT);
        } catch (NumberFormatException e) {
            System.out.println("Port number format is invalid. Using default port.");
            port = DEFAULT_PORT;
        }

        // 서버 소켓 생성 및 시작
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Quiz Server is running on " + host + ":" + port);

            while (running) { // 서버 실행 상태에 따라 루프 지속
                try {
                    Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 수락
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    new Thread(new ClientHandler(clientSocket, serverSocket)).start(); // 새로운 스레드에서 클라이언트 처리
                } catch (SocketException e) {
                    if (!running) {
                        System.out.println("Server shutting down...");
                    } else {
                        System.out.println("Connection error: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    // 클라이언트를 처리하는 Runnable 클래스
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ServerSocket serverSocket;

        public ClientHandler(Socket socket, ServerSocket serverSocket) {
            this.socket = socket; // 클라이언트와의 소켓 연결 저장
            this.serverSocket = serverSocket;
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
                    } else if (clientResponse != null && clientResponse.equals("QUIT")) {
                        // 클라이언트로부터 QUIT 메시지를 받은 경우
                        System.out.println("Received QUIT command. Attempting to close server socket...");
                        running = false; 
                        new Thread(() -> {
                            try {
                                serverSocket.close(); // 서버 소켓 종료
                                System.out.println("Server socket closed.");
                            } catch (IOException e) {
                                System.err.println("Error closing server socket: " + e.getMessage());
                            }
                        }).start();
                        break; // 반복문 종료
                    } else {
                        out.println("ERROR#Unexpected message.");
                    }
                }

                // 최종 점수 전송
                out.println("SCORE#" + score);

            } catch (IOException e) {
                System.out.println("Connection error with client: " + e.getMessage());
            }
        }

        // 퀴즈 데이터를 파일에서 읽어오는 메서드
        private static List<String[]> loadQuizData() {
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
