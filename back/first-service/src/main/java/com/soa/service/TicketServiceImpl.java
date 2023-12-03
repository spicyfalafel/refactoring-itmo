package com.soa.service;

import com.soa.exception.ErrorDescriptions;
import com.soa.mapper.TicketModelMapper;
import com.soa.model.*;
import com.soa.model.enums.TicketType;
import com.soa.model.events.Event;
import com.soa.model.events.EventDto;
import com.soa.model.tickets.Coordinates;
import com.soa.model.tickets.Ticket;
import com.soa.model.tickets.TicketDto;
import com.soa.model.tickets.TypesDto;
import com.soa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import com.soa.mapper.EventModelMapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    private final EventRepository eventRepository;

    private final EventService eventService;

    private final TicketModelMapper ticketModelMapper;

    private final EventModelMapper eventModelMapper;

    private void validateCreateTicketRequest(CreateTicketRequest request) {
        if (request.getRefundable() == null) {
            throw ErrorDescriptions.REFUNDABLE_MUST_PRESENT.exception();
        }

        if (request.getCoordinates() == null ||
                request.getCoordinates().getX() == null ||
                request.getCoordinates().getY() == null
        ) {
            throw ErrorDescriptions.COORDINATES_MUST_PRESENT.exception();
        }

        long MIN_TICKET_X = -686L;
        if (request.getCoordinates().getX() <= MIN_TICKET_X) {
            throw ErrorDescriptions.X_BAD.exception();
        }

        int MIN_TICKET_DISCOUNT = 1;
        int MAX_TICKET_DISCOUNT = 100;
        if (request.getDiscount() == null || request.getDiscount() < MIN_TICKET_DISCOUNT || request.getDiscount() > MAX_TICKET_DISCOUNT) {
            throw ErrorDescriptions.DISCOUNT_MUST_PRESENT.exception();
        }
    }

    private Event createEventFromTicketRequest(CreateTicketRequest request){
        EventDto newEvent = eventService.createEvent(CreateEventRequest.of(
                request.getEvent().getName(),
                request.getEvent().getDate(),
                request.getEvent().getMinAge(),
                request.getEvent().getEventType()
        ));
        var maybeEvent = eventRepository.findById(newEvent.getId());
        if (maybeEvent.isPresent()) {
            return maybeEvent.get();
        }
        throw ErrorDescriptions.EVENT_ALREADY_EXISTS.exception();
    }

    private Event getEventFromRequest(CreateTicketRequest request){
        if (request.getEvent() != null) {
            if (request.getEvent().getId() != null) {
                var maybeEvent = eventRepository.findById(request.getEvent().getId());
                if (maybeEvent.isPresent()) {
                    return maybeEvent.get();
                }
                throw ErrorDescriptions.EVENT_NOT_FOUND.exception();
            } else {  // создать новый
                return createEventFromTicketRequest(request);
            }
        }
        return null;
    }

    @Override
    public TicketDto createTicket(CreateTicketRequest request) {
        validateCreateTicketRequest(request);
        Ticket ticket = ticketModelMapper.map(request);
        TicketDto createdTicket = new TicketDto();
        Event event = getEventFromRequest(request);
        ticket.setEvent(event);
        createdTicket.setEvent(eventModelMapper.map(event));
        ticketRepository.save(ticket);
        createdTicket = ticketModelMapper.map(ticket);
        return createdTicket;
    }

    private Stream<Ticket> filterStream(Stream<Ticket> ticketsStream, List<FilterCriteria> filterBy){
        for (var f :filterBy){
            if (f.getKey().equals("creationDate")){
                switch (f.getOperation()) {
                    case "eq" ->
                            ticketsStream = ticketsStream.filter(ticket -> ticket.getCreationDate().equals((Date) f.getValue()));
                    case "ne" ->
                            ticketsStream = ticketsStream.filter(ticket -> !ticket.getCreationDate().equals((Date) f.getValue()));
                    case "gt" ->
                            ticketsStream = ticketsStream.filter(ticket -> ticket.getCreationDate().before((Date) f.getValue()));
                    default ->
                            ticketsStream = ticketsStream.filter(ticket -> ticket.getCreationDate().after((Date) f.getValue()));
                }
            } else if (f.getKey().equals("type")){
                switch (f.getOperation()) {
                    case "eq" -> ticketsStream = ticketsStream.filter(event -> event.getType().equals(f.getValue()));
                    case "ne" -> ticketsStream = ticketsStream.filter(event -> !event.getType().equals(f.getValue()));
                    case "gt" ->
                            ticketsStream = ticketsStream.filter(event -> (event.getType().compareTo((TicketType) f.getValue()) < 0));
                    default ->
                            ticketsStream = ticketsStream.filter(event -> (event.getType().compareTo((TicketType) f.getValue()) > 0));
                }
            }
        }
        return ticketsStream;
    }

    private Stream<Ticket> sortStream(Stream<Ticket> ticketsStream, List<SortCriteria> sortBy){
        if (sortBy != null && sortBy.size() != 0) {
            Comparator<Ticket> c = null;
            for (SortCriteria sortCriteria : sortBy) {
                Comparator<Ticket> currentComp;
                var desc = !sortCriteria.getAscending();
                switch (sortCriteria.getKey()) {
                    case "id" -> currentComp = Comparator.comparing(Ticket::getId);
                    case "name" -> currentComp = Comparator.comparing(Ticket::getName);
                    case "coordinateX" -> currentComp = Comparator.comparing(Ticket::getCoordinateX);
                    case "coordinateY" -> currentComp = Comparator.comparing(Ticket::getCoordinateY);
                    case "creationDate" -> currentComp = Comparator.comparing(Ticket::getCreationDate);
                    case "price" -> currentComp = Comparator.comparing(Ticket::getPrice);
                    case "discount" -> currentComp = Comparator.comparing(Ticket::getDiscount);
                    default -> throw ErrorDescriptions.INCORRECT_SORT.exception();
                }
                if (desc) currentComp = currentComp.reversed();
                if (c == null) {
                    c = currentComp;
                } else {
                    c = c.thenComparing(currentComp);
                }
            }
            if (c != null) ticketsStream = ticketsStream.sorted(c);
        }
        return ticketsStream;
    }
    @Override
    public List<TicketDto> getAllTickets(
            List<FilterCriteria> filterBy, List<SortCriteria> sortBy, Long limit, Long offset
    ) throws Exception {
        try {
            TicketSpecification spec = new TicketSpecification(filterBy);
            var ticketsStream = ticketRepository.findAll(spec).stream();
            ticketsStream = filterStream(ticketsStream, filterBy);
            ticketsStream = sortStream(ticketsStream, sortBy);
            return ticketsStream
                    .skip(offset)
                    .limit(limit)
                   .map(ticketModelMapper::map)
                   .collect(Collectors.toList());
        } catch (InvalidDataAccessApiUsageException exc){
            throw new Exception("В фильтре передано недопустимое значение для сравнения");
        }
    }

    @Override
    public TicketDto getTicketById(Long ticketId) {
        var maybeTicket = ticketRepository.findById(ticketId);
        if (maybeTicket.isPresent()){
            return ticketModelMapper.map(maybeTicket.get());
        }
        throw ErrorDescriptions.TICKET_NOT_FOUND.exception();
    }

    @Override
    public TicketDto newVipTicketById(Long ticketId) {
        var maybeTicket = ticketRepository.findById(ticketId);
        if (maybeTicket.isPresent()){
            var ticket = maybeTicket.get();
            TicketDto newVipTicket = ticketModelMapper.map(ticket);
            newVipTicket.setPrice(ticket.getPrice()*2);
            newVipTicket.setType(TicketType.VIP);
            return newVipTicket;
        }
        throw ErrorDescriptions.TICKET_NOT_FOUND.exception();
    }

    @Override
    public TicketDto newDiscountTicketById(Long ticketId, Double discount) {
        var maybeTicket = ticketRepository.findById(ticketId);
        if (maybeTicket.isPresent()) {
            var ticket = maybeTicket.get();
            return createTicket(CreateTicketRequest.of(
                    ticket.getName(),
                    Coordinates.of(ticket.getCoordinateX(), ticket.getCoordinateY()),
                    ticket.getPrice() * (1 - discount / 100.0),
                    discount,
                    ticket.getRefundable(),
                    ticket.getType(),
                    eventModelMapper.map(ticket.getEvent())
            ));
        }
        throw ErrorDescriptions.TICKET_NOT_FOUND.exception();
    }

    @Override
    public void deleteTicketById(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)){
            throw ErrorDescriptions.TICKET_NOT_FOUND.exception();
        }
        ticketRepository.deleteById(ticketId);
    }

    @Override
    public TicketDto updateTicketById(Long ticketId, CreateTicketRequest request) {
        validateCreateTicketRequest(request);
        Ticket updatedTicket = ticketModelMapper.map(request);
        updatedTicket.setId(ticketId);

        Event event = getEventFromRequest(request);
        updatedTicket.setEvent(event);
        ticketRepository.save(updatedTicket);
        return ticketModelMapper.map(updatedTicket);
    }

    @Override
    public long countTickets() {
        return ticketRepository.count();
    }

    @Override
    public TypesDto getTypes() {
        TypesDto typesDto = new TypesDto();
        var res = new ArrayList<Map<String, String>>();
        for (var type : TicketType.values()) {
            Map<String, String> a = new HashMap<>();
            a.put("value", type.name());
            a.put("desc", type.toString());
            res.add(a);
        }
        typesDto.setTypes(res);
        return typesDto;
    }

    @Override
    public Double sumOfDiscount() {
        var all = ticketRepository.findAll();
        return StreamSupport.stream(all.spliterator(), false)
                .map(Ticket::getDiscount)
                .reduce((double) 0, Double::sum);
    }

    @Override
    public Object sumOfDiscountCount() {
        var all = ticketRepository.findAll();
        var groupedByDiscount = StreamSupport.stream(all.spliterator(), false)
                .collect(groupingBy(
                        Ticket::getDiscount
                ));
        var res = new ArrayList<Map<String, Number>>();

        for(var t : groupedByDiscount.entrySet()){
            var h =
                    Map.of("discount", t.getKey(), "count", (Number) t.getValue().size());
            res.add(h);
        }
        return res;
    }

    @Override
    public Long getTicketsTypeCount(TicketType ticketType) {
        var all = ticketRepository.findAll();
        return StreamSupport.stream(all.spliterator(), false)
                .filter(ticket -> ticket.getType().getValue() < ticketType.getValue())
                .count();
    }
}
