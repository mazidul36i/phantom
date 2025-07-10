package com.gliesestudio.phantom.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Converter(autoApply = true)
public class VectorConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) return null;
        return IntStream.range(0, attribute.length)
                        .mapToObj(i -> String.format("%.6f", attribute[i]))
                        .collect(Collectors.joining(" "));
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new float[0];
        String[] parts = dbData.split(" ");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i]);
        }
        return result;
    }

}

