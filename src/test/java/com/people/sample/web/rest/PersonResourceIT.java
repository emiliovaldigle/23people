package com.people.sample.web.rest;

import com.people.sample.SampleApp;
import com.people.sample.domain.Person;
import com.people.sample.repository.PersonRepository;
import com.people.sample.service.PersonService;
import com.people.sample.service.dto.PersonDTO;
import com.people.sample.service.mapper.PersonMapper;
import com.people.sample.web.rest.errors.ExceptionTranslator;
import com.people.sample.service.dto.PersonCriteria;
import com.people.sample.service.PersonQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static com.people.sample.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link PersonResource} REST controller.
 */
@SpringBootTest(classes = SampleApp.class)
public class PersonResourceIT {

    private static final String DEFAULT_RUT = "AAAAAAAAAA";
    private static final String UPDATED_RUT = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_AGE = 1;
    private static final Integer UPDATED_AGE = 2;
    private static final Integer SMALLER_AGE = 1 - 1;

    private static final String DEFAULT_COURSE = "AAAAAAAAAA";
    private static final String UPDATED_COURSE = "BBBBBBBBBB";

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonQueryService personQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restPersonMockMvc;

    private Person person;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PersonResource personResource = new PersonResource(personService, personQueryService);
        this.restPersonMockMvc = MockMvcBuilders.standaloneSetup(personResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Person createEntity(EntityManager em) {
        Person person = new Person()
            .rut(DEFAULT_RUT)
            .name(DEFAULT_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .age(DEFAULT_AGE)
            .course(DEFAULT_COURSE);
        return person;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Person createUpdatedEntity(EntityManager em) {
        Person person = new Person()
            .rut(UPDATED_RUT)
            .name(UPDATED_NAME)
            .lastName(UPDATED_LAST_NAME)
            .age(UPDATED_AGE)
            .course(UPDATED_COURSE);
        return person;
    }

    @BeforeEach
    public void initTest() {
        person = createEntity(em);
    }

    @Test
    @Transactional
    public void createPerson() throws Exception {
        int databaseSizeBeforeCreate = personRepository.findAll().size();

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);
        restPersonMockMvc.perform(post("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(personDTO)))
            .andExpect(status().isCreated());

        // Validate the Person in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeCreate + 1);
        Person testPerson = personList.get(personList.size() - 1);
        assertThat(testPerson.getRut()).isEqualTo(DEFAULT_RUT);
        assertThat(testPerson.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPerson.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testPerson.getAge()).isEqualTo(DEFAULT_AGE);
        assertThat(testPerson.getCourse()).isEqualTo(DEFAULT_COURSE);
    }

    @Test
    @Transactional
    public void createPersonWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = personRepository.findAll().size();

        // Create the Person with an existing ID
        person.setId(1L);
        PersonDTO personDTO = personMapper.toDto(person);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPersonMockMvc.perform(post("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(personDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Person in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllPeople() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList
        restPersonMockMvc.perform(get("/api/people?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(person.getId().intValue())))
            .andExpect(jsonPath("$.[*].rut").value(hasItem(DEFAULT_RUT)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].course").value(hasItem(DEFAULT_COURSE)));
    }
    
    @Test
    @Transactional
    public void getPerson() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get the person
        restPersonMockMvc.perform(get("/api/people/{id}", person.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(person.getId().intValue()))
            .andExpect(jsonPath("$.rut").value(DEFAULT_RUT))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE))
            .andExpect(jsonPath("$.course").value(DEFAULT_COURSE));
    }


    @Test
    @Transactional
    public void getPeopleByIdFiltering() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        Long id = person.getId();

        defaultPersonShouldBeFound("id.equals=" + id);
        defaultPersonShouldNotBeFound("id.notEquals=" + id);

        defaultPersonShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultPersonShouldNotBeFound("id.greaterThan=" + id);

        defaultPersonShouldBeFound("id.lessThanOrEqual=" + id);
        defaultPersonShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllPeopleByRutIsEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where rut equals to DEFAULT_RUT
        defaultPersonShouldBeFound("rut.equals=" + DEFAULT_RUT);

        // Get all the personList where rut equals to UPDATED_RUT
        defaultPersonShouldNotBeFound("rut.equals=" + UPDATED_RUT);
    }

    @Test
    @Transactional
    public void getAllPeopleByRutIsNotEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where rut not equals to DEFAULT_RUT
        defaultPersonShouldNotBeFound("rut.notEquals=" + DEFAULT_RUT);

        // Get all the personList where rut not equals to UPDATED_RUT
        defaultPersonShouldBeFound("rut.notEquals=" + UPDATED_RUT);
    }

    @Test
    @Transactional
    public void getAllPeopleByRutIsInShouldWork() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where rut in DEFAULT_RUT or UPDATED_RUT
        defaultPersonShouldBeFound("rut.in=" + DEFAULT_RUT + "," + UPDATED_RUT);

        // Get all the personList where rut equals to UPDATED_RUT
        defaultPersonShouldNotBeFound("rut.in=" + UPDATED_RUT);
    }

    @Test
    @Transactional
    public void getAllPeopleByRutIsNullOrNotNull() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where rut is not null
        defaultPersonShouldBeFound("rut.specified=true");

        // Get all the personList where rut is null
        defaultPersonShouldNotBeFound("rut.specified=false");
    }
                @Test
    @Transactional
    public void getAllPeopleByRutContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where rut contains DEFAULT_RUT
        defaultPersonShouldBeFound("rut.contains=" + DEFAULT_RUT);

        // Get all the personList where rut contains UPDATED_RUT
        defaultPersonShouldNotBeFound("rut.contains=" + UPDATED_RUT);
    }

    @Test
    @Transactional
    public void getAllPeopleByRutNotContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where rut does not contain DEFAULT_RUT
        defaultPersonShouldNotBeFound("rut.doesNotContain=" + DEFAULT_RUT);

        // Get all the personList where rut does not contain UPDATED_RUT
        defaultPersonShouldBeFound("rut.doesNotContain=" + UPDATED_RUT);
    }


