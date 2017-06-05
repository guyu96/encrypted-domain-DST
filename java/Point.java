public class Point {
    public final long x, y; // x, y that make up the share
    public final long prime; // operations are done on finite field Zp

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public Point(long x, long y, long prime) {
        this.x = x;
        this.y = y;
        this.prime = prime;
    }

    public Point multiply(Point p) {
        assert x == p.x && prime == p.prime;
        return new Point(x, (y * p.y), prime);
    }

    public Point multiply(long num) {
        return new Point(x, (y * num), prime);
    }

    public Point add(Point p) {
        assert x == p.x && prime == p.prime;
        return new Point(x, (y + p.y), prime);
    }

    public Point add(long num) {
        return new Point(x, (y + num), prime);
    }
}