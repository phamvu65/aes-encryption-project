package aes;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n====================================================");
            System.out.println("       HE THONG MA HOA AES ĐA BIEN THE (CBC)        ");
            System.out.println("====================================================");
            System.out.println("1. AES-128 (Key 16 bytes)");
            System.out.println("2. AES-192 (Key 24 bytes)");
            System.out.println("3. AES-256 (Key 32 bytes)");
            System.out.println("0. THOAT CHUONG TRINH");
            System.out.print("Vui long chon (0-3): ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println(">> Loi: Vui long nhap so!");
                continue;
            }

            if (choice == 0) {
                System.out.println(">> Tam biet!");
                break;
            }

            if (choice < 1 || choice > 3) {
                System.out.println(">> Lua chon khong hop le!");
                continue;
            }

            int keySize = (choice == 2) ? 24 : (choice == 3) ? 32 : 16;
            String modeName = "AES-" + (keySize * 8);

            // ===== NHAP KEY =====
            System.out.print("Nhap khoa bi mat (" + keySize + " ky tu): ");
            String keyStr = sc.nextLine();

            if (keyStr.length() != keySize) {
                System.out.println(">> Loi: Key phai dung " + keySize + " ky tu!");
                continue;
            }

            byte[] key = keyStr.getBytes(StandardCharsets.UTF_8);

            // ===== NHAP PLAINTEXT =====
            System.out.print("Nhap noi dung can ma hoa: ");
            String plaintext = sc.nextLine();

            if (plaintext.isEmpty()) {
                System.out.println(">> Loi: Khong duoc de trong!");
                continue;
            }

            try {
                // ===== MA HOA =====
                long startEnc = System.nanoTime();
                String cipher = AESService.encrypt(
                        plaintext.getBytes(StandardCharsets.UTF_8),
                        key
                );
                long endEnc = System.nanoTime();

                // ===== GIAI MA =====
                long startDec = System.nanoTime();
                byte[] decrypted = AESService.decrypt(cipher, key);
                long endDec = System.nanoTime();

                String recovered = new String(decrypted, StandardCharsets.UTF_8);

                // ===== HIEN THI =====
                displayResults(
                        modeName,
                        cipher,
                        recovered,
                        endEnc - startEnc,
                        endDec - startDec
                );

                // ===== SAVE FILE =====
                AESService.saveToFile("ket_qua_aes.txt", modeName, plaintext, cipher, endEnc - startEnc, endDec - startDec);
                System.out.println(">> Da luu file 'ket_qua_aes.txt' thong qua AESService");

            } catch (Exception e) {
                System.out.println(">> Loi: " + e.getMessage());
            }
        }

        sc.close();
    }

    // ================= DISPLAY =================
    private static void displayResults(String mode, String cipher, String plain, long tEnc, long tDec) {
        System.out.println("\n--- KET QUA THUC NGHIEM ---");
        System.out.println("+ Che do: " + mode + " (CBC)");
        System.out.println("+ Ban ma (Base64): " + cipher);
        System.out.println("+ Ban ro sau giai ma: " + plain);
        System.out.println("+ Thoi gian ma hoa: " + tEnc + " ns");
        System.out.println("+ Thoi gian giai ma: " + tDec + " ns");
    }

}