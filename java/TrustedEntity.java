import java.util.HashMap;
import java.util.Random;

public class TrustedEntity {
    private HashMap<String, Patient> patients;
    private int numPatients;
    private class Patient {
        private String ID; // unique ID of the patient
        private int falseFactor; // false vs. true data ratio


        private int[] mixedSNPStates; // array of SNP states (both true and false)
        private HashMap<String, Integer> snpPosIndex; // map SNP position (string) to index in the mixed array
        private int[] falseIndices; // array of indices of false SNP states in the mixed array

        public Patient(String ID, int falseFactor, String[] snpPositions, int[] snpStates) {
            assert snpPositions.length == snpStates.length;
            this.ID = ID;
            this.falseFactor = falseFactor;

            int trueSize = snpPositions.length;
            int mixedSize = trueSize * (falseFactor + 1);
            Random r = new Random();

            snpPosIndex = new HashMap<>();
            mixedSNPStates = new int[mixedSize];
            falseIndices = new int[mixedSize - trueSize];

            // randomly populate the mixed array with true SNP states
            // boolean array to keep track of indices already generated
            boolean[] occupied = new boolean[mixedSize];
            for (int i = 0; i < mixedSize; i++) occupied[i] = false;

            int index;
            for (int i = 0; i < trueSize; i++) {
                while (true) {
                    index = r.nextInt(mixedSize);
                    if (!occupied[index]) {
                        occupied[index] = true;
                        break;
                    }
                }
                snpPosIndex.put(snpPositions[i], index);
                mixedSNPStates[index] = snpStates[i];
            }

            // populate the rest of the mixed array and the array of false indices
            int i = 0;
            for (int j = 0; j < mixedSize; j++) {
                if (!occupied[j]) {
                    falseIndices[i] = j;
                    i++;
                    mixedSNPStates[j] = r.nextInt(3);
                }
            }
        }

        public void displayData() {
            System.out.println("Patient ID: " + ID);
            System.out.println("False factor: " + falseFactor);

            System.out.print("mixed SNP states: ");
            for (int i = 0; i < mixedSNPStates.length; i++)
                System.out.print(i + ":" + mixedSNPStates[i] + " ");
            System.out.println();

            System.out.print("false SNPs are stored at indices: ");
            for (int i = 0; i < falseIndices.length; i++)
                System.out.print(falseIndices[i] + " ");
            System.out.println();

            for (String snpPos: snpPosIndex.keySet()) {
                int i = snpPosIndex.get(snpPos);
                System.out.println("SNP " + snpPos + " stored @" + i + " has state " + mixedSNPStates[i]);
            }
        }
    }

    public TrustedEntity() {
        patients = new HashMap<>();
        numPatients = 0;
    }

    public void addPatient(int pFalseFactor, String[] pSNPPositions, int[] pSNPStates) {
        String pID = "p#" + numPatients;
        Patient p = new Patient(pID, pFalseFactor, pSNPPositions, pSNPStates);
        patients.put(pID, p);

        numPatients++;
    }

    private void displayPatientData(String patientID) {
        // for debugging purposes
        patients.get(patientID).displayData();
    }

    public Point[][] divide(String patientID, int numShares, int threshold) {
        Patient p = patients.get(patientID);
        int mixedSize = p.mixedSNPStates.length;

        Point[][] encrypted = new Point[numShares][mixedSize];
        Shamir s = new Shamir(numShares, threshold);
//        s.displayParam();
        for (int i = 0; i < mixedSize; i++) {
            Point[] shares = s.genShares(p.mixedSNPStates[i]);
            for (int j = 0; j < numShares; j++) {
                encrypted[j][i] = shares[j];
//                System.out.println("each share: " + shares[j].y);
            }
        }
        return encrypted;
    }

    public int[][] getFalseInfo(String patientID, int size) {
        Patient p = patients.get(patientID);
        int falseSize = p.falseIndices.length;
        assert size <= falseSize;

        // 1st row = indices, 2nd row = states
        int[][] falseInfo = new int[2][size];
        Random r = new Random();
        boolean[] chosen = new boolean[falseSize];
        for (int i = 0; i < chosen.length; i++) chosen[i] = false;

        int i = 0;
        while (i < size) {
            int idx;
            while (true) {
                idx = r.nextInt(falseSize);
                if (!chosen[idx]) {
                    chosen[idx] = true;
                    falseInfo[0][i] = p.falseIndices[idx];
                    falseInfo[1][i] = p.mixedSNPStates[p.falseIndices[idx]];
                    break;
                }
            }
            i++;
        }

        return falseInfo;
    }

    public int[] getTrueIndices(String patientID, String[] snpPos) {
        Patient p = patients.get(patientID);
        int[] trueIndices = new int[snpPos.length];
        for (int i = 0; i < snpPos.length; i++)
            trueIndices[i] = p.snpPosIndex.get(snpPos[i]);

        return trueIndices;
    }

    public static void main(String[] args) {
        int falseFactor = 3;
        String[] snpPos = {"rs1234", "rs128874", "rs129573", "rs5362875"};
        int[] snpStates = {1, 2, 0, 1};
        String pID = "p#0";

        TrustedEntity te = new TrustedEntity();
        te.addPatient(falseFactor, snpPos, snpStates);
        te.displayPatientData(pID);

        int[][] fi = te.getFalseInfo(pID, 5);
        System.out.print("Here are some false SNP indices/states: ");
        for (int i = 0; i < fi[0].length; i++) {
            System.out.print(fi[0][i] + "/" + fi[1][i] + " ");
        }
        System.out.println();

        Point[][] encrypted = te.divide(pID, 5, 3);
        for (Point[] storage: encrypted) {
            for (Point share: storage) {
                System.out.print(share + " ");
            }
            System.out.println();
        }

        long[] decrypted = new long[encrypted[0].length];
        Point[] temp = new Point[encrypted.length];
        for (int i = 0; i < decrypted.length; i++) {
            for (int j = 0; j < temp.length; j++) {
                temp[j] = encrypted[j][i];
            }
            decrypted[i] = Shamir.decrypt(temp, 3);
            System.out.print(decrypted[i] + " ");
        }
        System.out.println();
    }
}
