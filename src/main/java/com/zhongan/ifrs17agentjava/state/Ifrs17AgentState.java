package com.zhongan.ifrs17agentjava.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ifrs17AgentState extends AgentState {

    // 👇 核心：定义 evidence_list 使用 appender 通道（自动追加合并）
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            "evidence_list", Channels.appender(ArrayList::new)
    );

    public Ifrs17AgentState(Map<String, Object> initData) {
        super(initData);
    }

    public String question() { return (String) value("question").orElse(""); }

    public String answer() { return (String) value("answer").orElse(""); }

    // 👇 就是少了的这个方法！RouterNode 会往 State 里写入 intent，这里必须能读取出来
    public String intent() { return (String) value("intent").orElse(""); }

    // 下面两个是你之前测试长对话时加的（如果不需要可以删掉，但建议保留）
    public String history() { return (String) value("history").orElse(""); }
    public String rules() { return (String) value("rules").orElse(""); }
    public String details() { return (String) value("details").orElse(""); }

    // 获取并发收集的证据列表
    @SuppressWarnings("unchecked")
    public List<String> evidenceList() {
        return (List<String>) value("evidence_list").orElse(new ArrayList<>());
    }
}