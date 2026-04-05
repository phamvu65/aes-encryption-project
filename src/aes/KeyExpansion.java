package aes;

public class KeyExpansion {
    public static byte[] expandKey(byte[] key) {
        int keySize = key.length;
        int numRounds;

        // Xác định số vòng dựa trên độ dài khóa đầu vào
        if (keySize == 16) numRounds = 10;      // AES-128
        else if (keySize == 24) numRounds = 12; // AES-192
        else if (keySize == 32) numRounds = 14; // AES-256
        else throw new IllegalArgumentException("Key size phai la 16, 24, hoac 32 bytes");

        int expandedKeySize = (numRounds + 1) * 16;
        byte[] expanded = new byte[expandedKeySize];
        System.arraycopy(key, 0, expanded, 0, keySize);

        int bytesGenerated = keySize;
        int rconIter = 0;
        byte[] temp = new byte[4];

        while (bytesGenerated < expandedKeySize) {
            for (int i = 0; i < 4; i++) temp[i] = expanded[bytesGenerated - 4 + i];

            // Bước core xử lý mỗi khi hoàn thành 1 chu kỳ khóa gốc
            if (bytesGenerated % keySize == 0) {
                temp = core(temp, rconIter++);
            }
            // Quy tắc đặc biệt riêng cho AES-256: Thêm SubWord nếu ở giữa chu kỳ
            else if (keySize == 32 && bytesGenerated % keySize == 16) {
                for (int i = 0; i < 4; i++)
                    temp[i] = (byte) AESConstants.S_BOX[temp[i] & 0xff];
            }

            for (int i = 0; i < 4; i++) {
                expanded[bytesGenerated] = (byte)(expanded[bytesGenerated - keySize] ^ temp[i]);
                bytesGenerated++;
            }
        }
        return expanded;
    }

    private static byte[] core(byte[] in, int i) {
        byte[] out = new byte[4];
        // Rotate: Dịch vòng trái 1 byte
        out[0] = in[1]; out[1] = in[2]; out[2] = in[3]; out[3] = in[0];
        // Substitute: Tra bảng S-Box
        for (int j = 0; j < 4; j++) out[j] = (byte) AESConstants.S_BOX[out[j] & 0xff];
        // Rcon: XOR với hằng số vòng
        out[0] ^= (byte)(AESConstants.RCON[i] & 0xff);
        return out;
    }
}