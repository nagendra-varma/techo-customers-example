package com.techolution.customerdb.controllers;

import com.techolution.customerdb.controllers.exceptions.EmailAlreadyExistsException;
import com.techolution.customerdb.controllers.exceptions.UsernameAlreadyExistsException;
import com.techolution.customerdb.models.Customer;
import com.techolution.customerdb.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static com.techolution.customerdb.URLs.CUSTOMERS;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(CUSTOMERS)
public class CustomersController {

    private CustomerService customerService;

    @Autowired
    public CustomersController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @RequestMapping(method = POST)
    public ResponseEntity register(@RequestBody @Valid Customer customer, BindingResult result) {
        if (result.hasErrors()) {
            return status(BAD_REQUEST).body(result.getAllErrors().get(0));
        }

        Customer existingEmail = customerService.findByEmail(customer.getEmail());
        if (existingEmail != null) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Customer existingUserName = customerService.findByUsername(customer.getUsername());
        if (existingUserName != null) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        Customer newCustomer = customerService.save(customer);
        return status(CREATED).body(newCustomer);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    public List<Customer> getAll() {
        return customerService.findAll();
    }

    @RequestMapping(method = PUT)
    public ResponseEntity updateCustomer(@Valid @RequestBody Customer updateCustomer, BindingResult result) {
        if (result.hasErrors()) {
            return status(BAD_REQUEST).body(result.getAllErrors());
        }

        Customer existingCustomer = customerService.findById(updateCustomer.getId());
        if (existingCustomer == null) {
            return status(NOT_FOUND).body(null);
        }

        checkEmailConflicts(existingCustomer, updateCustomer);
        checkUsernameConflicts(existingCustomer, updateCustomer);
        Customer updatedCustomer = customerService.save(updateCustomer);
        return ok(updatedCustomer);

    }

    @RequestMapping(method = GET, value = "/{id}")
    public ResponseEntity getCustomer(@PathVariable("id") Long id) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            return status(NOT_FOUND).body(null);
        }
        return ok(customer);
    }

    @RequestMapping(method = GET, value = "/email/{email:.+}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable("email") String email) {
        Customer customer = customerService.findByEmail(email);
        if (customer == null) {
            return status(NOT_FOUND).body(null);
        }
        return ok(customer);
    }

    private void checkEmailConflicts(Customer existingCustomer, Customer updateCustomer) {
        if (existingCustomer.getEmail().equals(updateCustomer.getEmail())) {
            return;
        }
        Customer existingEmail = customerService.findByEmail(updateCustomer.getEmail());
        if (existingEmail != null) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
    }

    private void checkUsernameConflicts(Customer existingCustomer, Customer updateCustomer) {
        if (existingCustomer.getUsername().equals(updateCustomer.getUsername())) {
            return;
        }

        Customer existingUsername = customerService.findByUsername(updateCustomer.getUsername());
        if (existingUsername != null) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException exception,
                                                   HttpServletResponse response) throws IOException {
        response.sendError(BAD_REQUEST.value(), exception.getMessage());
    }
}