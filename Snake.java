import java.util.LinkedList;

public class Snake {
    private LinkedList<Point> body;

    public Snake(int startRow, int startCol) {
        body = new LinkedList<>();
        // Start with 3 segments lined up horizontally, head on the right.
        body.add(new Point(startRow, startCol));       // head
        body.add(new Point(startRow, startCol - 1));   // middle
        body.add(new Point(startRow, startCol - 2));   // tail
    }

    public Point head() {
        return body.getFirst();
    }

    public Point tail() {
        return body.getLast();
    }

    public int length() {
        return body.size();
    }

    public LinkedList<Point> body() {
        return body;
    }

    // Add a new head node to the FRONT of the list (every step, and when growing). */
    public void addHead(Point newHead) {
        body.addFirst(newHead);
    }

    // Remove the tail node from the BACK of the list (on a normal move). */
    public void removeTail() {
        body.removeLast();
    }

    // Does the snake's body occupy this point? Used so apples never spawn on the snake. */
    public boolean occupies(Point p) {
        return body.contains(p);   // uses Point.equals under the hood
    }
}