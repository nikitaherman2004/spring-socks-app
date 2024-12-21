package com.socks.backend.repository;

import com.socks.backend.entity.Socks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocksRepository extends JpaRepository<Socks, Long>, JpaSpecificationExecutor<Socks> {

    Optional<Socks> findByColorAndCottonPercent(String color, Double cottonPercent);
}