import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEvent;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEventCompleteUpdateFile;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMAppEventHandler {
    private CMClientStub m_clientStub;
    private int fileLogicalClock = 1;
    private String completedPath;

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
//            case CMInfo.CM_FILE_SYNC_EVENT:
//                System.out.println("파일 수정된 이벤트 발생!!!!!");
//                processFileSyncEvent(cme);
//                break;
            default:
                return;
        }
    }

    private void processFileSyncEvent(CMEvent cme) {//5/25추가 파일 동기화작업
        CMFileSyncEvent fse = (CMFileSyncEvent) cme;
        if (fse.getID() == CMFileSyncEvent.COMPLETE_FILE_SYNC)
            System.out.println("=========파일 동기화 작업을 완료했습니다..");
        else if (fse.getID() == CMFileSyncEvent.START_FILE_LIST_ACK) {
            System.out.println("=========파일 동기화 작업 시작");
        } else if (fse.getID() == CMFileSyncEvent.COMPLETE_UPDATE_FILE) {
            CMFileSyncEventCompleteUpdateFile updateFileEvent = (CMFileSyncEventCompleteUpdateFile) fse;
            System.out.println("파일이 수정되었습니다: " + updateFileEvent.getCompletedPath());
            // Increase the logical clock for the modified file 0601추가
            completedPath = updateFileEvent.getCompletedPath().toString();
            String filePath = completedPath.substring(completedPath.lastIndexOf("\\") + 1);
//            int fileLogicalClock = 1;
            if (CMClientApp.fileLogicalClocks.containsKey(filePath)) {
                fileLogicalClock = CMClientApp.fileLogicalClocks.get(filePath) + 1;
            }
            CMClientApp.fileLogicalClocks.put(filePath, fileLogicalClock);
        }
////        System.out.println("발생 이벤트는 : " + fse.getID());
//        switch(fse.getID())
//        {
////            case CMFileSyncEvent.COMPLETE_NEW_FILE:
////                CMFileSyncEventCompleteNewFile newFileEvent = (CMFileSyncEventCompleteNewFile) fse;
////                System.out.println("파일이 생성되었습니다: " + newFileEvent.getCompletedPath());
////                //System.out.println(fse.getID());
////                //System.out.println("새로운 파일이 생성되었습니다. ");
////                break;
//            case CMFileSyncEvent.COMPLETE_UPDATE_FILE:
//                CMFileSyncEventCompleteUpdateFile updateFileEvent = (CMFileSyncEventCompleteUpdateFile) fse;
//                System.out.println("파일이 수정되었습니다: " + updateFileEvent.getCompletedPath());
//                //System.out.println(fse.getID());
//                //System.out.println("선택한 파일이 수정되었습니다. ");
//                break;
////            case CMFileSyncEvent.SKIP_UPDATE_FILE:
////                CMFileSyncEventSkipUpdateFile skipFileEvent = (CMFileSyncEventSkipUpdateFile) fse;
////                System.out.println("파일이 삭제되었습니다: " + skipFileEvent.getSkippedPath());
////                //System.out.println("선택한 파일이 삭제되었습니다. ");
////                break;
//            case CMFileSyncEvent.START_FILE_LIST_ACK:
//                System.out.println("파일 동기화 작업 시작...");
//            case CMFileSyncEvent.COMPLETE_FILE_SYNC:
//                System.out.println("파일 동기화 작업 완료... ");
//                break;
//
//            default:
//                return;
//        }
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