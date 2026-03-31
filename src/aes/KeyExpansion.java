package aes;

public class KeyExpansion {
    public static byte[] expandKey(byte[] key) {
        byte[] expanded = new byte[176];
        System.arraycopy(key, 0, expanded, 0, 16);
        int bytesGenerated = 16;
        int rconIter = 0;
        byte[] temp = new byte[4];

        while (bytesGenerated < 176) {
            for (int i = 0; i < 4; i++) temp[i] = expanded[bytesGenerated - 4 + i];
            if (bytesGenerated % 16 == 0) temp = core(temp, rconIter++);
            for (int i = 0; i < 4; i++) {
                expanded[bytesGenerated] = (byte)(expanded[bytesGenerated - 16] ^ temp[i]);
                bytesGenerated++;
            }
        }
        return expanded;
    }

    private static byte[] core(byte[] in, int i) {
        byte[] out = new byte[4];
        out[0] = in[1]; out[1] = in[2]; out[2] = in[3]; out[3] = in[0]; // Rotate
        for (int j = 0; j < 4; j++) out[j] = (byte) AESConstants.S_BOX[out[j] & 0xff];
        out[0] ^= (byte)(AESConstants.RCON[i] & 0xff);
        return out;
    }
}