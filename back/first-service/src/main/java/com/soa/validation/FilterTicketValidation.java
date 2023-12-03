package com.soa.validation;

import com.soa.exception.ErrorDescriptions;
import com.soa.model.enums.TicketType;
import com.soa.repository.FilterCriteria;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilterTicketValidation implements ValidateFilter{
    @Override
    public List<FilterCriteria> map(String[] filter) {
        List<FilterCriteria> filters = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        var allowedFilters = List.of(
                "id",
                "name",
                "coordinateX",
                "coordinateY",
                "creationDate",
                "price",
                "discount",
                "refundable",
                "type"
        );
        if (filter != null){
            try {
                for (String f : filter) {
                    var key = f.split("\\[", 2)[0];
                    if (!allowedFilters.contains(key)) {
                        throw new Exception("Недопустимое значение фильтра " + key + ", должно быть одно иззначений: " + allowedFilters);
                    }

                    var val = f.split("\\]", 2)[1];
                    val = val.substring(1);
                    if (key.equals("refundable") && !(val.equals("true") | val.equalsIgnoreCase("false"))){
                        throw new Exception("Недопустимое значение refundable: должно быть одно из значений: [true, false]");
                    } else if (key.equals("type")){
                        val = val.toUpperCase();
                        try {
                            TicketType.valueOf(val);
                        } catch (Exception exc) {
                            throw new Exception("Недопустимое значение type: должно быть одно из значений: [CHEAP, BUDGETARY, USUAL, VIP]");
                        }
                    } else if (key.equals("creationDate")) {
                        Date date = formatter.parse(val);
                        if (date == null){
                            throw new Exception("Недопустимое значение creationDate: ожидается строка вида yyyy-MM-dd");
                        }
                    }

                    var op = f.split("\\[", 2)[1].split("\\]", 2)[0];

                    filters.add(
                            new FilterCriteria(
                                    key,
                                    op,
                                    key.equals("refundable") ? (Boolean) val.equals("true") : key.equals("type") ? TicketType.valueOf(val) : key.equals("creationDate") ? formatter.parse(val) : val
                            )
                    );
                }
            } catch (Exception exc){
                throw ErrorDescriptions.INCORRECT_FILTER.exception();
            }
        }
        return filters;
    }
}
