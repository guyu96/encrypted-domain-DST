import java.math.BigInteger;
import java.util.Random;

public class Paillier {
    private BigInteger n, g; // public key
    private BigInteger x, x1, x2; // private key
    private BigInteger nSquared; // to simplify calculations

    private BigInteger getSafePrime(int length) {
        Random r = new Random();
        BigInteger p1, p2;
        while (true) {
            p1 = BigInteger.probablePrime(length, r);
            p2 = p1.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
            if (p2.isProbablePrime(100)) // P(composite) <= 2^(-100)
                break;
        }
        return p1;
    }

    public Paillier() {
        n = new BigInteger("1917931985440060214786069115658132072098682944165453472951402096987240" +
                "0442392297071942571153838628836976060612342596167364908304680523914565480373568143" +
                "439885248616453162376555695381909927670746467160790798058327503411468529654483567" +
                "241264883097983897424839428517627286432223652580065761233388956321747184449229733308" +
                "455061627066809044837337423549193376593577917160036653362989800502379705633610071315" +
                "651439409498903525135125745367784482775822605262840856372518051394082091114911203729" +
                "8521989037504339125488220920600122808525451279533369888914269611590714547806206077487" +
                "27788674388824225051671665627627647290552700001");
//        System.out.println(n.bitLength());
        g = new BigInteger("196591496745333997905784174017395747388411319265720600646011537475105005323" +
                "619955129598491097749859158982807687219846138891160392437587942059241405382259457674462" +
                "40921738385326955178661030008166619536905073654712045169386648214249667043529297140629" +
                "67325672112433684687965536861570100508109251228695059367628816490093148581420556405515" +
                "560578547429230019387261758493034299180087999405420776168192044274478096284757912096672" +
                "778768415057977880914846877444333942978776732039517168235864171859715562478513434387279" +
                "0733264788196843212939236009432755861453807589965707146764341360198807576851924757787412" +
                "9463551956237285331127180623799796129327490085229319355099403262817739125422870344631186" +
                "06101945182810014787257494608604575151367980611600584243416642357293088943562988810005330" +
                "97574655758671418164930063233791689179123832671344043176832904755433590898684105403867190" +
                "6628692894416248150473349592483559854936150468548766918240207324550506562923922367372984" +
                "18190912941479001652310255009338585798671365515566516372468671804500688817494819583439533" +
                "13398869358468738791041804947479390091467857154771609761465147145860924610417295329061874" +
                "801593661428638350054359645112549174106927726616813024762149100523611973986992923739237576" +
                "857137330294306");
        x = new BigInteger("12283346447434073841463338098526234420066957443187037598260318396430040337538319" +
                "30177209038526668263395616073607831472122103825038741141137058508679722199807866574559719488" +
                "97513900301314486746569135085583589585484768641449808903619021295040068570435900437968391904" +
                "66307214048459562431126010192976514426290157106762270965886642626861256453824457810881312183" +
                "00561277446557624576893636500072622547849833062816714009508357285674716065470507767950091173" +
                "40905556152502213459363465831121426106749943359155273226259170717838265056600560641756971540" +
                "26881904797428238563169328086710953130143967278288290643312222844784635279297291773034350326" +
                "81052595110683234352772366318599894590115327062454816211646005751500003995174650193777493929" +
                "05231033909594111847248063308635488802648100296955073408902134574192781223503470002991311649" +
                "69681443928652437568994493082051605277441735034208450556927412089279529138425943878724420342" +
                "27685143835926399256124717313817426092999331233940371005768328277373509223551112288260316967" +
                "28429239667239144194447987832252199527647236901544868798383766104410353020062272961676164667" +
                "54727784426188935795331979051309418961706048527264928191743467036514094249191420513630182746" +
                "0038171335728130739143483153825680580125188520110");
        x1 = new BigInteger("3880710246695327419276146193485487501233122155680712366206636192024190168453265271" +
                "69620064804174541045517495192262055321300411026487765078987007371452949881613892766588221132306" +
                "74220593975278018576726859300993630138464377300268873730416171665304827608415634813356235359129" +
                "693932792254397968604968390553768086025118321536707798056598376667225906061576912562369294413905" +
                "34146454540439607075846444940903538012303796943573924216017491907316750786561667418629292861129" +
                "50462003299679023170591138555660180038618733606730319605343161806300923271188590797045578879730" +
                "53582656092201035007217126269756349428269013790686411796794378884019683899774205577983052821757" +
                "61606522872241829875722941806984342346577204232701148435185710347251459098602467552484194807392" +
                "67501472804954842588308457458572854441064080425185038672853201382897437494221545800810409771146" +
                "29500614090059877633158513431761927609657955279693963477757914133375533309691214391858973943031" +
                "02541989240375352184617573940782223696511070207203125814308843482174709036062167680923679731689" +
                "22704442105698449586186337400726566633283052431917202686694335830685945719408203886115907606556" +
                "58799813489267733440241236544223525073769360940922782227148043956753531303167200712361556172957" +
                "795162984");
        x2 = new BigInteger("8402636200738746422187191905040746918833835287506325232053682204405850169085054030075" +
                "8897372249372235009857841556941680080341401225337605807150130826924992625268179313126784283226080" +
                "7205114685505583587242885918546301770725086347452908788684032656082920223335785484277130107906628" +
                "3205686213332479675370913348504250438812215862821201418787101867204723620926768683333075042099314" +
                "3959253936503790335574270901548362981399989325311431627977609287143500666719263223890918413163135" +
                "8632191090476360877931372693643858100448063045222843800116646444214097393434163944125781106247749" +
                "0849629422254651312655700485321465715994099617853884632381936833105393308527061351962056663126704" +
                "0712775476827411777741134803107673388033113162274276878314245009234069269899174136558488208307163" +
                "8414661093275516166904677847387049996027176711733599530650171319442946489678161910154386843574727" +
                "0892599213751219328313733610456547530948628928939298107390142553375349778835071718940752936987219" +
                "0924837425005500385811653039197567888608293607492576878522517638751034935307257203026331699910179" +
                "7500260317536896917570297699558959981136947756673174171329311789223335339582606717833815484771934" +
                "33820917417418144844195379045232890127378974599435976282441464124407167393357126");
        nSquared = n.multiply(n);
    }
    public Paillier(int length) {
        Random r = new Random();
        System.out.println("Generating keys...");

        // initializing n
        BigInteger p = getSafePrime(length / 2);
        BigInteger q = getSafePrime(length / 2);
        n = p.multiply(q);
        nSquared = n.multiply(n);

        // initializing x, x1, and x2
        // x is a random number < n^2 / 2, x1 + x2 = x
        x = new BigInteger(nSquared.bitLength() - 2, r);
        x1 = new BigInteger(nSquared.bitLength() - 3, r);
        x2 = x.subtract(x1);

        // initializing g
        BigInteger a; // a < n^2 and a in the group (Z^*)_n^2
        while (true) {
            a = new BigInteger(nSquared.bitLength() - 1, r);
            if (a.gcd(nSquared).equals(BigInteger.ONE))
                break;
        }

        // g = (a^(2n))^(-1)
        BigInteger twoN = n.multiply(BigInteger.valueOf(2));
        try {
            g = a.modPow(twoN, nSquared).modInverse(nSquared);
        } catch (ArithmeticException ae) {
            System.out.println(g + "\nis not invertible mod\n" + nSquared);
            g = BigInteger.valueOf(-1);
        }

        System.out.println("Done.");
    }

