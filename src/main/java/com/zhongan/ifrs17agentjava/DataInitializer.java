package com.zhongan.ifrs17agentjava;

import com.zhongan.ifrs17agentjava.entity.LedgerDetail;
import com.zhongan.ifrs17agentjava.entity.MetricDict;
import com.zhongan.ifrs17agentjava.repository.LedgerRepository;
import com.zhongan.ifrs17agentjava.repository.MetricDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LedgerRepository repository;
    private final MetricDictRepository metricDictRepository;

    @Override
    public void run(String... args) {
        // 模拟上海机构（3101）的准备金科目（RESERVE）在新旧账套下的数据
        repository.save(new LedgerDetail(null, "3101", "RESERVE", "OLD", 10000000.0)); // 旧账套1000万
        repository.save(new LedgerDetail(null, "3101", "RESERVE", "NEW", 12500000.0)); // 新账套1250万

        // 模拟深圳机构（4403）的数据
        repository.save(new LedgerDetail(null, "4403", "RESERVE", "OLD", 8000000.0));
        repository.save(new LedgerDetail(null, "4403", "RESERVE", "NEW", 8200000.0));

        System.out.println(">>> [数据库初始化] 账务明细数据已灌入 H2 数据库！");

        // 注入语义层指标字典
        MetricDict metric = new MetricDict(null, "准备金余额", "SUM(closing_balance)", "dws_ifrs17_reserve", "reserve_type = 'NET'");
        metricDictRepository.save(metric);
        System.out.println(">>> [数据库初始化] 语义层指标字典已灌入！");
    }
}