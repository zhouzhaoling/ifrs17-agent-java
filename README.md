# IFRS17 智能财务分析与运营 Agent 平台 (Java版)

## 📖 项目背景
本项目是一个基于 Spring Boot 3.4 + Spring AI + LangGraph4j 构建的企业级单 Agent 后端原型。旨在验证将大模型能力（意图识别、工具调用、状态机编排、RAG 检索）接入传统财务核算（IFRS17）场景的可行性。用于个人架构验证与技术演示。

## 🛠️ 技术栈
- **后端框架**: Spring Boot 3.4.x, Java 21
- **AI 框架**: Spring AI 1.1.x
- **Agent 编排**: LangGraph4j 1.8.x
- **大模型服务**: Ollama 本地私有化部署 (Qwen2.5-7B / nomic-embed-text)
- **接口协议**: RESTful API (HTTP)

## 🏗️ 核心架构设计
1. **状态机编排 (LangGraph)**: 将大模型调用封装为 Node 节点，定义全局 State，告别简单的 API 直调。
2. **意图路由 (Router)**: 通过强约束的 Prompt，实现 DIFF（差异分析）和 KNOWLEDGE（知识问答）的智能分发。
3. **工具调用 (Tool Calling)**: 基于 `@Tool` 注解，将底层 IFRS17 查库逻辑封装为原子工具。大模型隐式判断后，自动提取参数查库。
4. **检索增强生成 (RAG)**: 挂载 `QuestionAnswerAdvisor`，将企业内部规则和 UAT 文档进行向量化检索，有效抑制大模型幻觉。

## 🚀 快速启动
1. 本地安装 Ollama 并拉取模型: `ollama run qwen2.5:7b` 和 `ollama pull nomic-embed-text`
2. 启动 Spring Boot 应用。
3. 访问测试接口: 
   `GET http://localhost:8080/api/agent/chat?question=帮我查一下上海机构准备金的账套差异`

## 📝 演进规划 (TODO)
- [ ] 引入 Checkpointer 将 State 存入 Redis，支持分布式断点续跑。
- [ ] 接入 MCP (Model Context Protocol) 网关，将本地 Tool 升级为远程微服务。
- [ ] 实现 SSE 流式输出，防止网关超时。


## 🐳 快速部署 (Docker 一键启动)
本项目已实现全容器化部署。无需配置本地 Maven 环境，只需在宿主机开启 Ollama，执行以下命令即可一键拉起 Spring Boot 应用与 Redis 服务：

```bash
# 1. 本地打包 (需具备 Maven 和 JDK 21)
mvn clean package -DskipTests

# 2. 一键启动容器
docker-compose up -d --build
