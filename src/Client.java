
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author NET	WORK
 */
public class Client {
	BufferedReader in;
	PrintWriter out;
	private JFrame frame;
	private JTextField messageField;
	private JTable onlineTable;
	private JButton sendButton;
	private JTextArea groupChat;
	private JLabel nameLabel;
	private DefaultTableModel model;
	/**
	 * Constructs the client by laying out the GUI and registering a
	 * listener with the textfield so that pressing Return in the
	 * listener sends the textfield contents to the server.  Note
	 * however that the textfield is initially NOT editable, and
	 * only becomes editable AFTER the client receives the NAMEACCEPTED
	 * message from the server.
	 */
	public Client() {
		
		// Layout GUI
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(0, 206, 209));
		frame.setBounds(100, 100, 578, 374);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		messageField = new JTextField();
		messageField.setText("Enter message..");
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
		
		groupChat = new JTextArea();
		groupChat.setBackground(new Color(240, 248, 255));
		groupChat.setBounds(25, 36, 402, 223);
		groupChat.setEditable(false);
		frame.getContentPane().add(groupChat);

		nameLabel = new JLabel("Hi, <name>!");
		nameLabel.setForeground(new Color(255, 255, 255));
		nameLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		nameLabel.setBounds(25, 11, 126, 14);
		frame.getContentPane().add(nameLabel);

		model = new DefaultTableModel(); 
		onlineTable = new JTable(model);
		onlineTable.setBackground(new Color(240, 248, 255));
		onlineTable.setBounds(446, 36, 95, 220);
		model.addColumn("Online List:");

		model.addRow(new Object[]{"Online List:"});

		frame.getContentPane().add(onlineTable);

		// Add Listeners
		messageField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server.    Then clear
			 * the text area in preparation for the next message.
			 */
			public void actionPerformed(ActionEvent e) {
				out.println(messageField.getText());
				messageField.setText("");
			}
		});
	}

	/**
	 * Prompt for and return the address of the server.
	 */
	private String getServerAddress() {
		return JOptionPane.showInputDialog(
				frame,
				"Enter IP Address of the Server:",
				"Welcome to the Chatter",
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Prompt for and return the desired screen name.
	 */
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
		String name = "";

		in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		System.out.println("client" + socket.getRemoteSocketAddress());
		// Process all messages from server, according to the protocol.
		while (true) {
			String line = in.readLine();
			System.out.println("line " + line);
			if (line.startsWith("SUBMITNAME")) {
				name = getName();
				out.println(name);
			} else if (line.startsWith("NAMEACCEPTED")) {
				nameLabel.setText("Hi, " + name + "!	");
			} else if (line.startsWith("MESSAGE")) {
				groupChat.append(line.substring(8) + "\n");
			} else if (line.startsWith("ONLINELIST")) {
				String online = line.substring(11);
				String[] onlineList = online.substring(1, online.length()-1).split("\\,");
				
				model.setRowCount(0);
				model.addRow(new Object[]{"Online List:"});

				for(int i=0; i<onlineList.length; i++) {
					model.addRow(new Object[]{onlineList[i]});

				}
				//System.out.println("list :" +onlineList.length);
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
