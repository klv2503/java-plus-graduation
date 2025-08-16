package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.practicum.clients.EventServiceFeign;
import ru.yandex.practicum.clients.UserServiceFeign;

@SpringBootTest
class StatServerTest {

    @MockBean
    EventServiceFeign eventServiceFeign;

    @MockBean
    UserServiceFeign userServiceFeign;

    @Test
    void contextLoads() {
    }

}
