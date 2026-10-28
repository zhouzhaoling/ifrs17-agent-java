package com.zhongan.ifrs17agentjava.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor; // 👈 加上这个
import lombok.Data;
import lombok.NoArgsConstructor;  // 👈 加上这个

@Entity
@Data
@AllArgsConstructor // 👈 加上这个，生成全参构造
@NoArgsConstructor  // 👈 加上这个，生成无参构造（JPA 规范要求必须有）
public class LedgerDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orgCode;
    private String subjectCode;
    private String ledgerType;
    private Double amount;
}