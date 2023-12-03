package com.soa.service;


import com.soa.model.CreateEventRequest;
import com.soa.model.events.EventDto;
import com.soa.model.events.TypesDto;
import com.soa.repository.FilterCriteria;
import com.soa.repository.SortCriteria;

import java.util.List;

public interface EventService {
    EventDto createEvent(CreateEventRequest request);

    List<EventDto> getAllEvents(List<FilterCriteria> filterBy, List<SortCriteria> sortBy, Long limit, Long offset) throws Exception;

    EventDto getEventById(Long ticketId);

    void deleteEventById(Long eventId);

    EventDto updateEventById(Long eventId, CreateEventRequest request);

    long countEvents();

    TypesDto getTypes();
}
