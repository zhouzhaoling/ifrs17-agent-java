package com.zhongan.ifrs17agentjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zhongan.ifrs17agentjava.entity.LedgerDetail;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerDetail, Long> {
    // Spring Data JPA 会自动帮你生成 SQL，按机构和科目查账套明细
    List<LedgerDetail> findByOrgCodeAndSubjectCode(String orgCode, String subjectCode);
}