    public void displayKeys() {
        System.out.println("n = " + n);
        System.out.println("x = " + x);
        System.out.println("g = " + g);
        System.out.println("x1 = " + x1);
        System.out.println("x2 = " + x2);
    }

    public BigInteger[] encrypt(int m) {
        BigInteger M = BigInteger.valueOf(m);
        BigInteger[] ciphertext = new BigInteger[2];

        // r is a random number < n/4
        BigInteger r = new BigInteger(n.bitLength() - 3, new Random());

        // T1 = g^r mod n^2
        ciphertext[0] = g.modPow(r, nSquared);
        // T2 = h^r (1 + mn) mod n^2 = ((g^(xr) mod n^2) * ((1 + mn) mod n^2)) mod n^2
        BigInteger temp1 = g.modPow(x.multiply(r), nSquared);
        BigInteger temp2 = BigInteger.ONE.add(M.multiply(n)).mod(nSquared);
        ciphertext[1] = temp1.multiply(temp2).mod(nSquared);
        return ciphertext;
    }

    public BigInteger[] reEncrypt(BigInteger[] ciphertext) {
        BigInteger[] reCipher = new BigInteger[2];

        // r is a random number < n/4
        BigInteger r = new BigInteger(n.bitLength() - 3, new Random());

        // T1' = g^r' * T1 mod n^2
        BigInteger temp1 = g.modPow(r, nSquared);
        BigInteger temp2 = ciphertext[0].mod(nSquared);
        reCipher[0] = temp1.multiply(temp2).mod(nSquared);

        // T2' = h^r' * T2 mod n^2 = ((g^(xr) mod n^2) * (T2 mod n^2)) mod n^2
        temp1 = g.modPow(x.multiply(r), nSquared);
        temp2 = ciphertext[1].mod(nSquared);
        reCipher[1] = temp1.multiply(temp2).mod(nSquared);
        return reCipher;
    }

