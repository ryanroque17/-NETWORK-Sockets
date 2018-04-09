import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

	private String filePath;
	private String fileName;
	private long fileSize;
	private boolean isAttached = false;
	
	private Socket socket; 
	/**
	 * Create the application.
	 */
	public GroupChatWindow(Socket socket, String id, String name, BufferedReader in, PrintWriter out, String[] onlineList) {
		initialize();
		this.socket = socket;
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

		
		JLabel attachLabel = new JLabel();
		attachLabel.setBounds(130, 233, 150, 28);
		frmGroupChat.getContentPane().add(attachLabel);
		
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
				String message = messageField.getText();

				if (isAttached) {
					out.println("GROUPATTACHMENT " + groupChatId + ", " + name + ", " + filePath +", " +fileName + ", " + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					messageArea.append(name + ": (Attachment: " +fileName+")" + "\n");
					//message = message + "(Attachment: " + fileName + ")";
					out.println("");
				}else {
					
					out.println("GROUPCHATMESSAGE " + groupChatId + "," + name + ": " + messageField.getText());
				}
				
				out.flush();
				filePath = "";
				fileName = "";
				fileSize = 0;
				attachLabel.setText("");
				isAttached = false;
				messageField.setText("");
			}
		});

		
		JButton sendButton = new JButton("Send");
		sendButton.setBounds(446, 269, 95, 56);
		sendButton.setForeground(new Color(255, 255, 255));
		sendButton.setBackground(new Color(25, 25, 112));
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageField.getText();

				if (isAttached) {
					out.println("GROUPATTACHMENT " + groupChatId + ", " + name + ", " + filePath +", " +fileName + ", " + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					messageArea.append(name + ": (Attachment: " +fileName+")" + "\n");
					//message = message + "(Attachment: " + fileName + ")";
					out.println("");
				}else {
					
					out.println("GROUPCHATMESSAGE " + groupChatId + "," + name + ": " + messageField.getText());
				}
				
				out.flush();
				filePath = "";
				fileName = "";
				fileSize = 0;
				attachLabel.setText("");
				isAttached = false;
				messageField.setText("");
			}
		});
		frmGroupChat.getContentPane().add(sendButton);

		messageArea = new JTextArea();
		messageArea.setBounds(25, 36, 402, 193);
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

	

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setBounds(75, 63, 270, 146);
		frmGroupChat.getContentPane().add(fileChooser);
		fileChooser.setVisible(false);
		fileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("ApproveSelection")) {
					filePath = fileChooser.getSelectedFile().getAbsolutePath();
					//System.out.println(filePath);
					fileName = fileChooser.getSelectedFile().getName();
					//System.out.println("fileName " + fileName);
					fileSize =  fileChooser.getSelectedFile().length(); 
					attachLabel.setText(fileName);
					isAttached = true;
				} else {
					filePath = "";
					fileName = "";
					attachLabel.setText(fileName);
					isAttached = false;
				}
			}
		});
		JButton attachButton = new JButton("Attach");
		attachButton.setForeground(new Color(255, 255, 255));
		attachButton.setBackground(new Color(25, 25, 112));
		attachButton.setBounds(25, 233, 95, 28);
		attachButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setVisible(true);
				fileChooser.showOpenDialog(frmGroupChat);
			}
		});
		frmGroupChat.getContentPane().add(attachButton);

		model = new DefaultTableModel(); 
		usersTable = new JTable(model);
		usersTable.setBackground(new Color(240, 248, 255));
		usersTable.setBounds(446, 36, 95, 220);
		usersTable.setDefaultEditor(Object.class, null);
		frmGroupChat.getContentPane().add(usersTable);

		model.addColumn("Online List:");
		model.addRow(new Object[]{"Online List:"});


	}	
	
	public void sendFile(String file, Socket socket) throws IOException {
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[4096];

		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}
		dos.flush();

		//		fis.close();
		//		dos.close();	
	}

	private void saveFile(Socket clientSock, String filename, int filesize) throws IOException {
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());

		String filepath = "[Client] " + senderName;
		File serverFolder = new File(filepath);
		serverFolder.mkdirs();
		filepath = filepath + "/" + filename;
		System.out.println("filepath" + filepath);
		FileOutputStream fos = new FileOutputStream(filepath);

		byte[] buffer = new byte[4096];

		int read = 0;
		int totalRead = 0;
		int remaining = filesize;

		while(remaining > 0 && (read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) !=-1) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");

			fos.write(buffer, 0, read);

		}
		fos.flush();

		fos.close();
		System.out.println("file saved from server " + System.currentTimeMillis() % 1000);
		//				fos.close();
		//				dis.close();
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

	public JFrame getFrmGroupChat() {
		return frmGroupChat;
	}

	public void setFrmGroupChat(JFrame frmGroupChat) {
		this.frmGroupChat = frmGroupChat;
	}
	
	




}
