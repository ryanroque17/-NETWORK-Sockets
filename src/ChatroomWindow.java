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

public class ChatroomWindow {

	private String chatroomId;
	private JFrame frmChatroom;
	private String senderName; 	
	private BufferedReader in;
	private PrintWriter out;
	private JTextField messageField;
	private JTextArea messageArea;
	private DefaultTableModel model;
	private JTable usersTable;
	private String name;
	private String[] onlineList;
	private String password;

	private String filePath;
	private String fileName;
	private long fileSize;
	private boolean isAttached = false; 
	
	private Socket socket;
	/**
	 * Create the application.
	 */
	public ChatroomWindow(Socket socket, String id, String name, BufferedReader in, PrintWriter out, String[] onlineList) {
		initialize();
		this.socket = socket;
		this.chatroomId = id;
		this.name = name;
		this.in = in;
		this.out = out;
		this.onlineList = onlineList;
		frmChatroom.setTitle("Chat Room: " + id);
		frmChatroom.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmChatroom = new JFrame();
		frmChatroom.getContentPane().setBackground(new Color(124, 145, 249));
		frmChatroom.setBounds(100, 100, 578, 374);
		frmChatroom.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmChatroom.getContentPane().setLayout(null);
		
		JLabel attachLabel = new JLabel();
		attachLabel.setBounds(130, 233, 150, 28);
		frmChatroom.getContentPane().add(attachLabel);
		
		messageField = new JTextField();
		messageField.setBounds(25, 270, 402, 54);
		//messageField.setText("Enter message..");
		TextPrompt tp = new TextPrompt("Enter message...", messageField);
		frmChatroom.getContentPane().add(messageField);
		messageField.setColumns(10);
		// Add Listeners
		messageField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server.    Then clear
			 * the text area in preparation for the next message.
			 */
			public void actionPerformed(ActionEvent e) {

				if (isAttached) {
					out.println("CHATROOMATTACHMENT " + chatroomId + ", " + name + ", " + filePath +", " +fileName + ", " + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					
					messageArea.append(name + ": (Attachment: " +fileName+")" + "\n");
					out.println("");
				}else {
					
					out.println("CHATROOMMESSAGE " + chatroomId + "," + name + ": " + messageField.getText());
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

				if (isAttached) {
					out.println("CHATROOMATTACHMENT " + chatroomId + ", " + name + ", " + filePath +", " +fileName + ", " + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					
					messageArea.append(name + ": (Attachment: " +fileName+")" + "\n");
					out.println("");
				}else {
					
					out.println("CHATROOMMESSAGE " + chatroomId + "," + name + ": " + messageField.getText());
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
		frmChatroom.getContentPane().add(sendButton);
		
		messageArea = new JTextArea();
		messageArea.setBounds(25, 36, 402, 193);
		messageArea.setBackground(new Color(240, 248, 255));
		messageArea.setEditable(false);
		frmChatroom.getContentPane().add(messageArea);
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setBounds(75, 63, 270, 146);
		frmChatroom.getContentPane().add(fileChooser);
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
				fileChooser.showOpenDialog(frmChatroom);
			}
		});
		frmChatroom.getContentPane().add(attachButton);

		model = new DefaultTableModel(); 
		usersTable = new JTable(model);
		usersTable.setBackground(new Color(240, 248, 255));
		usersTable.setBounds(446, 36, 95, 220);
		usersTable.setDefaultEditor(Object.class, null);
		frmChatroom.getContentPane().add(usersTable);

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

	public String getChatroomId() {
		return chatroomId;
	}

	public void setChatroomId(String chatroomId) {
		this.chatroomId = chatroomId;
	}

	public JFrame getFrmChatroom() {
		return frmChatroom;
	}

	public void setFrmChatroom(JFrame frmChatroom) {
		this.frmChatroom = frmChatroom;
	}

	public JTextArea getMessageArea() {
		return messageArea;
	}

	public void setMessageArea(JTextArea messageArea) {
		this.messageArea = messageArea;
	}

	public DefaultTableModel getModel() {
		return model;
	}

	public void setModel(DefaultTableModel model) {
		this.model = model;
	}

	public String[] getOnlineList() {
		return onlineList;
	}

	public void setOnlineList(String[] onlineList) {
		this.onlineList = onlineList;
	}

}
