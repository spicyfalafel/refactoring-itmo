package com.soa.model.tickets;


import com.soa.model.enums.TicketType;
import com.soa.model.events.Event;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    @SequenceGenerator(name = "sequence", allocationSize = 1, sequenceName = "ticket_seq")
    private Long id;

    @Column(name = "name")
    @Lob
    private String name;

    @Column(name = "coordinate_x")
    private Integer coordinateX;

    @Column(name = "coordinate_y")
    private Float coordinateY;

    @Column(name = "creation_date")
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    @Column(name = "price")
    private Double price; //Значение поля должно быть больше 0

    @Column(name = "discount")
    private Double discount; //Поле не может быть null, Значение поля должно быть больше 0, Максимальное значение поля: 100

    @Column(name = "refundable")
    private Boolean refundable; //Поле не может быть null

    @Column(name = "type")
    private TicketType type; //Поле может быть null

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="event_id")
    private Event event;  //Поле не может быть null
}
