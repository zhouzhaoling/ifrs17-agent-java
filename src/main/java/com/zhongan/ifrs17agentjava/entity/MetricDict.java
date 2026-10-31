package com.zhongan.ifrs17agentjava.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricDict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metricName;    // 指标名称，如：准备金余额
    private String formula;       // 计算公式，如：SUM(closing_balance)
    private String sourceTable;   // 来源物理表，如：dws_ifrs17_reserve
    private String defaultFilter; // 默认过滤条件，如：reserve_type = 'NET'
}