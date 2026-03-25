package com.ra.batshop.repository;

import com.ra.batshop.model.User;
import com.ra.batshop.model.UserVoucher;
import com.ra.batshop.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);
}
