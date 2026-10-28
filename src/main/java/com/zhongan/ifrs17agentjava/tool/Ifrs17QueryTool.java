package com.zhongan.ifrs17agentjava.tool;

import com.zhongan.ifrs17agentjava.entity.LedgerDetail;
import com.zhongan.ifrs17agentjava.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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
}