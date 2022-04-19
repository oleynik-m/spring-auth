package com.example.dlg.repository;

import com.example.dlg.domain.People;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the People entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PeopleRepository extends JpaRepository<People, Long> {}
