package aes;

import java.security.SecureRandom;
import java.util.Base64;

public class AESService {

    // ================= ENCRYPT =================
    public static String encrypt(byte[] plaintext, byte[] key) {
        validateKey(key);

        byte[] expandedKey = KeyExpansion.expandKey(key);

        // 1. Generate IV random
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        // 2. Padding
        byte[] padded = pad(plaintext);

        // 3. CBC Encrypt
        byte[] cipher = processCBC(padded, expandedKey, iv, true);

        // 4. Gộp IV + Ciphertext
        byte[] finalOutput = new byte[iv.length + cipher.length];
        System.arraycopy(iv, 0, finalOutput, 0, 16);
        System.arraycopy(cipher, 0, finalOutput, 16, cipher.length);

        // 5. Encode Base64
        return Base64.getEncoder().encodeToString(finalOutput);
    }

    // ================= DECRYPT =================
    public static byte[] decrypt(String base64Cipher, byte[] key) {
        validateKey(key);

        byte[] allBytes = Base64.getDecoder().decode(base64Cipher);

        // 1. Tách IV
        byte[] iv = new byte[16];
        byte[] cipher = new byte[allBytes.length - 16];

        System.arraycopy(allBytes, 0, iv, 0, 16);
        System.arraycopy(allBytes, 16, cipher, 0, cipher.length);

        byte[] expandedKey = KeyExpansion.expandKey(key);

        // 2. CBC decrypt
        byte[] padded = processCBC(cipher, expandedKey, iv, false);

        // 3. Unpad + validate
        return unpad(padded);
    }

    // ================= CBC =================
    private static byte[] processCBC(byte[] data, byte[] expandedKey, byte[] iv, boolean encrypt) {
        byte[] result = new byte[data.length];
        byte[] prev = iv;

        for (int i = 0; i < data.length; i += 16) {
            byte[] block = new byte[16];
            System.arraycopy(data, i, block, 0, 16);

            if (encrypt) {
                block = AESUtils.xorBlocks(block, prev);
                block = AES.encryptBlock(block, expandedKey);
                prev = block;
            } else {
                byte[] temp = block.clone();
                block = AES.decryptBlock(block, expandedKey);
                block = AESUtils.xorBlocks(block, prev);
                prev = temp;
            }

            System.arraycopy(block, 0, result, i, 16);
        }
        return result;
    }

    // ================= PADDING =================
    private static byte[] pad(byte[] data) {
        int padLen = 16 - (data.length % 16);
        byte[] res = new byte[data.length + padLen];
        System.arraycopy(data, 0, res, 0, data.length);

        for (int i = data.length; i < res.length; i++) {
            res[i] = (byte) padLen;
        }
        return res;
    }

    private static byte[] unpad(byte[] data) {
        int padLen = data[data.length - 1] & 0xFF;

        if (padLen <= 0 || padLen > 16) {
            throw new IllegalArgumentException("Invalid padding");
        }

        // validate padding
        for (int i = data.length - padLen; i < data.length; i++) {
            if ((data[i] & 0xFF) != padLen) {
                throw new IllegalArgumentException("Invalid padding");
            }
        }

        byte[] res = new byte[data.length - padLen];
        System.arraycopy(data, 0, res, 0, res.length);
        return res;
    }

    // ================= VALIDATION =================
    private static void validateKey(byte[] key) {
        if (key == null || !(key.length == 16 || key.length == 24 || key.length == 32)) {
            throw new IllegalArgumentException("Key must be 16, 24, or 32 bytes");
        }
    }
}