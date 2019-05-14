import java.io.UnsupportedEncodingException;

public class Sha1 {
    // Output format options
    public static final int OUT_HEX = 0;
    public static final int OUT_HEXW = 1;


    public static String hash(String msgIn) throws UnsupportedEncodingException {
        return hash(msgIn, OUT_HEX);
    }

    public static String hash(String msgIn, int outFormat) throws UnsupportedEncodingException {

        // default is to convert string to UTF-8, as SHA only deals with byte-streams
        byte[] oldMsg = utf8Encode(msgIn);

        // constants [§4.2.1]
        int[] constant = { 0x5a827999, 0x6ed9eba1, 0x8f1bbcdc, 0xca62c1d6 };

        // initial hash value [§5.3.1]
        int[] hash = { 0x67452301, 0xefcdab89, 0x98badcfe, 0x10325476, 0xc3d2e1f0 };

        // PREPROCESSING [§6.1.1]
        byte[] msg = new byte[oldMsg.length + 1];
        for (int i = 0; i < oldMsg.length; i++) {
            msg[i] = oldMsg[i];
        }
        msg[oldMsg.length] = (byte) 0x80; // add trailing '1' bit (+ 0's padding) to string [§5.1.1]

        // convert string msg into 512-bit/16-integer blocks arrays of ints [§5.2.1]
        int l = msg.length / 4 + 2; 
        int N = (int) Math.ceil(l / 16.0); 
        int[][] M = new int[N][16];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < 16; j++) { // encode 4 chars per integer, big-endian encoding
                M[i][j] = (getCharCode(msg, i * 64 + j * 4 + 0) << 24) | (getCharCode(msg, i * 64 + j * 4 + 1) << 16)
                        | (getCharCode(msg, i * 64 + j * 4 + 2) << 8) | (getCharCode(msg, i * 64 + j * 4 + 3) << 0);
            } // note running off the end of msg is okay, bitwise ops on null return 0
        }

        M[N - 1][14] = (int) Math.floor(((msg.length - 1) * 8) / Math.pow(2, 32));
        M[N - 1][15] = ((msg.length - 1) * 8) & 0xffffffff;

        // HASH COMPUTATION [§6.1.2]
        for (int i = 0; i < N; i++) {
            int[] W = new int[80];

            // 1 - prepare message schedule 'W'
            for (int t = 0; t < 16; t++) {
                W[t] = M[i][t];
            }
            for (int t = 16; t < 80; t++) {
                W[t] = rotateLeft(W[t - 3] ^ W[t - 8] ^ W[t - 14] ^ W[t - 16], 1);
            }

            // 2 - initialise five working variables a, b, c, d, e with previous hash value
            int a = hash[0];
            int b = hash[1];
            int c = hash[2];
            int d = hash[3];
            int e = hash[4];

            // // 3 - main loop (use JavaScript '>>> 0' to emulate UInt32 variables)
            for (int t = 0; t < 80; t++) {
                int s = (int) Math.floor(t / 20.0); // seq for blocks of 'f' functions and 'K' constants
                int T = (rotateLeft(a, 5) + functionF(s, b, c, d) + e + constant[s] + W[t]) >>> 0;
                e = d;
                d = c;
                c = rotateLeft(b, 30) >>> 0;
                b = a;
                a = T;
            }

            // 4 - compute the new intermediate hash value
            hash[0] = (hash[0] + a) >>> 0;
            hash[1] = (hash[1] + b) >>> 0;
            hash[2] = (hash[2] + c) >>> 0;
            hash[3] = (hash[3] + d) >>> 0;
            hash[4] = (hash[4] + e) >>> 0;
        }

        // concatenate H0..H4, with separator if required
        String separator = outFormat == OUT_HEXW ? " " : "";

        // convert H0..H4 to hex strings (with leading zeros)
        String output = "";
        for (int h = 0; h < hash.length - 1; h++) {
            String strValue = "00000000" + Integer.toHexString(hash[h]);
            output += strValue.substring(strValue.length() - 8) + separator;
        }
        String strValue = "00000000" + Integer.toHexString(hash[hash.length - 1]);
        output += strValue.substring(strValue.length() - 8);

        return output;
    }


    public static int getCharCode(byte[] str, int index) throws UnsupportedEncodingException {
        int value = 0;

        if (index < str.length) {
            value = Math.abs((int) str[index]);
        }
        return value;

    }


    public static byte[] utf8Encode(String str) throws UnsupportedEncodingException {
        byte[] result = str.getBytes("UTF-8");
        return result;
    }

    /**
     * Function 'f' [§4.1.1].
     */
    public static int functionF(int s, int x, int y, int z) {
        int output;
        switch (s) {
        // Ch()
        case 0:
            output = (x & y) ^ (~x & z);
            break;

        // Parity()
        case 1:
            output = x ^ y ^ z;
            break;

        // Maj()
        case 2:
            output = (x & y) ^ (x & z) ^ (y & z);
            break;

        // Parity()
        case 3:
            output = x ^ y ^ z;
            break;
        default:
            throw (new RuntimeException("Unexpected case."));
        }

        return output;
    }


    public static int rotateLeft(int value, int numberOfPositions) {
        return (value << numberOfPositions) | (value >>> (32 - numberOfPositions));
    }
}