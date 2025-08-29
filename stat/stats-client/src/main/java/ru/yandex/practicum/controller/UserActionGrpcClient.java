package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;


import ru.yandex.practicum.dto.user.UserActionDto;
import ru.yandex.practicum.enums.ActionType;
import ru.yandex.practicum.grpc.stat.user_action.ActionTypeProto;
import ru.yandex.practicum.grpc.stat.user_action.UserActionControllerGrpc;
import ru.yandex.practicum.grpc.stat.user_action.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub blockingStub;

    public void sendUserAction(UserActionDto userActionDto) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userActionDto.getUserId())
                .setEventId(userActionDto.getEventId())
                .setActionType(actionToProto(userActionDto.getActionType()))
                .setTimestamp(timeToProto(userActionDto.getTimestamp()))
                .build();
        try {
            blockingStub.collectUserAction(request);
            log.debug("Sent UserAction to collector: {}", request);
        } catch (Exception e) {
            log.error("Failed to send UserAction to collector", e);
        }
    }

    public static ActionTypeProto actionToProto(ActionType type) {
        return switch (type) {
            case VIEW -> ActionTypeProto.ACTION_VIEW;
            case REGISTER -> ActionTypeProto.ACTION_REGISTER;
            case LIKE -> ActionTypeProto.ACTION_LIKE;
        };
    }

    public static com.google.protobuf.Timestamp timeToProto(Instant time) {
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(time.getEpochSecond())
                .setNanos(time.getNano())
                .build();
    }

}