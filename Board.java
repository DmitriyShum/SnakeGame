import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class Board extends JPanel implements ActionListener, KeyListener {

    // ----- Board configuration (change these to resize / re-speed the game) -----
    private final int rows = 24;        // number of rows
    private final int cols = 24;        // number of columns
    private final int cellSize = 24;    // pixels per cell  -> window is 480 x 480
    private final int delay = 128;      // milliseconds between steps (snake speed)

    // ----- Game state -----
    private Snake snake;
    private Point apple;
    private Direction direction;        // the direction the snake is moving RIGHT NOW
    private Direction nextDirection;    // the direction queued by the most recent key press
    private boolean gameOver;
    private boolean gameWon;
    private int score;

    private final Random random = new Random();
    private final Timer timer;

    // The three things a board cell can be. */
    enum Cell {
        EMPTY,
        FOOD,
        SNAKE_NODE
    }

    // The four directions the snake can travel. Only one is true at a time. Will be captured by KeyEvent.
    enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public Board() {
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        setBackground(Color.BLACK);
        setFocusable(true);             // a JPanel must be focusable to receive key events
        addKeyListener(this);

        timer = new Timer(delay, this); // the Swing Timer IS our game loop
        startGame();
    }

    // (Re)initialize everything and start the loop. Also used to restart after a loss/win. */
    private void startGame() {
        snake = new Snake(rows / 2, cols / 2);   // start in the middle
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        gameOver = false;
        gameWon = false;
        score = 0;
        spawnApple();
        timer.start();
    }

    /**
     // Place an apple on a random EMPTY cell, never on the snake's body.
     // We pick a random cell and, if it lands on the snake, pick again.
     */
    private void spawnApple() {
        // If the snake already fills the board there is no empty cell -> nothing to place.
        if (snake.length() >= rows * cols) {
            return;
        }
        Point candidate;
        do {
            int r = random.nextInt(rows);   // 0 .. rows-1
            int c = random.nextInt(cols);   // 0 .. cols-1
            candidate = new Point(r, c);
        } while (snake.occupies(candidate)); // keep trying until we miss the snake
        apple = candidate;
    }

    // -------- The game loop: this method runs once every 'delay' milliseconds --------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gameWon) {
            step();      // advance the game by one move
            repaint();   // redraw the screen
        }
    }

    /** Advance the game by a single step (one cell of movement). */
    private void step() {
        // 1) Lock in the queued direction for this step (done once per step on purpose).
        direction = nextDirection;

        // 2) Work out where the head wants to go.
        Point head = snake.head();
        int newRow = head.getRow();
        int newCol = head.getCol();

        if (direction == Direction.UP)    newRow = newRow - 1;
        if (direction == Direction.DOWN)  newRow = newRow + 1;
        if (direction == Direction.LEFT)  newCol = newCol - 1;
        if (direction == Direction.RIGHT) newCol = newCol + 1;

        Point newHead = new Point(newRow, newCol);

        // 3) EDGE CASE - hit a wall: the head left the grid bounds.
        if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
            gameOver = true;
            timer.stop();
            return;
        }

        // 4) Will the snake eat the apple on this step?
        boolean willEat = newHead.equals(apple);

        // 5) EDGE CASE - hit itself: the head moves onto a body segment.
        //    Subtlety: if the snake is NOT eating, the tail moves out of its cell
        //    on this same step, so moving INTO the old tail cell is allowed.
        //    (We use == here to ask "is this the SAME object as the tail?" - that is
        //     reference identity, which is the right comparison for that question.)
        Point currentTail = snake.tail();
        for (Point segment : snake.body()) {
            if (!willEat && segment == currentTail) {
                continue; // the tail will vacate this cell, so ignore it
            }
            if (segment.equals(newHead)) {
                gameOver = true;
                timer.stop();
                return;
            }
        }

        // 6) Move: add the new head to the FRONT of the LinkedList.
        snake.addHead(newHead);

        if (willEat) {
            // EDGE CASE - ate an apple: keep the tail (snake grows), score up, new apple.
            score = score + 1;
            spawnApple();
            // EDGE CASE - board full: the snake fills every cell -> the player wins.
            if (snake.length() >= rows * cols) {
                gameWon = true;
                timer.stop();
            }
        } else {
            // Normal move: remove the tail so the length stays the same.
            snake.removeTail();
        }
    }

    // Makes the window components visible by setting color,
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   // clears the panel to the black background

        // Build a small grid that mirrors the current snake + apple positions.
        // The LinkedList is the "source of truth"; this grid is only for drawing,
        // and it shows how the Cell enum maps the snake onto rows and columns.
        Cell[][] grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = Cell.EMPTY;
            }
        }
        if (apple != null) {
            grid[apple.getRow()][apple.getCol()] = Cell.FOOD;
        }
        for (Point segment : snake.body()) {
            grid[segment.getRow()][segment.getCol()] = Cell.SNAKE_NODE;
        }

        // Paint each cell according to its type.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == Cell.FOOD) {
                    g.setColor(Color.RED);
                    g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                } else if (grid[r][c] == Cell.SNAKE_NODE) {
                    g.setColor(new Color(0, 170, 0));
                    g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                }
                // EMPTY cells are left as the black background.
            }
        }

        // Draw the head a brighter green so you can see which way you are going.
        Point head = snake.head();
        g.setColor(new Color(0, 230, 0));
        g.fillRect(head.getCol() * cellSize, head.getRow() * cellSize, cellSize, cellSize);

        // Score text in the corner.
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Score: " + score, 8, 18);

        // End-of-game messages.
        if (gameOver || gameWon) {
            String message = gameWon ? "You Win!" : "Game Over";
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.drawString(message, (cols * cellSize) / 2 - 80, (rows * cellSize) / 2);
            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g.drawString("Press ENTER to play again or ESC to exit",
                    (cols * cellSize) / 2 - 110, (rows * cellSize) / 2 + 30);
        }
    }

    // -------- Keyboard input (KeyListener) --------
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Restart after the game ends.
        if ((gameOver || gameWon) && key == KeyEvent.VK_ENTER) {
            startGame();
            return;
        }
        if((gameOver || gameWon) && key == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }

        // Steer the snake (arrow keys OR W/A/S/D).
        // EDGE CASE - cannot reverse directly: a key opposite to the CURRENT direction
        // is ignored, so the snake can't turn back into its own neck. We check against
        // 'direction' (the committed direction) and only apply it once per step in step(),
        // which also stops a fast double-tap from causing a reversal.
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
        }
    }

    // KeyListener requires these two, but we don't need them here.
    @Override
    public void keyReleased(KeyEvent e) { }
    @Override
    public void keyTyped(KeyEvent e) { }
}