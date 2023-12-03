package com.soa.mapper;


import com.soa.model.CreateTicketRequest;
import com.soa.model.events.Event;
import com.soa.model.tickets.Coordinates;
import com.soa.model.events.EventDto;
import com.soa.model.tickets.Ticket;
import com.soa.model.tickets.TicketDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TicketModelMapper {
    public TicketDto map(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setName(ticket.getName());
        dto.setCoordinates(Coordinates.of(ticket.getCoordinateX(), ticket.getCoordinateY()));
        dto.setCreationDate(Date.from(ticket.getCreationDate().toInstant()));
        dto.setPrice(ticket.getPrice());
        dto.setDiscount(ticket.getDiscount());
        dto.setRefundable(ticket.getRefundable());
        dto.setType(ticket.getType());
        if(ticket.getEvent() != null){
            dto.setEvent(EventDto.of(
                    ticket.getEvent().getId(),
                    ticket.getEvent().getName(),
                    ticket.getEvent().getDate(),
                    ticket.getEvent().getMinAge(),
                    ticket.getEvent().getEventType()
            ));
        }
        return dto;
    }

    public Ticket map(CreateTicketRequest request) {
        Ticket updatedTicket = new Ticket();
        updatedTicket.setName(request.getName());
        updatedTicket.setCoordinateX(request.getCoordinates().getX());
        updatedTicket.setCoordinateY(request.getCoordinates().getY());
        updatedTicket.setCreationDate(new Date());
        updatedTicket.setPrice(request.getPrice());
        updatedTicket.setDiscount(request.getDiscount());
        updatedTicket.setRefundable(request.getRefundable());
        updatedTicket.setType(request.getType());
        return updatedTicket;
    }
}
