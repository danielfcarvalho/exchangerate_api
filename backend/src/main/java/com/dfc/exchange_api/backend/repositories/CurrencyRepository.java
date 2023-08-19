package com.dfc.exchange_api.backend.repositories;

import com.dfc.exchange_api.backend.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);
    boolean existsByCode(String code);
}
