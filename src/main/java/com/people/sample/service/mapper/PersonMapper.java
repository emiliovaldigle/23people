package com.people.sample.service.mapper;

import com.people.sample.domain.*;
import com.people.sample.service.dto.PersonDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Person} and its DTO {@link PersonDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PersonMapper extends EntityMapper<PersonDTO, Person> {



    default Person fromId(Long id) {
        if (id == null) {
            return null;
        }
        Person person = new Person();
        person.setId(id);
        return person;
    }
}
