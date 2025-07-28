package ru.practicum.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.practicum.dto.ReadEndpointHitDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EndpointHitMapper implements RowMapper<ReadEndpointHitDto> {

    @Override
    public ReadEndpointHitDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReadEndpointHitDto readEndpointHitDto = new ReadEndpointHitDto();
        readEndpointHitDto.setApp(rs.getString("app"));
        readEndpointHitDto.setUri(rs.getString("uri"));
        readEndpointHitDto.setHits(rs.getInt("count"));

        return readEndpointHitDto;
    }
}
