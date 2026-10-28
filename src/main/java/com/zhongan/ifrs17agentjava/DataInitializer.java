package com.zhongan.ifrs17agentjava;

import com.zhongan.ifrs17agentjava.entity.LedgerDetail;
import com.zhongan.ifrs17agentjava.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LedgerRepository repository;

    @Override
    public void run(String... args) {
        // 模拟上海机构（3101）的准备金科目（RESERVE）在新旧账套下的数据
        repository.save(new LedgerDetail(null, "3101", "RESERVE", "OLD", 10000000.0)); // 旧账套1000万
        repository.save(new LedgerDetail(null, "3101", "RESERVE", "NEW", 12500000.0)); // 新账套1250万

        // 模拟深圳机构（4403）的数据
        repository.save(new LedgerDetail(null, "4403", "RESERVE", "OLD", 8000000.0));
        repository.save(new LedgerDetail(null, "4403", "RESERVE", "NEW", 8200000.0));

        System.out.println(">>> [数据库初始化] 账务明细数据已灌入 H2 数据库！");
    }
}