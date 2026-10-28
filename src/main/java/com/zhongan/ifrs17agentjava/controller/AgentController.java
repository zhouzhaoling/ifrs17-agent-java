package com.zhongan.ifrs17agentjava.controller;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final CompiledGraph<Ifrs17AgentState> simpleChatGraph;

    @GetMapping("/chat")
    public String chat(@RequestParam String question) throws Exception {
        System.out.println(">>> 收到用户请求: " + question);

        // 初始化图的起点状态
        Map<String, Object> initState = Map.of("question", question);

        // 运行图（Agent 开始按节点流转）
        Optional<Ifrs17AgentState> result = simpleChatGraph.invoke(initState);

        // 返回最终 state 里的 answer 字段
        return result.map(Ifrs17AgentState::answer).orElse("Agent 执行失败");
    }
}