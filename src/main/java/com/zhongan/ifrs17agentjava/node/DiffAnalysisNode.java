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
public class DiffAnalysisNode implements NodeAction<Ifrs17AgentState> {
    private final ChatClient.Builder chatClientBuilder;
    private final Ifrs17QueryTool tool;

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [差异分析节点] 启动多步排查流程...");

        String answer = chatClientBuilder.build().prompt()
                .system("你是一个财务差异排查专家。请先调用工具查询规则，再调用工具查询明细，最后按【差异金额、原因归因、修复建议】生成报告。")
                .user(state.question())
                .tools(tool) // 把武器库交给这个节点
                .call().content();

        return Map.of("answer", answer);
    }
}