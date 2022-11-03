package com.mt.springbatch.processors;

import com.mt.springbatch.entities.Employee;
import com.mt.springbatch.entities.ConvertedData;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeToIdentityProcessor implements ItemProcessor<Employee, ConvertedData> {

    @Override
    public ConvertedData process(Employee employee) {
        return new ConvertedData(employee.getEmployeeFirstName().toUpperCase(),
                employee.getEmployeeLastName() + "InIdentity");
    }
}
