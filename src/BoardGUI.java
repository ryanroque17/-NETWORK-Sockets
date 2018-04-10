import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

public class BoardGUI extends JPanel
{
    ArrayList<JButton> buttons = new ArrayList<JButton>();
    int alternate = 0;//if this number is a even, then put a X. If it's odd, then put an O
    
    public BoardGUI()
    {
      setLayout(new GridLayout(3,3));
      initializebuttons(); 
    }
    
    public void initializebuttons()
    {
        for(int i = 0; i <= 8; i++)
        {
            JButton newButton = new JButton();
            newButton.setText("");
            newButton.addActionListener(new buttonListener());
            
            buttons.add(newButton);
            add(newButton);         
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
            
            JButton buttonClicked = (JButton)e.getSource(); //get the particular button that was clicked
            
            System.out.println(buttons.indexOf(buttonClicked));
            
            if(alternate%2 == 0)
                buttonClicked.setText("X");
            else
                buttonClicked.setText("O");
            
            if(checkForWin() == true)
            {
                JOptionPane.showConfirmDialog(null, "Player " + buttonClicked.getText() + " Wins!");
                resetButtons();
            }
                
            alternate++;
            
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
    
    public static void main(String[] args) 
    {
        JFrame window = new JFrame("Tic-Tac-Toe");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().add(new BoardGUI());
        window.setBounds(600,400,600,600);
        window.setVisible(true);
    }
}
