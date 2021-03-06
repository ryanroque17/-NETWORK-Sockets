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

public class SocketHandler extends Thread {
	private String name; 	
	private Socket socket;  
	private BufferedReader in;
	private PrintWriter out;

	private static ArrayList<String> names = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static ArrayList<BufferedReader> readers = new ArrayList<BufferedReader>();
	private static ArrayList<Socket> sockets = new ArrayList<Socket>();
	private static ArrayList<GroupChat> groupChats = new ArrayList<GroupChat>();
	private static ArrayList<Chatroom> chatrooms = new ArrayList<Chatroom>();
	private static ArrayList<Game> games = new ArrayList<Game>();
	private static ArrayList<String> chatroomNames = new ArrayList<String>();

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
						sockets.add(socket);
						for (PrintWriter writer : writers) {
							writer.println("ONLINELIST " + names.toString());
							writer.println("");
						}
						if(!chatroomNames.isEmpty()) {
							for (PrintWriter writer : writers) {
								writer.println("CHATROOMLIST " + chatroomNames.toString());
								writer.println("");
							}
						}
						break;
					}
				}
			}

			out.println("NAMEACCEPTED");

			while (true) {
				out.println("");
				String input = in.readLine();
				if (input == null) {
					return;
				}
				if(input.contains("INITIATEPM")) {
					String[] pmUsers = input.substring(11).split("\\,");
					// recepientName = names.get(Integer.parseInt(pmUsers[1])-1));
					writers.get(names.indexOf(pmUsers[0])).println("INITIATEPM " + names.get(Integer.parseInt(pmUsers[1])-1));
				}else if(input.contains("PRIVATEMESSAGE")){
					//System.out.println("input pm" + input);
					String senderName = input.substring(15).split("\\,")[0];
					String recepientName = input.substring(15).split("\\,")[1];
					String message = input.substring(15).split("\\,")[2];

					//System.out.println("senderName" + senderName + "|index: " + names.indexOf(senderName));
					//System.out.println("recepientName" + recepientName+ "|index: " + names.indexOf(recepientName));

					writers.get(names.indexOf(senderName)).println("");
					writers.get(names.indexOf(recepientName)).println("");
					writers.get(names.indexOf(senderName)).println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);
					writers.get(names.indexOf(recepientName)).println("PRIVATEMESSAGE " + senderName + "," + recepientName + "," + message);

				}else if (input.contains("PRIVATEATTACHMENT")) {
					String sender = input.substring(18).split("\\,")[0];
					String recepient = input.substring(18).split("\\,")[1];
					String filePath = input.substring(18).split("\\,")[2];
					String fileName = input.substring(18).split("\\,")[3];
					String fileSize = input.substring(18).split("\\,")[4];

					saveFile(socket, fileName, Integer.parseInt(fileSize));

					int index = names.indexOf(recepient);
					writers.get(index).println("PRIVATEATTACHMENT " + sender + "," + recepient + ","+ fileName + "," + fileSize);
					sendFile("server/"+fileName, sockets.get(index));

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
						//System.out.println(index);
						gcUserNames[i] = names.get(index-1);
					}
					String names = Arrays.toString(gcUserNames);
					String gcUserNamesList = names.substring(1, names.length()-1);

					//send the groupchatId and the list of the users to the clients of the GC
					for(int i=0; i<gcUsers.length; i++) {
						int index = Integer.parseInt(gcUsers[i]);
						//System.out.println(index);
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
						writers.get(index-1).println("");
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
						//System.out.println(index);
						newGcUsers[i] = groupChatUsers[i];
						newGcUserNames[i] = names.get(index-1);
					}
					newGcUserNames[newGcUserNames.length-1] = invitedUser;
