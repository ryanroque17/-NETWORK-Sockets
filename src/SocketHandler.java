import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class SocketHandler extends Thread {
	private String name; 	
	private Socket socket;  
	private BufferedReader in;
	private PrintWriter out;
	
	private static ArrayList<String> names = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static ArrayList<BufferedReader> readers = new ArrayList<BufferedReader>();

	public SocketHandler(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			while (true) {
				out.println("SUBMITNAME");
				name = in.readLine();
				if (name == null) {
					return;
				}
				synchronized (names) {
					if (!names.contains(name)) {
						names.add(name);
						writers.add(out);
						readers.add(in);

						for (PrintWriter writer : writers) {
							writer.println("ONLINELIST " + names.toString());
						}
						break;
					}
				}
			}

			out.println("NAMEACCEPTED");

			while (true) {
				String input = in.readLine();
				if (input == null) {
					return;
				}
				if(input.contains("INITIATEPM")) {
					String[] pmUsers = input.substring(11).split("\\,");
					
					writers.get(names.indexOf(pmUsers[0])).println("INITIATEPM " + names.get(Integer.parseInt(pmUsers[1])-1));
				}else if(input.contains("PRIVATEMESSAGE")){
					String senderName = input.substring(15).split("\\,")[0];
					String recepientName = input.substring(15).split("\\,")[1];
					String message = input.substring(15).split("\\,")[2];
					
					writers.get(names.indexOf(senderName)).println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);
					writers.get(names.indexOf(recepientName)).println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);;

				}else {
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + name + ": " + input);
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			// This client is going down!  Remove its name and its print
			// writer from the sets, and close its socket.
			if (name != null) {
				names.remove(name);
				for (PrintWriter writer : writers) {
					writer.println("ONLINELIST " + names.toString());
				}
			}
			if (out != null) {
				writers.remove(out);
			}
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}