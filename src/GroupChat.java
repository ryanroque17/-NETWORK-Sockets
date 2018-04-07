import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GroupChat{
	
	
	private String groupChatId;
	private String[] users;
	/**
	 * Create the application.
	 */
	public GroupChat(String[] users) {
		this.groupChatId = UUID.randomUUID().toString();
		this.users = users;
	}
	
	public String getGroupChatId() {
		return groupChatId;
	}
	public void setGroupChatId(String groupChatId) {
		this.groupChatId = groupChatId;
	}
	public String[] getUsers() {
		return users;
	}
	public void setUsers(String[] users) {
		this.users = users;
	}
}