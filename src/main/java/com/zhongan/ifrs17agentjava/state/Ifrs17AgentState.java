package com.zhongan.ifrs17agentjava.state;

import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;

public class Ifrs17AgentState extends AgentState {

    public Ifrs17AgentState(Map<String, Object> initData) {
        super(initData);
    }

    public String question() { return (String) value("question").orElse(""); }

    public String answer() { return (String) value("answer").orElse(""); }

    // 新增：用来判断走哪条分支
    public String intent() { return (String) value("intent").orElse(""); }

    // 新增：存储查到的中间结果
    public String rules() { return (String) value("rules").orElse(""); }

    public String details() { return (String) value("details").orElse(""); }

    // 新增：读取长对话历史记忆
    public String history() {
        return (String) value("history").orElse("");
    }
}