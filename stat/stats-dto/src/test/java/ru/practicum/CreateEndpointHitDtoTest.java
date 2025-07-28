package ru.practicum;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.dto.CreateEndpointHitDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateEndpointHitDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidDto_thenNoViolations() {
        CreateEndpointHitDto dto = new CreateEndpointHitDto("testApp", "/test", "192.168.1.1", LocalDateTime.now());
        Set<ConstraintViolation<CreateEndpointHitDto>> violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }

    @Test
    void whenAppIsBlank_thenValidationFails() {
        CreateEndpointHitDto dto = new CreateEndpointHitDto("", "/test", "192.168.1.1", LocalDateTime.now());
        Set<ConstraintViolation<CreateEndpointHitDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    void whenUriIsBlank_thenValidationFails() {
        CreateEndpointHitDto dto = new CreateEndpointHitDto("testApp", "", "192.168.1.1", LocalDateTime.now());
        Set<ConstraintViolation<CreateEndpointHitDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    void whenIpIsBlank_thenValidationFails() {
        CreateEndpointHitDto dto = new CreateEndpointHitDto("testApp", "/test", "", LocalDateTime.now());
        Set<ConstraintViolation<CreateEndpointHitDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    void whenTimestampIsNull_thenValidationFails() {
        CreateEndpointHitDto dto = new CreateEndpointHitDto("testApp", "/test", "192.168.1.1", null);
        Set<ConstraintViolation<CreateEndpointHitDto>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }
}