import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;
import kr.ac.konkuk.ccslab.cm.stub.CMStub;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;
    private CMStub m_clientStub;

    private String[] tempstr = null;

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
            case CMInfo.CM_INTEREST_EVENT:
                processInterestEvent(cme);
                break;
            default:
                return;
        }
    }

    private void processInterestEvent(CMEvent cme) {
        CMInterestEvent ie = (CMInterestEvent) cme;
        switch (ie.getID())
        {
            case CMInterestEvent.USER_TALK :
                System.out.println("<"+ie.getUserName()+">: "+ie.getTalk());
                String[] msg = ie.getTalk().split("\\s+");
                tempstr = msg;
                break;
            default:
                return;
        }
    }

    private void handleClientMessage(String[] msg) {
        //받은메세지로부터 client-file-path 공유 공간으로 파일전송
        String command = msg[0];
        if (command.equals("파일전송")) {
            String filepath = msg[1];
            String target = msg[2];
            System.out.println("파일경로 : " + msg[1]+" 타겟 : "+msg[2]);
            boolean b_return = m_serverStub.pushFile(msg[1], msg[2]);
            System.out.println("전송결과 !!!!!!!"+b_return);
        }
        else {
            System.out.println("오류 : " + command);
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
       // System.out.println(fe.getID());
        switch(fe.getID())
        {
            case CMFileEvent.START_FILE_TRANSFER:
                System.out.println(fe.getFileReceiver() + " : 파일 받기 시작");//파일받음
                break;
            case CMFileEvent.CONTINUE_FILE_TRANSFER:
                System.out.println(fe.getFileSender() + " : 로부터 파일 받는중....");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
                System.out.println(fe.getFileReceiver() + " : 파일 받기 완료");//파일다받음
//                System.out.println("tempstr      "+tempstr[0]+"  "+tempstr[1]+"  "+tempstr[2]);
//                System.out.println(" : "+tempstr);
                handleClientMessage(tempstr);
                break;
            case CMFileEvent.START_FILE_TRANSFER_ACK:
                System.out.println("서버에서 "+tempstr[2]+" 로 파일 전송중...");
                break;
            case CMFileEvent.END_FILE_TRANSFER_ACK:
                System.out.println("서버에서 "+tempstr[2]+" 로 파일 전송완료!");
                break;
            default:
                return;
        }
        return;
    }
}