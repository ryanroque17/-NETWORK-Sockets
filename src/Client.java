
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


public class Client {
	BufferedReader in;
	PrintWriter out;
	private JFrame frame;
	private JTextField messageField;
	private JTable onlineTable;
	private JButton sendButton, chatButton, attachButton, playButton, joinButton, createButton;
	private JTextArea allChat;
	private JLabel nameLabel, attachLabel;
	private DefaultTableModel model, model1;
	private JFileChooser fileChooser;

	private String filePath;
	private String fileName;
	private long fileSize;
	private boolean isAttached = false;

	private String name;
	private String[] onlineList;
	private ArrayList<PrivateMessage> privateMessages = new ArrayList<>();
	private ArrayList<GroupChatWindow> groupChats = new ArrayList<>();
	private ArrayList<GameWindow> games = new ArrayList<>();
	private ArrayList<ChatroomWindow> chatrooms = new ArrayList<>();

	private Socket socket;
	PrivateMessage currentPM = null;
	GroupChatWindow currentGC = null;
	ChatroomWindow currentCR = null;
	private JTable chatroomTable;

	public Client() {

		// Layout GUI
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(0, 206, 209));
		frame.setBounds(100, 100, 692, 374);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		playButton = new JButton("Play");
		playButton.setForeground(new Color(255, 255, 255));
		playButton.setBackground(new Color(25, 25, 112));
		playButton.setBounds(330, 233, 95, 28);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String choice = "";
				while(true) {
					choice = (String)JOptionPane.showInputDialog(
							frame,
							"Invite another player:",
							"Play TicTacToe",
							JOptionPane.PLAIN_MESSAGE,
							null,
							onlineList,
							onlineList[0]);

					if(choice.equals(name)) {
						JOptionPane.showMessageDialog(frame,
								"Give others a chance to play with you!",
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}else
						break;
				}
				
				out.println("INVITEGAME " + name + ", " + choice);

			}
		});
		frame.getContentPane().add(playButton);

		attachButton = new JButton("Attach");
		attachButton.setForeground(new Color(255, 255, 255));
		attachButton.setBackground(new Color(25, 25, 112));
		attachButton.setBounds(25, 233, 95, 28);
		attachButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setVisible(true);
				fileChooser.showOpenDialog(frame);
			}
		});
		frame.getContentPane().add(attachButton);
		fileChooser = new JFileChooser();
		fileChooser.setBounds(75, 63, 270, 146);
		frame.getContentPane().add(fileChooser);
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

		messageField = new JTextField();
		//messageField.setText("Enter message..");
		TextPrompt tp = new TextPrompt("Enter message...", messageField);
		messageField.setBounds(25, 270, 402, 54);
		frame.getContentPane().add(messageField);
		messageField.setColumns(10);

		sendButton = new JButton("Send");
		sendButton.setForeground(new Color(255, 255, 255));
		sendButton.setBackground(new Color(25, 25, 112));
		sendButton.setBounds(446, 269, 202, 56);
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageField.getText();

				if (isAttached) {
					out.println("GLOBALATTACHMENT " + name + ", " + filePath +", " +fileName + ", " + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					allChat.append(name + ": (Attachment: " +fileName+")" + "\n");
					//message = message + "(Attachment: " + fileName + ")";
					out.println("");
				}else {

					out.println("GLOBALMESSAGE " + message);
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
		frame.getContentPane().add(sendButton);

		// Add Listeners (Enter key)
		messageField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageField.getText();

				if (isAttached) {
					out.println("GLOBALATTACHMENT " + name + ", " + filePath +", " +fileName + ", " + fileSize);
					try {
						sendFile(filePath, socket);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					allChat.append(name + ": (Attachment: " +fileName+")" + "\n");
					//message = message + "(Attachment: " + fileName + ")";
					out.println("");
				}else {

					out.println("GLOBALMESSAGE " + message);
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

		allChat = new JTextArea();
		allChat.setBackground(new Color(240, 248, 255));
		allChat.setBounds(25, 36, 402, 193);
		allChat.setEditable(false);
		frame.getContentPane().add(allChat);

		nameLabel = new JLabel("Hi, <name>!");
		nameLabel.setForeground(new Color(255, 255, 255));
		nameLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		nameLabel.setBounds(25, 11, 126, 14);
		frame.getContentPane().add(nameLabel);

		chatButton = new JButton("Chat");
		chatButton.setForeground(new Color(255, 255, 255));
		chatButton.setBackground(new Color(25, 25, 112));
		chatButton.setBounds(446, 233, 95, 28);
		chatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] row = onlineTable.getSelectedRows();
				int numSelectedRows = onlineTable.getSelectedRows().length;
				if(numSelectedRows==0) {
					JOptionPane.showMessageDialog(frame,
							"Choose a recepient first");	
				}else if(numSelectedRows ==1) {
					out.println("INITIATEPM " + name + "," + row[0]);
				}else {
					int userIndex = -1;
					for(int i=0; i<model.getRowCount(); i++) {
						if(model.getValueAt(i, 0).equals(name)) {
							userIndex = i;
						}
					}

					String groupChatUsers = Arrays.toString(row);
					groupChatUsers = groupChatUsers.substring(1, groupChatUsers.length()-1);
					//System.out.println("groupChatUsersb " +groupChatUsers);

					groupChatUsers = groupChatUsers + ", " + userIndex;
					//System.out.println("groupChatUsers " +groupChatUsers);
					//System.out.println("ey" + groupChatUsers.substring(1, groupChatUsers.length()-1));
					//System.out.println("INITIATEGC " + groupChatUsers);
					out.println("INITIATEGC " + groupChatUsers);
				}
			}
		});
		frame.getContentPane().add(chatButton);


		attachLabel = new JLabel();
		attachLabel.setBounds(130, 233, 150, 28);
		frame.getContentPane().add(attachLabel);
		model = new DefaultTableModel(); 
		onlineTable = new JTable(model);
		onlineTable.setBackground(new Color(240, 248, 255));
		onlineTable.setBounds(446, 36, 95, 220);
		onlineTable.setDefaultEditor(Object.class, null);
		frame.getContentPane().add(onlineTable);
		
		//create chatroom
		JButton createButton = new JButton("Create");
		createButton.setForeground(Color.WHITE);
		createButton.setBackground(new Color(25, 25, 112));
		createButton.setBounds(553, 235, 95, 28);
		frame.getContentPane().add(createButton);
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String chatroomName;
				
				chatroomName = JOptionPane.showInputDialog(
						frame,
						"Enter Chat Room Name",
						"Create Chat Room",
						JOptionPane.PLAIN_MESSAGE);
				out.println("CREATECHATROOM " + chatroomName);
			}
		});
		
		//join chatroom
		joinButton = new JButton("Join");
		joinButton.setForeground(Color.WHITE);
		joinButton.setBackground(new Color(25, 25, 112));
		joinButton.setBounds(553, 206, 95, 28);
		frame.getContentPane().add(joinButton);
		joinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String password;
				int row = chatroomTable.getSelectedRow();
				Boolean passwordChecker = false;
				
				/* check if Password is correct
				password = JOptionPane.showInputDialog(
						frame,
						"Enter password:",
						"Join Chatroom",
						JOptionPane.PLAIN_MESSAGE);
				if
				*/
				out.println("JOINCHATROOM " + name + "," + row);
				
			}
		});
		
		model1 = new DefaultTableModel();
		chatroomTable = new JTable(model1);
		chatroomTable.setBackground(new Color(240, 248, 255));
		chatroomTable.setBounds(553, 36, 95, 220);
		chatroomTable.setDefaultEditor(Object.class, null);
		frame.getContentPane().add(chatroomTable);
		
		model1.addColumn("Chatrooms:");
		model1.addRow(new Object[]{"Chatrooms:"});

		model.addColumn("Online List:");
		model.addRow(new Object[]{"Online List:"});




	}

	private String getName() {
		return JOptionPane.showInputDialog(
				frame,
				"Choose a screen name:",
				"Screen name selection",
				JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException {

		// Make connection and initialize streams
		socket = new Socket("localhost", 9001);
		name = "";

		in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Process all messages from server, according to the protocol.
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				name = getName();
				out.println(name);
			} else if (line.startsWith("NAMEACCEPTED")) {
				nameLabel.setText("Hi, " + name + "!	");
			} else if (line.startsWith("MESSAGE")) {
				allChat.append(line.substring(8) + "\n");
			} else if (line.startsWith("ONLINELIST")) {
				String online = line.substring(11);
				onlineList = online.substring(1, online.length()-1).split("\\, ");

				model.setRowCount(0);
				model.addRow(new Object[]{"Online List:"});

				for(int i=0; i<onlineList.length; i++) {
					model.addRow(new Object[]{onlineList[i]});
				}
				for(int i=0; i<groupChats.size();i++) {
					groupChats.get(i).setOnlineList(onlineList);
				}
			} else if (line.startsWith("INITIATEPM")) {
				String receipientName = line.substring(11);
				if(name.equals(receipientName)) {
					JOptionPane.showMessageDialog(frame,
							"You can't choose yourself. Choose another person");
				}
				else {
					privateMessages.add(new PrivateMessage(socket, name, receipientName, in, out));
				}
			} else if (line.startsWith("PRIVATEMESSAGE")) {
				String senderName = line.substring(15).split("\\,")[0];
				String recepientName = line.substring(15).split("\\,")[1];
				String message = line.substring(15).split("\\,")[2];
				boolean checker = false;

				if(name.equals(senderName) || name.equals(recepientName)) {
					//Create a new PM Window
					if(privateMessages.size()==0) {
						privateMessages.add(new PrivateMessage(socket, recepientName, senderName, in, out));
						privateMessages.get(0).getMessageArea().append(senderName +": " + message + "\n");
						checker = true;
					}else {
						for(int i=0; i<privateMessages.size(); i++) {
							if((senderName.equals(privateMessages.get(i).getSenderName()) && recepientName.equals(privateMessages.get(i).getRecepientName())) || (senderName.equals(privateMessages.get(i).getRecepientName()) && recepientName.equals(privateMessages.get(i).getSenderName()))) {
								privateMessages.get(i).getMessageArea().append(senderName +": " + message + "\n");
								checker = true;
							}
						}
					}

					//Create a new PM Window
					if(!checker) {
						privateMessages.add(new PrivateMessage(socket, recepientName, senderName, in, out));
						privateMessages.get(privateMessages.size()-1).getMessageArea().append(senderName +": " + message + "\n");
					}
				}

			} else if (line.startsWith("INITIATEGC")) {
				String groupChatId = line.substring(11).split("\\, ")[0];
				//array of the users in the gc
				String[] groupChatUsers = Arrays.copyOfRange(line.substring(11).split("\\, "), 1, line.substring(11).split("\\, ").length);

				//new groupchatwindow
				GroupChatWindow newGroupChatWindow = new GroupChatWindow(socket, groupChatId, name, in, out, onlineList);
				groupChats.add(newGroupChatWindow);

				//set the users table list
				DefaultTableModel model = newGroupChatWindow.getModel();
				model.setRowCount(0);
				model.addRow(new Object[]{"Users:"});

				for(int i=0; i<groupChatUsers.length; i++) {
					model.addRow(new Object[]{groupChatUsers[i]});
				}
				newGroupChatWindow.setModel(model);

			} else if (line.startsWith("GROUPCHATMESSAGE")) {
				String groupChatId = line.substring(17).split("\\,")[0];
				String message = line.substring(17).split("\\,")[1];

				GroupChatWindow groupChat = null;
				//gets the specific groupchatwindow
				for(int i=0; i<groupChats.size();i++) {
					if(groupChatId.equals(groupChats.get(i).getGroupChatId())) {
						groupChat = groupChats.get(i);
					}
				}
				//sets the message to the text area
				groupChat.getMessageArea().append(message + "\n");
			} else if (line.startsWith("GROUPCHATINVITE")) {
				String groupChatId = line.substring(16).split("\\,")[0];
				String invitedUser = line.substring(16).split("\\,")[1];

				//array of the users in the groupchat
				String[] groupChatUsers = Arrays.copyOfRange(line.substring(16).split("\\, "), 2, line.substring(11).split("\\, ").length);

				invitedUser = invitedUser.substring(1);

				//if this client is the invited user, new window
				if(invitedUser.equals(name)) {
					GroupChatWindow newGroupChatWindow = new GroupChatWindow(socket, groupChatId, name, in, out, onlineList);
					groupChats.add(newGroupChatWindow);
				}

				GroupChatWindow groupChat = null;

				//get the specific groupchat
				for(int i=0; i<groupChats.size();i++) {
					//System.out.println(groupChatId + " = " + groupChats.get(i).getGroupChatId());
					if(groupChatId.equals(groupChats.get(i).getGroupChatId())) {
						groupChat = groupChats.get(i);
					}
				}

				//update the user list of the gc

				DefaultTableModel model = groupChat.getModel();
				model.setRowCount(0);
				model.addRow(new Object[]{"Users:"});

				for(int i=0; i<groupChatUsers.length; i++) {
					model.addRow(new Object[]{groupChatUsers[i]});

				}
				groupChat.setModel(model);
			}else if (line.contains("GROUPATTACHMENT")) {
				String groupChatId = line.substring(16).split("\\, ")[0];
				String sender = line.substring(16).split("\\, ")[1];
				String fileName = line.substring(16).split("\\, ")[2];
				String fileSize = line.substring(16).split("\\, ")[3];

				GroupChatWindow groupChat = null;

				//get the specific groupchat
				for(int i=0; i<groupChats.size();i++) {
					//System.out.println(groupChatId + " = " + groupChats.get(i).getGroupChatId());
					if(groupChatId.equals(groupChats.get(i).getGroupChatId())) {
						groupChat = groupChats.get(i);
					}
				}

				if(!sender.equals(name)) {
					int answer = JOptionPane.showConfirmDialog(
							groupChat.getFrmGroupChat(),
							"Accept attachment " + fileName + " from " + sender + "?",
							"Attachment",
							JOptionPane.YES_NO_OPTION);
					currentGC = groupChat;
					if(answer == 0)
						saveFile(socket, fileName, Integer.parseInt(fileSize));		

					currentGC.getMessageArea().append(sender + ": (Attachment: " +fileName+")" + "\n");
					currentGC = null;
				}
				allChat.append(sender + ": (Attachment: " +fileName+")" + "\n");

			}else if (line.contains("GLOBALATTACHMENT")) {
				String sender = line.substring(17).split("\\, ")[0];
				String fileName = line.substring(17).split("\\, ")[1];
				String fileSize = line.substring(17).split("\\, ")[2];

				int answer = JOptionPane.showConfirmDialog(
						frame,
						"Accept attachment " + fileName + " from " + sender + "?",
						"Attachment",
						JOptionPane.YES_NO_OPTION);

				if(answer == 0)
					saveFile(socket, fileName, Integer.parseInt(fileSize));		

				allChat.append(sender + ": (Attachment: " +fileName+")" + "\n");
			}else if (line.contains("PRIVATEATTACHMENT")) {
				String senderName = line.substring(18).split("\\,")[0];
				String recepientName = line.substring(18).split("\\,")[1];

				//System.out.println("senderpa " + senderName);
				//System.out.println("recepientpa " + recepientName);

				String fileName = line.substring(18).split("\\,")[2];
				String fileSize = line.substring(18).split("\\,")[3];

				boolean checker = false;
				currentPM = null;
				if(name.equals(senderName) || name.equals(recepientName)) {
					//Create a new PM Window
					if(privateMessages.size()==0) {
						PrivateMessage newPM = new PrivateMessage(socket, recepientName, senderName, in, out);
						privateMessages.add(newPM);
						privateMessages.get(0).getMessageArea().append(senderName + ": (Attachment: " +fileName+")" + "\n");
						currentPM = newPM;
						checker = true;
					}else {
						for(int i=0; i<privateMessages.size(); i++) {
							if((senderName.equals(privateMessages.get(i).getSenderName()) && recepientName.equals(privateMessages.get(i).getRecepientName())) || (senderName.equals(privateMessages.get(i).getRecepientName()) && recepientName.equals(privateMessages.get(i).getSenderName()))) {
								privateMessages.get(i).getMessageArea().append(senderName + ": (Attachment: " +fileName+")" + "\n");
								checker = true;
								currentPM = privateMessages.get(i);
							}
						}
					}

					//Create a new PM Window
					if(!checker) {
						PrivateMessage newPM = new PrivateMessage(socket, recepientName, senderName, in, out);
						privateMessages.add(newPM);
						privateMessages.get(privateMessages.size()-1).getMessageArea().append(senderName + ": (Attachment: " +fileName+")" + "\n");
						currentPM = newPM;

					}
				}
				int answer = JOptionPane.showConfirmDialog(
						currentPM.getFrmPrivateChatWith(),
						"Accept attachment " + fileName + " from " + senderName + "?",
						"Attachment",
						JOptionPane.YES_NO_OPTION);

				if(answer == 0)
					saveFile(socket, fileName, Integer.parseInt(fileSize));		
				currentPM=null;
			} else if (line.contains("INVITEGAME")) {
				String gameId = line.substring(11).split("\\, ")[0];
				String senderName = line.substring(11).split("\\, ")[1];
				String invitedPlayer = line.substring(11).split("\\, ")[2];
				
				GameWindow newGameWindow = null;
				if(!name.equals(senderName)) {
					 newGameWindow = new GameWindow(gameId, invitedPlayer, senderName, "O", in, out);
				}else {
					 newGameWindow = new GameWindow(gameId, senderName, invitedPlayer, "X", in, out);
					 newGameWindow.setTurn(true);
				}
				
				games.add(newGameWindow);

			}else if (line.contains("PLAYERMOVE")) {
				String gameId = line.substring(11).split("\\, ")[0];
				String buttonIndex = line.substring(11).split("\\, ")[1];
				
				GameWindow currentGame = null;
				for(int i=0; i<games.size();i++) {
				//	System.out.println(gameId + " = " + games.get(i).getGameId());
					if(gameId.equals(games.get(i).getGameId())) {
						currentGame = games.get(i);
					}
				}
				
				if(currentGame.getCharacter().equals("X"))
					currentGame.getButtons().get(Integer.parseInt(buttonIndex)).setText("O");
				else
					currentGame.getButtons().get(Integer.parseInt(buttonIndex)).setText("X");

				currentGame.setTurn(true);

			}else if (line.contains("GAMERESULT")) {
				String gameId = line.substring(11).split("\\, ")[0];
				
				GameWindow currentGame = null;
				for(int i=0; i<games.size();i++) {
				//	System.out.println(gameId + " = " + games.get(i).getGameId());
					if(gameId.equals(games.get(i).getGameId())) {
						currentGame = games.get(i);
					}
				}
				currentGame.showDefeat();
			}else if (line.contains("QUITGAME")) {
				String gameId = line.substring(9).split("\\, ")[0];
				
				GameWindow currentGame = null;
				for(int i=0; i<games.size();i++) {
				//	System.out.println(gameId + " = " + games.get(i).getGameId());
					if(gameId.equals(games.get(i).getGameId())) {
						currentGame = games.get(i);
					}
				}
				
				currentGame.enemyQuitted();
			}/*else if (line.contains("CREATECHATROOM")){
				String chatroomId = line.substring(15).split("\\,")[0];
				
				ChatroomWindow newChatroomWindow = new ChatroomWindow(socket, chatroomId, name, in, out, onlineList);
				chatrooms.add(newChatroomWindow);
				
				//set the users table list
				DefaultTableModel model = newChatroomWindow.getModel();
				model.setRowCount(0);
				
				model.addRow(new Object[]{"Users:"});
				model.addRow(new Object[]{name});
				
				newChatroomWindow.setModel(model);
			}else if (line.contains("CHATROOMMESSAGE")){
				String chatroomId = line.substring(16).split("\\,")[0];
				String message = line.substring(16).split("\\,")[1];

				ChatroomWindow chatRoom = null;
				//gets the specific chatroomWindow
				for(int i=0; i<chatrooms.size();i++) {
					if(chatroomId.equals(chatrooms.get(i).getChatroomId())) {
						chatRoom = chatrooms.get(i);
					}
				}
				//sets the message to the text area
				chatRoom.getMessageArea().append(message + "\n");
			}else if (line.contains("CHATROOMATTACHMENT")) {
				String chatroomId = line.substring(19).split("\\, ")[0];
				String sender = line.substring(19).split("\\, ")[1];
				String fileName = line.substring(19).split("\\, ")[2];
				String fileSize = line.substring(19).split("\\, ")[3];

				ChatroomWindow chatRoom = null;

				//get the specific chatroom
				for(int i=0; i<chatrooms.size();i++) {
					if(chatroomId.equals(chatrooms.get(i).getChatroomId())) {
						chatRoom = chatrooms.get(i);
					}
				}

				if(!sender.equals(name)) {
					int answer = JOptionPane.showConfirmDialog(
							chatRoom.getFrmChatroom(),
							"Accept attachment " + fileName + " from " + sender + "?",
							"Attachment",
							JOptionPane.YES_NO_OPTION);
					currentCR = chatRoom;
					if(answer == 0)
						saveFile(socket, fileName, Integer.parseInt(fileSize));		

					currentCR.getMessageArea().append(sender + ": (Attachment: " +fileName+")" + "\n");
					currentCR = null;
				}
				allChat.append(sender + ": (Attachment: " +fileName+")" + "\n");
			}else if (line.contains("JOINCHATROOM")){
				//check if password is correct
			}*/
		}
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

		String filepath = "[Client] " + name;
		File serverFolder = new File(filepath);
		serverFolder.mkdirs();
		filepath = filepath + "/" + filename;
		//System.out.println("filepath" + filepath);
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
		clientSock.getOutputStream().flush();
		out.flush();
		out.println("");
		out.println("");
		out.println("");

		fos.close();
		//System.out.println("file saved from server " + System.currentTimeMillis() % 1000);
		if(currentPM==null && currentGC==null)
			JOptionPane.showMessageDialog(frame,
					"File saved in [Client]" + name + " folder!");
		else if(currentPM!=null)
			JOptionPane.showMessageDialog(currentPM.getFrmPrivateChatWith(),
					"File saved in [Client]" + name + " folder!");
		else if(currentGC!=null)
			JOptionPane.showMessageDialog(currentGC.getFrmGroupChat(),
					"File saved in [Client]" + name + " folder!");
		else if(currentCR!=null)
			JOptionPane.showMessageDialog(currentCR.getFrmChatroom(),
					"File saved in [Client]" + name + " folder!");
		//				fos.close();
		//				dis.close();
	}

	/**
	 * Runs the client as an application with a closeable frame.
	 */
	public static void main(String[] args) throws Exception {
		Client client = new Client();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.run();
	}
}
