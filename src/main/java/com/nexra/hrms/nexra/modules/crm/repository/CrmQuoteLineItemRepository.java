package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmQuoteLineItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmQuoteLineItemRepository extends JpaRepository<CrmQuoteLineItemEntity, String> {

    List<CrmQuoteLineItemEntity> findAllByQuoteIdOrderByLineNoAsc(String quoteId);
}
