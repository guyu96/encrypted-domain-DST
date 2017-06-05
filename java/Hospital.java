import java.util.HashMap;
import java.util.Random;

public class Hospital {
    private HashMap<String, DiseaseData> diseases;
    private int precision;

    private class DiseaseData {
        private String name; // disease name
        private String[] snpPos; // relevant SNP positions
        private double[] ctrb; // contribution of each SNP to disease
        private double[][] prob; // state-sensitive probability of each SNP for disease
    }

    public Hospital(int precision) {
        diseases = new HashMap<>();
        this.precision = precision;
    }

    public void addDisease(String name, String[] snpPos,
                           double[] ctrb, double[][] prob) {
        DiseaseData dd = new DiseaseData();
        dd.name = name;
        dd.snpPos = snpPos;
        dd.ctrb = ctrb;
        dd.prob = prob;

        diseases.put(name, dd);
    }

    private int normalize(double m) {
        return (int) (m * precision);
    }

    public double computeSusceptibilityShamir(String patientID, String diseaseName,
                                        TrustedEntity te, DataCenter[] DCs) {
        DiseaseData dd = diseases.get(diseaseName);
        double finalCtrbSum = 0;
        double finalProbSum = 0;

        // data for true SNPs
        int[] trueIndices = te.getTrueIndices(patientID, dd.snpPos);
        int trueSize = dd.snpPos.length;
        double[] trueCtrb = dd.ctrb;
        double[][] trueProb = dd.prob;

        // compute the sum of contributions
        for (double c: trueCtrb)
                finalCtrbSum += c;

        // data for false SNPs
        int falseSize = trueSize; // size of false dataset
        int[][] falseInfo = te.getFalseInfo(patientID, falseSize);
        int[] falseIndices = falseInfo[0];
        int[] falseStates = falseInfo[1];
//        double[] falseCtrb = new double[falseSize];
//        double[][] falseProb = new double[3][falseSize];
        int[] falseCtrb = new int[falseSize];
        int[][] falseProb = new int[3][falseSize];


        Random r = new Random();
        // generate false contributions and probabilities
        for (int i = 0; i < falseSize; i++)
            falseCtrb[i] = r.nextInt(precision);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < falseSize; j++)
                falseProb[i][j] = r.nextInt(precision);


        // compute false sum of state-sensitive probabilities
        // this sum will be subtracted from the total sum at the end
        long falseSumP = 0;
        int c;
        int fs;
        for (int i = 0; i < falseSize; i++) {
            c = falseCtrb[i];
            fs = falseStates[i];
            falseSumP += falseProb[fs][i] * c;
        }

        // merge the true and false info randomly
        // initialize variables
        int mixedSize = trueSize + falseSize;
        int[] mixedIndices = new int[mixedSize];
        int[] mixedCtrb = new int[mixedSize]; // store contributions multiplied by precision
        int[][] mixedProb = new int[3][mixedSize]; // store probabilities multiplied by precision


        boolean[] occupied = new boolean[mixedSize];
        for (int i = 0; i < mixedSize; i++)
            occupied[i] = false;

        // begin merging
        int i = 0;
        int idx;
        while (i < mixedSize) {
            while (true) {
                idx = r.nextInt(mixedSize);
                if (!occupied[idx]) {
                    occupied[idx] = true;
                    // if there are true SNPs left to be placed
                    if (i < trueSize) {
                        mixedIndices[idx] = trueIndices[i];
                        mixedCtrb[idx] = normalize(trueCtrb[i]) * 2;
                        for (int j = 0; j < 3; j++)
                            mixedProb[j][idx] = normalize(trueProb[j][i]) * 2;
                    } else {
                        mixedIndices[idx] = falseIndices[i - trueSize];
                        mixedCtrb[idx] = falseCtrb[i - trueSize] * 2;
                        for (int j = 0; j < 3; j++)
                            mixedProb[j][idx] = falseProb[j][i - trueSize] * 2;
                    }
                    i++;
                    break;
                }
            }
        }

        // compute result
        // TODO: add division of data
        Point[] shares = new Point[DCs.length];
        for (int j = 0; j < DCs.length; j++)
        shares[j] = DCs[j].compute(patientID, mixedIndices, mixedCtrb, mixedProb);

        long totalSumP = Shamir.decrypt(shares, shares.length) / 4;
        finalProbSum = ((double) (totalSumP - falseSumP)) / (precision * precision);
        return finalProbSum / finalCtrbSum;
    }

    public static void main(String[] args) {
        Hospital h = new Hospital(10000);
    }
}
