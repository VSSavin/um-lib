package com.github.vssavin.umlib.domain.event;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author vssavin on 01.09.2023
 */
@Component
public class EventDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        return new Date(LocalDateTime.parse(source).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}