//					System.out.println("index of invited " + names.indexOf(invitedUser));
//					System.out.println("invited user" +invitedUser);

					newGcUsers[newGcUsers.length-1] = (names.indexOf(invitedUser)+1) +"";

					//update the user list of the group chat
					groupChat.setUsers(newGcUsers);

					//converts array to String form of 1,2,3 eg. ['person1','person2', 'person3'] -> "person1, person2, person3"
					String names = Arrays.toString(newGcUserNames);
					String gcUserNamesList = names.substring(1, names.length()-1);

					//sends the invite to the invited user
					for(int i=0; i<newGcUsers.length; i++) {
						int index = Integer.parseInt(newGcUsers[i]);
//						System.out.println("index" + (index-1));
						writers.get(index-1).println("GROUPCHATINVITE " + groupChatId + ", " + invitedUser + ", "+ gcUserNamesList);
					}

				}else if (input.contains("GROUPATTACHMENT")) {
					String groupChatId = input.substring(16).split("\\, ")[0];
					String sender = input.substring(16).split("\\, ")[1];
					String filePath = input.substring(16).split("\\, ")[2];
					String fileName = input.substring(16).split("\\, ")[3];
					String fileSize = input.substring(16).split("\\, ")[4];

					saveFile(socket, fileName, Integer.parseInt(fileSize));

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

						writers.get(index-1).println("GROUPATTACHMENT " + groupChatId + ", "+ sender + ", " + fileName + ", " + fileSize);
						sendFile("server/"+fileName, sockets.get(index-1));
					}

				}else if (input.contains("GLOBALATTACHMENT")) {
					String sender = input.substring(17).split("\\, ")[0];
					String filePath = input.substring(17).split("\\, ")[1];
					String fileName = input.substring(17).split("\\, ")[2];
					String fileSize = input.substring(17).split("\\, ")[3];

					saveFile(socket, fileName, Integer.parseInt(fileSize));

//					System.out.println("sender : " + sender);
//					System.out.println("sender index: " + names.indexOf(sender));

					for(int i=0; i<writers.size();i++) {
						if(names.indexOf(sender) != i) {
							writers.get(i).println("GLOBALATTACHMENT " + sender + ", " + fileName + ", " + fileSize);
							sendFile("server/"+fileName, sockets.get(i));
						}
					}


				}else if(input.contains("GLOBALMESSAGE")){
					//System.out.println("input " + input);
					String message = input.substring(14).split("\\, ")[0];
					//System.out.println(name + " glob " + message);
					for (PrintWriter writer : writers) {
						writer.println("");

						writer.println("MESSAGE " + name + ": " + message);
					}
				}else if(input.contains("INVITEGAME")) {
					String sender = input.substring(11).split("\\, ")[0];
					String invitedPlayer = input.substring(11).split("\\, ")[1];
					
					String players[] = new String[2];
					players[0] = sender;
					players[1] = invitedPlayer;
					
					Game newGame = new Game(players);
					games.add(newGame);
					
					writers.get(names.indexOf(sender)).println("INVITEGAME " + newGame.getGameId() + ", " +  sender + ", " + invitedPlayer);
					writers.get(names.indexOf(invitedPlayer)).println("INVITEGAME " + newGame.getGameId() + ", " +  sender + ", " + invitedPlayer);
				}else if(input.contains("PLAYERMOVE")) {
					String gameId = input.substring(11).split("\\, ")[0];
					String nextPlayer = input.substring(11).split("\\, ")[1];
					String buttonIndex = input.substring(11).split("\\, ")[2];
					
					writers.get(names.indexOf(nextPlayer)).println("PLAYERMOVE " + gameId + ", " + buttonIndex);
				}else if(input.contains("GAMERESULT")) {
					String gameId = input.substring(11).split("\\, ")[0];
					String player = input.substring(11).split("\\, ")[1];
					
					writers.get(names.indexOf(player)).println("GAMERESULT " + gameId);
				}else if(input.contains("QUITGAME")) {
					String gameId = input.substring(9).split("\\, ")[0];
					String player = input.substring(9).split("\\, ")[1];
					
					writers.get(names.indexOf(player)).println("QUITGAME " + gameId);
				}else if(input.contains("CREATECHATROOM")) {
					String chatroomName = input.substring(15).split("\\, ")[0];
					
					Chatroom newChatroom = new Chatroom(chatroomName);
					chatrooms.add(newChatroom);
					chatroomNames.add(chatroomName);
					for (PrintWriter writer : writers) {
						writer.println("");

						writer.println("CREATECHATROOM " + chatroomName);
					}
				}else if(input.contains("JOINCHATROOM")) {
					String name = input.substring(13).split("\\, ")[0];
					String chatroomName = input.substring(13).split("\\, ")[1];
					
					//gets the specific chatroom
					Chatroom currChatroom = null;
					for(int i=0; i<chatrooms.size();i++) {
						if(chatroomName.equals(chatrooms.get(i).getChatroomId())) {
							currChatroom = chatrooms.get(i);
						}
					}
					currChatroom.addUser(name);
					String currUsers = currChatroom.getUsers().toString();
					currUsers = currUsers.substring(1, currUsers.length()-1);
					
					for(int i=0; i<currChatroom.getUsers().size();i++)
						writers.get(names.indexOf(currChatroom.getUsers().get(i))).println("JOINCHATROOM " + chatroomName  + ", " + name + ", " + currUsers);
					
				}else if(input.contains("CHATROOMMESSAGE")) {
					String chatroomName = input.substring(16).split("\\, ")[0];
					String message = input.substring(16).split("\\, ")[1];

					Chatroom currChatroom = null;
					for(int i=0; i<chatrooms.size();i++) {
						if(chatroomName.equals(chatrooms.get(i).getChatroomId())) {
							currChatroom = chatrooms.get(i);
						}
					}
					for(int i=0; i<currChatroom.getUsers().size();i++)
						writers.get(names.indexOf(currChatroom.getUsers().get(i))).println("CHATROOMMESSAGE " + chatroomName + ", " +  message);
					
				}else if (input.contains("CHATROOMATTACHMENT")) {
					String chatroomId = input.substring(19).split("\\, ")[0];
					String sender = input.substring(19).split("\\, ")[1];
					String filePath = input.substring(19).split("\\, ")[2];
					String fileName = input.substring(19).split("\\, ")[3];
					String fileSize = input.substring(19).split("\\, ")[4];

					saveFile(socket, fileName, Integer.parseInt(fileSize));

					Chatroom chatroom = null;

					//get the specific groupchat
					for(int i=0; i<chatrooms.size();i++) {
						if(chatroomId.equals(chatrooms.get(i).getChatroomId())) {
							chatroom = chatrooms.get(i);
						}
					}

					//get the clients of the groupchat
					ArrayList<String> groupChatUsers = chatroom.getUsers();
					for(int i=0; i<chatroom.getUsers().size();i++) {
						writers.get(names.indexOf(chatroom.getUsers().get(i))).println("CHATROOMATTACHMENT " + chatroomId + ", "+ sender + ", " + fileName + ", " + fileSize);
						sendFile("server/"+fileName, sockets.get(names.indexOf(chatroom.getUsers().get(i))));
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
				//System.out.println("socketClosed");
				socket.close();
			} catch (IOException e) {
				System.out.println("ERROR " + e);
			}
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
		fis.close();
		//System.out.println("file sent from server " + System.currentTimeMillis() % 1000);
		//		try {
		//			//this.sleep(5000);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		dos.close();
		//		fis.close();
	}
	private void saveFile(Socket clientSock, String filename, int filesize) throws IOException {
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		File serverFolder = new File("server");
		serverFolder.mkdirs();
		FileOutputStream fos = new FileOutputStream("server/" +filename);
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

		//		fos.close();
		//		dis.close();
	}
}