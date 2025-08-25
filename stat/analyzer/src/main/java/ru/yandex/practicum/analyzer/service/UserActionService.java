package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.model.EventSums;
import ru.yandex.practicum.enums.ActionType;
import ru.yandex.practicum.analyzer.model.interaction.Interaction;
import ru.yandex.practicum.analyzer.model.interaction.InteractionId;
import ru.yandex.practicum.analyzer.repository.EventSumsRepository;
import ru.yandex.practicum.analyzer.repository.InteractionRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserActionService {

    private final InteractionRepository interactionRepository;
    private final EventSumsRepository eventSumsRepository;

    public void addUserAction(UserActionAvro userActionAvro) {
        InteractionId thisId = InteractionId.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .build();
        double weight = ActionType.fromAvro(userActionAvro.getActionType()).getWeight();
        interactionRepository.findById(thisId)
                .map(existing -> {
                    double oldWeight = existing.getWeight();
                    if (weight > oldWeight) {
                        existing.setWeight(weight);
                        interactionRepository.save(existing);

                        // обновляем сумму на разницу
                        double delta = weight - oldWeight;
                        eventSumsRepository.findById(userActionAvro.getEventId())
                                .ifPresent(eventSum -> {
                                    eventSum.setScore(eventSum.getScore() + delta);
                                    eventSumsRepository.save(eventSum);
                                });
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Interaction newInteraction = Interaction.builder()
                            .id(thisId)
                            .weight(weight)
                            .build();
                    Interaction saved = interactionRepository.save(newInteraction);

                    // новая запись в суммах
                    eventSumsRepository.findById(userActionAvro.getEventId())
                            .map(eventSum -> {
                                eventSum.setScore(eventSum.getScore() + weight);
                                return eventSumsRepository.save(eventSum);
                            })
                            .orElseGet(() -> eventSumsRepository.save(EventSums.builder()
                                    .eventId(userActionAvro.getEventId())
                                    .score(weight)
                                    .build()));
                    return saved;
                });
    }

    @Transactional(readOnly = true)
    public List<Interaction> getUsersInteraction(long userId) {
        return interactionRepository.findByIdUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<EventSums> getEventSums(List<Long> ids) {
        return eventSumsRepository.findAllById(ids);
    }
}
