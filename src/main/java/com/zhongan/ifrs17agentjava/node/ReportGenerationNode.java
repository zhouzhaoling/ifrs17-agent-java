package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.service.DocumentService;
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
    private final DocumentService documentService;

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [报告生成节点] 启动，正在撰写专业财务分析报告并生成 Excel 附件...");

        // ==========================================
        // 1. 先生成 Excel 数据报表 (调用 Service)
        // ==========================================
        String downloadUrl = "";
        try {
            // 模拟底层查库得到的差异数据
            double oldAmt = 1000.0;
            double newAmt = 1250.0;

            // 调用我们在上一步写好的 Service 生成 Excel 并落盘
            String excelFileName = documentService.generateExcelReport("上海机构", oldAmt, newAmt);

            // 拼装成前端可以直接点击的完整下载 URL (注意端口和你本地保持一致)
            downloadUrl = "http://localhost:8080/api/agent/download/" + excelFileName;
            System.out.println(">>> [报告生成节点] Excel 附件已生成，下载路径: " + downloadUrl);
        } catch (IOException e) {
            System.err.println(">>> Excel 生成失败: " + e.getMessage());
        }

        // ==========================================
        // 2. 让大模型生成 Markdown 文本报告
        // ==========================================
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

        // ==========================================
        // 3. 把 Excel 下载链接巧妙地拼接到 Markdown 结尾
        // ==========================================
        if (!downloadUrl.isEmpty()) {
            // 追加带有 Markdown 语法的超链接，前端 marked.js 会自动渲染成可点击的按钮
            answer += "\n\n---\n### 📎 报表附件\n[👉 点击此处下载完整的 Excel 数据分析报表](" + downloadUrl + ")";
        }

        // ==========================================
        // 4. 将最终合成的文本写入本地 .md 文件 (保留你原来的落盘逻辑)
        // ==========================================
        String fileName = "IFRS17_分析报告_" + System.currentTimeMillis() + ".md";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(answer);
            System.out.println(">>> [报告生成节点] Markdown 报告已保存为: " + fileName);
        } catch (IOException e) {
            System.err.println(">>> Markdown 落盘失败: " + e.getMessage());
        }

        // 5. 返回带链接的最终回答
        return Map.of("answer", answer);
    }
}