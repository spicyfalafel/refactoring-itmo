package com.soa.service;


import com.soa.model.CreateTicketRequest;
import com.soa.model.tickets.TicketDto;
import com.soa.model.enums.TicketType;
import com.soa.model.tickets.TypesDto;
import com.soa.repository.FilterCriteria;
import com.soa.repository.SortCriteria;

import java.util.List;

public interface TicketService {

    TicketDto createTicket(CreateTicketRequest request);

    List<TicketDto> getAllTickets(List<FilterCriteria> filterBy, List<SortCriteria> sortBy, Long limit, Long offset) throws Exception;
    TicketDto getTicketById(Long ticketId);

    TicketDto newVipTicketById(Long ticketId);

    TicketDto newDiscountTicketById(Long ticketId, Double discount);

    void deleteTicketById(Long ticketId);

    TicketDto updateTicketById(Long ticketId, CreateTicketRequest request);

    long countTickets();

    TypesDto getTypes();

    Double sumOfDiscount();

    Object sumOfDiscountCount();

    Object getTicketsTypeCount(TicketType type);
}
