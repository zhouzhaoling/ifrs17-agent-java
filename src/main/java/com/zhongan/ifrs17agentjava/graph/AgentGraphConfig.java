package com.zhongan.ifrs17agentjava.graph;

import com.zhongan.ifrs17agentjava.node.DiffAnalysisNode;
import com.zhongan.ifrs17agentjava.node.LlmChatNode;
import com.zhongan.ifrs17agentjava.node.RouterNode;
import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

import com.zhongan.ifrs17agentjava.node.RagQueryNode;

@Configuration
@RequiredArgsConstructor
public class AgentGraphConfig {

    private final LlmChatNode llmChatNode;
    private final RouterNode routerNode;
    private final DiffAnalysisNode diffAnalysisNode;
    private final RagQueryNode ragQueryNode; // 👈 注入 RAG 节点

    @Bean
    public CompiledGraph<Ifrs17AgentState> simpleChatGraph() throws Exception {
        return new StateGraph<>(Ifrs17AgentState::new)
                .addNode("router", node_async(routerNode))
                .addNode("chat", node_async(llmChatNode))
                .addNode("diff_analysis", node_async(diffAnalysisNode))
                .addNode("rag_query", node_async(ragQueryNode)) // 👈 加入图

                .addEdge(START, "router")

                .addConditionalEdges(
                        "router",
                        edge_async(state -> state.intent()),
                        Map.of(
                                "CHAT", "chat",
                                "DIFF", "diff_analysis",
                                "KNOWLEDGE", "rag_query" // 👈 新增路由分支
                        )
                )

                .addEdge("chat", END)
                .addEdge("diff_analysis", END)
                .addEdge("rag_query", END) // 👈 RAG 跑完直接结束
                .compile();
    }
}