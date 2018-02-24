import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
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
	/**
	 * Create the application.
	 */
	public PrivateMessage(String senderName, String recepientName, BufferedReader in, PrintWriter out) {
		initialize();
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

		messageField = new JTextField();
		messageField.setBounds(25, 270, 402, 54);
		//messageField.setText("Enter message..");
		TextPrompt tp = new TextPrompt("Enter message...", messageField);
		frmPrivateChatWith.getContentPane().add(messageField);
		messageField.setColumns(10);
		// Add Listeners
		messageField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server.    Then clear
			 * the text area in preparation for the next message.
			 */
			public void actionPerformed(ActionEvent e) {
				out.println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + messageField.getText());
				messageField.setText("");
			}
		});

		JButton sendButton = new JButton("Send");
		sendButton.setBounds(446, 269, 95, 56);
		sendButton.setForeground(new Color(255, 255, 255));
		sendButton.setBackground(new Color(25, 25, 112));
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + messageField.getText());
				messageField.setText("");
			}
		});
		frmPrivateChatWith.getContentPane().add(sendButton);

		messageArea = new JTextArea();
		messageArea.setBounds(25, 36, 402, 223);
		messageArea.setBackground(new Color(240, 248, 255));
		messageArea.setEditable(false);
		frmPrivateChatWith.getContentPane().add(messageArea);
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





}
