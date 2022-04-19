package com.example.dlg.web.rest;

import static com.example.dlg.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.dlg.IntegrationTest;
import com.example.dlg.domain.Products;
import com.example.dlg.repository.ProductsRepository;
import java.math.BigDecimal;
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
 * Integration tests for the {@link ProductsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProductsResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/products";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductsMockMvc;

    private Products products;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Products createEntity(EntityManager em) {
        Products products = new Products().title(DEFAULT_TITLE).category(DEFAULT_CATEGORY).price(DEFAULT_PRICE);
        return products;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Products createUpdatedEntity(EntityManager em) {
        Products products = new Products().title(UPDATED_TITLE).category(UPDATED_CATEGORY).price(UPDATED_PRICE);
        return products;
    }

    @BeforeEach
    public void initTest() {
        products = createEntity(em);
    }

    @Test
    @Transactional
    void createProducts() throws Exception {
        int databaseSizeBeforeCreate = productsRepository.findAll().size();
        // Create the Products
        restProductsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isCreated());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeCreate + 1);
        Products testProducts = productsList.get(productsList.size() - 1);
        assertThat(testProducts.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testProducts.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testProducts.getPrice()).isEqualByComparingTo(DEFAULT_PRICE);
    }

    @Test
    @Transactional
    void createProductsWithExistingId() throws Exception {
        // Create the Products with an existing ID
        products.setId(1L);

        int databaseSizeBeforeCreate = productsRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = productsRepository.findAll().size();
        // set the field null
        products.setTitle(null);

        // Create the Products, which fails.

        restProductsMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isBadRequest());

        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProducts() throws Exception {
        // Initialize the database
        productsRepository.saveAndFlush(products);

        // Get all the productsList
        restProductsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(products.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))));
    }

    @Test
    @Transactional
    void getProducts() throws Exception {
        // Initialize the database
        productsRepository.saveAndFlush(products);

        // Get the products
        restProductsMockMvc
            .perform(get(ENTITY_API_URL_ID, products.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(products.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)));
    }

    @Test
    @Transactional
    void getNonExistingProducts() throws Exception {
        // Get the products
        restProductsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewProducts() throws Exception {
        // Initialize the database
        productsRepository.saveAndFlush(products);

        int databaseSizeBeforeUpdate = productsRepository.findAll().size();

        // Update the products
        Products updatedProducts = productsRepository.findById(products.getId()).get();
        // Disconnect from session so that the updates on updatedProducts are not directly saved in db
        em.detach(updatedProducts);
        updatedProducts.title(UPDATED_TITLE).category(UPDATED_CATEGORY).price(UPDATED_PRICE);

        restProductsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedProducts.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedProducts))
            )
            .andExpect(status().isOk());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
        Products testProducts = productsList.get(productsList.size() - 1);
        assertThat(testProducts.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testProducts.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testProducts.getPrice()).isEqualByComparingTo(UPDATED_PRICE);
    }

    @Test
    @Transactional
    void putNonExistingProducts() throws Exception {
        int databaseSizeBeforeUpdate = productsRepository.findAll().size();
        products.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, products.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProducts() throws Exception {
        int databaseSizeBeforeUpdate = productsRepository.findAll().size();
        products.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProducts() throws Exception {
        int databaseSizeBeforeUpdate = productsRepository.findAll().size();
        products.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProductsWithPatch() throws Exception {
        // Initialize the database
        productsRepository.saveAndFlush(products);

        int databaseSizeBeforeUpdate = productsRepository.findAll().size();

        // Update the products using partial update
        Products partialUpdatedProducts = new Products();
        partialUpdatedProducts.setId(products.getId());

        partialUpdatedProducts.title(UPDATED_TITLE).price(UPDATED_PRICE);

        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProducts.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProducts))
            )
            .andExpect(status().isOk());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
        Products testProducts = productsList.get(productsList.size() - 1);
        assertThat(testProducts.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testProducts.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testProducts.getPrice()).isEqualByComparingTo(UPDATED_PRICE);
    }

    @Test
    @Transactional
    void fullUpdateProductsWithPatch() throws Exception {
        // Initialize the database
        productsRepository.saveAndFlush(products);

        int databaseSizeBeforeUpdate = productsRepository.findAll().size();

        // Update the products using partial update
        Products partialUpdatedProducts = new Products();
        partialUpdatedProducts.setId(products.getId());

        partialUpdatedProducts.title(UPDATED_TITLE).category(UPDATED_CATEGORY).price(UPDATED_PRICE);

        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProducts.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProducts))
            )
            .andExpect(status().isOk());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
        Products testProducts = productsList.get(productsList.size() - 1);
        assertThat(testProducts.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testProducts.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testProducts.getPrice()).isEqualByComparingTo(UPDATED_PRICE);
    }

    @Test
    @Transactional
    void patchNonExistingProducts() throws Exception {
        int databaseSizeBeforeUpdate = productsRepository.findAll().size();
        products.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, products.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProducts() throws Exception {
        int databaseSizeBeforeUpdate = productsRepository.findAll().size();
        products.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProducts() throws Exception {
        int databaseSizeBeforeUpdate = productsRepository.findAll().size();
        products.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(products))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Products in the database
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProducts() throws Exception {
        // Initialize the database
        productsRepository.saveAndFlush(products);

        int databaseSizeBeforeDelete = productsRepository.findAll().size();

        // Delete the products
        restProductsMockMvc
            .perform(delete(ENTITY_API_URL_ID, products.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Products> productsList = productsRepository.findAll();
        assertThat(productsList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
