package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RouterNode implements NodeAction<Ifrs17AgentState> {
    private final ChatClient.Builder chatClientBuilder;

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [路由节点] 正在识别用户意图...");

        // 修改后的 System Prompt
        String systemPrompt = """
            你是一个资深的财务系统意图识别路由器。请分析用户的输入，并严格将其分类为以下三个意图之一：
            1. DIFF：如果用户问的是新旧账套差异、准备金异常、金额对不上、对账排查。
            2. KNOWLEDGE：如果用户问的是 UAT 报错原因、系统操作步骤、核算规则解释、业务文档说明。
            3. CHAT：如果是其他闲聊、通用常识、或者与财务系统无关的问题。
            
            要求：你只能输出 DIFF、KNOWLEDGE 或 CHAT 这三个纯英文单词之一，绝对不要输出任何标点符号、解释或其他文字！
            """;

        String intent = chatClientBuilder.build().prompt()
                .system(systemPrompt)
                .user(state.question())
                .call().content();

        // 增加一层代码级的兜底，防止大模型抽风返回乱七八糟的东西
        String cleanIntent = "CHAT";
        if (intent != null) {
            if (intent.contains("DIFF")) {
                cleanIntent = "DIFF";
            } else if (intent.contains("KNOWLEDGE")) {
                cleanIntent = "KNOWLEDGE";
            }
        }

        System.out.println(">>> [路由节点] 识别结果为: " + cleanIntent);
        return Map.of("intent", cleanIntent);
    }
}