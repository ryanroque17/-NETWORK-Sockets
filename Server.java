
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class Server  {
	private JFrame frame;
	private JPanel panel;
	private JButton toggleButton;
	private JLabel nameLabel, attachLabel;
	private JTextField textfield;
	private JLabel status;
	private static final int PORT = 9001;
	private ServerSocket listener;
	
	public Server() {
		status = new JLabel("Server is Online"); //since the server when launched is online right away
		
		status.setForeground(new Color(255, 255, 255));
		status.setFont(new Font("Tahoma", Font.BOLD, 13));
		status.setBounds(50, 11, 126, 50);
	
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(0, 206, 209));
		frame.setBounds(100, 100, 578, 374);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(status);
		
		//Toggles Server on and Off
		toggleButton = new JButton("Toggle");
		toggleButton.setBackground(new Color(25,25,112));
		toggleButton.setForeground(new Color(255,255,255));
		toggleButton.setBounds(446, 269, 95, 56);
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					try{
					if(listener.isClosed()){// if server is offline
						try {
								listener = new ServerSocket(PORT);
						}finally {
						status.setText("Server is Online");
						}
						
					}else if(!listener.isClosed()){// if server is online
						listener.close();
						status.setText("Server is Offline");
					}
					
				}catch(Exception E){
					E.printStackTrace();
		}
		}
		});
		frame.getContentPane().add(toggleButton);

	}
	private void add(JLabel status2) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.frame.setVisible(true);
		
		server.listener = new ServerSocket(PORT);
		try {
			while (true) {
				new SocketHandler(server.listener.accept()).start();
			}
		} finally {
			server.listener.close();
		}
	}

	
}

