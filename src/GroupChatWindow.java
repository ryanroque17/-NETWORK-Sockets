import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class GroupChatWindow{
	
	private String groupChatId;
	private JFrame frmGroupChat;
	private String recepientName; 	
	private String senderName; 	
	private BufferedReader in;
	private PrintWriter out;
	private JTextField messageField;
	private JTextArea messageArea;
	private DefaultTableModel model;
	private JTable usersTable;
	private String name;
	private JButton inviteButton;
	private String[] onlineList;
	/**
	 * Create the application.
	 */
	public GroupChatWindow(String id, String name, BufferedReader in, PrintWriter out, String[] onlineList) {
		initialize();
		this.groupChatId = id;
		this.name = name;
		this.in = in;
		this.out = out;
		this.onlineList = onlineList;
		frmGroupChat.setTitle("Group Chat " + id + " - " + name);
		frmGroupChat.setVisible(true);

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGroupChat = new JFrame();
		frmGroupChat.setBounds(100, 100, 578, 374);
		frmGroupChat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmGroupChat.getContentPane().setLayout(null);

		messageField = new JTextField();
		messageField.setBounds(25, 270, 402, 54);
		//messageField.setText("Enter message..");
		TextPrompt tp = new TextPrompt("Enter message...", messageField);
		frmGroupChat.getContentPane().add(messageField);
		messageField.setColumns(10);
		// Add Listeners
		messageField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server.    Then clear
			 * the text area in preparation for the next message.
			 */
			public void actionPerformed(ActionEvent e) {
				out.println("GROUPCHATMESSAGE " + groupChatId + "," + name + ": " + messageField.getText());
				messageField.setText("");
			}
		});

		JButton sendButton = new JButton("Send");
		sendButton.setBounds(446, 269, 95, 56);
		sendButton.setForeground(new Color(255, 255, 255));
		sendButton.setBackground(new Color(25, 25, 112));
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println("GROUPCHATMESSAGE " + groupChatId + "," + name + ": " + messageField.getText());
				messageField.setText("");
			}
		});
		frmGroupChat.getContentPane().add(sendButton);

		messageArea = new JTextArea();
		messageArea.setBounds(25, 36, 402, 223);
		messageArea.setBackground(new Color(240, 248, 255));
		messageArea.setEditable(false);
		frmGroupChat.getContentPane().add(messageArea);
		
		inviteButton = new JButton("Invite");
		inviteButton.setForeground(new Color(255, 255, 255));
		inviteButton.setBackground(new Color(25, 25, 112));
		inviteButton.setBounds(446, 233, 95, 28);
		inviteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String invitedUser;
				Boolean addedAlreadyChecker = false;
				Boolean userDoesntExistChecker = true;

				invitedUser = JOptionPane.showInputDialog(
						frmGroupChat,
						"Add another user:",
						"Screen name selection",
						JOptionPane.PLAIN_MESSAGE);
				
				for(int i=0; i<model.getRowCount(); i++) {
					if(model.getValueAt(i, 0).equals(invitedUser)) {
						addedAlreadyChecker = true;
					}
				}
				for(int i=0; i<onlineList.length; i++) {
					if(invitedUser.equals(onlineList[i])) {
						userDoesntExistChecker = false;
					}
				}
				if(addedAlreadyChecker) {
					JOptionPane.showMessageDialog(frmGroupChat, "User is already in the group chat!");
				}else if(userDoesntExistChecker){
					JOptionPane.showMessageDialog(frmGroupChat, "User doesn't exist!");

				}else {
					System.out.println("invited user: " + invitedUser);
					out.println("GROUPCHATINVITE " + groupChatId + "," + invitedUser);
				}
			}
		});
		frmGroupChat.getContentPane().add(inviteButton);
		
		model = new DefaultTableModel(); 
		usersTable = new JTable(model);
		usersTable.setBackground(new Color(240, 248, 255));
		usersTable.setBounds(446, 36, 95, 220);
		usersTable.setDefaultEditor(Object.class, null);
		frmGroupChat.getContentPane().add(usersTable);

		model.addColumn("Online List:");
		model.addRow(new Object[]{"Online List:"});
		
		
	}	
	
	public DefaultTableModel getModel() {
		return model;
	}

	public void setModel(DefaultTableModel model) {
		this.model = model;
	}

	public JTextArea getMessageArea() {
		return messageArea;
	}

	public String getGroupChatId() {
		return groupChatId;
	}

	public void setGroupChatId(String groupChatId) {
		this.groupChatId = groupChatId;
	}

	public String[] getOnlineList() {
		return onlineList;
	}

	public void setOnlineList(String[] onlineList) {
		this.onlineList = onlineList;
	}

	



}
