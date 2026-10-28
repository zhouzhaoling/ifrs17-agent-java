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

        // 构建 ChatClient 时，把工具交给大模型
        ChatClient chatClient = chatClientBuilder.build();

        String answer = chatClient.prompt()
                .system("你是一个 IFRS17 财务核算专家。你可以使用提供的工具来查询数据。如果需要查数据，请直接调用工具。")
                .user(state.question())
                .tools(ifrs17QueryTool) // 👈 关键点：把工具挂载到本次请求中
                .call()
                .content();

        return Map.of("answer", answer);
    }
}