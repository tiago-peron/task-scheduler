package com.tiagoperon.userservice.infrastructure.repository;


import com.tiagoperon.userservice.infrastructure.entity.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Long> {

}
