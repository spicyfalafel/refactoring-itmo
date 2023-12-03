package com.soa.model.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EventDto {
    private Long id;

    @NotBlank(message = "Название дисциплины не должно быть пустым")
    @NotNull
    private String name;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date date;

    private Integer minAge;

    private EventType eventType;
}

