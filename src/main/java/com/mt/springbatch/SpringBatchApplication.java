package com.mt.springbatch;

import com.mt.springbatch.repositories.ConvertedDataRepository;
import com.mt.springbatch.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class SpringBatchApplication implements CommandLineRunner {

    private final ConvertedDataRepository convertedDataRepository;
    private final EmployeeRepository employeeRepository;

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Random random = new Random();

        /*
        for (int i = 0; i < 100000; i++) {
            Employee employee = new Employee();
            employee.setHiringDate(String.valueOf(random.nextInt((14010810 - 14010805) + 14010810)));
            employee.setEmployeeFirstName("John" + i);
            employee.setEmployeeLastName("Doe" + i);
            employee.setStatus(Status.randomStatus());
            employeeRepository.save(employee);
        }*/

        /*
        for (int i = 0; i < 10; i++) {
            ConvertedData convertedData = new ConvertedData("IdentityFirst" + i, "IdentityLast" + i);
            convertedDataRepository.save(convertedData);
        }*/

        log.info("Initialization DONE");
    }
}