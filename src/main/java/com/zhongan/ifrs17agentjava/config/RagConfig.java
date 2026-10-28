package com.zhongan.ifrs17agentjava.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RagConfig {

    // 初始化一个内存向量库，并把知识塞进去
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 模拟我们从 PDF/Word 中解析出来的文本块
        List<Document> documents = List.of(
                new Document("UAT报错E1001：准备金计算溢出。原因：底层折现率配置为空。解决建议：请前往系统参数配置页，补充该机构当月的折现率。"),
                new Document("IFRS17合同分组规则：具有相同风险特征和盈利特征的保单应分入同一合同组。亏损合同必须单独分组。"),
                new Document("费用分摊说明：费用分摊采用三级漏斗模型，依次按照机构、产品、险种进行分摊归集。")
        );

        // 将文档向量化并存入内存库
        vectorStore.add(documents);
        System.out.println(">>> [RAG 初始化] 知识库文档已向量化并加载完毕！");
        return vectorStore;
    }
}