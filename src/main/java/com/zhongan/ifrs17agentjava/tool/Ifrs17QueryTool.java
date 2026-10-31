package com.zhongan.ifrs17agentjava.tool;

import com.zhongan.ifrs17agentjava.entity.LedgerDetail;
import com.zhongan.ifrs17agentjava.entity.MetricDict;
import com.zhongan.ifrs17agentjava.repository.LedgerRepository;
import com.zhongan.ifrs17agentjava.repository.MetricDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor // 加上这个，用构造器注入
public class Ifrs17QueryTool {

    private final LedgerRepository ledgerRepository; // 注入数据库操作

    @Tool(description = "查询指定机构下，IFRS17指定科目的新旧账套差异明细")
    public String queryLedgerDiff(
            @ToolParam(description = "机构代码，如 3101代表上海，4403代表深圳") String orgCode,
            @ToolParam(description = "科目代码，如 RESERVE 代表准备金") String subjectCode) {

        System.out.println(">>> [工具被调用] 正在穿透数据库查询：orgCode=" + orgCode + ", subjectCode=" + subjectCode);

        // 真正去查数据库
        List<LedgerDetail> details = ledgerRepository.findByOrgCodeAndSubjectCode(orgCode, subjectCode);

        if (details.isEmpty()) {
            return "未查询到该机构的科目数据。";
        }

        // 组装结果返回给大模型
        StringBuilder sb = new StringBuilder("查询结果：\n");
        for (LedgerDetail d : details) {
            sb.append(String.format("账套类型：%s, 金额：%.2f\n", d.getLedgerType(), d.getAmount()));
        }
        return sb.toString();
    }

    @Autowired
    private MetricDictRepository metricDictRepository;

    @Tool(description = "查询财务指标的语义定义。当用户询问某个指标（如准备金余额）是怎么算的、口径是什么时调用此工具。")
    public String queryMetricDefinition(
            @ToolParam(description = "要查询的指标名称，如：准备金余额") String metricName) {

        System.out.println(">>> [MCP 工具调用] 正在查询语义层指标定义：" + metricName);

        MetricDict dict = metricDictRepository.findByMetricNameContaining(metricName);
        if (dict == null) {
            return "未在语义层找到该指标的定义。";
        }

        // 返回结构化的语义信息
        return String.format(
                "【语义层指标定义】\n- 指标名称：%s\n- 计算公式：%s\n- 来源表：%s\n- 默认过滤：%s",
                dict.getMetricName(), dict.getFormula(), dict.getSourceTable(), dict.getDefaultFilter()
        );
    }
}