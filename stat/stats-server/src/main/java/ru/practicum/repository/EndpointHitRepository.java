package ru.practicum.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import ru.practicum.dto.TakeHitsDto;
import ru.practicum.dto.ReadEndpointHitDto;
import ru.practicum.model.EndpointHit;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
public class EndpointHitRepository {
    private final RowMapper<ReadEndpointHitDto> mapper;
    private final NamedParameterJdbcOperations jdbc;

    @Autowired
    public EndpointHitRepository(RowMapper<ReadEndpointHitDto> mapper, NamedParameterJdbcOperations jdbc) {
        this.mapper = mapper;
        this.jdbc = jdbc;
    }

    public void save(EndpointHit endpointHit) {
        String sql = "INSERT INTO endpoint_hit (app, uri, ip, timestamp) VALUES (:app, :uri, :ip, :timestamp)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("app", endpointHit.getApp());
        params.addValue("uri", endpointHit.getUri());
        params.addValue("ip", endpointHit.getIp());
        params.addValue("timestamp", Timestamp.valueOf(endpointHit.getTimestamp()));

        jdbc.update(sql, params);
    }

    public Collection<ReadEndpointHitDto> get(TakeHitsDto takeHitsDto) {
        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append("SELECT ");

        if (takeHitsDto.isUnique()) {
            sql.append(" app, uri, COUNT(distinct ip) as count, ip FROM endpoint_hit WHERE timestamp BETWEEN :start AND :end " +
                    "GROUP BY app, uri, ip");
        } else {
            sql.append(" app, uri, COUNT(id) as count, ip FROM endpoint_hit WHERE timestamp BETWEEN :start AND :end " +
                    "GROUP BY app, uri, ip");
        }

        params.addValue("start", takeHitsDto.getStart());
        params.addValue("end", takeHitsDto.getEnd());

        if (!CollectionUtils.isEmpty(takeHitsDto.getUris())) {
            List<String> uris = takeHitsDto.getUris();
            StringBuilder urisString = new StringBuilder(uris.getFirst());

            for (int i = 1; i < uris.size(); i++) {
                urisString.append(", ").append(uris.get(i));
            }

            params.addValue("uris", uris);
            sql.append(" HAVING uri IN (:uris)");
        }

        return jdbc.query(sql.toString(), params, mapper);
    }

    public void saveAll(List<EndpointHit> listHits) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO endpoint_hit (app, uri, ip, timestamp) VALUES ");

        int lastIndex = listHits.size() - 1;
        for (var i = 0; i < lastIndex; i++) {
            String val = "(:app" + i + ", :uri" + i + ", :ip" + i + ", :timestamp" + i + "), ";
            sql.append(val);
        }

        sql.append("(:app" + lastIndex + ", :uri" + lastIndex + ", :ip" + lastIndex + ", :timestamp" + lastIndex + ");");

        MapSqlParameterSource params = new MapSqlParameterSource();

        for (var i = 0; i <= lastIndex; i++) {
            params.addValue("app" + i, listHits.get(i).getApp());
            params.addValue("uri" + i, listHits.get(i).getUri());
            params.addValue("ip" + i, listHits.get(i).getIp());
            params.addValue("timestamp" + i, Timestamp.valueOf(listHits.get(i).getTimestamp()));
        }

        jdbc.update(sql.toString(), params);
    }

}