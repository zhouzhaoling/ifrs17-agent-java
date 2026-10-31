package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import com.zhongan.ifrs17agentjava.tool.Ifrs17QueryTool;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmChatNode implements NodeAction<Ifrs17AgentState> {

    private final ChatClient.Builder chatClientBuilder;
    private final Ifrs17QueryTool ifrs17QueryTool;

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [汇总分析节点] 正在整合多源证据并调用大模型...");

        // 1. 获取并发收集到的证据列表
        List<String> evidenceList = state.evidenceList();

        // 2. 把 List 拼接成优美的字符串格式
        StringBuilder contextBuilder = new StringBuilder();
        if (evidenceList == null || evidenceList.isEmpty()) {
            contextBuilder.append("暂无底层明细和规则数据。");
        } else {
            for (int i = 0; i < evidenceList.size(); i++) {
                contextBuilder.append(i + 1).append(". ").append(evidenceList.get(i)).append("\n");
            }
        }
        String context = contextBuilder.toString();

        // 3. 组装专业的 Prompt（结合系统角色、证据列表、用户问题）
        String systemPrompt = "你是一个资深的 IFRS17 财务核算专家。你的任务是根据系统提供的【底层收集到的证据】，对用户的财务问题给出专业的归因分析。";

        String userPrompt = String.format(
                "【用户问题】\n%s\n\n【底层收集到的证据】\n%s\n\n请结合上述证据，给出结构化的差异分析和处理建议。",
                state.question(), context
        );

        System.out.println(">>> [发送给大模型的 Prompt]: \n" + userPrompt);

        // 4. 调用大模型生成最终答案
        String answer = chatClientBuilder.build().prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        // 5. 将大模型的最终输出写回 State
        return Map.of("answer", answer);
    }
}