    public BigInteger[] partialDecrypt1(BigInteger[] ciphertext) {
        // T1' = T1
        // T2' = T2 / T1^x1 mod n^2
        //     = (T2 mod n^2 * (T1^-1)^x1 mod n^2) mod n^2
        BigInteger temp1 = ciphertext[1].mod(nSquared);
        BigInteger temp2 = ciphertext[0].modInverse(nSquared).modPow(x1, nSquared);

        BigInteger[] pdCipher = new BigInteger[2];
        pdCipher[0] = ciphertext[0];
        pdCipher[1] = temp1.multiply(temp2).mod(nSquared);
        return pdCipher;
    }

    public BigInteger partialDecrypt2(BigInteger[] ciphertext) {
        // f(u) = ((u - 1) mod n^2) / n
        // m = f(T2 / T1^x2)
        // T2 / T1^x2 = T2 * ((T1^-1)^x2 mod n^2)

        BigInteger temp1 = ciphertext[0].modInverse(nSquared).modPow(x2, nSquared);
        BigInteger temp2 = ciphertext[1].multiply(temp1);
        return temp2.subtract(BigInteger.ONE).mod(nSquared).divide(n);
    }

    public BigInteger decrypt(BigInteger[] ciphertext) {
        // f(u) = ((u - 1) mod n^2) / n
        // m = f(T2 / T1^x)
        // T2 / T1^x = T2 * ((T1^-1)^x mod n^2)

        BigInteger temp1 = ciphertext[0].modInverse(nSquared).modPow(x, nSquared);
        BigInteger temp2 = ciphertext[1].multiply(temp1);
        return temp2.subtract(BigInteger.ONE).mod(nSquared).divide(n);
    }

    public BigInteger[] add(BigInteger[] cipher1, BigInteger[] cipher2) {
        BigInteger[] sum = new BigInteger[2];
        sum[0] = cipher1[0].multiply(cipher2[0]);
        sum[1] = cipher1[1].multiply(cipher2[1]);
        return sum;
    }

    public BigInteger[] multiply(BigInteger[] ciphertext, BigInteger num) {
        BigInteger[] product = new BigInteger[2];
        product[0] = ciphertext[0].modPow(num, nSquared);
        product[1] = ciphertext[1].modPow(num, nSquared);
        return product;
    }

    public BigInteger[] multiply(BigInteger[] ciphertext, long num) {
        BigInteger bigNum = BigInteger.valueOf(num);
        BigInteger[] product = new BigInteger[2];
        product[0] = ciphertext[0].modPow(bigNum, nSquared);
        product[1] = ciphertext[1].modPow(bigNum, nSquared);
        return product;
    }

    public static void main(String[] args) {
        Paillier p = new Paillier(2048);
        p.displayKeys();
        System.out.print(p.n);
        int m1 = 3;
        int m2 = 5;

        // test for encryption, re-encryption, and decryption
        BigInteger[] ct = p.encrypt(m1);
        for (BigInteger bi: ct) {
            System.out.println("size: " + bi.bitLength());
        }
        ct = p.reEncrypt(ct);
        System.out.println(p.decrypt(ct));

        // test for partial decryptions
        ct = p.encrypt(m1);
        ct = p.partialDecrypt1(ct);
        System.out.println(p.partialDecrypt2(ct));

        // test for homomorphism
        BigInteger[] ct1 = p.encrypt(m1);
        BigInteger[] ct2 = p.encrypt(m2);
        BigInteger num = new BigInteger(128, new Random());
        System.out.println(p.decrypt(p.add(ct1, ct2)));
        System.out.println(num + " * " + m1 + " = " + p.decrypt(p.multiply(ct1, num)));
    }
}