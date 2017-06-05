import java.math.BigInteger;
import java.util.HashMap;

public class PaillierMain {
    public static void main(String[] args) {
        String[] snpPos = {"rs2793831", "rs7578597", "rs1801282", "rs6802898", "rs2197423",
                "rs4402960", "rs10012946", "rs1042714", "rs4712523", "rs7756992", "rs1635852",
                "rs13266634", "rs2383208", "rs10811661", "rs7903146", "rs7901695", "rs1111875",
                "rs5015480", "rs5219", "rs5215", "rs1353362"};
        int[] snpStates = {0, 1, 0, 0, 0, 2, 0, 1, 0, 0, 1, 1, 2, 2, 0, 0, 1, 0, 0, 0, 0};

        String[] diseaseSNPPos = {"rs2793831", "rs7578597", "rs1801282", "rs6802898", "rs2197423",
                "rs4402960", "rs10012946", "rs1042714", "rs4712523", "rs7756992", "rs1635852",
                "rs13266634", "rs2383208", "rs10811661", "rs7903146", "rs7901695", "rs1111875",
                "rs5015480", "rs5219", "rs5215", "rs1353362"};
        double[] diseaseSNPCtrb = {0.9551, 0.0878, 0.9385, 0.3744, 0.68, 0.3141,
                0.5039, 0.6091, 0.6923, 0.7142, 0.7475, 0.4153, 0.1646, 0.9598,
                0.0326, 0.0074, 0.3962, 0.4153, 0.5428, 0.9701, 0.7027};
        double[][] diseaseSNPProb = {{0.0261, 0.4639, 0.8246, 0.9669, 0.3473, 0.1016, 0.3894, 0.1688, 0.0669, 0.3413, 0.1649, 0.0886, 0.9635, 0.7034, 0.0845, 0.0227, 0.0076, 0.4693, 0.4004, 0.2759, 0.0656},
                {0.0513, 0.1641, 0.8002, 0.5433, 0.232, 0.4611, 0.5455, 0.7572, 0.394, 0.2713, 0.3241, 0.611, 0.0061, 0.846, 0.5103, 0.1469, 0.1144, 0.9942, 0.4198, 0.3127, 0.8801},
                {0.3864, 0.5022, 0.5727, 0.6916, 0.8652, 0.1959, 0.3914, 0.1384, 0.5686, 0.6362, 0.5458, 0.2798, 0.6934, 0.8652, 0.1288, 0.0488, 0.1986, 0.8054, 0.205, 0.7006, 0.328}};

        // CI encrypts the SNP states
//        Paillier p = new Paillier(2048);
        Paillier p = new Paillier();
//        p.displayKeys();
        HashMap<String, BigInteger[]> encrypted = new HashMap<>();
        HashMap<String, BigInteger[]> encryptedSquared = new HashMap<>();
        for (int i = 0; i < snpStates.length; i++) {
            encrypted.put(snpPos[i], p.encrypt(snpStates[i]));
            encryptedSquared.put(snpPos[i], p.encrypt(snpStates[i] * snpStates[i]));
        }

        // MC sends request to SPU
        // SPU re-encrypts
        BigInteger[][] enc = new BigInteger[diseaseSNPPos.length][2];
        BigInteger[][] encSquared = new BigInteger[diseaseSNPPos.length][2];
        for (int i = 0; i < diseaseSNPPos.length; i++) {
            enc[i] = p.reEncrypt(encrypted.get(diseaseSNPPos[i]));
            encSquared[i] = p.reEncrypt(encryptedSquared.get(diseaseSNPPos[i]));
        }

        // MC does pre-computation preparation
        int precision = 10000;
        double ctrbSum = 0;
        int[] ctrb = new int[diseaseSNPCtrb.length];
        int[] a = new int[ctrb.length];
        int[] b = new int[ctrb.length];
        int[] c = new int[ctrb.length];

        for (int i = 0; i < ctrb.length; i++) {
            ctrb[i] = (int) (diseaseSNPCtrb[i] * precision) * 2;
            a[i] = (int) (diseaseSNPProb[0][i] * precision);
            b[i] = ((int) (diseaseSNPProb[1][i] * precision)) * -2;
            c[i] = (int) (diseaseSNPProb[2][i] * precision);
            ctrbSum += diseaseSNPCtrb[i];
        }

        int times = 100;
        double s = 0;
        for (int j = 0; j < times; j++) {
            // MC computes susceptibility
            BigInteger[] x, y, z, tempSum;
            BigInteger[] sus = p.encrypt(0);
            long start = System.nanoTime();
            //        System.out.println("begin");
            for (int i = 0; i < ctrb.length; i++) {
                x = p.multiply(encSquared[i], a[i] + b[i] + c[i]);
                y = p.multiply(enc[i], -3 * a[i] - 2 * b[i] - c[i]);
                z = p.encrypt(2 * a[i]);
                tempSum = p.add(p.add(x, y), z);
                tempSum = p.multiply(tempSum, ctrb[i]);
                sus = p.add(tempSum, sus);
            }

            // SPU partially decrypts
            BigInteger[] pdec = p.partialDecrypt1(sus);


            // MC partial decrypts to get the final result
            long dec = p.partialDecrypt2(pdec).longValue();
            double restored = ((double) dec) / (4 * precision * precision);
            long end = System.nanoTime();
            //        System.out.println(restored / ctrbSum);
//            System.out.println(((double) end - start) / 1e6);
            s += ((double) end - start) / 1e6;
        }
        System.out.println(s / times);
    }
}
