package com.zhongan.ifrs17agentjava.controller;

import com.zhongan.ifrs17agentjava.state.Ifrs17AgentState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final CompiledGraph<Ifrs17AgentState> simpleChatGraph;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    @GetMapping("/chat")
    public String chat(
            @RequestParam String question,
            @RequestParam(defaultValue = "user_001") String userId,
            @RequestParam(defaultValue = "session_001") String sessionId) throws Exception {

        String cleanQuestion = question.trim();
        System.out.println(">>> 收到请求 [" + userId + "]: " + cleanQuestion);

        // === 第一层：入口限流 (INCR) ===
        String limitKey = "limit:req:" + userId;
        Long count = redisTemplate.opsForValue().increment(limitKey);
        if (count != null && count == 1) redisTemplate.expire(limitKey, 1, TimeUnit.MINUTES);
        if (count != null && count > 10) return "❌ 请求太频繁，请稍后再试！";

        // === 第二层：防重提交锁 (Redisson) ===
        String lockKey = "lock:submit:" + userId + ":" + cleanQuestion.hashCode();
        RLock submitLock = redissonClient.getLock(lockKey);
        if (!submitLock.tryLock(0, 30, TimeUnit.SECONDS)) {
            return "❌ 问题正在处理中，请勿重复提交！";
        }

        try {
            // === 第三层：结果缓存 (带 sessionId 防串话) ===
            String cacheKey = "cache:answer:" + sessionId + ":" + cleanQuestion.hashCode();
            String cachedAnswer = redisTemplate.opsForValue().get(cacheKey);
            if (cachedAnswer != null) {
                return "✅ [缓存命中] " + cachedAnswer;
            }

            // === 第四层：防击穿锁 (双检锁) ===
            String cacheLockKey = "lock:cache:" + cacheKey;
            RLock cacheLock = redissonClient.getLock(cacheLockKey);
            if (!cacheLock.tryLock(2, 10, TimeUnit.SECONDS)) {
                return "⏳ 排队人数过多，请稍后刷新！";
            }

            try {
                // 双重检查
                cachedAnswer = redisTemplate.opsForValue().get(cacheKey);
                if (cachedAnswer != null) return "✅ [缓存命中] " + cachedAnswer;

                // === 第五层：读取工作流状态 (从 Hash 中读取历史对话) ===
                String stateHashKey = "agent:state:" + sessionId;
                // 👈 关键修改：使用 opsForHash().get() 读取指定 field
                String history = (String) redisTemplate.opsForHash().get(stateHashKey, "history");
                if (history == null) history = "";

                // 组装初始 State
                Map<String, Object> initState = new HashMap<>();
                initState.put("question", cleanQuestion);
                initState.put("history", history);

                // 执行状态图
                Optional<Ifrs17AgentState> result = simpleChatGraph.invoke(initState);
                String answer = result.map(Ifrs17AgentState::answer).orElse("Agent 执行失败");

                // 写回第三层：结果缓存
                redisTemplate.opsForValue().set(cacheKey, answer, 10, TimeUnit.MINUTES);

                // === 第五层：更新工作流状态 (Hash 字段级更新) ===
                // 截断保护，防止长对话 Token 爆炸
                String newHistory = history + "\n用户: " + cleanQuestion + "\nAI: " + answer;
                if (newHistory.length() > 1000) {
                    newHistory = newHistory.substring(newHistory.length() - 800);
                }

                Map<String, String> stateUpdates = new HashMap<>();
                stateUpdates.put("history", newHistory);
                stateUpdates.put("last_question", cleanQuestion);
                stateUpdates.put("status", "COMPLETED");

                // 👈 关键修改：使用 opsForHash().putAll() 批量更新 Hash 字段
                redisTemplate.opsForHash().putAll(stateHashKey, stateUpdates);
                // 每次对话重置 TTL 为 30 分钟
                redisTemplate.expire(stateHashKey, 30, TimeUnit.MINUTES);

                return answer;
            } finally {
                if (cacheLock.isLocked() && cacheLock.isHeldByCurrentThread()) cacheLock.unlock();
            }
        } finally {
            if (submitLock.isLocked() && submitLock.isHeldByCurrentThread()) submitLock.unlock();
        }
    }
}