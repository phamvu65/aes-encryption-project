package aes;

public class AES {
    public static void addRoundKey(byte[] state, byte[] rk) {
        // Thực hiện phép toán XOR từng byte giữa dữ liệu và khóa bí mật
        for (int i = 0; i < 16; i++) state[i] ^= rk[i];
    }

    public static void subBytes(byte[] state) {
        for (int i = 0; i < 16; i++) state[i] = (byte) AESConstants.S_BOX[state[i] & 0xff];
    }

    public static void invSubBytes(byte[] state) {
        for (int i = 0; i < 16; i++) state[i] = (byte) AESConstants.INV_S_BOX[state[i] & 0xff];
    }

    public static void shiftRows(byte[] s) {
        byte t;
        //r=1
        t=s[1];
        s[1]=s[5]; s[5]=s[9]; s[9]=s[13]; s[13]=t;
        //r=2
        t=s[2];
        s[2]=s[10]; s[10]=t; t=s[6]; s[6]=s[14]; s[14]=t;
        //r=3
        t=s[3];
        s[3]=s[15]; s[15]=s[11]; s[11]=s[7]; s[7]=t;
    }

    public static void invShiftRows(byte[] s) {
        byte t;
        t=s[13]; s[13]=s[9]; s[9]=s[5]; s[5]=s[1]; s[1]=t;
        t=s[2]; s[2]=s[10]; s[10]=t; t=s[6]; s[6]=s[14]; s[14]=t;
        t=s[7]; s[7]=s[11]; s[11]=s[15]; s[15]=s[3]; s[3]=t;
    }

    public static void mixColumns(byte[] s) {
        for (int i = 0; i < 4; i++) {
            int c = i * 4;
            byte a=s[c], b=s[c+1], c1=s[c+2], d=s[c+3];

            // Tính toán 4 byte mới cho cột dựa trên ma trận hằng số
            s[c]   = (byte)(AESUtils.gmul(a,2) ^ AESUtils.gmul(b,3) ^ c1 ^ d);
            s[c+1] = (byte)(a ^ AESUtils.gmul(b,2) ^ AESUtils.gmul(c1,3) ^ d);
            s[c+2] = (byte)(a ^ b ^ AESUtils.gmul(c1,2) ^ AESUtils.gmul(d,3));
            s[c+3] = (byte)(AESUtils.gmul(a,3) ^ b ^ c1 ^ AESUtils.gmul(d,2));
        }
    }

    public static void invMixColumns(byte[] s) {
        for (int i = 0; i < 4; i++) {
            int c = i * 4;

            byte a=s[c], b=s[c+1], c1=s[c+2], d=s[c+3];

            s[c]   = (byte)(AESUtils.gmul(a,14) ^ AESUtils.gmul(b,11) ^
                            AESUtils.gmul(c1,13) ^ AESUtils.gmul(d,9));
            s[c+1] = (byte)(AESUtils.gmul(a,9)  ^ AESUtils.gmul(b,14) ^
                            AESUtils.gmul(c1,11) ^ AESUtils.gmul(d,13));
            s[c+2] = (byte)(AESUtils.gmul(a,13) ^ AESUtils.gmul(b,9)  ^
                            AESUtils.gmul(c1,14) ^ AESUtils.gmul(d,11));
            s[c+3] = (byte)(AESUtils.gmul(a,11) ^ AESUtils.gmul(b,13) ^
                            AESUtils.gmul(c1,9)  ^ AESUtils.gmul(d,14));
        }
    }

    public static byte[] encryptBlock(byte[] input, byte[] expandedKey) {
        byte[] state = input.clone();
        addRoundKey(state, getRoundKey(expandedKey, 0));
        for (int r = 1; r <= 9; r++) {
            subBytes(state); shiftRows(state); mixColumns(state);
            addRoundKey(state, getRoundKey(expandedKey, r));
        }
        subBytes(state); shiftRows(state);
        addRoundKey(state, getRoundKey(expandedKey, 10));
        return state;
    }

    public static byte[] decryptBlock(byte[] input, byte[] expandedKey) {
        byte[] state = input.clone();
        addRoundKey(state, getRoundKey(expandedKey, 10));
        for (int r = 9; r >= 1; r--) {
            invShiftRows(state); invSubBytes(state);
            addRoundKey(state, getRoundKey(expandedKey, r));
            invMixColumns(state);
        }
        invShiftRows(state); invSubBytes(state);
        addRoundKey(state, getRoundKey(expandedKey, 0));
        return state;
    }

    private static byte[] getRoundKey(byte[] expanded, int round) {
        byte[] rk = new byte[16];
        System.arraycopy(expanded, round * 16, rk, 0, 16);
        return rk;
    }
}