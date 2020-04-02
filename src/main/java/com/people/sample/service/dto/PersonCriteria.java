package com.people.sample.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.people.sample.domain.Person} entity. This class is used
 * in {@link com.people.sample.web.rest.PersonResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /people?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PersonCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter rut;

    private StringFilter name;

    private StringFilter lastName;

    private IntegerFilter age;

    private StringFilter course;

    public PersonCriteria(){
    }

    public PersonCriteria(PersonCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.rut = other.rut == null ? null : other.rut.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.lastName = other.lastName == null ? null : other.lastName.copy();
        this.age = other.age == null ? null : other.age.copy();
        this.course = other.course == null ? null : other.course.copy();
    }

    @Override
    public PersonCriteria copy() {
        return new PersonCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getRut() {
        return rut;
    }

    public void setRut(StringFilter rut) {
        this.rut = rut;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getLastName() {
        return lastName;
    }

    public void setLastName(StringFilter lastName) {
        this.lastName = lastName;
    }

    public IntegerFilter getAge() {
        return age;
    }

    public void setAge(IntegerFilter age) {
        this.age = age;
    }

    public StringFilter getCourse() {
        return course;
    }

    public void setCourse(StringFilter course) {
        this.course = course;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PersonCriteria that = (PersonCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(rut, that.rut) &&
            Objects.equals(name, that.name) &&
            Objects.equals(lastName, that.lastName) &&
            Objects.equals(age, that.age) &&
            Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        rut,
        name,
        lastName,
        age,
        course
        );
    }

    @Override
    public String toString() {
        return "PersonCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (rut != null ? "rut=" + rut + ", " : "") +
                (name != null ? "name=" + name + ", " : "") +
                (lastName != null ? "lastName=" + lastName + ", " : "") +
                (age != null ? "age=" + age + ", " : "") +
                (course != null ? "course=" + course + ", " : "") +
            "}";
    }

}
