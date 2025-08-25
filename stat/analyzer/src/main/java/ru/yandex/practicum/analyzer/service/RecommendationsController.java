package ru.yandex.practicum.analyzer.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.analyzer.model.EventSums;
import stats.service.dashboard.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final EventSimilarityService similarityService;
    private final UserActionService userActionService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            List<RecommendedEventProto> recommendations = similarityService
                    .getTopRecommendationsForUser(userId, 0L, maxResults); // eventId=0 для общих рекомендаций

            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.debug("Sent {} recommendations for user {}", recommendations.size(), userId);

        } catch (Exception e) {
            log.error("Failed to get recommendations for user {}", request.getUserId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            long eventId = request.getEventId();
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            List<RecommendedEventProto> recommendations = similarityService
                    .getTopRecommendationsForUser(userId, eventId, maxResults);

            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.debug("Sent {} similar events for event {}", recommendations.size(), eventId);

        } catch (Exception e) {
            log.error("Failed to get similar events for event {}", request.getEventId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<Long> eventIds = request.getEventIdList();

            // достаём суммы из БД
            List<EventSums> sums = userActionService.getEventSums(eventIds);
            Map<Long, Double> scoreMap = sums.stream()
                    .collect(Collectors.toMap(EventSums::getEventId, EventSums::getScore));
            for (Long eventId : eventIds) {
                double score = scoreMap.getOrDefault(eventId, 0.0);

                RecommendedEventProto response = RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(score)
                        .build();

                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();
            log.debug("Sent interaction counts for {} events", sums.size());

        } catch (Exception e) {
            log.error("Failed to get interaction counts", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}