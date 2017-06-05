import java.util.Random;

public class Shamir {
    public final int n; // number of distributed shares
    public final int k; // threshold number of shares
    public final long p; // prime used in encryption
    private long[] coefficients; // coefficients for polynomial (excluding m)

    public Shamir(int n, int k) throws IllegalArgumentException {
        // checking bounds
        if (k < 2 || n <= 2*(k-1)) {
            throw new IllegalArgumentException("Ensure that k >= 2 and n > 2(k-1)");
        }
        this.n = n;
        this.k = k;

        // generate p (calculations are done in finite field Zp)
        // p needs to be large in order to enable homomorphic multiplication of two int values
        // p randomly chosen from the next 100 primes greater than max integer value
//        p = 9223372036854775799L;
        p = 360000000007L;

        // generate random polynomial coefficients
        Random r = new Random();
        coefficients = new long[k-1];
        for (int i = 0; i < k-1; i++) {
            coefficients[i] = r.nextInt(100) + 1;
        }

    }

    public Point[] genShares(long m) {
        // Horner's method
        Point[] shares = new Point[n];
        long x, y;
        for (int i = 0; i < n; i++) {
            x = i + 1;
            y = 0;
            for (int j = k-2; j >= 0; j--) {
                y = y * x + coefficients[j];
            }
            y = y * x + m;
            shares[i] = new Point(x, y, p);
        }
        return shares;
    }

    public void displayParam() {
        System.out.println("n = " + n);
        System.out.println("k = " + k);
        System.out.println("p = " + p);
        for (long c : coefficients) {
            System.out.print(c + " ");
        }
        System.out.println();
    }

    public static long decrypt(Point[] s, int shareNum) throws IllegalArgumentException {
        if (s.length < shareNum)
            throw new IllegalArgumentException("Too few shares");

        double m = 0;
        for (int i = 0; i < shareNum; i++) {
            double temp = 1.0;
            for (int j = 0; j < shareNum; j++) {
                if (i == j) continue;
                temp *= ( (double) (s[j].x) / (s[j].x - s[i].x) );
            }
            m += (s[i].y * temp);
        }

        while (m < 0)
            m += s[0].prime;
        return (((long) (m + 0.5)) % s[0].prime);
    }

    // adding a constant c
    public static Point[] add(long c, Point[] shares) {
        Point[] sum = new Point[shares.length];
        for (int i = 0; i < shares.length; i++) {
            sum[i] = shares[i].add(c);
        }
        return sum;
    }

    // adding two different shares
    public static Point[] add(Point[] shares1, Point[] shares2) {
        assert shares1.length == shares2.length;
        Point[] sum = new Point[shares1.length];
        for (int i = 0; i < shares1.length; i++) {
            sum[i] = shares1[i].add(shares2[i]);
        }
        return sum;
    }

    // multiplying by a constant c
    public static Point[] multiply(long c, Point[] shares) {
        Point[] product = new Point[shares.length];
        for (int i = 0; i < shares.length; i++) {
            product[i] = shares[i].multiply(c);
        }
        return product;
    }

    // multiplying two different shares
    public static Point[] multiply(Point[] shares1, Point[] shares2) {
        assert shares1.length == shares2.length;
        Point[] product = new Point[shares1.length];
        for (int i = 0; i < shares1.length; i++) {
            product[i] = shares1[i].multiply(shares2[i]);
        }
        return product;
    }

    public static void main(String[] args) {
        // 5 shares, threshold = 3, message = 15 and 10
        int n = 5;
        int k = 3;
        long m1 = 123123, m2 = 321321;

        // emulate share distribution
        Shamir s = new Shamir(n, k);
        s.displayParam();
        Point[] shares1 = s.genShares(m1);
        Point[] shares2 = s.genShares(m2);

        // test decryption
        long dec1 = decrypt(shares1, k);
        System.out.println("m1 = " + dec1);
        long dec2 = decrypt(shares2, k);
        System.out.println("m2 = " + dec2);

        // test homomorphic multiplication of two ciphertexts
        Point[] shares = multiply(shares1, shares2);
        long dec = decrypt(shares, 2*k - 1);
        System.out.println("m1 * m2 = " + dec);

        // test homomorphic multiplication of a constant
        long c = -3;
        shares = multiply(c, shares1);
        shares = multiply(c, shares);
        dec = decrypt(shares, k);
        System.out.println("m1 * (" + c + ")^2 = " + dec);

        // test homomorphic addition of two ciphertexts
        shares = add(shares1, shares2);
        dec = decrypt(shares, k);
        System.out.println("m1 + m2 = " + dec);

        // test homomorphic addition of a constant
        shares = add(c, shares1);
        dec = decrypt(shares, k);
        System.out.println("m1 + " + c + " = " + dec);
    }
}
