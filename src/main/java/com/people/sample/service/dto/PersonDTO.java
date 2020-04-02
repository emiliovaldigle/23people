package com.people.sample.service.dto;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.people.sample.domain.Person} entity.
 */
public class PersonDTO implements Serializable {

    private Long id;

    private String rut;

    private String name;

    private String lastName;

    private Integer age;

    private String course;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
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

        PersonDTO personDTO = (PersonDTO) o;
        if (personDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), personDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PersonDTO{" +
            "id=" + getId() +
            ", rut='" + getRut() + "'" +
            ", name='" + getName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", age=" + getAge() +
            ", course='" + getCourse() + "'" +
            "}";
    }
}
