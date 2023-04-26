import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;

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
        System.out.println("비밀번호 입력:");
        String user_pwd = scanner.nextLine();
        ret = CMClientStub.loginCM(user_id, user_pwd);
        if (ret)
            System.out.println("로그인 성공");
        else {
            System.err.println("로그인 실패");
            return;
        }
    }

    public static void testSendMultipleFiles(CMClientStub clientStub)
    {
        String[] strFiles = null;
        String strFileList = null;
        int nMode = -1; // 1: push, 2: pull
        int nFileNum = -1;
        String strTarget = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== pull/push multiple files");
        try {
            System.out.print("Select mode (1: 보내기, 2: 받기): ");
            nMode = Integer.parseInt(br.readLine());
            if(nMode == 1)
            {
                //System.out.print("Input receiver name: ");
                strTarget = "SERVER";
            }
            else if(nMode == 2)
            {
                System.out.print("Input file owner name: ");
                strTarget = br.readLine();
            }
            else
            {
                System.out.println("Incorrect transmission mode!");
                return;
            }

            System.out.print("보낼 파일의 갯수: ");
            nFileNum = Integer.parseInt(br.readLine());
            System.out.print("반드시 .\\client-file-path\\ 뒤에 파일이름 입력:  ");
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
        if(strFiles.length != nFileNum)
        {
            System.out.println("파일의 갯수가 다릅니다!");
            return;
        }

        for(int i = 0; i < nFileNum; i++)
        {
            switch(nMode)
            {
                case 1: // push
                    CMFileTransferManager.pushFile(strFiles[i], strTarget, clientStub.getCMInfo());
                    System.out.println("파일전송완료");
                    break;
                case 2: // pull
                    CMFileTransferManager.requestPermitForPullFile(strFiles[i], strTarget, clientStub.getCMInfo());
                    break;
            }
        }
        return;
    }


    public static void main(String[] args) {
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
// login CM server
//        System.out.println("아이디 입력: ");
//        String user_id = scanner.nextLine();
//        System.out.println("비밀번호 입력:");
//        String user_pwd = scanner.nextLine();
//        ret = clientStub.loginCM(user_id, user_pwd);
//        if (ret)
//            System.out.println("로그인 성공");
//        else {
//            System.err.println("로그인 실패");
//            return;
//        }
// wait before executing next API
        while(true) {
            System.out.println("==================================================");
            System.out.println("원하는 숫자 입력 단 0 로그아웃, 1 로그인, 2 서버로 파일전송 : ");
            int i = 0;
            int select = scanner.nextInt();
            switch (select) {
                case 0:
                    testLogoutDS(clientStub);
                    break;
                case 1:
                    loginClient(clientStub);
                    break;
                case 2:
                    testSendMultipleFiles(clientStub);
                    break;
            }
        }
    }
}
