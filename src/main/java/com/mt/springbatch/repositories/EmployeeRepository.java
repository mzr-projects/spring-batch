package com.mt.springbatch.repositories;

import com.mt.springbatch.entities.Employee;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
	Employee findByEmployeeFirstName(String firstName);
}
