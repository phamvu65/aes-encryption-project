package aes;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("--- HE THONG MA HOA AES-128 (CHE DO CBC) ---");
        System.out.print("Nhap plaintext: ");
        String input = sc.nextLine();

        // Neu chuoi rong, thoat chuong trinh
        if (input.isEmpty()) {
            System.out.println("Loi: Chuoi nhap vao khong duoc de trong!");
            return;
        }

        // Key va IV co dinh de de kiem tra (16 bytes moi loai)
        byte[] key = { 0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f,0x10 };
        byte[] iv =  { 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 };

        // 1. Padding du lieu (vi du nhap 5 ky tu se duoc pad len thanh 16 ky tu)
        byte[] data = pad(input.getBytes());
        byte[] expandedKey = KeyExpansion.expandKey(key);

        // --- ENCRYPT CBC ---
        long startEnc = System.nanoTime();
        byte[] encryptedResult = new byte[data.length];
        byte[] prevBlockEnc = iv; // IV cho khoi dau tien

        for (int i = 0; i < data.length; i += 16) {
            byte[] block = new byte[16];
            System.arraycopy(data, i, block, 0, 16);

            // XOR voi khoi truoc do (Chinh la CBC)
            byte[] xored = AESUtils.xorBlocks(block, prevBlockEnc);
            byte[] encBlock = AES.encryptBlock(xored, expandedKey);

            System.arraycopy(encBlock, 0, encryptedResult, i, 16);
            prevBlockEnc = encBlock; // Luu lai lam IV cho khoi sau
        }
        long endEnc = System.nanoTime();

        String base64Enc = encodeBase64(encryptedResult);
        System.out.println("\nKet qua ma hoa (Base64): " + base64Enc);
        System.out.println("Thoi gian ma hoa: " + (endEnc - startEnc) + " ns");

        // --- DECRYPT CBC ---
        long startDec = System.nanoTime();
        byte[] decryptedResult = new byte[encryptedResult.length];
        byte[] prevBlockDec = iv;

        for (int i = 0; i < encryptedResult.length; i += 16) {
            byte[] currentCipherBlock = new byte[16];
            System.arraycopy(encryptedResult, i, currentCipherBlock, 0, 16);

            // Giai ma AES khoi hien tai
            byte[] decBlock = AES.decryptBlock(currentCipherBlock, expandedKey);
            // XOR voi ciphertext khoi truoc do
            byte[] xored = AESUtils.xorBlocks(decBlock, prevBlockDec);

            System.arraycopy(xored, 0, decryptedResult, i, 16);
            prevBlockDec = currentCipherBlock; // Quan trong: Luu Ciphertext khoi truoc
        }

        byte[] finalPlain = unpad(decryptedResult);
        long endDec = System.nanoTime();

        System.out.println("\nKet qua giai ma: " + new String(finalPlain));
        System.out.println("Thoi gian giai ma: " + (endDec - startDec) + " ns");

        sc.close();
    }

    // --- BASE64 CUSTOM  ---
    private static String encodeBase64(byte[] data) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        StringBuilder sb = new StringBuilder();
        // Xu ly tung nhom 3 byte de chuyen thanh 4 ky tu Base64
        for (int i = 0; i < data.length; i += 3) {
            int val = (data[i] & 0xFF) << 16;
            int count = 1;
            if (i + 1 < data.length) {
                val |= (data[i + 1] & 0xFF) << 8;
                count++;
            }
            if (i + 2 < data.length) {
                val |= (data[i + 2] & 0xFF);
                count++;
            }

            sb.append(alphabet.charAt((val >> 18) & 0x3F));
            sb.append(alphabet.charAt((val >> 12) & 0x3F));
            sb.append(count > 1 ? alphabet.charAt((val >> 6) & 0x3F) : '=');
            sb.append(count > 2 ? alphabet.charAt(val & 0x3F) : '=');
        }
        return sb.toString();
    }

    // PKCS7 Padding
    private static byte[] pad(byte[] data) {
        int padLen = 16 - (data.length % 16);
        byte[] res = new byte[data.length + padLen];
        System.arraycopy(data, 0, res, 0, data.length);
        for (int i = data.length; i < res.length; i++) {
            res[i] = (byte) padLen;
        }
        return res;
    }

    // PKCS7 Unpadding
    private static byte[] unpad(byte[] data) {
        int padLen = data[data.length - 1];
        if (padLen <= 0 || padLen > 16) return data; // Phong tru loi
        byte[] res = new byte[data.length - padLen];
        System.arraycopy(data, 0, res, 0, res.length);
        return res;
    }
}