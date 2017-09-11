package com.techolution.customerdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techolution.customerdb.models.Customer;
import com.techolution.customerdb.services.CustomerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CustomersApplication.class)
@WebAppConfiguration
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class CustomersControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerService customerService;

    @Before
    public void setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldSaveTheCustomerDetailsCorrectly() throws Exception {
        Customer customer = getCustomer();

        mockMvc.perform(post(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isCreated());
        Customer testUser = customerService.findById(customer.getId());
        reflectionEquals(customer, testUser, new String[]{"id", "password"});
        assertNull(testUser.getPassword());
    }

    @Test
    public void shouldNotAllowCustomerRegistrationWithDuplicateEmail() throws Exception {
        Customer customer = insertCustomer();
        mockMvc.perform(post(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldNotAllowCustomerRegistrationWithDuplicateUsername() throws Exception {
        Customer customer = insertCustomer();
        mockMvc.perform(post(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnBadRequestIfRequiredParamsAreNotSupplied() throws Exception {
        Customer customer = getCustomer();
        customer.setEmail(null);
        mockMvc.perform(post(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isBadRequest());

        customer = getCustomer();
        customer.setUsername(null);
        mockMvc.perform(post(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetTheRequestedCustomerDetails() throws Exception {
        Customer customer = insertCustomer();

        mockMvc.perform(get(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Arrays.toString(new Customer[]{customer})));
    }

    @Test
    public void shouldUpdateTheCustomerDetailsById() throws Exception {
        Customer customer = insertCustomer();
        customer.setLastName("No last name");
        mockMvc.perform(put(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isOk())
                .andExpect(content().json(customer.toJson()));
    }

    @Test
    public void shouldReturnBadRequestIfInvalidParametersGivenWhileUpdatingCustomerDetails() throws Exception {
        Customer customer = getCustomer();
        customer.setUsername(null);
        mockMvc.perform(post(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnNotFoundIfUpdatingCustomerNotFound() throws Exception {
        Customer customer = insertCustomer();
        customer.setId(2L);
        mockMvc.perform(put(URLs.CUSTOMERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(customer.toJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnTheCustomerDetailsById() throws Exception {
        Customer customer = insertCustomer();
        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(customer.toJson()));
    }

    @Test
    public void shouldReturnNotFoundIfUnknownGivenWhileGettingCustomerDetails() throws Exception {
        mockMvc.perform(get("/customers/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnTheCustomerDetailsByEmail() throws Exception {
        Customer customer = insertCustomer();
        mockMvc.perform(get("/customers/email/test@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(content().json(customer.toJson()));
    }

    @Test
    public void shouldReturnNotFoundIfUnknownEmailGiven() throws Exception {
        mockMvc.perform(get("/customers/email/test@gmail.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnBadRequestIfEmailNotProvided() throws Exception {
        mockMvc.perform(get("/customers/email/"))
                .andExpect(status().isBadRequest());
    }

    private Customer insertCustomer() throws Exception {
        Customer customer = getCustomer();
        customerService.save(customer);
        return customer;
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUsername("test");
        customer.setEmail("test@gmail.com");
        customer.setPassword("testpassword");
        customer.setFirstName("First Name");
        customer.setLastName("Last Name");
        return customer;
    }

}