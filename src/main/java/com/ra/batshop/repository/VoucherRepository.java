package com.ra.batshop.repository;

import com.ra.batshop.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByCodeContainingIgnoreCase(String keyword);
}
