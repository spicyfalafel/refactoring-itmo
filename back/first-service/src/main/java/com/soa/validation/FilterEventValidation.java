package com.soa.validation;

import com.soa.repository.FilterCriteria;

import com.soa.exception.ErrorDescriptions;
import com.soa.model.events.EventType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilterEventValidation implements ValidateFilter{
    @Override
    public List<FilterCriteria> map(String[] filter) {
        var allowedFilters = List.of(
                "id",
                "name",
                "date",
                "minAge",
                "eventType"
        );
        List<FilterCriteria> filters = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        if (filter != null){
            try {
                for (String f : filter) {
                    var key = f.split("\\[", 2)[0];
                    if (!allowedFilters.contains(key)) {
                        throw new Exception("Недопустимое значение фильтра " + key + ", должно быть одно иззначений: " + allowedFilters);
                    }

                    var val = f.split("\\]", 2)[1];
                    val = val.substring(1);
                    if (key.equals("eventType")){
                        val = val.toUpperCase();
                        try {
                            EventType.valueOf(val);
                        } catch (Exception exc) {
                            throw new Exception("Недопустимое значение eventType: должно быть одно из значений: [CONCERT, BASEBALL, BASKETBALL, THEATRE_PERFORMANCE]");
                        }
                    } else if (key.equals("date")) {
                        Date date = formatter.parse(val);
                        if (date == null){
                            throw new Exception("Недопустимое значение date: ожидается строка вида yyyy-MM-dd");
                        }
                    }

                    var op = f.split("\\[", 2)[1].split("\\]", 2)[0];

                    filters.add(
                            new FilterCriteria(
                                    key,
                                    op,
                                    key.equals("eventType") ? EventType.valueOf(val) : key.equals("date") ? formatter.parse(val) : val
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
