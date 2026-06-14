import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.color.ColorSpace;
import java.util.LinkedList;


public class SnakeGame {
    private int size = 0; // Size to be incremented by one everytime the worm comes in contact with an apple
    private final int rows = 60;
    private final int cols = 60;
    private boolean gameRunning;
    
    public enum Cell {
        EMPTY,
        FOOD,
        SNAKE_NODE
    }
    
    public enum Directions{ // Type enum for values that can change. Since all directions can't be true at once, enum says only one of each can be true.
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public SnakeGame() {
        gameLoop();
    }

    public void board(){
        

    }
    public void gameLoop(){
       // Empty for now
        while(gameRunning == true){
            board();
        }
        
    }

    public boolean gameWon(){
        return false;
    }
    public boolean gameOver(){
        return false;
    }
    public static void main(String[] args) {
        gameLoop();
    }
}

