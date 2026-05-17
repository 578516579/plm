package cn.com.bosssfot.dv.plm.common.ai.dto;

import java.io.Serializable;

/**
 * 单条对话消息 — 与 OpenAI / Anthropic / Dify 三家协议都兼容。
 *
 * <p>role 取值:</p>
 * <ul>
 *   <li>{@code system} — 系统指令(Anthropic 独立顶层字段,OpenAI 仍走 messages)</li>
 *   <li>{@code user}   — 用户输入</li>
 *   <li>{@code assistant} — 模型回复</li>
 * </ul>
 *
 * @author plm
 */
public class AiChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String role;
    private String content;

    public AiChatMessage() {}

    public AiChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static AiChatMessage system(String content)    { return new AiChatMessage("system", content); }
    public static AiChatMessage user(String content)      { return new AiChatMessage("user", content); }
    public static AiChatMessage assistant(String content) { return new AiChatMessage("assistant", content); }

    public String getRole()    { return role; }
    public void setRole(String v)    { this.role = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
}
