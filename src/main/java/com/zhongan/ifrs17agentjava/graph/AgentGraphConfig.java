package com.zhongan.ifrs17agentjava.graph;

import com.zhongan.ifrs17agentjava.node.DiffAnalysisNode;
import com.zhongan.ifrs17agentjava.node.LlmChatNode;
import com.zhongan.ifrs17agentjava.node.RagQueryNode;
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
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Configuration
@RequiredArgsConstructor
public class AgentGraphConfig {

    private final RouterNode routerNode;
    private final LlmChatNode llmChatNode;
    private final DiffAnalysisNode diffAnalysisNode;
    private final RagQueryNode ragQueryNode;

    @Bean
    public CompiledGraph<Ifrs17AgentState> simpleChatGraph() throws Exception {
        // 👇 这里显式加上泛型 <Ifrs17AgentState> 解决编译报错
        return new StateGraph<Ifrs17AgentState>(Ifrs17AgentState.SCHEMA, Ifrs17AgentState::new)
                .addNode("router", node_async(routerNode))
                .addNode("chat", node_async(llmChatNode))
                .addNode("diff_analysis", node_async(diffAnalysisNode))
                .addNode("rag_query", node_async(ragQueryNode))

                .addEdge(START, "router")

                .addConditionalEdges(
                        "router",
                        edge_async(state -> state.intent()),
                        Map.of(
                                "CHAT", "chat",
                                "DIFF", "diff_analysis",
                                "KNOWLEDGE", "rag_query"
                        )
                )

                // 👇 核心修复：差异分析并发查完证据后，必须走向 chat 汇总，再结束！
                .addEdge("diff_analysis", "chat")
                // 👇 RAG 查完直接结束（或者如果要汇总，也可以指向 "chat"）
                .addEdge("rag_query", END)
                .addEdge("chat", END)

                .compile();
    }
}