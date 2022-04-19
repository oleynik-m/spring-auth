package com.example.dlg.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.dlg.IntegrationTest;
import com.example.dlg.domain.People;
import com.example.dlg.repository.PeopleRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PeopleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PeopleResourceIT {

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/people";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPeopleMockMvc;

    private People people;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static People createEntity(EntityManager em) {
        People people = new People().address(DEFAULT_ADDRESS).email(DEFAULT_EMAIL).password(DEFAULT_PASSWORD).name(DEFAULT_NAME);
        return people;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static People createUpdatedEntity(EntityManager em) {
        People people = new People().address(UPDATED_ADDRESS).email(UPDATED_EMAIL).password(UPDATED_PASSWORD).name(UPDATED_NAME);
        return people;
    }

    @BeforeEach
    public void initTest() {
        people = createEntity(em);
    }

    @Test
    @Transactional
    void createPeople() throws Exception {
        int databaseSizeBeforeCreate = peopleRepository.findAll().size();
        // Create the People
        restPeopleMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isCreated());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeCreate + 1);
        People testPeople = peopleList.get(peopleList.size() - 1);
        assertThat(testPeople.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testPeople.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testPeople.getPassword()).isEqualTo(DEFAULT_PASSWORD);
        assertThat(testPeople.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createPeopleWithExistingId() throws Exception {
        // Create the People with an existing ID
        people.setId(1L);

        int databaseSizeBeforeCreate = peopleRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPeopleMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPasswordIsRequired() throws Exception {
        int databaseSizeBeforeTest = peopleRepository.findAll().size();
        // set the field null
        people.setPassword(null);

        // Create the People, which fails.

        restPeopleMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isBadRequest());

        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = peopleRepository.findAll().size();
        // set the field null
        people.setName(null);

        // Create the People, which fails.

        restPeopleMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isBadRequest());

        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPeople() throws Exception {
        // Initialize the database
        peopleRepository.saveAndFlush(people);

        // Get all the peopleList
        restPeopleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(people.getId().intValue())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getPeople() throws Exception {
        // Initialize the database
        peopleRepository.saveAndFlush(people);

        // Get the people
        restPeopleMockMvc
            .perform(get(ENTITY_API_URL_ID, people.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(people.getId().intValue()))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingPeople() throws Exception {
        // Get the people
        restPeopleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPeople() throws Exception {
        // Initialize the database
        peopleRepository.saveAndFlush(people);

        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();

        // Update the people
        People updatedPeople = peopleRepository.findById(people.getId()).get();
        // Disconnect from session so that the updates on updatedPeople are not directly saved in db
        em.detach(updatedPeople);
        updatedPeople.address(UPDATED_ADDRESS).email(UPDATED_EMAIL).password(UPDATED_PASSWORD).name(UPDATED_NAME);

        restPeopleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPeople.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPeople))
            )
            .andExpect(status().isOk());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
        People testPeople = peopleList.get(peopleList.size() - 1);
        assertThat(testPeople.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testPeople.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testPeople.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testPeople.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void putNonExistingPeople() throws Exception {
        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();
        people.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, people.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPeople() throws Exception {
        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();
        people.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPeople() throws Exception {
        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();
        people.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePeopleWithPatch() throws Exception {
        // Initialize the database
        peopleRepository.saveAndFlush(people);

        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();

        // Update the people using partial update
        People partialUpdatedPeople = new People();
        partialUpdatedPeople.setId(people.getId());

        partialUpdatedPeople.email(UPDATED_EMAIL).password(UPDATED_PASSWORD);

        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPeople.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPeople))
            )
            .andExpect(status().isOk());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
        People testPeople = peopleList.get(peopleList.size() - 1);
        assertThat(testPeople.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testPeople.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testPeople.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testPeople.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void fullUpdatePeopleWithPatch() throws Exception {
        // Initialize the database
        peopleRepository.saveAndFlush(people);

        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();

        // Update the people using partial update
        People partialUpdatedPeople = new People();
        partialUpdatedPeople.setId(people.getId());

        partialUpdatedPeople.address(UPDATED_ADDRESS).email(UPDATED_EMAIL).password(UPDATED_PASSWORD).name(UPDATED_NAME);

        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPeople.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPeople))
            )
            .andExpect(status().isOk());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
        People testPeople = peopleList.get(peopleList.size() - 1);
        assertThat(testPeople.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testPeople.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testPeople.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testPeople.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingPeople() throws Exception {
        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();
        people.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, people.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPeople() throws Exception {
        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();
        people.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPeople() throws Exception {
        int databaseSizeBeforeUpdate = peopleRepository.findAll().size();
        people.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(people))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the People in the database
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePeople() throws Exception {
        // Initialize the database
        peopleRepository.saveAndFlush(people);

        int databaseSizeBeforeDelete = peopleRepository.findAll().size();

        // Delete the people
        restPeopleMockMvc
            .perform(delete(ENTITY_API_URL_ID, people.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<People> peopleList = peopleRepository.findAll();
        assertThat(peopleList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
