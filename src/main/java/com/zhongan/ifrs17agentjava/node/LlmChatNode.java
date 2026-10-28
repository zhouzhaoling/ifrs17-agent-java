package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import com.zhongan.ifrs17agentjava.tool.Ifrs17QueryTool; // 引入刚才写的工具
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmChatNode implements NodeAction<Ifrs17AgentState> {

    private final ChatClient.Builder chatClientBuilder;
    private final Ifrs17QueryTool ifrs17QueryTool; // 注入工具类

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [Agent节点执行] 正在思考是否需要调用工具...");

        // 1. 👈 关键：从 State 里拿到 Redis 传过来的历史记录
        String history = state.history();

        // 2. 👈 关键：把历史记录拼进 System Prompt 里
        String systemPrompt = "你是一个 IFRS17 财务核算专家。你可以使用提供的工具来查询数据。如果需要查数据，请直接调用工具。\n" +
                "以下是你和用户之前的对话历史：\n" + history;

        String answer = chatClientBuilder.build().prompt()
                .system(systemPrompt) // 使用拼装后的 Prompt
                .user(state.question())
                .tools(ifrs17QueryTool)
                .call()
                .content();

        return Map.of("answer", answer);
    }
}