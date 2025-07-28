package ru.practicum.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.users.model.User;

import java.util.List;

@Repository
public interface AdminUserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    Page<User> findByIds(@Param("ids") List<Long> ids, Pageable pageable);
}