package aes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;

public class AESGui extends JFrame {

    private JComboBox<String> modeCombo;
    private JTextArea txtPlaintext, txtCiphertext, txtDecrypted;
    private JTextField txtKey;
    private JLabel lblTimeEnc, lblTimeDec; // Bỏ lblStatus
    private JButton btnEncrypt, btnDecrypt, btnClear, btnSave;

    // ===== FONT & COLOR CONSTANTS  =====
    private static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  28);
    private static final Font FONT_LABEL      = new Font("Segoe UI", Font.BOLD,  17);
    private static final Font FONT_COMBO      = new Font("Segoe UI", Font.PLAIN, 17);
    private static final Font FONT_KEY        = new Font("Consolas",  Font.PLAIN, 17);
    private static final Font FONT_TEXTAREA   = new Font("Consolas",  Font.PLAIN, 16);
    private static final Font FONT_TITLED     = new Font("Segoe UI", Font.BOLD,  15);
    private static final Font FONT_BUTTON     = new Font("Segoe UI", Font.BOLD,  17);
    private static final Font FONT_TIME       = new Font("Segoe UI", Font.BOLD,  15);

    private static final Color COLOR_BLUE     = new Color(0,  102, 204);
    private static final Color COLOR_GREEN    = new Color(22, 163, 117);
    private static final Color COLOR_GREY     = new Color(80,  90,  100);
    private static final Color COLOR_YELLOW   = new Color(200, 140,   0);
    private static final Color COLOR_BG       = new Color(245, 247, 250);

    private long lastTimeEnc = 0;
    private long lastTimeDec = 0;

    public AESGui() {
        initUI();
    }

    private void initUI() {
        setTitle("AES Encryption Tool - CBC Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800); // Điều chỉnh lại chiều cao một chút vì đã bỏ footer status
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(22, 22, 22, 22));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createMainContent(),  BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel title = new JLabel("HỆ THỐNG MÃ HÓA AES ĐA BIẾN THỂ (CBC)", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_BLUE);
        panel.add(title, BorderLayout.CENTER);
        return panel;
    }

    private JSplitPane createMainContent() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 14));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BLUE, 2, true),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel control = new JPanel(new GridLayout(2, 2, 15, 12));
        control.setOpaque(false);

        JLabel lblMode = new JLabel("Chế độ AES:");
        lblMode.setFont(FONT_LABEL);
        JLabel lblKey = new JLabel("Khóa bí mật:");
        lblKey.setFont(FONT_LABEL);

        modeCombo = new JComboBox<>(new String[]{
                "AES-128 (16 bytes)",
                "AES-192 (24 bytes)",
                "AES-256 (32 bytes)"});
        modeCombo.setFont(FONT_COMBO);
        modeCombo.setPreferredSize(new Dimension(0, 42));

        txtKey = new JTextField();
        txtKey.setFont(FONT_KEY);
        txtKey.setPreferredSize(new Dimension(0, 42));

        control.add(lblMode);
        control.add(lblKey);
        control.add(modeCombo);
        control.add(txtKey);

        inputPanel.add(control, BorderLayout.NORTH);

        txtPlaintext = new JTextArea();
        txtPlaintext.setFont(FONT_TEXTAREA);
        txtPlaintext.setLineWrap(true);
        txtPlaintext.setWrapStyleWord(true);

        JScrollPane scrollPlain = new JScrollPane(txtPlaintext);
        scrollPlain.setBorder(titledBorder("Nội dung cần mã hóa (Plaintext)", COLOR_BLUE));
        inputPanel.add(scrollPlain, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel(new BorderLayout(10, 14));
        outputPanel.setBackground(Color.WHITE);
        outputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_GREEN, 2, true),
                new EmptyBorder(18, 18, 18, 18)));

        txtCiphertext = new JTextArea();
        txtCiphertext.setEditable(false);
        txtCiphertext.setFont(FONT_TEXTAREA);
        txtCiphertext.setBackground(new Color(248, 250, 252));
        txtCiphertext.setLineWrap(true);

        JScrollPane scrollCipher = new JScrollPane(txtCiphertext);
        scrollCipher.setBorder(titledBorder("Bản mã (Ciphertext - Base64)", COLOR_GREEN));

        txtDecrypted = new JTextArea();
        txtDecrypted.setEditable(false);
        txtDecrypted.setFont(FONT_TEXTAREA);
        txtDecrypted.setBackground(new Color(248, 250, 252));
        txtDecrypted.setLineWrap(true);

        JScrollPane scrollDec = new JScrollPane(txtDecrypted);
        scrollDec.setBorder(titledBorder("Bản rõ sau giải mã", COLOR_GREEN));

        JPanel outputContent = new JPanel(new GridLayout(2, 1, 0, 15));
        outputContent.setOpaque(false);
        outputContent.add(scrollCipher);
        outputContent.add(scrollDec);
        outputPanel.add(outputContent, BorderLayout.CENTER);

        JPanel timePanel = new JPanel(new GridLayout(1, 2, 30, 0));
        timePanel.setOpaque(false);
        timePanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblTimeEnc = new JLabel("Thời gian mã hóa: 0 ns", SwingConstants.CENTER);
        lblTimeDec = new JLabel("Thời gian giải mã: 0 ns", SwingConstants.CENTER);
        lblTimeEnc.setFont(FONT_TIME);
        lblTimeDec.setFont(FONT_TIME);
        lblTimeEnc.setForeground(COLOR_BLUE);
        lblTimeDec.setForeground(COLOR_GREEN);

        timePanel.add(lblTimeEnc);
        timePanel.add(lblTimeDec);
        outputPanel.add(timePanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputPanel);
        split.setDividerLocation(590);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setDividerSize(8);
        return split;
    }

    private JPanel createFooterPanel() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 14));
        btnPanel.setOpaque(false);

        btnEncrypt = createStyledButton("🔒  MÃ HÓA",    COLOR_BLUE,   Color.WHITE);
        btnDecrypt = createStyledButton("🔓  GIẢI MÃ",   COLOR_GREEN,  Color.WHITE);
        btnClear   = createStyledButton("🗑  XÓA TẤT CẢ", COLOR_GREY,  Color.WHITE);
        btnSave    = createStyledButton("💾  LƯU KẾT QUẢ", COLOR_YELLOW, Color.WHITE);

        btnEncrypt.addActionListener(this::performEncrypt);
        btnDecrypt.addActionListener(this::performDecrypt);
        btnClear.addActionListener(e -> clearAll());
        btnSave.addActionListener(e -> saveResults());

        btnPanel.add(btnEncrypt);
        btnPanel.add(btnDecrypt);
        btnPanel.add(btnClear);
        btnPanel.add(btnSave);

        return btnPanel; // Trả về trực tiếp panel chứa nút, không bọc thêm lblStatus
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(14, 36, 14, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(210, 58));
        Color hoverBg = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hoverBg); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private TitledBorder titledBorder(String title, Color lineColor) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(lineColor, 1, true),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                FONT_TITLED, lineColor);
    }

    private void performEncrypt(ActionEvent e) {
        try {
            String modeStr = (String) modeCombo.getSelectedItem();
            byte[] key = txtKey.getText().getBytes(StandardCharsets.UTF_8);
            AESService.validateKey(key);

            String plain = txtPlaintext.getText().trim();
            if (plain.isEmpty()) {
                showError("Vui lòng nhập nội dung cần mã hóa!");
                return;
            }

            long start = System.nanoTime();
            String cipher = AESService.encrypt(plain.getBytes(StandardCharsets.UTF_8), key);
            long end = System.nanoTime();

            this.lastTimeEnc = end - start;
            txtCiphertext.setText(cipher);
            lblTimeEnc.setText("Thời gian mã hóa: " + this.lastTimeEnc + " ns");
            showSuccess("Mã hóa thành công!");

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    private void performDecrypt(ActionEvent e) {
        try {
            String cipher = txtCiphertext.getText().trim();
            if (cipher.isEmpty()) {
                showError("Vui lòng nhập bản mã trước!");
                return;
            }

            byte[] key = txtKey.getText().getBytes(StandardCharsets.UTF_8);
            AESService.validateKey(key);

            long start = System.nanoTime();
            byte[] decrypted = AESService.decrypt(cipher, key);
            long end = System.nanoTime();

            this.lastTimeDec = end - start;
            txtDecrypted.setText(new String(decrypted, StandardCharsets.UTF_8));
            lblTimeDec.setText("Thời gian giải mã: " + this.lastTimeDec + " ns");
            showSuccess("Giải mã thành công!");

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Lỗi giải mã: " + ex.getMessage());
        }
    }

    private void saveResults() {
        try {
            String plain = txtPlaintext.getText().trim();
            String cipher = txtCiphertext.getText().trim();
            String recovered = txtDecrypted.getText().trim();
            String modeStr = (String) modeCombo.getSelectedItem();

            if (cipher.isEmpty()) {
                showError("Không có dữ liệu bản mã để lưu!");
                return;
            }

            AESService.saveToFile(modeStr, plain, cipher, recovered, this.lastTimeEnc, this.lastTimeDec);
            showSuccess("Đã lưu kết quả vào file ket_qua_aes.txt");

        } catch (Exception ex) {
            showError("Lỗi khi ghi file: " + ex.getMessage());
        }
    }

    // Chỉnh sửa để hiển thị Dialog thay vì nhãn dán
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void clearAll() {
        txtPlaintext.setText("");
        txtCiphertext.setText("");
        txtDecrypted.setText("");
        lblTimeEnc.setText("Thời gian mã hóa: 0 ns");
        lblTimeDec.setText("Thời gian giải mã: 0 ns");
        this.lastTimeEnc = 0;
        this.lastTimeDec = 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new AESGui().setVisible(true);
        });
    }
}