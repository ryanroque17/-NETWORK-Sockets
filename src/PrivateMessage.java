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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PrivateMessage{

	private JFrame frmPrivateChatWith;
	private String recepientName; 	
	private String senderName; 	
	private BufferedReader in;
	private PrintWriter out;
	private JTextField messageField;
	private JTextArea messageArea;

	private String filePath;
	private String fileName;
	private long fileSize;
	private boolean isAttached = false;

	private Socket socket;
	/**
	 * Create the application.
	 */
	public PrivateMessage(Socket socket, String senderName, String recepientName, BufferedReader in, PrintWriter out) {
		initialize();
		this.socket = socket;
		this.recepientName = recepientName;
		this.senderName = senderName;
		this.in = in;
		this.out = out;
		frmPrivateChatWith.setTitle("Private Chat With " + recepientName);
		frmPrivateChatWith.setVisible(true);

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPrivateChatWith = new JFrame();
		frmPrivateChatWith.setTitle("Private Chat with");
		frmPrivateChatWith.setBounds(100, 100, 578, 374);
		frmPrivateChatWith.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmPrivateChatWith.getContentPane().setLayout(null);



		JLabel attachLabel = new JLabel();
		attachLabel.setBounds(130, 233, 150, 28);
		frmPrivateChatWith.getContentPane().add(attachLabel);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setBounds(75, 63, 270, 146);
		frmPrivateChatWith.getContentPane().add(fileChooser);
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
				fileChooser.showOpenDialog(frmPrivateChatWith);
			}
		});
		frmPrivateChatWith.getContentPane().add(attachButton);

		JButton sendButton = new JButton("Send");
		sendButton.setBounds(446, 269, 95, 56);
		sendButton.setForeground(new Color(255, 255, 255));
		sendButton.setBackground(new Color(25, 25, 112));
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageField.getText();

				System.out.println("senderName " +senderName);
				if (isAttached) {
					out.println("PRIVATEATTACHMENT " + senderName + "," +  recepientName + "," + filePath +"," +fileName + "," + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					messageArea.append(senderName + ": (Attachment: " +fileName+")" + "\n");

				}else {
					out.println("");
					out.println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);
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
		frmPrivateChatWith.getContentPane().add(sendButton);

		messageField = new JTextField();
		messageField.setBounds(25, 270, 402, 54);
		//messageField.setText("Enter message..");
		TextPrompt tp = new TextPrompt("Enter message...", messageField);
		frmPrivateChatWith.getContentPane().add(messageField);
		messageField.setColumns(10);
		// Add Listeners
		messageField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageField.getText();

				System.out.println("senderName " +senderName);
				if (isAttached) {
					out.println("PRIVATEATTACHMENT " + senderName + "," +  recepientName + "," + filePath +"," +fileName + "," + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					messageArea.append(senderName + ": (Attachment: " +fileName+")" + "\n");

				}else{
					out.println("");
					out.println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);
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
		messageArea = new JTextArea();
		messageArea.setBounds(25, 36, 402, 193);;
		messageArea.setBackground(new Color(240, 248, 255));
		messageArea.setEditable(false);
		frmPrivateChatWith.getContentPane().add(messageArea);
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
	public String getRecepientName() {
		return recepientName;
	}

	public String getSenderName() {
		return senderName;
	}

	public JTextArea getMessageArea() {
		return messageArea;
	}

	public JFrame getFrmPrivateChatWith() {
		return frmPrivateChatWith;
	}

	public void setFrmPrivateChatWith(JFrame frmPrivateChatWith) {
		this.frmPrivateChatWith = frmPrivateChatWith;
	}





}
