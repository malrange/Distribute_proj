import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMAppEventHandler {
    private CMClientStub m_clientStub;

    public CMClientEventHandler(CMClientStub stub)
    {
        m_clientStub = stub;
    }
    @Override
    public void processEvent(CMEvent cme) {//event어떤종류인지 구분
        switch(cme.getType())
        {
            case CMInfo.CM_SESSION_EVENT://SESSION_EVENT아래에서 발생한 이벤트 번호를 찾음 GETID()
                processSessionEvent(cme);
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            default:
                return;
        }
    }
    private void processSessionEvent(CMEvent cme)
    {
        CMSessionEvent se = (CMSessionEvent)cme;
        switch(se.getID())
        {
            case CMSessionEvent.LOGIN_ACK:
                if(se.isValidUser() == 0)
                {
                    System.err.println("This client fails authentication by the default server!");
                }
                else if(se.isValidUser() == -1)
                {
                    System.err.println("이미 서버에 로그인 되어있습니다!");
                }
                else
                {
                    System.out.println("성공적으로 로그인하였습니다!");
                }
                break;
            case CMSessionEvent.SESSION_ADD_USER:
                System.out.println( se.getUserName() +" 환영합니다");
                break;

            case CMSessionEvent.SESSION_REMOVE_USER:
                System.out.println( se.getUserName() +" 연결이 끊겼습니다.");
                break;

            default:
                return;
        }
    }

    private void processFileEvent(CMEvent cme)
    {
        CMFileEvent fe = (CMFileEvent) cme;
        int nOption = -1;
        switch(fe.getID()) {
            case CMFileEvent.START_FILE_TRANSFER_ACK:
                System.out.println(fe.getFileSender() + " 파일 전송 시작");//전송하기 시작합니다.
                break;

            case CMFileEvent.END_FILE_TRANSFER_ACK:
                System.out.println(fe.getFileSender() + " 파일 전송 완료");//전송 끝
                break;
        }
        return;
    }
}