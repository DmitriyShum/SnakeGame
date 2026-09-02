package snakegame;

// No global imports, only the necessary imports.
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener, KeyListener {

    // Board configurations
    private final int rows;
    private final int cols;
    private final int cellSize;

    // Graphics configurations
    private static final Color SNAKE_BODY_COLOR = new Color(0, 170, 0);
    private static final Color SNAKE_HEAD_COLOR = new Color(0, 230, 0);
    private static final Font SCORE_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font SUBTEXT_FONT = new Font("SansSerif", Font.PLAIN, 16);
    
    // Game state
    private Snake snake;
    private Point apple;
    private Direction direction;        // the direction the snake is moving RIGHT NOW
    private Direction nextDirection;    // the direction queued by the most recent key press
    private boolean gameOver;
    private boolean gamePaused;
    private boolean gameWon;
    private int score;
    private int prev;
    private final Random random = new Random();
    private final Timer timer;

    // The four directions the snake can travel. Only one is true at a time. Will be captured by KeyEvent.
    enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    // Default board: 24x24, adaptive cell size, 124ms per step.
    public Board() {
        this(24, 24, 124);
    }

    // Sizing is adaptive, and the window size will resize on smaller screens, but will use the classic size on big screens.
    public Board(int rows, int cols, int delay) {
        this.rows = rows;
        this.cols = cols;

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Use at most ~60% of the smaller screen dimension for the board.
        int maxBoardPixels = (int) (Math.min(screen.width, screen.height) * 0.6);
        int adaptive = maxBoardPixels / Math.max(rows, cols);

        // cell size should not change to be bigger than 24px and not smaller than 12px.
        this.cellSize = Math.max(adaptive, 12);

        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(this);

        // Game loop
        timer = new Timer(delay, this);
        startGame();
    }

    // (Re)initializing the game's variables everytime the game starts or restarts.
    private void startGame() {
        snake = new Snake(rows / 2, cols / 2);   // start in the middle
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        gameOver = false;
        gamePaused = false;
        gameWon = false;
        score = 0;
        spawnApple();
        timer.start();
    }

    //Placing the apple on an random empty cell
    private void spawnApple() {

        // If the snake already fills the board there is no empty cell, there's nothing to place.
        if (snake.length() >= rows * cols) {
            return;
        }
        Point candidate;
        do {
            // rows and columns are n-1
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            candidate = new Point(r, c);
        } while (snake.occupies(candidate));
        apple = candidate;
    }

    //  The game loop: this method runs once every 'delay' milliseconds
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gameWon && !gamePaused) {
            step();      // advance the game by one move
            repaint();   // redraw the screen
            
            //Linux solution specifically on X11, fixing the lag. 
            Toolkit.getDefaultToolkit().sync();
        }
    }

    
    private void step() {
        direction = nextDirection;

        // Changing the direction of snake's head
        Point head = snake.head();
        int newRow = head.getRow();
        int newCol = head.getCol();

        //switch-case statements faster and cleaner than if-else
        switch(direction){
            case UP:
                newRow = newRow-1;
                break;
            case DOWN:
                newRow = newRow + 1;
                break;
            case LEFT:
                newCol = newCol - 1;
                break;
            case RIGHT:
                newCol = newCol +1;
                break;
        }

        Point newHead = new Point(newRow, newCol);

        //  If snake hits a wall, or attempts to go out of bounds, the timer stops, and it's game over.
        if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
            gameOver = true;
            timer.stop();
            return;
        }

        // The snake eats an apple on a step
        boolean willEat = newHead.equals(apple);

        // In the case if snake attempts to eat itself
        if (snake.occupies(newHead)) {
            boolean movingIntoVacatingTail = !willEat && newHead.equals(snake.tail());
            if (!movingIntoVacatingTail) {
                gameOver = true;
                timer.stop();
                return;
            }
        }

         // adds a new head to the front of the LinkedList.
        if (willEat) {
            snake.addHead(newHead);
            // If snake eats an apple, the score is incremented, a new node to the tail is added
            score++;
            spawnApple();
            // If the snake fills up the 24 rows x 24 columns, the game is won
            if (snake.length() >= rows * cols) {
                gameWon = true;
                timer.stop();
            }
        } else {
            // when snake moves, the tail has to be removed from the end, and new head is added to the linkedlist.
            snake.removeTail();
            snake.addHead(newHead);
        }
    }

    // Makes the window components visible by setting color.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   // clears the panel to the black background

        if (apple != null) {
            g.setColor(Color.RED);
            g.fillRect(apple.getCol() * cellSize, apple.getRow() * cellSize, cellSize, cellSize);
        }

        g.setColor(SNAKE_BODY_COLOR);
        for (Point segment : snake.body()) {
            g.fillRect(segment.getCol() * cellSize, segment.getRow() * cellSize, cellSize, cellSize);
        }

        // Draw the head a brighter green so you can see which way you are going.
        Point head = snake.head();
        g.setColor(SNAKE_HEAD_COLOR);
        g.fillRect(head.getCol() * cellSize, head.getRow() * cellSize, cellSize, cellSize);

        //Calling gameText to show all messages in JFrame
        gameText(g);
    }

    // All game texts : the score, game won, game over, and game paused.
    public void gameText(Graphics g){
        // Score text in the corner.
        g.setColor(Color.WHITE);
        g.setFont(SCORE_FONT);
        g.drawString("Score: " + score, 8, 18);
        g.drawString("Last Score: " + prev, 82, 18);
        // End-of-game messages.
        if (gameOver || gameWon) {
            drawCenteredScreen(g, gameWon ? "You Win!" : "Game Over", "Press ENTER to play again or ESC to exit");
        }

        // KeyEvent.VK_SHIFT will stop timer, and set gamePaused to true, printing the text.
        if (gamePaused) {
            drawCenteredScreen(g, "Game Paused", "Press SHIFT to resume or ESC to exit"); // Calling the method below, where the font color is set, along with centered sizes.
        }
    }
    // Text configurator, sets the text color white, and size
    private void drawCenteredScreen(Graphics g, String title, String subtitle) {
        g.setColor(Color.WHITE);

        g.setFont(TITLE_FONT);
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (cols * cellSize - titleWidth) / 2, (rows * cellSize) / 2);

        g.setFont(SUBTEXT_FONT);
        int subWidth = g.getFontMetrics().stringWidth(subtitle);
        g.drawString(subtitle, (cols * cellSize - subWidth) / 2, (rows * cellSize) / 2 + 30);

    }

    //  Keyboard input (KeyListener)
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Restart after the game ends.
        if ((gameOver || gameWon) && key == KeyEvent.VK_ENTER) {
            int temp = prev;
            prev = score;
            score = temp;
            startGame();

            //save the previous score on the next game... After ENTER key is pressed.
            //Send this state to the text
            return;
        }

        // Steer the snake (arrow keys OR W/A/S/D).
        switch(key) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (direction != Direction.DOWN) nextDirection = Direction.UP;
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (direction != Direction.UP) nextDirection = Direction.DOWN;
                break;

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (direction != Direction.RIGHT) nextDirection = Direction.LEFT;
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (direction != Direction.LEFT) nextDirection = Direction.RIGHT;
                break;

            case KeyEvent.VK_ESCAPE: // Exit the game by pressing ESC at any time.
                System.exit(0);
                break;

            case KeyEvent.VK_SHIFT: // Pause / resume.

                if (gameOver || gameWon) {
                    break;
                }
            
                if (timer.isRunning()) {
                    timer.stop();
                    gamePaused = true;
                } else {
                    gamePaused = false;
                    timer.start();
                }
                repaint(); // Repainting the screen so that the action updates.
                break;
        }
    }

    // KeyListener requires these two, but we don't need them here.
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}
