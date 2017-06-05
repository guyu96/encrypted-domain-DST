import java.util.HashMap;

/**
 * This class computes the disease susceptibility based on
 * the stored encrypted SNPs and the contributions and
 * state-specific probabilities provided by the hospital
 */
public class DataCenter {
    // key = patient ID; value = the data center's share of that patient's SNP states
    private HashMap<String, Point[]> data;

    public DataCenter() {
        data = new HashMap<>();
    }

    public void addPatientData(String patientID, Point[] SNPsShare) {
        data.put(patientID, SNPsShare);
    }

    /*
    public Point compute(String patientID, int[] indices, double[] ctrb, double[][] prob, int precision) {

        // compute sum of state-sensitive probabilities
        Point[] pData = data.get(patientID);
        int c, pr0, pr1, pr2;
        Point share, s0, s1, s2, tempSum;
        Point sumP = new Point(pData[0].x, 0, pData[0].prime);

        for (int i = 0; i < indices.length; i++) {
            share = pData[indices[i]];
            c = (int) (ctrb[i] * precision);
            pr0 = (int) ((prob[0][i] * precision) / 2);
            pr1 = (int) ((prob[1][i] * precision) * -1);
            pr2 = (int) ((prob[2][i] * precision) / 2);
            s0 = share.add(-1).multiply(share.add(-2)).multiply(pr0);
            s1 = share.multiply(share.add(-2)).multiply(pr1);
            s2 = share.multiply(share.add(-1)).multiply(pr2);
            tempSum = s0.add(s1).add(s2).multiply(c);
            sumP = sumP.add(tempSum);
        }
        return sumP;
    }
    */

    /*
    public Point compute(String patientID, int[] indices, int[] ctrb, int[][] prob) {

        // compute sum of state-sensitive probabilities
        Point[] pData = data.get(patientID);
        int c, pr0, pr1, pr2;
        Point share, s0, s1, s2, tempSum;
        Point sumP = new Point(pData[0].x, 0, pData[0].prime);

        for (int i = 0; i < indices.length; i++) {
            share = pData[indices[i]];
            c = ctrb[i];
            pr0 = prob[0][i] / 2;
            pr1 = prob[1][i] * -1;
            pr2 = prob[2][i] / 2;
            s0 = share.add(-1).multiply(share.add(-2)).multiply(pr0);
            s1 = share.multiply(share.add(-2)).multiply(pr1);
            s2 = share.multiply(share.add(-1)).multiply(pr2);
            tempSum = s0.add(s1).add(s2).multiply(c);
            sumP = sumP.add(tempSum);
        }
        return sumP;
    }
    */
    public Point compute(String patientID, int[] indices, int[] ctrb, int[][] prob) {

        // compute sum of state-sensitive probabilities
        Point[] pData = data.get(patientID);
        int c, pr0, pr1, pr2;
        int c0, c1;
        Point share, s0, s1, tempSum;
        Point sumP = new Point(pData[0].x, 0, pData[0].prime);

        for (int i = 0; i < indices.length; i++) {
            share = pData[indices[i]];
            c = ctrb[i];
            pr0 = prob[0][i];
            pr1 = prob[1][i];
            pr2 = prob[2][i];
            c0 = (pr0 - 2*pr1 + pr2) / 2;
            c1 = (-3*pr0 + 4*pr1 - pr2) / 2;
            s0 = share.multiply(share).multiply(c0);
            s1 = share.multiply(c1);
            tempSum = s0.add(s1).add(pr0).multiply(c);
            sumP = sumP.add(tempSum);
        }
        return sumP;
    }
}
