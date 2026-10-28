package com.zhongan.ifrs17agentjava.node;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
// 👇 注意：包路径已变为 advisor.vectorstore
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RagQueryNode implements NodeAction<Ifrs17AgentState> {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    @Override
    public Map<String, Object> apply(Ifrs17AgentState state) {
        System.out.println(">>> [RAG节点] 正在检索知识库并生成回答...");

        // 👇 关键：使用 Builder 模式创建 Advisor，而不是 new QuestionAnswerAdvisor(vectorStore)
        QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore).build();

        String answer = chatClientBuilder.build().prompt()
                .system("你是一个IFRS17业务专家。请严格根据提供的上下文回答用户问题，如果上下文中没有，请回答不知道。")
                .user(state.question())
                .advisors(ragAdvisor) // 挂载 RAG Advisor
                .call()
                .content();

        return Map.of("answer", answer);
    }
}