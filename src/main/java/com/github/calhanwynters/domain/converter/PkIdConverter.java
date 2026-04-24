package com.github.calhanwynters.domain.converter;

import com.github.calhanwynters.domain.valueobjects.PkId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PkIdConverter implements AttributeConverter<PkId, Long> {
    @Override
    public Long convertToDatabaseColumn(PkId attribute) {
        return attribute == null ? null : attribute.value();
    }
    @Override
    public PkId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new PkId(dbData);
    }
}