package com.fxtreasury.capture.repository;

import com.fxtreasury.capture.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {
}