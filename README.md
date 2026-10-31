# IFRS17 智能财务分析与运营 Agent 平台 (Java版)

## 📖 项目背景
本项目是一个基于 Spring Boot 3.4 + Spring AI + LangGraph4j 构建的企业级多智能体（Multi-Agent）后端原型。旨在验证将大模型能力（意图识别、工具调用、状态机编排、RAG 检索、语义层防幻觉）接入传统财务核算（IFRS17）场景的可行性，具备高并发、高可用与强安全隔离特性。并配备了完整的可视化前端工作台。

## 🛠️ 技术栈
- **后端框架**: Spring Boot 3.4.x, Java 21
- **AI 框架**: Spring AI 1.1.x
- **Agent 编排**: LangGraph4j 1.8.x
- **大模型服务**: Ollama 本地私有化部署 (Qwen2.5-7B / nomic-embed-text)
- **数据与中间件**: Redis, H2/MySQL, Redisson, MyBatis
- **前端交互**: 原生 HTML + Tailwind CSS + marked.js
- **部署交付**: Docker, Docker Compose

## 🏗️ 核心架构设计
1. **复合技能路由 (Multi-Skill Router)**: 通过强约束的 Prompt，将用户意图精准分发至 `DIFF`（差异分析）、`KNOWLEDGE`（知识检索）、`METRIC`（指标查询）、`REPORT`（报告生成）四大独立分支。
2. **多源证据并发与 Reducer**: 在差异分析节点中，使用 `CompletableFuture` 多线程并发查库与查规则；利用 LangGraph 的 `Channel.appender` 实现全局 State 的安全合并，杜绝并发覆盖。
3. **语义层与指标防幻觉**: 大模型不直接生成物理表 SQL。通过语义层字典表与 `@Tool` 工具调用，将业务公式、来源表固化，确保口径唯一。
4. **企业级数据权限隔离**: 在 MCP 网关层自研 MyBatis `DataScopeInterceptor`，根据当前用户上下文自动拦截并拼接行级数据权限（`AND org_code = 'xxx'`），防止越权查询。
5. **检索增强生成 (RAG)**: 挂载 `QuestionAnswerAdvisor`，结合本地向量库，将企业规则和 UAT 文档向量化召回，配合引用来源抑制幻觉。
6. **高可用防护**: 基于 Redis 实现五层防护（INCR 限流、Redisson 防重、结果缓存、双检锁、Hash 状态存储），并集成 SSE 流式输出与全局异常降级。

## 🚀 快速启动
1. 本地安装 Ollama 并拉取模型: `ollama run qwen2.5:7b` 和 `ollama pull nomic-embed-text`
2. 启动 Spring Boot 应用。
3. 打开浏览器，访问可视化工作台：
   👉 **`http://localhost:8080/index.html`**

## 🎬 界面操作指南 (Demo)

进入工作台后，您可以直接在底部输入框提问，或点击左侧的**“技能快捷指令”**体验四大核心场景：

1. **🔴 差异排查 (DIFF)**
   - **操作**：点击【差异排查】按钮。
   - **表现**：系统并发查询账务明细与核算规则，收集完整证据链后，交由大模型输出结构化的差异归因报告。
2. **🟢 指标查询 (METRIC)**
   - **操作**：点击【指标查询】按钮。
   - **表现**：系统路由至语义层，调用专属工具获取口径公式与默认过滤条件，避免模型胡编乱造。
3. **🟡 知识问答 (KNOWLEDGE)**
   - **操作**：点击【知识问答】按钮。
   - **表现**：通过 RAG 向量检索从本地知识库召回 UAT 报错原因与解决方案，并结合出处生成回答。
4. **🟣 报告生成 (REPORT)**
   - **操作**：点击【报告生成】按钮。
   - **表现**：强约束 Prompt 生成四大财务模块的长文本报告，前端界面将 Markdown 渲染为精美的表格与报表。

## 🐳 一键部署 (Docker)
本项目已实现全链路容器化，无需配置本地 Maven，一键拉起应用与 Redis 服务：

```bash
# 1. 本地打包 (需具备 Maven 和 JDK 21)
mvn clean package -DskipTests

# 2. 一键启动容器
docker-compose up -d --build
```

## 📝 演进规划 (TODO)
- [ ] 引入 LangGraph Checkpointer 将 State 存入 Redis，实现真正的分布式断点续跑。
- [ ] 将本地 Tool 升级为独立部署的远程 MCP Server 集群。
- [ ] 实现 Word/PDF 格式的正式报告导出功能。
- [ ] 接入 Rerank 重排序模型，提升 RAG 召回精准度。