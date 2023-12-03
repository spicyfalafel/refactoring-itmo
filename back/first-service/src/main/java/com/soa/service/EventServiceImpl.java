package com.soa.service;

import com.soa.mapper.EventModelMapper;
import com.soa.model.events.Event;
import com.soa.model.CreateEventRequest;
import com.soa.model.events.EventDto;
import com.soa.model.events.TypesDto;
import com.soa.model.events.EventType;
import com.soa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import com.soa.exception.ErrorDescriptions;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final EventModelMapper eventModelMapper;

    @Override
    public EventDto createEvent(CreateEventRequest request) {
        Event event = eventModelMapper.map(request);
        eventRepository.save(event);
        return eventModelMapper.map(event);
    }


    private Stream<Event> filterEvents(Stream<Event> eventsStream, List<FilterCriteria> filterBy) {
        for (var f :filterBy){
            if (f.getKey().equals("date")){
                eventsStream = switch (f.getOperation()) {
                    case "eq" -> eventsStream.filter(event -> event.getDate().equals((Date) f.getValue()));
                    case "ne" -> eventsStream.filter(event -> !event.getDate().equals((Date) f.getValue()));
                    case "gt" -> eventsStream.filter(event -> event.getDate().before((Date) f.getValue()));
                    default -> eventsStream.filter(event -> event.getDate().after((Date) f.getValue()));
                };
            } else if (f.getKey().equals("eventType")){
                eventsStream = switch (f.getOperation()) {
                    case "eq" -> eventsStream.filter(event -> event.getEventType().equals(f.getValue()));
                    case "ne" -> eventsStream.filter(event -> !event.getEventType().equals(f.getValue()));
                    case "gt" ->
                            eventsStream.filter(event -> (event.getEventType().compareTo((EventType) f.getValue()) < 0));
                    default ->
                            eventsStream.filter(event -> (event.getEventType().compareTo((EventType) f.getValue()) > 0));
                };
            }
        }
        return eventsStream;
    }

    private Stream<Event> sortStream(Stream<Event> eventsStream, List<SortCriteria> sortBy) {
        if (sortBy != null && sortBy.size() != 0) {
            Comparator<Event> c = null;
            for (SortCriteria sortCriteria : sortBy) {
                Comparator<Event> currentComp;
                var desc = !sortCriteria.getAscending();
                switch (sortCriteria.getKey()) {
                    case "id" -> currentComp = Comparator.comparing(Event::getId);
                    case "name" -> currentComp = Comparator.comparing(Event::getName);
                    case "date" -> currentComp = Comparator.comparing(Event::getDate);
                    case "minAge" -> currentComp = Comparator.comparing(Event::getMinAge);
                    default -> throw ErrorDescriptions.INCORRECT_SORT.exception();
                }
                if (desc) currentComp = currentComp.reversed();
                if (c == null) {
                    c = currentComp;
                } else {
                    c = c.thenComparing(currentComp);
                }
            }
            if (c != null) eventsStream = eventsStream.sorted(c);
        }
        return eventsStream;
    }

    @Override
    public List<EventDto> getAllEvents(List<FilterCriteria> filterBy, List<SortCriteria> sortBy, Long limit, Long offset) throws Exception {
        try {
            EventSpecification spec = new EventSpecification(filterBy);
            var eventsStream = eventRepository.findAll(spec).stream();
            eventsStream = filterEvents(eventsStream, filterBy);
            eventsStream = sortStream(eventsStream, sortBy);

            return eventsStream
                    .skip(offset)
                    .limit(limit)
                    .map(eventModelMapper::map)
                    .collect(Collectors.toList());
        } catch (
            InvalidDataAccessApiUsageException exc){
                throw new Exception("В фильтре передано недопустимое значение для сравнения");
        }
    }

    @Override
    public EventDto getEventById(Long eventId) {
        if (!eventRepository.existsById(eventId)){
            throw ErrorDescriptions.EVENT_NOT_FOUND.exception();
        }
        Event event = eventRepository.getById(eventId);
        return eventModelMapper.map(event);
    }

    @Override
    @Transactional
    public void deleteEventById(Long eventId) {
        if (!eventRepository.existsById(eventId)){
            throw ErrorDescriptions.EVENT_NOT_FOUND.exception();
        }
        ticketRepository.deleteAllByEventId(eventId);
        eventRepository.deleteById(eventId);
    }

    @Override
    public EventDto updateEventById(Long eventId, CreateEventRequest request) {
        if (!eventRepository.existsById(eventId)) {
            throw ErrorDescriptions.EVENT_NOT_FOUND.exception();
        }
        Event updatedEvent = eventModelMapper.map(request);
        updatedEvent.setId(eventId);
        eventRepository.save(updatedEvent);
        return eventModelMapper.map(updatedEvent);
    }

    @Override
    public long countEvents() {
        return eventRepository.count();
    }

    @Override
    public TypesDto getTypes() {
        var res = new ArrayList<Map<String, String>>();
        var typesDto = new TypesDto();
        for (var type : EventType.values()) {
            HashMap<String, String> a = new HashMap<>();
            a.put("value", type.name());
            a.put("desc", type.toString());
            res.add(a);
        }
        typesDto.setTypes(res);
        return typesDto;
    }
}
