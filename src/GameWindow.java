import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.*;

public class GameWindow 
{
	private String gameId;

	private String player;
	private String enemy;
	private String character;

	private BufferedReader in;
	private PrintWriter out;

	private Boolean turn = false;
	private Boolean isQuit = true;
	
	private JFrame window;
	private JPanel panel = new JPanel();
	
	
	ArrayList<JButton> buttons = new ArrayList<JButton>();
	int alternate = 0;//if this number is a even, then put a X. If it's odd, then put an O

	public GameWindow(String id, String player, String enemy, String character, BufferedReader in, PrintWriter out)
	{	
		this.gameId = id;
		this.enemy = enemy;
		this.player = player;
		this.character = character;
		this.in = in;
		this.out = out;

		panel.setLayout(new GridLayout(3,3));
		
		initializebuttons(); 
		window = new JFrame("Tic-Tac-Toe id: " + gameId);
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.getContentPane().add(panel);
		window.setBounds(600,400,600,600);
		window.setVisible(true);
		
		window.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				if(isQuit)
					out.println("QUITGAME " + gameId + ", " + enemy);

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}	
			});
		}

	public void initializebuttons()
	{
		for(int i = 0; i <= 8; i++)
		{
			JButton newButton = new JButton();
			newButton.setText("");
			newButton.addActionListener(new buttonListener());

			buttons.add(newButton);
			panel.add(newButton);         
		}
	}
	public void resetButtons()
	{
		for(int i = 0; i <= 8; i++)
		{
			buttons.get(i).setText("");
		}
	}


	private class buttonListener implements ActionListener
	{

		public void actionPerformed(ActionEvent e) 
		{
			if(turn) {
				JButton buttonClicked = (JButton)e.getSource(); //get the particular button that was clicked

				int buttonIndex = buttons.indexOf(buttonClicked);

				out.println("PLAYERMOVE " + gameId + ", " + enemy + ", " +  buttonIndex);
				buttonClicked.setText(character);

				if(checkForWin() == true)
				{
					out.println("GAMERESULT " + gameId + ", " + enemy);

					JOptionPane.showMessageDialog(window,
							"You won!! Click ok to close the game");
					window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
					isQuit = false;
				}
				setTurn(false);
			}else
				JOptionPane.showMessageDialog(window,
						"Wait for your turn!");

		}

		public boolean checkForWin()
		{
			//horizontal win check
			if( checkAdjacent(0,1) && checkAdjacent(1,2) ) 
				return true;
			else if( checkAdjacent(3,4) && checkAdjacent(4,5) )
				return true;
			else if ( checkAdjacent(6,7) && checkAdjacent(7,8))
				return true;

			//vertical win check
			else if ( checkAdjacent(0,3) && checkAdjacent(3,6))
				return true;  
			else if ( checkAdjacent(1,4) && checkAdjacent(4,7))
				return true;
			else if ( checkAdjacent(2,5) && checkAdjacent(5,8))
				return true;

			//diagonal win check
			else if ( checkAdjacent(0,4) && checkAdjacent(4,8))
				return true;  
			else if ( checkAdjacent(2,4) && checkAdjacent(4,6))
				return true;
			else 
				return false;


		}

		public boolean checkAdjacent(int a, int b)
		{
			if ( buttons.get(a).getText().equals(buttons.get(b).getText()) && !buttons.get(a).getText().equals("") )
				return true;
			else
				return false;
		}

		

	}


	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public String getEnemy() {
		return enemy;
	}

	public void setEnemy(String enemy) {
		this.enemy = enemy;
	}

	public String getCharacter() {
		return character;
	}

	public void setCharacter(String character) {
		this.character = character;
	}

	public Boolean getTurn() {
		return turn;
	}

	public void setTurn(Boolean turn) {
		this.turn = turn;
	}

	public ArrayList<JButton> getButtons() {
		return buttons;
	}

	public void setButtons(ArrayList<JButton> buttons) {
		this.buttons = buttons;
	}

	public JFrame getWindow() {
		return window;
	}

	public void setWindow(JFrame window) {
		this.window = window;
	}
	
	public void showDefeat() {
		JOptionPane.showMessageDialog(window,
				"You lost!! Click ok to close the game");	
		window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		isQuit = false;
	}
	
	public void enemyQuitted() {
		JOptionPane.showMessageDialog(window,
				enemy + " has left the game. You won! Click ok to close the game");	
		window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		isQuit = false;
	}

}
