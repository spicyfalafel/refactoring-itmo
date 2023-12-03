package com.soa.controller;

import com.soa.common.Endpoints;
import com.soa.common.Utils;
import com.soa.mapper.SortMapper;
import com.soa.model.CreateEventRequest;
import com.soa.model.events.EventDto;
import com.soa.model.events.TypesDto;
import com.soa.service.EventService;
import com.soa.validation.FilterEventValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping(value = Endpoints.CREATE_EVENT)
    public ResponseEntity<EventDto> createEvent(
            @RequestBody CreateEventRequest request
    ) {
        EventDto newEvent = eventService.createEvent(request);
        return new ResponseEntity<>(newEvent, HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_ALL_EVENTS)
    public ResponseEntity<List<EventDto>> getAllEvents(
            @RequestParam(value = "filter", required = false) String[] filter,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Long limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
    ) throws Exception  {
        Utils.validateLimitOffset(limit, offset);
        var filters = new FilterEventValidation().map(filter);
        var sorts = SortMapper.map(sort);
        List<EventDto> events = eventService.getAllEvents(filters, sorts, limit, offset);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_EVENT_BY_ID)
    public ResponseEntity<EventDto> getEventById(
            @PathVariable("eventId") Long eventId
    ) {
        return new ResponseEntity<>(eventService.getEventById(eventId), HttpStatus.OK);
    }

    @DeleteMapping(value = Endpoints.DELETE_EVENT_BY_ID)
    public ResponseEntity<Object> deleteEventById(
            @PathVariable("eventId") Long eventId
    ) {
        eventService.deleteEventById(eventId);
        return new ResponseEntity<>("deleted", HttpStatus.OK);
    }

    @PutMapping(value = Endpoints.UPDATE_EVENT_BY_ID)
    public ResponseEntity<Object> updateEventById(
            @PathVariable("eventId") Long eventId,
            @RequestBody CreateEventRequest request
    ) {
        var res = eventService.updateEventById(eventId, request);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_EVENTS_COUNT)
    public ResponseEntity<Long> countTickets() {
        return new ResponseEntity<>(eventService.countEvents(), HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_EVENTS_TYPES)
    public ResponseEntity<TypesDto> eventsTypes() {
        return new ResponseEntity<>(eventService.getTypes(), HttpStatus.OK);
    }
}
