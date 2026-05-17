package cn.com.bosssfot.dv.plm.mcp.config;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * MCP / Integration 凭据加密的 Bean 配置。
 *
 * <p>启动期校验：
 * <ol>
 *   <li>{@code MCP_ENCRYPT_KEY} 必须配置（不可空）</li>
 *   <li>不允许保留 .env.example 默认值（"please-change-me-..."）</li>
 *   <li>base64 解码后长度必须 = 32 字节（AES-256）</li>
 * </ol>
 *
 * 不满足 → 抛 IllegalStateException 拒绝启动（错误码 809）。
 */
@Configuration
public class McpCryptoConfig {

    private static final Logger log = LoggerFactory.getLogger(McpCryptoConfig.class);

    private static final String DEFAULT_KEY_HINT = "please-change-me";

    @Value("${plm.mcp.encrypt-key:${MCP_ENCRYPT_KEY:}}")
    private String encryptKey;

    @Value("${plm.mcp.encrypt-key-version:${MCP_ENCRYPT_KEY_VERSION:v1}}")
    private String keyVersion;

    @Bean
    public AesGcmCipher mcpAesGcmCipher() {
        if (encryptKey == null || encryptKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "[errorCode=809] MCP_ENCRYPT_KEY 未配置；本系统加密凭据存储依赖此 key，拒绝启动。"
                + " 见 .env.example 与 02-设计/MCP-集成-设计.md §4.1。");
        }
        if (encryptKey.contains(DEFAULT_KEY_HINT)) {
            throw new IllegalStateException(
                "[errorCode=809] MCP_ENCRYPT_KEY 仍是默认占位值 (please-change-me-...)，禁止启动。"
                + " 用 `openssl rand -base64 32` 生成 32 字节随机 key 后设置 MCP_ENCRYPT_KEY 环境变量。");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encryptKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "[errorCode=809] MCP_ENCRYPT_KEY 不是合法 base64 字符串。"
                + " 用 `openssl rand -base64 32` 生成 32 字节随机 key。", e);
        }
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "[errorCode=809] MCP_ENCRYPT_KEY 解 base64 后长度=" + keyBytes.length
                + " 字节，要求 32 字节（AES-256）。");
        }
        log.info("[plm-mcp] AES-GCM 加密 key 已加载，版本 = {}", keyVersion);
        return new AesGcmCipher(keyBytes);
    }
}
