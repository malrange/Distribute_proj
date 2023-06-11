import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;

public class CMClientApp {
    private static CMClientStub m_clientStub;
    private static CMClientEventHandler m_eventHandler;
    static boolean flag = false;
    static HashMap<String,Integer> fileLogicalClocks = new HashMap<>();
    static List<String> UserList = new ArrayList<>();
    static String id_name = null;
    public CMClientApp() {
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub);
    }

    public CMClientStub getClientStub() {
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }

    public static void testLogoutDS(CMClientStub clientStub) {
        boolean bRequestResult = false;
        System.out.println("====== 서버로부터 로그아웃");
        bRequestResult = clientStub.logoutCM();
        if (bRequestResult)
            System.out.println("로그아웃 성공");
        else
            System.err.println("로그아웃 실패");
        System.out.println("======");
    }

    public static void loginClient (CMClientStub CMClientStub)
    {
        Scanner scanner = new Scanner(System.in);
        boolean ret = false;
        System.out.println("아이디 입력: ");
        String user_id = scanner.nextLine();
        id_name = user_id;
        System.out.println("비밀번호 입력:");
        String user_pwd = scanner.nextLine();
        ret = CMClientStub.loginCM(user_id, user_pwd);
        if (ret) {
            System.out.println("로그인 성공");
            updateFileLogicalClocksTo1();

            // 로그인 성공 시 디렉토리 생성
            String UserListPath = ".\\client-file-path" + File.separator + user_id;
            File directory = new File(UserListPath);
            UserList.add(user_id);

            if (!directory.exists()) {
                boolean created = directory.mkdir();

                if (created) {
                    System.out.println("디렉토리 생성 성공");
                } else {
                    System.err.println("디렉토리 생성 실패");
                }
            }
        }
        else {
            System.err.println("로그인 실패");
            return;
        }
    }

    private static void updateFileLogicalClocksTo1() {
        File folder = new File("client-file-path/");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String filePath = file.getAbsolutePath();
                System.out.println(filePath);
                int logicalClock = 1; // Set the logical clock to 1 (initial value)
                updateFileLogicalClock(filePath, logicalClock);
                //System.out.println("나실행됌ㅋㅋ");
            }
        }
    }


    public static void testSendMultipleFiles(CMClientStub clientStub) {
        String[] strFiles = null;
        String strFileList = null;
        int nMode = -1; // 1: push, 2: pull
        int nFileNum = -1;
        String strTarget = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== pull/push multiple files");
        try {
            System.out.print("Select mode (1: push, 2: pull): ");
            nMode = Integer.parseInt(br.readLine());

            if (nMode == 1) {
                System.out.print("Input receiver name: ");
                strTarget = br.readLine();
            } else if (nMode == 2) {
                System.out.print("Input file owner name: ");
                strTarget = br.readLine();
            } else {
                System.out.println("Incorrect transmission mode!");
                return;
            }

            System.out.print("Number of files to send: ");
            nFileNum = Integer.parseInt(br.readLine());
            System.out.print("Enter the file names separated by spaces: ");
            strFileList = br.readLine();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        strFileList.trim();
        strFiles = strFileList.split("\\s+");
        if (strFiles.length != nFileNum) {
            System.out.println("Number of files does not match!");
            return;
        }

        for (int i = 0; i < nFileNum; i++) {
            switch (nMode) {
                case 1: // push
                    String currentDir = System.getProperty("user.dir");
                    System.out.println("Current working directory: " + currentDir);
                    String filePath = currentDir + "\\client-file-path\\" + strFiles[i];
                    String serverFilePath = currentDir + "\\server-file-path\\"+id_name+"\\"+ strFiles[i];
                    String severlogic = currentDir + "\\server-file-path\\"+strFiles[i];
                    int localLogicalClock = getFileLogicalClock(filePath);
                    int serverLogicalClock = getFileLogicalClock(severlogic);

                    System.out.println("경로 : "+filePath + " 로컬 시계 : "+getFileLogicalClock(filePath)+"서버 시계 : " + getFileLogicalClock(severlogic));
                    if (localLogicalClock > serverLogicalClock) {
                        flag = true;
                        String msg =  "파일전송 " + serverFilePath + " " +strTarget + " " + localLogicalClock;
                        testChat2(msg);//서버로 파일경로와 target과 logicalclock값을 보냄

                        //shouldSendFile = true;
                        updateFileLogicalClock(severlogic, localLogicalClock);//파일전송이벤트발생시 로컬 논리시계값으로
                        m_clientStub.pushFile(filePath, "SERVER");//파일 전송

                        System.out.println("File sent: " + filePath);
                        System.out.println("타겟은 : "+strTarget);

                    } else {
                        System.out.println("파일 충돌 위험으로 파일 전송 거부됨.");
                    }
                    break;
                case 2: // pull
                    CMFileTransferManager.requestPermitForPullFile(strFiles[i], strTarget, clientStub.getCMInfo());
                    break;
            }
        }
    }


    private static int getFileLogicalClock(String filePath) {
        if (fileLogicalClocks.containsKey(filePath)) {
            return fileLogicalClocks.get(filePath);
        } else {
            return 0; // Initial logical clock value
        }
    }
    private static void updateFileLogicalClock(String filePath, int logicalClock) {
        if (logicalClock == 0) {logicalClock = 1;}

        fileLogicalClocks.put(filePath, logicalClock);
        //System.out.println("update함수들어옴.." + fileLogicalClocks);
    }


    private static void removeFile(String filePath) {
        System.out.println("removeFile() 호출됨. 파일: " + filePath);
        //파일삭제

        try {
            Files.delete(Paths.get(filePath));
            System.out.println("서버에 파일삭제 : " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testChat2(String msg)
    {
        //System.out.println("====== chat");
        //System.out.print("message: ");
        m_clientStub.chat("/SERVER", msg);
        //System.out.println("======");
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        CMClientApp client = new CMClientApp();
        CMClientStub clientStub = client.getClientStub();
        CMClientEventHandler eventHandler = client.getClientEventHandler();
        CMSessionEvent se = new CMSessionEvent();

        boolean ret = false;
// initialize CM
        clientStub.setAppEventHandler((CMAppEventHandler) eventHandler);
        ret = clientStub.startCM();
        if (ret)
            System.out.println("초기화 성공");
        else {
            System.err.println("초기화 실패");
            return;
        }
        loginClient(clientStub);

        // 파일 변경 사항 모니터링 스레드 시작
        Thread fileMonitoringThread = new Thread(() -> monitorFileChanges());
        fileMonitoringThread.start();

        while(true) {
            System.out.println("==================================================");
            System.out.println("원하는 숫자 입력 0 메뉴 보기 1 로그아웃  2 로그인  3 파일생성  4 파일수정  5 파일삭제  6 파일전송  7 논리시계값 확인");
            int i = 0;
            int select = scanner.nextInt();
            switch (select) {
                case 0 :
                    showmenu();
                    break;
                case 1:
                    testLogoutDS(clientStub);
                    break;
                case 2:
                    loginClient(clientStub);
                    break;
                case 3:
                    createfile();
                    break;
                case 4:
                    modifyfile();
                    break;
                case 5:
                    deletefile();
                    break;
                case 6:
                    testSendMultipleFiles(clientStub);
                    flag = false;
                    break;
                case 7:
                    checkLogical();
                    break;
            }
        }
    }

    private static void showmenu() {
        System.out.println("==================================================");
        System.out.println("원하는 숫자 입력 0 메뉴 보기 1 로그아웃  2 로그인  3 파일생성  4 파일수정  5 파일삭제  6 파일전송  7 논리시계값 확인");
    }

    private static void createfile() {
        String currentDir = System.getProperty("user.dir");
        Scanner scanner = new Scanner(System.in);
        System.out.println("생성할 파일명을 입력하세요 : ");
        String fileName = scanner.nextLine();
        String filePath = currentDir + "\\client-file-path\\" + fileName;
        //String ServerPath = currentDir + "\\server-file-path\\"+id_name+"\\"+fileName;
        try {
            File file = new File(filePath);

            // 이미 파일이 존재하는지 확인
            if (file.exists()) {
                System.out.println("이미 동일한 이름의 파일이 존재합니다.");
                return;
            }

            // 파일 생성
            if (file.createNewFile()) {
                System.out.println("파일이 성공적으로 생성되었습니다.");
                updateFileLogicalClock(filePath,1);
          //      updateFileLogicalClock(ServerPath,1);
            } else {
                System.out.println("파일 생성 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            System.out.println("파일 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private static void deletefile() {
        String currentDir = System.getProperty("user.dir");
        Scanner scanner = new Scanner(System.in);
        System.out.println("삭제할 파일명을 입력하세요 : ");
        String fileName = scanner.nextLine();
        String filePath = currentDir + "\\client-file-path\\" + fileName;

        try {
            File file = new File(filePath);

            // 파일 존재 여부 확인
            if (!file.exists()) {
                System.out.println("파일이 존재하지 않습니다.");
                return;
            }

            // 파일 삭제
            if (file.delete()) {
                System.out.println("파일이 성공적으로 삭제되었습니다.");
                fileLogicalClocks.remove(filePath);
            } else {
                System.out.println("파일 삭제 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            System.out.println("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private static void modifyfile() {
        String currentDir = System.getProperty("user.dir");
        Scanner scanner = new Scanner(System.in);
        System.out.println("수정할 파일명을 입력하세요 : ");
        String fileName = scanner.nextLine();
        String filePath = currentDir + "\\client-file-path\\" + fileName;
        try {
            // 파일 존재 여부 확인
            Path file = Paths.get(filePath);
            if (!Files.exists(file)) {
                System.out.println("파일이 존재하지 않습니다.");
                return;
            }
            // 기존 파일 내용 덮어쓰기
            System.out.println("파일의 새로운 내용을 입력하세요. 종료하려면 빈 줄에서 엔터를 입력하세요.");
            StringBuilder content = new StringBuilder();
            String line;
            while (!(line = scanner.nextLine()).isEmpty()) {
                content.append(line).append(System.lineSeparator());
            }

            FileWriter writer = new FileWriter(filePath);
            writer.write(content.toString());
            writer.close();

            System.out.println("파일이 성공적으로 수정되었습니다.");
        } catch (IOException e) {
            System.out.println("파일 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    private static void checkLogical() throws IOException {
        String currentDir = System.getProperty("user.dir");
        Scanner scanner = new Scanner(System.in);
        System.out.println("논리시계값 확인할 파일이름 입력 : ");
        String fileName = scanner.nextLine();
        String filePath = currentDir + "\\client-file-path\\" + fileName;

        String serverFilePath = currentDir + "\\server-file-path\\"+id_name+"\\"+ fileName;
        String severlogic = currentDir + "\\server-file-path\\"+fileName;
        int localLogicalClock = getFileLogicalClock(filePath);
        int serverLogicalClock = getFileLogicalClock(severlogic);

        if (localLogicalClock == 0){
            return;
        }
        System.out.println("로컬 경로: " + filePath + " 서버 경로: " + serverFilePath);
        System.out.println("로컬 시간: " + localLogicalClock + " 서버 시간: " + serverLogicalClock);

//        if(localLogicalClock == 0){
//            updateFileLogicalClock(filePath,1);//로그인한후 생성된 파일에대해
//            System.out.println("로컬 시간 1로 초기화");
//            localLogicalClock = 1;
//        }

        if (localLogicalClock == serverLogicalClock) {
            System.out.println("파일수정이 일어나지 않았습니다.");
        } else if (localLogicalClock > serverLogicalClock) {
            System.out.println("파일이 최신버전입니다. 서버로 전송하겠습니다.");
            updateFileLogicalClock(severlogic, localLogicalClock); // Update the file's logical clock
            System.out.println("논리시계값 업데이트 :  " + localLogicalClock);
            m_clientStub.pushFile(filePath,"SERVER");//로컬 값이 더크면 파일전송까지
        } else {
            System.out.println("다른 클라이언트로부터 파일이 수정되었습니다.");
        }
    }
    private static void pushFiletoServer(Path modifiedFilePath) {
        //서버에게 파일전송
        System.out.println("pushFiletoServer 함수 실행됨");
        String currentDir = System.getProperty("user.dir");
        String filePath = currentDir + "\\client-file-path\\" + modifiedFilePath;
        String serverFilePath = currentDir + "\\server-file-path\\" + id_name + "\\" + modifiedFilePath;
        String severlogic = currentDir + "\\server-file-path\\"+modifiedFilePath;
        int localLogicalClock = getFileLogicalClock(filePath);
        int serverLogicalClock = getFileLogicalClock(severlogic);

        System.out.println("경로 : " + filePath + " 로컬 시계 : " + getFileLogicalClock(filePath)
                + " 서버 시계 : " + getFileLogicalClock(severlogic));
        if (localLogicalClock > serverLogicalClock) {
            flag = true;
            m_clientStub.pushFile(filePath, "SERVER"); // 파일 전송

            String msg = "파일전송 " + serverFilePath + " " + id_name + " " + localLogicalClock;
            testChat2(msg); // 서버로 파일경로와 target과 logicalclock값을 보냄

            //shouldSendFile = true;
            System.out.println("pushfiletoserver함수에서 :   "+filePath);
            updateFileLogicalClock(severlogic, localLogicalClock); // 파일전송이벤트발생시 로컬 논리시계값으로
            m_clientStub.pushFile(filePath, "SERVER"); // 파일 전송

            System.out.println("File sent: " + filePath);
            System.out.println("타겟은 : " + id_name);

        } else {
            System.out.println("파일 충돌 위험으로 파일 전송 거부됨.");
        }
        checkFileExists(modifiedFilePath);
    }


    private static void checkFileExists(Path modifiedFilePath) {
        String currentDir = System.getProperty("user.dir");
        for (String user : UserList) {
            //if(user == id_name) continue;
            String userServerDirPath = currentDir + "\\server-file-path\\" + user;
            File userServerDir = new File(userServerDirPath);
            if (userServerDir.exists() && userServerDir.isDirectory()) {
                String filePathInServer = userServerDirPath + "\\" + modifiedFilePath;
                File serverFile = new File(filePathInServer);
                if (serverFile.exists()) {
                    flag = true;
                    String userFilePath = currentDir + "\\client-file-path\\" + modifiedFilePath;
                    m_clientStub.pushFile(userFilePath, "SERVER");
                    System.out.println("파일을 서버로 전송하였습니다. 경로: " + userFilePath + " 대상 유저: " + user);
                    System.out.println(user + "에서도 파일이 수정되었습니다.");
                } else {
                    System.out.println("User " + user + "의 서버 디렉토리에 해당 파일이 존재하지 않습니다. 파일 전송이 불가능합니다.");
                }
            } else {
                System.out.println("User " + user + "의 서버 디렉토리가 존재하지 않습니다.");
            }
        }
    }


    private static void monitorFileChanges() {
        try {
            Path directory = Paths.get("client-file-path");
            WatchService watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // 파일 생성 이벤트 처리
                    if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        WatchEvent<Path> createEvent = (WatchEvent<Path>) event;
                        Path createdFilePath = createEvent.context();
                        String createdFilePath2 = createdFilePath.toString();
                        if (createdFilePath2.endsWith("~")) {
                            continue;
                            //createdFilePath2 = createdFilePath2.substring(0, createdFilePath2.length() - 1); // ~ 제거
                        }
                        System.out.println("File created: " + createdFilePath2);
                        String currentDir = System.getProperty("user.dir");
                        String localpath = currentDir + "\\client-file-path\\" + createdFilePath2;
                        // 파일 생성에 대한 추가 처리 수행
                        updateFileLogicalClock(localpath, 1);
                    }

                    // 파일 수정 이벤트 처리
                    if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        WatchEvent<Path> modifyEvent = (WatchEvent<Path>) event;
                        Path modifiedFilePath = modifyEvent.context();
                        String modifiedFilePath2 = modifiedFilePath.toString();
                        if (modifiedFilePath2.endsWith("~")) {
                            continue;
//                            modifiedFilePath2 = modifiedFilePath2.substring(0, modifiedFilePath2.length() - 1); // ~ 제거
                        }
                        //syncflag = true;
                        String currentDir = System.getProperty("user.dir");
                        String localpath = currentDir + "\\client-file-path\\" + modifiedFilePath;
                        int logicalClock = getFileLogicalClock(localpath);
                        logicalClock += 1;
                        System.out.println("File modified: " + modifiedFilePath);
                        updateFileLogicalClock(localpath,logicalClock);

                        if (flag) {
                            //flag = false;
                           // syncflag = false;
                            continue;  // 파일 전송 중인 경우, 파일 변경 이벤트 무시
                        }
                        System.out.println("수정된 파일의 로컬 시계 값은 : " +logicalClock);

                        //if (shouldSendFile) {
                        //  flag = true;
                        // 파일 전송 메소드 호출
                        pushFiletoServer(modifiedFilePath);
                       // if(syncflag) {
                            //checkFileExists(modifiedFilePath);
                        //}

                        System.out.println("여기 호출되는겨????????????????????????");
                        // flag = false;
                        //}//                 checkFileExists(modifiedFilePath);//파일 수정시
//                        System.out.println("경로: "+ modifiedFilePath);
                        System.out.println("함수로 불러봤을때 로컬 시계 값은 : " + getFileLogicalClock(localpath));
                    }

                    // 파일 삭제 이벤트 처리
                    if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        WatchEvent<Path> deleteEvent = (WatchEvent<Path>) event;
                        Path deletedFilePath = deleteEvent.context();
                        // 파일 삭제에 대한 추가 처리 수행
                        // 파일 삭제에 대한 추가 처리 수행 서버파일경로에서도 동일파일삭제
                        String deletedFileName = deletedFilePath.toString();
                        if (deletedFileName.endsWith("~")) {
                            continue;
                            //deletedFileName = deletedFileName.substring(0, deletedFileName.length() - 1); // ~ 제거
                        }
                        System.out.println("파일이 삭제됌: " + deletedFilePath);
                        String currentDir = System.getProperty("user.dir");
                        String serverFilePath = currentDir + "\\server-file-path\\" + id_name + "\\" + deletedFilePath;
                        String localFilePath = currentDir + "\\client-file-path\\" +deletedFilePath;

                        System.out.println("remove실행됌!!!!!!!!");
                        removeFile(serverFilePath);//파일삭제
                        fileLogicalClocks.remove(localFilePath);
                        fileLogicalClocks.remove(serverFilePath.toString());  // 삭제된 파일의 논리 시계 정보를 제거합니다.
                    }
                }

                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
