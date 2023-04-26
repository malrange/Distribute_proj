import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerApp {
    private CMServerStub m_serverStub;  //CMServerStub 객체선언
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
        CMServerApp server = new CMServerApp();
        CMServerStub cmStub = server.getServerStub();
        cmStub.setAppEventHandler(server.getServerEventHandler());
        cmStub.startCM();
    }
}
