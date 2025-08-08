package ru.yandex.practicum.eureka;

import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class EurekaRegistrationLogger {

    private static final Logger log = LoggerFactory.getLogger(EurekaRegistrationLogger.class);

    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        InstanceInfo info = event.getInstanceInfo();

        // Попробуем достать IP клиента из текущего запроса
        String clientIp = "unknown";
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            if (request != null) {
                clientIp = request.getRemoteAddr();
            }
        }

        log.warn("Eureka registration: appName={}, instanceId={}, status={}, from IP={}",
                info.getAppName(),
                info.getInstanceId(),
                info.getStatus(),
                clientIp);
    }
}
