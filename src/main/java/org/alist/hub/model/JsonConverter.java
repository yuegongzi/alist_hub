package org.alist.hub.model;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.alist.hub.utils.JsonUtil;

import java.util.Map;

@Converter
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        return JsonUtil.toJson(attribute);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        return JsonUtil.toMap(dbData);
    }
}
