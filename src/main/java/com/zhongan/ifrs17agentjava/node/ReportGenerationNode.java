package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReportGenerationNode implements NodeAction<Ifrs17AgentState> {

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [报告生成节点] 启动，正在撰写专业财务分析报告...");

        // 强约束的 Prompt 模板，要求大模型按固定格式输出
        String systemPrompt = """
            你是一个顶级的 IFRS17 财务分析师。
            请根据用户的提问，生成一份严谨、结构化的财务分析报告。
            报告必须包含以下四大固定模块：
            ## 一、报告概览（统计期间、核算范围、核心结论）
            ## 二、核心指标明细（以 Markdown 表格输出，包含指标名称、金额、环比变动）
            ## 三、异常与风险提示（列出发现的数据差异或合规风险）
            ## 四、下期工作建议（提供具体的修复或优化方向）
            
            请使用专业、客观、严谨的财务话术，禁止口语化。
            """;

        String answer = chatClientBuilder.build().prompt()
                .system(systemPrompt)
                .user(state.question())
                .call()
                .content();

        // 👇 亮点：把大模型生成的报告，保存到本地硬盘（模拟导出文件）
        String fileName = "IFRS17_分析报告_" + System.currentTimeMillis() + ".md";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(answer);
            System.out.println(">>> [报告生成节点] 报告已成功保存为文件: " + fileName);
        } catch (IOException e) {
            System.err.println(">>> 报告落盘失败: " + e.getMessage());
        }

        return Map.of("answer", answer);
    }
}