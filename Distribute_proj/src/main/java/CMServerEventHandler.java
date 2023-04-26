import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;
    public CMServerEventHandler(CMServerStub serverStub)
    {
        m_serverStub = serverStub;
    }
    @Override
    public void processEvent(CMEvent cme) {

        switch(cme.getType())
        {
            case CMInfo.CM_SESSION_EVENT:
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
        CMSessionEvent se = (CMSessionEvent) cme;
        switch(se.getID())
        {
            case CMSessionEvent.JOIN_SESSION:
                System.out.println("["+se.getUserName()+"] requests to join " + se.getSessionName());
                break;
            case CMSessionEvent.SESSION_REMOVE_USER:
                System.out.println("["+se.getUserName()+"] leaves " +se.getSessionName());
                break;
            case CMSessionEvent.LOGOUT://추가
            {
                System.out.println("["+se.getUserName()+"] reguests logout.");
                break;
            }

            default:
                return;
        }
    }

    private void processFileEvent(CMEvent cme)
    {
        CMFileEvent fe = (CMFileEvent) cme;
        switch(fe.getID())
        {
            case CMFileEvent.START_FILE_TRANSFER:
                System.out.println(fe.getFileReceiver() + " : 파일 받기 시작");//파일받음
                break;

            case CMFileEvent.END_FILE_TRANSFER:
                System.out.println(fe.getFileReceiver() + " : 파일 받기 완료");//파일다받음
                break;
        }
        return;
    }


}