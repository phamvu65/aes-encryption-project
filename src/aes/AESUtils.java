package aes;

public class AESUtils {
    public static byte gmul(byte a, int b) {
        int aa = a & 0xff;
        int p = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0) p ^= aa;
            boolean hi = (aa & 0x80) != 0;
            aa <<= 1;
            if (hi) aa ^= 0x11b;
            b >>= 1;
        }
        return (byte) (p & 0xff);
    }

    public static byte[] xorBlocks(byte[] b1, byte[] b2) {
        byte[] res = new byte[16];
        for (int i = 0; i < 16; i++) res[i] = (byte) (b1[i] ^ b2[i]);
        return res;
    }
}