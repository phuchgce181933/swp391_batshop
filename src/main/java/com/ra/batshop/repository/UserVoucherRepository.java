package com.ra.batshop.repository;

import com.ra.batshop.model.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
}
