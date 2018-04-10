import java.util.UUID;

public class Game {
	private String gameId;
	private String[] players;
	/**
	 * Create the application.
	 */
	public Game(String[] players) {
		this.gameId = UUID.randomUUID().toString();
		this.players = players;
	}
	
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public String[] getPlayers() {
		return players;
	}
	public void setPlayers(String[] players) {
		this.players = players;
	}
	
}
