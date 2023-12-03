package com.soa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель страничного запроса.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PageDto {

    /**
     * Номер страницы.
     */
    private Integer page;

    /**
     * Количество элементов на странице.
     */
    private Integer limit;

}
