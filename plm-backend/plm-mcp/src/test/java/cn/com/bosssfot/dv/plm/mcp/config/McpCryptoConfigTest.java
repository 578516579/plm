package cn.com.bosssfot.dv.plm.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * McpCryptoConfig 启动期校验测试。
 *
 * 覆盖 errorCode=809 三个失败路径 + 1 个成功路径：
 *   - key 未配置（空）
 *   - key 仍是默认占位值（含 "please-change-me"）
 *   - key 长度错（base64 解码后 ≠ 32 字节）
 *   - 合法 key → 返回可用 AesGcmCipher
 *
 * <p>用反射注入 @Value 字段，避免拉 Spring context。
 */
class McpCryptoConfigTest {

    private static McpCryptoConfig configWithKey(String key, String version) {
        McpCryptoConfig cfg = new McpCryptoConfig();
        setField(cfg, "encryptKey", key == null ? "" : key);
        setField(cfg, "keyVersion", version);
        return cfg;
    }

    private static void setField(Object target, String name, String value) {
        try {
            Field f = McpCryptoConfig.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("key 未配置 → 拒启 + errorCode=809 提示")
    void rejectMissingKey() {
        assertThatThrownBy(() -> configWithKey("", "v1").mcpAesGcmCipher())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("809")
            .hasMessageContaining("MCP_ENCRYPT_KEY 未配置");
    }

    @Test
    @DisplayName("key 仍是默认占位值 → 拒启")
    void rejectDefaultKey() {
        assertThatThrownBy(() -> configWithKey(
            "please-change-me-44chars-base64-AAAAAAAAAAAAAA=", "v1").mcpAesGcmCipher())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("809")
            .hasMessageContaining("默认占位值");
    }

    @Test
    @DisplayName("key 长度不对（base64 解出来 16 字节）→ 拒启")
    void rejectShortKey() {
        // 16 字节随机 → base64 = 24 字符
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> configWithKey(shortKey, "v1").mcpAesGcmCipher())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("809")
            .hasMessageContaining("32 字节");
    }

    @Test
    @DisplayName("非法 base64 → 拒启")
    void rejectInvalidBase64() {
        assertThatThrownBy(() -> configWithKey("!!!not-base64!!!", "v1").mcpAesGcmCipher())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("809");
    }

    @Test
    @DisplayName("合法 32 字节 key → 返回可用 cipher")
    void acceptValidKey() {
        // 32 字节随机 → base64 = 44 字符
        byte[] raw = new byte[32];
        for (int i = 0; i < 32; i++) raw[i] = (byte) (i + 1);
        String validKey = Base64.getEncoder().encodeToString(raw);

        AesGcmCipher cipher = configWithKey(validKey, "v1").mcpAesGcmCipher();
        assertThat(cipher).isNotNull();
        // 用它真做一次加解密，验证可用
        assertThat(cipher.decrypt(cipher.encrypt("ok"))).isEqualTo("ok");
    }
}
