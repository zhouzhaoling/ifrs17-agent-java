package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import com.zhongan.ifrs17agentjava.tool.Ifrs17QueryTool;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MetricQueryNode implements NodeAction<Ifrs17AgentState> {

    private final ChatClient.Builder chatClientBuilder;
    private final Ifrs17QueryTool ifrs17QueryTool;

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [指标查询节点] 启动，准备检索语义层...");

        // 让大模型自动去调 queryMetricDefinition 工具
        String answer = chatClientBuilder.build().prompt()
                .system("你是一个财务核算专家。请调用工具查询用户询问的指标定义，并整理成易懂的回答。")
                .user(state.question())
                .tools(ifrs17QueryTool)
                .call()
                .content();

        return Map.of("answer", answer);
    }
}