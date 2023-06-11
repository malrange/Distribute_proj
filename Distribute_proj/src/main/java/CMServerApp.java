import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;
import java.util.Scanner;

public class CMServerApp {
    private static CMServerStub m_serverStub;  //CMServerStub 객체선언
    private CMServerEventHandler m_eventHandler;

    public CMServerApp()
    {
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMServerEventHandler(m_serverStub);
    }
    public CMServerStub getServerStub()
    {
        return m_serverStub;
    }
    public CMServerEventHandler getServerEventHandler()
    {
        return m_eventHandler;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CMServerApp server = new CMServerApp();
        CMServerEventHandler eventHandler = server.getServerEventHandler();
        CMServerStub cmStub = server.getServerStub();
        cmStub.setAppEventHandler(eventHandler);
        cmStub.startCM();
    }
}
