package ru.yandex.practicum.eureka;

import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class EurekaRegistrationLogger {

    private static final Logger log = LoggerFactory.getLogger(EurekaRegistrationLogger.class);

    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        InstanceInfo info = event.getInstanceInfo();

        String clientIp = "unknown";
        //Комментарий себе для памяти.
        //Одновременно проверяем, что атрибуты запроса не null и что они принадлежат к типу ServletRequestAttributes.
        //Если условие выполнено, переменная attrs автоматически приводится к этому типу и доступна только внутри {}.
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            clientIp = attrs.getRequest().getRemoteAddr();
        }

        log.warn("Eureka registration: appName={}, instanceId={}, status={}, from IP={}",
                info.getAppName(),
                info.getInstanceId(),
                info.getStatus(),
                clientIp);
    }
}
