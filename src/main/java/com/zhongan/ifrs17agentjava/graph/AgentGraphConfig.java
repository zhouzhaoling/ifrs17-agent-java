package com.zhongan.ifrs17agentjava.graph;

import com.zhongan.ifrs17agentjava.node.*;
import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Configuration
@RequiredArgsConstructor
public class AgentGraphConfig {

    private final RouterNode routerNode;
    private final LlmChatNode llmChatNode;
    private final DiffAnalysisNode diffAnalysisNode;
    private final RagQueryNode ragQueryNode;
    private final MetricQueryNode metricQueryNode; // 👈 第二战役新增的语义层节点

    @Bean
    public CompiledGraph<Ifrs17AgentState> simpleChatGraph() throws Exception {
        return new StateGraph<Ifrs17AgentState>(Ifrs17AgentState.SCHEMA, Ifrs17AgentState::new)
                // 注册所有节点
                .addNode("router", node_async(routerNode))
                .addNode("chat", node_async(llmChatNode))
                .addNode("diff_analysis", node_async(diffAnalysisNode))
                .addNode("rag_query", node_async(ragQueryNode))
                .addNode("metric_query", node_async(metricQueryNode))

                .addEdge(START, "router")

                // 👇 条件路由（完美映射四大意图）
                .addConditionalEdges(
                        "router",
                        edge_async(Ifrs17AgentState::intent),
                        Map.of(
                                "CHAT", "chat",
                                "DIFF", "diff_analysis",
                                "KNOWLEDGE", "rag_query",
                                "METRIC", "metric_query"
                        )
                )

                // 👇 汇总与结束边
                .addEdge("diff_analysis", "chat") // 差异分析收集完证据，走向大模型汇总
                .addEdge("rag_query", END)        // RAG 查询完直接返回
                .addEdge("metric_query", END)     // 语义层查询完直接返回
                .addEdge("chat", END)

                .compile();
    }
}