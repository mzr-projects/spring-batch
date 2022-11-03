package com.mt.springbatch.configs;

import com.mt.springbatch.entities.Employee;
import com.mt.springbatch.entities.ConvertedData;
import com.mt.springbatch.payloads.EmployeeDto;
import com.mt.springbatch.processors.EmployeeToIdentityProcessor;
import com.mt.springbatch.readers.EmployeeRepositoryReaderPagination;
import com.mt.springbatch.repositories.EmployeeRepository;
import com.mt.springbatch.repositories.EmployeeRepositoryPagination;
import com.mt.springbatch.repositories.ConvertedDataRepository;
import com.mt.springbatch.writers.WriteToDb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.HibernateItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
public class BatchConfig extends DefaultBatchConfigurer {

    private final EmployeeRepository employeeRepository;

    private final EmployeeRepositoryPagination employeeRepositoryPagination;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final ConvertedDataRepository convertedDataRepository;

    private final EmployeeToIdentityProcessor employeeToIdentityProcessor;

    private final DataSource dataSource;

    private final SessionFactory hibernateFactory;

    @PersistenceUnit
    private final EntityManagerFactory entityManagerFactory;

    @Value("${batch.chunk.size}")
    private int chunkSize;

    @Value("${batch.corePoolSize}")
    private int corePoolSize;

    @Value("${batch.throttleLimit}")
    private int throttleLimit;

    @Value("${batch.pageSize}")
    private int pageSize;

    @Value("classPath:/input/inputData.csv")
    private Resource inputResource;

    @Bean
    @Primary
    public JpaTransactionManager jpaTransactionManager() {
        final JpaTransactionManager tm = new JpaTransactionManager();
        tm.setDataSource(dataSource);
        return tm;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        // override to do not set datasource even if a datasource exist.
        // initialize will use a Map based JobRepository (instead of database)
    }

    @Bean
    public Job readFromDbJob() {
        return jobBuilderFactory
                .get("readFromEmployeeTableProcessThenWriteToIdentity")
                .incrementer(new RunIdIncrementer())
                .start(step())
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutorDb() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setThreadNamePrefix("spring-batch-demo");
        return taskExecutor;
    }

    @Bean
    public Step step() {

        WriteToDb writeToDb = new WriteToDb();
        writeToDb.setRepository(convertedDataRepository);

        return stepBuilderFactory
                .get("stepReadingFromEmployee")
                .<Employee, ConvertedData>chunk(chunkSize)
                .reader(itemReader())
                .processor(employeeToIdentityProcessor)
                .writer(jpaItemWriter())
                .taskExecutor(taskExecutorDb())
                .transactionManager(jpaTransactionManager())
                .throttleLimit(throttleLimit)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Employee> itemReader() {
        return new JpaPagingItemReaderBuilder<Employee>()
                .name("Employee")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select e from Employee e")
                .pageSize(pageSize)
                .build();
    }

    @Bean
    public JpaItemWriter<ConvertedData> jpaItemWriter() {
        JpaItemWriter<ConvertedData> identityJpaItemWriter = new JpaItemWriter<>();
        identityJpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return identityJpaItemWriter;
    }

    @Bean
    public HibernateItemWriter<ConvertedData> hibernateItemWriter() {
        HibernateItemWriterBuilder<ConvertedData> hibernateItemWriterBuilder = new HibernateItemWriterBuilder<>();
        hibernateItemWriterBuilder.sessionFactory(hibernateFactory);
        return hibernateItemWriterBuilder.build();
    }

    public EmployeeRepositoryReaderPagination employeeItemReader() {

        EmployeeRepositoryReaderPagination employeeRepositoryReaderPagination =
                new EmployeeRepositoryReaderPagination();
        employeeRepositoryReaderPagination.setMethodName("findEmployeeByEmployeeFirstNameOrderByStatus");
        employeeRepositoryReaderPagination.setRepository(employeeRepositoryPagination);

        List<String> arguments = new ArrayList<>();
        arguments.add("John1000");
        employeeRepositoryReaderPagination.setArguments(arguments);

        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("hiringDate", Sort.Direction.ASC);
        employeeRepositoryReaderPagination.setSort(sorts);

        employeeRepositoryReaderPagination.setPageSize(pageSize);

        return employeeRepositoryReaderPagination;
    }

    /*
     * Here we read from files
     * */
    //@Bean
    public FlatFileItemReader<EmployeeDto> reader() {
        FlatFileItemReader<EmployeeDto> itemReader = new FlatFileItemReader<>();
        itemReader.setLineMapper(lineMapper());
        itemReader.setLinesToSkip(1);
        itemReader.setResource(inputResource);
        return itemReader;
    }

    /*
     * Auxiliary for reading from File
     * */
    //@Bean
    public LineMapper<EmployeeDto> lineMapper() {
        DefaultLineMapper<EmployeeDto> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("firstName", "lastName");
        lineTokenizer.setIncludedFields(0, 1);
        BeanWrapperFieldSetMapper<EmployeeDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(EmployeeDto.class);
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

}
