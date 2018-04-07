import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class SocketHandler extends Thread {
	private String name; 	
	private Socket socket;  
	private BufferedReader in;
	private PrintWriter out;
	
	private static ArrayList<String> names = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static ArrayList<BufferedReader> readers = new ArrayList<BufferedReader>();

	private static ArrayList<GroupChat> groupChats = new ArrayList<GroupChat>();
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
					// recepientName = names.get(Integer.parseInt(pmUsers[1])-1));
					writers.get(names.indexOf(pmUsers[0])).println("INITIATEPM " + names.get(Integer.parseInt(pmUsers[1])-1));
				}else if(input.contains("PRIVATEMESSAGE")){
					String senderName = input.substring(15).split("\\,")[0];
					String recepientName = input.substring(15).split("\\,")[1];
					String message = input.substring(15).split("\\,")[2];
					
					writers.get(names.indexOf(senderName)).println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);
					writers.get(names.indexOf(recepientName)).println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);;

				}else if(input.contains("INITIATEGC")){
					//gcUsers - array of indices based from the selected rows in the online table
					String[] gcUsers = input.substring(11).split("\\, ");
					
					//create new GroupChat
					GroupChat newGroupChat = new GroupChat(gcUsers);
					groupChats.add(newGroupChat);
					
					//get the names of the user based sa index
					String[] gcUserNames = new String[gcUsers.length];
					for(int i=0; i<gcUsers.length; i++) {
						int index = Integer.parseInt(gcUsers[i]);
						System.out.println(index);
						gcUserNames[i] = names.get(index-1);
					}
					String names = Arrays.toString(gcUserNames);
					String gcUserNamesList = names.substring(1, names.length()-1);
					
					//send the groupchatId and the list of the users to the clients of the GC
					for(int i=0; i<gcUsers.length; i++) {
						int index = Integer.parseInt(gcUsers[i]);
						System.out.println(index);
						writers.get(index-1).println("INITIATEGC " + newGroupChat.getGroupChatId() + ", " +gcUserNamesList);
					}

				}else if(input.contains("GROUPCHATMESSAGE")){
					String groupChatId = input.substring(17).split("\\,")[0];
					String message = input.substring(17).split("\\,")[1];
					GroupChat groupChat = null;
					
					//get the specific groupchat
					for(int i=0; i<groupChats.size();i++) {
						if(groupChatId.equals(groupChats.get(i).getGroupChatId())) {
							groupChat = groupChats.get(i);
						}
					}
					
					//get the clients of the groupchat
					String[] groupChatUsers = groupChat.getUsers();
					for(int i=0; i<groupChatUsers.length; i++) {
						int index = Integer.parseInt(groupChatUsers[i]);

						writers.get(index-1).println("GROUPCHATMESSAGE " + groupChatId + "," + message);
					}

				}else if(input.contains("GROUPCHATINVITE")){	//when a user invited another user to the gc
					String groupChatId = input.substring(16).split("\\,")[0];
					String invitedUser = input.substring(16).split("\\,")[1];
					
					//get the group chat
					GroupChat groupChat = null;
					for(int i=0; i<groupChats.size();i++) {
						if(groupChatId.equals(groupChats.get(i).getGroupChatId())) {
							groupChat = groupChats.get(i);
						}
					}
					
					//add the new user
					String[] groupChatUsers = groupChat.getUsers();
					String[] newGcUserNames = new String[groupChatUsers.length + 1];
					String[] newGcUsers = new String[groupChatUsers.length + 1];

					for(int i=0; i<groupChatUsers.length; i++) {
						int index = Integer.parseInt(groupChatUsers[i]);
						System.out.println(index);
						newGcUsers[i] = groupChatUsers[i];
						newGcUserNames[i] = names.get(index-1);
					}
					newGcUserNames[newGcUserNames.length-1] = invitedUser;
					System.out.println("index of invited " + names.indexOf(invitedUser));
					System.out.println("invited user" +invitedUser);

					newGcUsers[newGcUsers.length-1] = (names.indexOf(invitedUser)+1) +"";
					
					//update the user list of the group chat
					groupChat.setUsers(newGcUsers);
					
					//converts array to String form of 1,2,3 eg. ['person1','person2', 'person3'] -> "person1, person2, person3"
					String names = Arrays.toString(newGcUserNames);
					String gcUserNamesList = names.substring(1, names.length()-1);
					
					//sends the invite to the invited user
					for(int i=0; i<newGcUsers.length; i++) {
						int index = Integer.parseInt(newGcUsers[i]);
						System.out.println("index" + (index-1));
						writers.get(index-1).println("GROUPCHATINVITE " + groupChatId + ", " + invitedUser + ", "+ gcUserNamesList);
					}

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