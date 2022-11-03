package com.mt.springbatch.repositories;

import com.mt.springbatch.entities.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EmployeeRepositoryPagination extends PagingAndSortingRepository<Employee, Long> {
    Page<Employee> findEmployeeByEmployeeFirstNameOrderByStatus(String firstName, Pageable pageable);
}
