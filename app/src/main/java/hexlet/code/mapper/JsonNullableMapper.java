package hexlet.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class JsonNullableMapper {

    public <T> JsonNullable<T> map(T value) {
        return JsonNullable.of(value);
    }

    public <T> T map(JsonNullable<T> nullable) {
        return nullable == null || !nullable.isPresent() ? null : nullable.get();
    }
}