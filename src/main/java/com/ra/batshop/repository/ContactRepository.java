package com.ra.batshop.repository;

import com.ra.batshop.model.ContactSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactSupport, Integer> {
}