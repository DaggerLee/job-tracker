package com.jobtracker.repository;

import com.jobtracker.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStatusOrderByCreatedAtDesc(String status);
    List<Application> findAllByOrderByCreatedAtDesc();
}