package cn.com.bosssfot.dv.plm.mcp.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256-GCM 加密 / 解密工具。
 *
 * <p>用途：对 {@code tb_mcp_server.oauth_client_secret_enc}、
 *    {@code tb_integration_connector.credential_enc} 等存储凭据做对称加密。
 *
 * <p>密文格式（base64 编码后）：{@code [12 bytes IV][N bytes ciphertext + 16 bytes GCM tag]}。
 *
 * <p>密钥来源：构造函数传入 32 字节原始 key（{@code MCP_ENCRYPT_KEY} env 解 base64）。
 *    长度不对则立即抛 {@link IllegalArgumentException}（启动期失败，符合"密钥未配置拒启动"约束，错误码 809）。
 */
public class AesGcmCipher {

    private static final String ALGO = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LEN_BYTES = 12;
    private static final int TAG_LEN_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom rng = new SecureRandom();

    public AesGcmCipher(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != 32) {
            throw new IllegalArgumentException(
                "AES-256 密钥必须是 32 字节原始 / 44 字符 base64，当前长度=" + (keyBytes == null ? -1 : keyBytes.length));
        }
        this.keySpec = new SecretKeySpec(keyBytes, ALGO);
    }

    /** 加密字符串，返回 base64(IV || ciphertext+tag) */
    public String encrypt(String plain) {
        if (plain == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LEN_BYTES];
            rng.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LEN_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM 加密失败", e);
        }
    }

    /** 解密 base64(IV || ciphertext+tag) */
    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return null;
        }
        try {
            byte[] all = Base64.getDecoder().decode(encrypted);
            if (all.length < IV_LEN_BYTES + 16) {
                throw new IllegalArgumentException("密文长度不足，可能不是 AES-GCM 输出");
            }
            byte[] iv = new byte[IV_LEN_BYTES];
            byte[] cipherText = new byte[all.length - IV_LEN_BYTES];
            System.arraycopy(all, 0, iv, 0, IV_LEN_BYTES);
            System.arraycopy(all, IV_LEN_BYTES, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LEN_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM 解密失败", e);
        }
    }
}
