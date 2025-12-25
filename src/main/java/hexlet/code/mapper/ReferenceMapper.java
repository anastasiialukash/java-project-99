package hexlet.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;

import java.lang.reflect.InvocationTargetException;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class ReferenceMapper {

    public <T> T map(Long id, @TargetType Class<T> entityClass) {
        if (id == null) {
            return null;
        }

        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            entityClass.getMethod("setId", Long.class).invoke(entity, id);
            return entity;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}