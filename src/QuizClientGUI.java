import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class QuizClientGUI {
    // 서버 정보 파일 경로 
    private static final String CONFIG_FILE = "src/server_info.txt"; // 텍스트 파일로 서버 정보 불러오기
    private static final String DEFAULT_HOST = "localhost"; // 기본 호스트 (로컬 서버)
    private static final int DEFAULT_PORT = 1234; // 기본 포트 번호

    private String host; // 서버의 호스트 이름
    private int port; // 서버의 포트 번호
    private String currentQuestionNumber; // 현재 문제 번호 저장 (서버에서 받은 질문의 번호)

    // GUI 구성 요소
    private JFrame frame; // 메인 프레임
    private JLabel questionLabel; // 질문을 표시하는 레이블
    private JTextField answerField; // 답변 입력 필드
    private JButton submitButton; // 제출 버튼
    private JTextArea resultArea; // 결과를 표시하는 텍스트 영역

    // 서버와 통신을 위한 스트림
    private PrintWriter out; // 서버로 메시지를 보내는 출력 스트림
    private BufferedReader in; // 서버로부터 메시지를 받는 입력 스트림

    // 생성자: GUI 초기화 및 서버 연결
    public QuizClientGUI() {
        loadServerConfig(); // 서버 설정 불러오기
        setupGUI(); // GUI 설정
        connectToServer(); // 서버 연결
    }

    // 서버 설정 파일에서 호스트와 포트 정보를 읽어오는 메서드
    private void loadServerConfig() {
        host = DEFAULT_HOST; // 기본 호스트
        port = DEFAULT_PORT; // 기본 포트
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            host = br.readLine().trim(); // 첫 번째 줄에서 호스트 정보 읽기
            port = Integer.parseInt(br.readLine().trim()); // 두 번째 줄에서 포트 정보 읽기
        } catch (IOException e) { // 설정 파일을 읽지 못한 경우 기본값 사용
            System.out.println("Using default server settings: " + DEFAULT_HOST + ":" + DEFAULT_PORT);
        }
    }

    // GUI 구성 요소 설정
    private void setupGUI() {
        frame = new JFrame("Quiz Game"); // 메인 프레임 생성
        frame.setSize(400, 300); // 프레임 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫기 버튼 동작 설정
        frame.setLayout(new BorderLayout()); // 레이아웃 설정

        questionLabel = new JLabel("Connecting to the server..."); // 질문 표시 레이블 초기화
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER); // 텍스트를 가운데 정렬
        frame.add(questionLabel, BorderLayout.NORTH); // 레이블을 프레임 상단에 추가

        answerField = new JTextField(); // 답변 입력 필드 생성
        frame.add(answerField, BorderLayout.CENTER); // 입력 필드를 프레임 중앙에 추가

        submitButton = new JButton("Submit"); // 제출 버튼 생성
        frame.add(submitButton, BorderLayout.SOUTH); // 버튼을 프레임 하단에 추가

        resultArea = new JTextArea(); // 결과 표시 영역 생성
        resultArea.setEditable(false); // 읽기 전용 설정
        frame.add(new JScrollPane(resultArea), BorderLayout.EAST); // 스크롤 가능한 텍스트 영역 추가

        submitButton.addActionListener(new SubmitButtonListener()); // 버튼에 이벤트 리스너 추가

        frame.setVisible(true); // 프레임 표시
    }

    // 서버에 연결하는 메서드
    private void connectToServer() {
        try {
            Socket socket = new Socket(host, port); // 서버와의 소켓 연결 생성
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 입력 스트림 초기화
            out = new PrintWriter(socket.getOutputStream(), true); // 출력 스트림 초기화
            new Thread(new ServerListener()).start(); // 서버로부터 메시지를 처리하는 스레드 실행
        } catch (IOException e) { // 연결 실패 시 오류 메시지 표시
            JOptionPane.showMessageDialog(frame, "Error connecting to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // 프로그램 종료
        }
    }

    // 제출 버튼 클릭 시 동작 정의
    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String answer = answerField.getText().trim(); // 입력 필드에서 답변 읽기
            if (!answer.isEmpty()) { // 답변이 비어있지 않은 경우
                if (currentQuestionNumber != null) { // 현재 질문 번호가 있는 경우
                    out.println("ANSWER#" + currentQuestionNumber + "#" + answer); // 서버로 메시지 전송
                    answerField.setText(""); // 입력 필드 초기화
                } else { // 질문 번호가 없는 경우
                    JOptionPane.showMessageDialog(frame, "No active question!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else { // 답변이 비어있는 경우
                JOptionPane.showMessageDialog(frame, "Please enter an answer!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 서버 메시지를 처리하는 스레드
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) { // 서버 메시지 수신
                    if (serverMessage.startsWith("QUESTION#")) { // 질문 메시지인 경우
                        currentQuestionNumber = serverMessage.split("#")[1]; // 문제 번호 저장
                        questionLabel.setText(serverMessage.split("#", 3)[2]); // 질문 텍스트 표시
                    } else if (serverMessage.startsWith("RESULT#")) { // 결과 메시지인 경우
                        resultArea.append(serverMessage.split("#")[2] + "\n"); // 결과를 텍스트 영역에 추가
                    } else if (serverMessage.startsWith("SCORE#")) { // 최종 점수 메시지인 경우
                        JOptionPane.showMessageDialog(frame, "Final Score: " + serverMessage.split("#")[1], "Quiz Completed", JOptionPane.INFORMATION_MESSAGE);
                        out.println("QUIT"); // 서버에 종료 명령 전송
                        System.exit(0); // 프로그램 종료
                    } else if (serverMessage.startsWith("ERROR#")) { // 오류 메시지인 경우
                        JOptionPane.showMessageDialog(frame, "Error: " + serverMessage.split("#", 2)[1], "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IOException e) { // 서버와의 연결이 끊어진 경우
                JOptionPane.showMessageDialog(frame, "Connection lost: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // 프로그램 종료
            }
        }
    }

    // 프로그램 시작 지점
    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizClientGUI::new); // GUI 생성 및 실행
    }
}