    @Test
    @Transactional
    public void getAllPeopleByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where name equals to DEFAULT_NAME
        defaultPersonShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the personList where name equals to UPDATED_NAME
        defaultPersonShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where name not equals to DEFAULT_NAME
        defaultPersonShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the personList where name not equals to UPDATED_NAME
        defaultPersonShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByNameIsInShouldWork() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where name in DEFAULT_NAME or UPDATED_NAME
        defaultPersonShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the personList where name equals to UPDATED_NAME
        defaultPersonShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where name is not null
        defaultPersonShouldBeFound("name.specified=true");

        // Get all the personList where name is null
        defaultPersonShouldNotBeFound("name.specified=false");
    }
                @Test
    @Transactional
    public void getAllPeopleByNameContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where name contains DEFAULT_NAME
        defaultPersonShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the personList where name contains UPDATED_NAME
        defaultPersonShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByNameNotContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where name does not contain DEFAULT_NAME
        defaultPersonShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the personList where name does not contain UPDATED_NAME
        defaultPersonShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }


    @Test
    @Transactional
    public void getAllPeopleByLastNameIsEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where lastName equals to DEFAULT_LAST_NAME
        defaultPersonShouldBeFound("lastName.equals=" + DEFAULT_LAST_NAME);

        // Get all the personList where lastName equals to UPDATED_LAST_NAME
        defaultPersonShouldNotBeFound("lastName.equals=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByLastNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where lastName not equals to DEFAULT_LAST_NAME
        defaultPersonShouldNotBeFound("lastName.notEquals=" + DEFAULT_LAST_NAME);

        // Get all the personList where lastName not equals to UPDATED_LAST_NAME
        defaultPersonShouldBeFound("lastName.notEquals=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByLastNameIsInShouldWork() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where lastName in DEFAULT_LAST_NAME or UPDATED_LAST_NAME
        defaultPersonShouldBeFound("lastName.in=" + DEFAULT_LAST_NAME + "," + UPDATED_LAST_NAME);

        // Get all the personList where lastName equals to UPDATED_LAST_NAME
        defaultPersonShouldNotBeFound("lastName.in=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByLastNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where lastName is not null
        defaultPersonShouldBeFound("lastName.specified=true");

        // Get all the personList where lastName is null
        defaultPersonShouldNotBeFound("lastName.specified=false");
    }
                @Test
    @Transactional
    public void getAllPeopleByLastNameContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where lastName contains DEFAULT_LAST_NAME
        defaultPersonShouldBeFound("lastName.contains=" + DEFAULT_LAST_NAME);

        // Get all the personList where lastName contains UPDATED_LAST_NAME
        defaultPersonShouldNotBeFound("lastName.contains=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    public void getAllPeopleByLastNameNotContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where lastName does not contain DEFAULT_LAST_NAME
        defaultPersonShouldNotBeFound("lastName.doesNotContain=" + DEFAULT_LAST_NAME);

        // Get all the personList where lastName does not contain UPDATED_LAST_NAME
        defaultPersonShouldBeFound("lastName.doesNotContain=" + UPDATED_LAST_NAME);
    }


    @Test
    @Transactional
    public void getAllPeopleByAgeIsEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age equals to DEFAULT_AGE
        defaultPersonShouldBeFound("age.equals=" + DEFAULT_AGE);

        // Get all the personList where age equals to UPDATED_AGE
        defaultPersonShouldNotBeFound("age.equals=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    public void getAllPeopleByAgeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age not equals to DEFAULT_AGE
        defaultPersonShouldNotBeFound("age.notEquals=" + DEFAULT_AGE);

        // Get all the personList where age not equals to UPDATED_AGE
        defaultPersonShouldBeFound("age.notEquals=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    public void getAllPeopleByAgeIsInShouldWork() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age in DEFAULT_AGE or UPDATED_AGE
        defaultPersonShouldBeFound("age.in=" + DEFAULT_AGE + "," + UPDATED_AGE);

        // Get all the personList where age equals to UPDATED_AGE
        defaultPersonShouldNotBeFound("age.in=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    public void getAllPeopleByAgeIsNullOrNotNull() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age is not null
        defaultPersonShouldBeFound("age.specified=true");

        // Get all the personList where age is null
        defaultPersonShouldNotBeFound("age.specified=false");
    }

    @Test
    @Transactional
    public void getAllPeopleByAgeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age is greater than or equal to DEFAULT_AGE
        defaultPersonShouldBeFound("age.greaterThanOrEqual=" + DEFAULT_AGE);

        // Get all the personList where age is greater than or equal to UPDATED_AGE
        defaultPersonShouldNotBeFound("age.greaterThanOrEqual=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    public void getAllPeopleByAgeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age is less than or equal to DEFAULT_AGE
        defaultPersonShouldBeFound("age.lessThanOrEqual=" + DEFAULT_AGE);

        // Get all the personList where age is less than or equal to SMALLER_AGE
        defaultPersonShouldNotBeFound("age.lessThanOrEqual=" + SMALLER_AGE);
    }

    @Test
    @Transactional
    public void getAllPeopleByAgeIsLessThanSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age is less than DEFAULT_AGE
        defaultPersonShouldNotBeFound("age.lessThan=" + DEFAULT_AGE);

        // Get all the personList where age is less than UPDATED_AGE
        defaultPersonShouldBeFound("age.lessThan=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    public void getAllPeopleByAgeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where age is greater than DEFAULT_AGE
        defaultPersonShouldNotBeFound("age.greaterThan=" + DEFAULT_AGE);

        // Get all the personList where age is greater than SMALLER_AGE
        defaultPersonShouldBeFound("age.greaterThan=" + SMALLER_AGE);
    }


    @Test
    @Transactional
    public void getAllPeopleByCourseIsEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where course equals to DEFAULT_COURSE
        defaultPersonShouldBeFound("course.equals=" + DEFAULT_COURSE);

        // Get all the personList where course equals to UPDATED_COURSE
        defaultPersonShouldNotBeFound("course.equals=" + UPDATED_COURSE);
    }

    @Test
    @Transactional
    public void getAllPeopleByCourseIsNotEqualToSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where course not equals to DEFAULT_COURSE
        defaultPersonShouldNotBeFound("course.notEquals=" + DEFAULT_COURSE);

        // Get all the personList where course not equals to UPDATED_COURSE
        defaultPersonShouldBeFound("course.notEquals=" + UPDATED_COURSE);
    }

    @Test
    @Transactional
    public void getAllPeopleByCourseIsInShouldWork() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where course in DEFAULT_COURSE or UPDATED_COURSE
        defaultPersonShouldBeFound("course.in=" + DEFAULT_COURSE + "," + UPDATED_COURSE);

        // Get all the personList where course equals to UPDATED_COURSE
        defaultPersonShouldNotBeFound("course.in=" + UPDATED_COURSE);
    }

    @Test
    @Transactional
    public void getAllPeopleByCourseIsNullOrNotNull() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where course is not null
        defaultPersonShouldBeFound("course.specified=true");

        // Get all the personList where course is null
        defaultPersonShouldNotBeFound("course.specified=false");
    }
                @Test
    @Transactional
    public void getAllPeopleByCourseContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where course contains DEFAULT_COURSE
        defaultPersonShouldBeFound("course.contains=" + DEFAULT_COURSE);

        // Get all the personList where course contains UPDATED_COURSE
        defaultPersonShouldNotBeFound("course.contains=" + UPDATED_COURSE);
    }

    @Test
    @Transactional
    public void getAllPeopleByCourseNotContainsSomething() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        // Get all the personList where course does not contain DEFAULT_COURSE
        defaultPersonShouldNotBeFound("course.doesNotContain=" + DEFAULT_COURSE);

        // Get all the personList where course does not contain UPDATED_COURSE
        defaultPersonShouldBeFound("course.doesNotContain=" + UPDATED_COURSE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPersonShouldBeFound(String filter) throws Exception {
        restPersonMockMvc.perform(get("/api/people?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(person.getId().intValue())))
            .andExpect(jsonPath("$.[*].rut").value(hasItem(DEFAULT_RUT)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].course").value(hasItem(DEFAULT_COURSE)));

        // Check, that the count call also returns 1
        restPersonMockMvc.perform(get("/api/people/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPersonShouldNotBeFound(String filter) throws Exception {
        restPersonMockMvc.perform(get("/api/people?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPersonMockMvc.perform(get("/api/people/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingPerson() throws Exception {
        // Get the person
        restPersonMockMvc.perform(get("/api/people/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePerson() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        int databaseSizeBeforeUpdate = personRepository.findAll().size();

        // Update the person
        Person updatedPerson = personRepository.findById(person.getId()).get();
        // Disconnect from session so that the updates on updatedPerson are not directly saved in db
        em.detach(updatedPerson);
        updatedPerson
            .rut(UPDATED_RUT)
            .name(UPDATED_NAME)
            .lastName(UPDATED_LAST_NAME)
            .age(UPDATED_AGE)
            .course(UPDATED_COURSE);
        PersonDTO personDTO = personMapper.toDto(updatedPerson);

        restPersonMockMvc.perform(put("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(personDTO)))
            .andExpect(status().isOk());

        // Validate the Person in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeUpdate);
        Person testPerson = personList.get(personList.size() - 1);
        assertThat(testPerson.getRut()).isEqualTo(UPDATED_RUT);
        assertThat(testPerson.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPerson.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testPerson.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testPerson.getCourse()).isEqualTo(UPDATED_COURSE);
    }

    @Test
    @Transactional
    public void updateNonExistingPerson() throws Exception {
        int databaseSizeBeforeUpdate = personRepository.findAll().size();

        // Create the Person
        PersonDTO personDTO = personMapper.toDto(person);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPersonMockMvc.perform(put("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(personDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Person in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePerson() throws Exception {
        // Initialize the database
        personRepository.saveAndFlush(person);

        int databaseSizeBeforeDelete = personRepository.findAll().size();

        // Delete the person
        restPersonMockMvc.perform(delete("/api/people/{id}", person.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
