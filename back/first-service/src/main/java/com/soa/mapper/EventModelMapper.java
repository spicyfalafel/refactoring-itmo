package com.soa.mapper;

import com.soa.model.CreateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.soa.model.events.Event;
import com.soa.model.events.EventDto;

@Component
@RequiredArgsConstructor
public class EventModelMapper {
    public EventDto map(Event event) {
        EventDto dto = new EventDto();
        if (event !=null) {
            dto.setId(event.getId());
            dto.setName(event.getName());
            dto.setDate(event.getDate());
            dto.setMinAge(event.getMinAge());
            dto.setEventType(event.getEventType());
        }
        return dto;
    }

    public Event map(EventDto event) {
        Event dto = new Event();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDate(event.getDate());
        dto.setMinAge(event.getMinAge());
        dto.setEventType(event.getEventType());
        return dto;
    }

    public Event map(CreateEventRequest request) {
        Event event = new Event();
        event.setName(request.getName());
        event.setDate(request.getDate());
        event.setMinAge(request.getMinAge());
        event.setEventType(request.getEventType());
        return event;
    }
}
