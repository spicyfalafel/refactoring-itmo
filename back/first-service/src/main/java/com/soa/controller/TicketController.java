package com.soa.controller;

import com.soa.common.Endpoints;
import com.soa.common.Utils;
import com.soa.mapper.SortMapper;
import com.soa.model.CreateTicketRequest;
import com.soa.model.tickets.TicketDto;
import com.soa.model.enums.TicketType;
import com.soa.model.tickets.TypesDto;
import com.soa.service.TicketService;
import com.soa.validation.FilterTicketValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping(value = Endpoints.CREATE_TICKET)
    public ResponseEntity<TicketDto> createTicket(
            @RequestBody CreateTicketRequest request
    ) {
        TicketDto ticketDto = ticketService.createTicket(request);
        return new ResponseEntity<>(ticketDto, HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_ALL_TICKETS)
    public ResponseEntity<List<TicketDto>> getAllTickets(
            @RequestParam(value = "filter", required = false) String[] filter,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Long limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset
    ) throws Exception {
        Utils.validateLimitOffset(limit, offset);
        var filters = new FilterTicketValidation().map(filter);
        var sorts = SortMapper.map(sort);
        List<TicketDto> tickets = ticketService.getAllTickets(filters, sorts, limit, offset);
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_TICKET_BY_ID)
    public ResponseEntity<TicketDto> getTicketById(
            @PathVariable("ticketId") Long ticketId
    ) {
        return new ResponseEntity<>(ticketService.getTicketById(ticketId), HttpStatus.OK);
    }

    @DeleteMapping(value = Endpoints.DELETE_TICKET_BY_ID)
    public ResponseEntity<Object> deleteTicketById(
            @PathVariable("ticketId") Long ticketId
    ) {
        ticketService.deleteTicketById(ticketId);
        return new ResponseEntity<>("deleted", HttpStatus.OK);
    }

    @PutMapping(value = Endpoints.UPDATE_TICKET_BY_ID)
    public ResponseEntity<Object> updateTicketById(
            @PathVariable("ticketId") Long ticketId,
            @RequestBody CreateTicketRequest request
    ) {
        var res = ticketService.updateTicketById(ticketId, request);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_TICKETS_COUNT)
    public ResponseEntity<Long> countTickets() {
        return new ResponseEntity<>(ticketService.countTickets(), HttpStatus.OK);
    }


    @GetMapping(value = Endpoints.GET_TICKETS_TYPES)
    public ResponseEntity<TypesDto> eventsTypes() {
        return new ResponseEntity<>(ticketService.getTypes(), HttpStatus.OK);
    }


    @GetMapping(value = Endpoints.GET_TICKETS_DISCOUNT_SUM)
    public ResponseEntity<Double> getSumOfDiscount() {
        return new ResponseEntity<>(ticketService.sumOfDiscount(), HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_TICKETS_DISCOUNT_COUNT)
    public ResponseEntity<Object> getSumOfDiscountCount() {
        return new ResponseEntity<>(ticketService.sumOfDiscountCount(), HttpStatus.OK);
    }

    @GetMapping(value = Endpoints.GET_TICKETS_TYPE_COUNT)
    public ResponseEntity<Object> getTicketsTypeCount(
            @RequestParam("type") TicketType type
    ) {
        if (type == null) {
            return new ResponseEntity<>("type parameter is required", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(ticketService.getTicketsTypeCount(type), HttpStatus.OK);
    }

    @PostMapping(value = Endpoints.NEW_VIP_TICKET_BY_ID)
    public ResponseEntity<Object> createVipTicketById(
            @PathVariable("ticketId") Long ticketId
    ) {
        var res = ticketService.newVipTicketById(ticketId);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping(value = Endpoints.DISCOUNT_TICKET_BY_ID)
    public ResponseEntity<Object> createDiscountTicketById(
            @PathVariable("ticketId") Long ticketId,
            @PathVariable("discount") Double discount
    ) {
        var res = ticketService.newDiscountTicketById(ticketId, discount);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
