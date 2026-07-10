package snakegame;

import java.util.HashSet;
import java.util.LinkedList;

public class Snake {
    private final LinkedList<Point> body;

    // Mirror of the body for O(1) "is this cell occupied?" checks.
    // LinkedList.contains() is O(n); on slower machines this matters when the
    // snake gets long (apple spawning + self-collision both ask this question).
    // Point already overrides equals() and hashCode(), so it works in a HashSet.
    private final HashSet<Point> occupied;

    public Snake(int startRow, int startCol) {
        body = new LinkedList<>();
        occupied = new HashSet<>();
        // Start with 3 segments lined up horizontally, head on the right.
        addHead(new Point(startRow, startCol - 2));   // tail
        addHead(new Point(startRow, startCol - 1));   // middle
        addHead(new Point(startRow, startCol));       // head
    }

    public Point head() {
        return body.getFirst();
    }

    // Returns a linkedlist's last node
    public Point tail() {
        return body.getLast();
    }
    // returns total size of linkedlist
    public int length() {
        return body.size();
    }

    public LinkedList<Point> body() {
        return body;
    }

    // Adding a new node to the front of the list.
    public void addHead(Point newHead) {
        body.addFirst(newHead);
        occupied.add(newHead);
    }

    // Remove the tail node from the back of the list for the snake to move forward
    public void removeTail() {
        Point removed = body.removeLast();
        
        // Clears the set entry only.
        if (!body.contains(removed)) {
            occupied.remove(removed);
        }
    }

    // Does the snake's body occupy this point? Used so apples never spawn on the snake. */
    public boolean occupies(Point p) {
        return occupied.contains(p);
    }
}