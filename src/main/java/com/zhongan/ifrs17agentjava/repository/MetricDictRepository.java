package com.zhongan.ifrs17agentjava.repository;

import com.zhongan.ifrs17agentjava.entity.MetricDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricDictRepository extends JpaRepository<MetricDict, Long> {
    // 根据指标名称模糊查询（用于语义匹配）
    MetricDict findByMetricNameContaining(String keyword);
}