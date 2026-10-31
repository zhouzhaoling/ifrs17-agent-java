package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DiffAnalysisNode implements NodeAction<Ifrs17AgentState> {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [差异分析节点] 启动多线程并发收集证据...");

        // 并发任务 1：查明细库
        CompletableFuture<String> detailFuture = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "数据库结果：上海机构准备金旧账套 1000 万，新账套 1250 万。";
        }, executor);

        // 并发任务 2：查规则库
        CompletableFuture<String> ruleFuture = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "规则库结果：IFRS17规定，准备金折现率必须使用即期利率曲线。";
        }, executor);

        // 等待两个任务都完成
        CompletableFuture.allOf(detailFuture, ruleFuture).join();

        List<String> evidence = new ArrayList<>();
        try {
            evidence.add(detailFuture.get());
            evidence.add(ruleFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(">>> [差异分析节点] 证据收集完毕，共 " + evidence.size() + " 条");

        // 返回列表，LangGraph4j 会根据 SCHEMA 中的 appender 自动合并
        return Map.of("evidence_list", evidence);
    }
}