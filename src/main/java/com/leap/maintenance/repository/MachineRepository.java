package com.leap.maintenance.repository;

import com.leap.maintenance.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findByMachineCode(String machineCode);
    boolean existsByMachineCode(String machineCode);
    boolean existsByMachineCodeAndIdNot(String machineCode, Long id);
}
