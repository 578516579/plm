package cn.com.bosssfot.dv.plm.mcp.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AesGcmCipher 单元测试。
 *
 * 覆盖：
 *   - 加解密往返
 *   - 不同明文 → 不同密文（IV 随机性）
 *   - key 长度校验
 *   - null / 空串处理
 *   - 密文被篡改 → 解密抛异常（GCM tag 完整性）
 */
class AesGcmCipherTest {

    private static byte[] randomKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return key;
    }

    @Test
    @DisplayName("加解密往返 — 同一 cipher 实例")
    void encryptDecryptRoundTrip() {
        AesGcmCipher cipher = new AesGcmCipher(randomKey());
        String plain = "hello-mcp-12345 中文混合测试";
        String enc = cipher.encrypt(plain);

        assertThat(enc).isNotNull().isNotEqualTo(plain);
        // base64(12B IV + ciphertext + 16B tag) → 至少 (12 + len(plain UTF-8) + 16) bytes
        byte[] plainBytes = plain.getBytes(StandardCharsets.UTF_8);
        // base64 expansion ≈ 4/3，所以 encoded length 接近 ceil((28+len)/3)*4
        assertThat(enc.length()).isGreaterThanOrEqualTo((28 + plainBytes.length) * 4 / 3);

        String back = cipher.decrypt(enc);
        assertThat(back).isEqualTo(plain);
    }

    @Test
    @DisplayName("同一明文加密两次 → 密文不同（IV 随机）")
    void differentIvProducesDifferentCiphertext() {
        AesGcmCipher cipher = new AesGcmCipher(randomKey());
        String plain = "same-plaintext";
        String c1 = cipher.encrypt(plain);
        String c2 = cipher.encrypt(plain);

        assertThat(c1).isNotEqualTo(c2);
        assertThat(cipher.decrypt(c1)).isEqualTo(plain);
        assertThat(cipher.decrypt(c2)).isEqualTo(plain);
    }

    @Test
    @DisplayName("key 长度不是 32 字节 → 构造时抛 IllegalArgumentException")
    void keyLengthValidation() {
        assertThatThrownBy(() -> new AesGcmCipher(new byte[16]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("32 字节");

        assertThatThrownBy(() -> new AesGcmCipher(null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new AesGcmCipher(new byte[24]))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null / 空串处理")
    void nullAndEmptyHandling() {
        AesGcmCipher cipher = new AesGcmCipher(randomKey());
        assertThat(cipher.encrypt(null)).isNull();
        assertThat(cipher.decrypt(null)).isNull();
        assertThat(cipher.decrypt("")).isNull();
    }

    @Test
    @DisplayName("密文被篡改 → 解密抛 RuntimeException（GCM tag 校验失败）")
    void tamperedCiphertextDetected() {
        AesGcmCipher cipher = new AesGcmCipher(randomKey());
        String plain = "sensitive-data";
        String enc = cipher.encrypt(plain);

        // 翻转 base64 中间一位（保持长度）
        char[] chars = enc.toCharArray();
        int mid = chars.length / 2;
        chars[mid] = chars[mid] == 'A' ? 'B' : 'A';
        String tampered = new String(chars);

        assertThatThrownBy(() -> cipher.decrypt(tampered))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("解密失败");
    }

    @Test
    @DisplayName("用错误的 key 解密 → 抛异常")
    void wrongKeyFails() {
        AesGcmCipher c1 = new AesGcmCipher(randomKey());
        AesGcmCipher c2 = new AesGcmCipher(randomKey());
        String enc = c1.encrypt("payload");
        assertThatThrownBy(() -> c2.decrypt(enc))
            .isInstanceOf(RuntimeException.class);
    }
}
