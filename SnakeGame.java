import javax.swing.JFrame;

public class SnakeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SnakeGame");
        Board board = new Board();

        frame.add(board);
        frame.pack();                        // size the window to the board's preferred size
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);   // center on screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        board.requestFocusInWindow();        // make sure the board receives key presses
    }
}