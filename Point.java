public class Point {
    private final int row;
    private final int col;

    public Point(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Point p = (Point) other;
        return this.row == p.row && this.col == p.col;
    }

    @Override
    public int hashCode() {
        // When equals() is overridden, hashCode() should be too. Standard pattern.
        return 31 * row + col;
    }
}