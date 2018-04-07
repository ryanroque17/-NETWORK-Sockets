
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public class Client {
	BufferedReader in;
	PrintWriter out;
	private JFrame frame;
	private JTextField messageField;
	private JTable onlineTable;
	private JButton sendButton, chatButton;
	private JTextArea allChat;
	private JLabel nameLabel;
	private DefaultTableModel model;
	String name;
	String[] onlineList;
	ArrayList<PrivateMessage> privateMessages = new ArrayList<>();
	ArrayList<GroupChatWindow> groupChats = new ArrayList<>();
	public Client() {

		// Layout GUI
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(0, 206, 209));
		frame.setBounds(100, 100, 578, 374);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		messageField = new JTextField();
		//messageField.setText("Enter message..");
		TextPrompt tp = new TextPrompt("Enter message...", messageField);
		messageField.setBounds(25, 270, 402, 54);
		frame.getContentPane().add(messageField);
		messageField.setColumns(10);

		sendButton = new JButton("Send");
		sendButton.setForeground(new Color(255, 255, 255));
		sendButton.setBackground(new Color(25, 25, 112));
		sendButton.setBounds(446, 269, 95, 56);
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println(messageField.getText());
				messageField.setText("");
			}
		});
		frame.getContentPane().add(sendButton);

		allChat = new JTextArea();
		allChat.setBackground(new Color(240, 248, 255));
		allChat.setBounds(25, 36, 402, 223);
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
					System.out.println("groupChatUsersb " +groupChatUsers);

					groupChatUsers = groupChatUsers + ", " + userIndex;
					System.out.println("groupChatUsers " +groupChatUsers);
					//System.out.println("ey" + groupChatUsers.substring(1, groupChatUsers.length()-1));
					System.out.println("INITIATEGC " + groupChatUsers);
					out.println("INITIATEGC " + groupChatUsers);
				}
			}
		});
		frame.getContentPane().add(chatButton);

		model = new DefaultTableModel(); 
		onlineTable = new JTable(model);
		onlineTable.setBackground(new Color(240, 248, 255));
		onlineTable.setBounds(446, 36, 95, 220);
		onlineTable.setDefaultEditor(Object.class, null);
		frame.getContentPane().add(onlineTable);

		model.addColumn("Online List:");
		model.addRow(new Object[]{"Online List:"});



		// Add Listeners
		messageField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println(messageField.getText());
				messageField.setText("");
			}
		});
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
		Socket socket = new Socket("localhost", 9001);
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
					privateMessages.add(new PrivateMessage(name, receipientName, in, out));
				}
			} else if (line.startsWith("PRIVATEMESSAGE")) {
				String senderName = line.substring(15).split("\\,")[0];
				String recepientName = line.substring(15).split("\\,")[1];
				String message = line.substring(15).split("\\,")[2];
				boolean checker = false;
				
				if(name.equals(senderName) || name.equals(recepientName)) {
					//Create a new PM Window
					if(privateMessages.size()==0) {
						privateMessages.add(new PrivateMessage(recepientName, senderName, in, out));
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
						privateMessages.add(new PrivateMessage(recepientName, senderName, in, out));
						privateMessages.get(privateMessages.size()-1).getMessageArea().append(senderName +": " + message + "\n");
					}
				}

			} else if (line.startsWith("INITIATEGC")) {
				String groupChatId = line.substring(11).split("\\, ")[0];
				//array of the users in the gc
				String[] groupChatUsers = Arrays.copyOfRange(line.substring(11).split("\\, "), 1, line.substring(11).split("\\, ").length);
				
				//new groupchatwindow
				GroupChatWindow newGroupChatWindow = new GroupChatWindow(groupChatId, name, in, out, onlineList);
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
					GroupChatWindow newGroupChatWindow = new GroupChatWindow(groupChatId, name, in, out, onlineList);
					groupChats.add(newGroupChatWindow);
				}

				GroupChatWindow groupChat = null;
				
				//get the specific groupchat
				for(int i=0; i<groupChats.size();i++) {
					System.out.println(groupChatId + " = " + groupChats.get(i).getGroupChatId());
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
			}
		}
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
