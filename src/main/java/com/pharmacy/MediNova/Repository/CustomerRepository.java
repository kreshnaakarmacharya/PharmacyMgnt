package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Customer findByEmail(String email);
    List<Customer> findByRole(String role);
    Customer findByResetToken(String token);

    @Query("Select c.fullName From Customer c Where c.id=:customerId")
    String findNameById(@Param("customerId") Long customerId);
}
