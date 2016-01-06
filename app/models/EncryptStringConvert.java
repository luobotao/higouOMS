package models;

import utils.StringUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class EncryptStringConvert implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return StringUtil.encode(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return StringUtil.decode(dbData);
    }
}
