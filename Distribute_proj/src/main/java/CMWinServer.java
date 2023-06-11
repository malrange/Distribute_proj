import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;
import java.util.List;

public class CMWinServer extends JFrame {

	private static final long serialVersionUID = 1L;

	private JButton m_startStopButton;
	private CMServerStub m_serverStub;
	private CMWinServerEventHandler m_eventHandler;

	CMWinServer() {
		MyActionListener cmActionListener = new MyActionListener();
		setTitle("CM Server");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);

		m_startStopButton = new JButton("Start Server CM");
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		topButtonPanel.add(m_startStopButton);

		setVisible(true);

		// create CM stub object and set the event handler
		m_serverStub = new CMServerStub();
		m_eventHandler = new CMWinServerEventHandler(m_serverStub, this);

		// start cm
		startCM();
	}

	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.getText().equals("Start Server CM")) {
				// start cm
				boolean bRet = m_serverStub.startCM();
				if (!bRet) {
					// printStyledMessage("CM initialization error!\n", "bold");
				} else {
					// printStyledMessage("Server CM starts.\n", "bold");
					// printMessage("Type \"0\" for menu.\n");
					// change button to "stop CM"
					button.setText("Stop Server CM");
				}
				m_startStopButton.requestFocus();
			} else if (button.getText().equals("Stop Server CM")) {
				// stop cm
				m_serverStub.terminateCM();
				// printMessage("Server CM terminates.\n");
				// change button to "start CM"
				button.setText("Start Server CM");
			}
		}
	}

	public void startCM() {
		boolean bRet = false;

		// get current server info from the server configuration file
		String strSavedServerAddress = null;
		List<String> localAddressList = null;
		int nSavedServerPort = -1;

		// set config home
		m_serverStub.setConfigurationHome(Paths.get("."));
		// set file-path home
		m_serverStub.setTransferedFileHome(m_serverStub.getConfigurationHome().resolve("server-file-path"));

		localAddressList = CMCommManager.getLocalIPList();
		if (localAddressList == null) {
			System.err.println("Local address not found!");
			return;
		}
		strSavedServerAddress = m_serverStub.getServerAddress();
		nSavedServerPort = m_serverStub.getServerPort();

		// ask the user if he/she would like to change the server info
		JTextField myCurrentAddressTextField = new JTextField(((java.util.List<?>) localAddressList).get(0).toString());
		myCurrentAddressTextField.setEnabled(false);
		JTextField serverAddressTextField = new JTextField(strSavedServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nSavedServerPort));
		Object msg[] = {
				"My Current Address:", myCurrentAddressTextField,
				"Server Address:", serverAddressTextField,
				"Server Port:", serverPortTextField
		};
		int option = JOptionPane.showConfirmDialog(null, msg, "Server Information", JOptionPane.OK_CANCEL_OPTION);

		// update the server info if the user would like to do
		if (option == JOptionPane.OK_OPTION) {
			String strNewServerAddress = serverAddressTextField.getText().toString();
			int nNewServerPort = Integer.parseInt(serverPortTextField.getText());
			if (!strNewServerAddress.equals(strSavedServerAddress) || nNewServerPort != nSavedServerPort)
				m_serverStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}

		// start cm
		bRet = m_serverStub.startCM();
		if (!bRet) {
			// printMessage("CM initialization error!\n", "bold");
		} else {
			// printStyledMessage("Server CM starts.\n", "bold");
			// printMessage("Type \"0\" for menu.\n");
			// change button to "stop CM"
			m_startStopButton.setEnabled(true);
			m_startStopButton.setText("Stop Server CM");
		}

		m_startStopButton.requestFocus();
	}

	public void terminateCM() {
		m_serverStub.terminateCM();
		// printMessage("Server CM terminates.\n");
		m_startStopButton.setText("Start Server CM");
	}

	public static void main(String[] args) {
		CMWinServer server = new CMWinServer();
	}
}
