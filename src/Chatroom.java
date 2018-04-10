import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Chatroom {

	private String chatroomId;
	private ArrayList<String> users;
	private String password;
	/**
	 * Create the application.
	 */
	public Chatroom(String name) {
		this.chatroomId = name;
		this.users = new ArrayList<String>();
	}
	
	public String getChatroomId() {
		return chatroomId;
	}
	public void setChatroomId(String chatroomId) {
		this.chatroomId = chatroomId;
	}
	public ArrayList<String> getUsers() {
		return users;
	}
	public void addUser(String user) {
		this.users.add(user);
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
