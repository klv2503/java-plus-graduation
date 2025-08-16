package ru.yandex.practicum.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.enums.ParticipationRequestStatus;
import ru.yandex.practicum.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long>, QuerydslPredicateExecutor<ParticipationRequest> {

    List<ParticipationRequest> findByUserId(Long userId);

    boolean existsByUserIdAndEvent(Long userId, Long eventId);

    Optional<ParticipationRequest> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.status = :status AND pr.event = :eventId")
    int countConfirmedRequestsByStatusAndEvent(@Param("status") ParticipationRequestStatus status, @Param("eventId") Long eventId);

    List<ParticipationRequest> findByEvent(Long event);

    @Modifying
    @Transactional
    @Query("UPDATE ParticipationRequest pr SET pr.status = :status WHERE pr.id IN :ids")
    void updateStatusByIds(@Param("status") ParticipationRequestStatus status, @Param("ids") List<Long> ids);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.id IN :ids")
    List<ParticipationRequest> findByIds(@Param("ids") List<Long> ids);

    @Query("SELECT pr.event, COUNT(pr) FROM ParticipationRequest pr WHERE pr.event IN :eventIds GROUP BY pr.event")
    List<Object[]> countByEventIds(@Param("eventIds") List<Long> eventIds);
}
