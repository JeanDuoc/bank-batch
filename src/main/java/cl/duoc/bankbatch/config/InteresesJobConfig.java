package cl.duoc.bankbatch.config;

import cl.duoc.bankbatch.dto.InteresCsv;
import cl.duoc.bankbatch.model.Interes;
import cl.duoc.bankbatch.policy.BankSkipPolicy;
import cl.duoc.bankbatch.processor.InteresProcessor;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.core.task.AsyncTaskExecutor;

import javax.sql.DataSource;

@Configuration
public class InteresesJobConfig {

    @Bean
    public FlatFileItemReader<InteresCsv> interesReader() {

        return new FlatFileItemReaderBuilder<InteresCsv>()
                .name("interesReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .targetType(InteresCsv.class)
                .build();
    }

    @Bean
    public InteresProcessor interesProcessor() {
        return new InteresProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Interes> interesWriter(
            DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Interes>()
                .dataSource(dataSource)
                .sql("""
                    INSERT IGNORE INTO intereses_procesados
                    (cuenta_id, nombre, saldo, edad, tipo,
                     interes_calculado, saldo_final)
                    VALUES
                    (:cuentaId, :nombre, :saldo, :edad, :tipo,
                     :interesCalculado, :saldoFinal)
                    """)
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step interesesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<InteresCsv> interesReader,
            InteresProcessor interesProcessor,
            JdbcBatchItemWriter<Interes> interesWriter,
            BankSkipPolicy bankSkipPolicy,
            AsyncTaskExecutor batchTaskExecutor) {

        return new StepBuilder("interesesStep", jobRepository)
                .<InteresCsv, Interes>chunk(10)
                .transactionManager(transactionManager)
                .reader(interesReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .taskExecutor(batchTaskExecutor)

                .faultTolerant()

                .skipPolicy(bankSkipPolicy)

                .retry(TransientDataAccessException.class)
                .retryLimit(3)

                .build();
    }

    @Bean
    public Job interesesJob(
            JobRepository jobRepository,
            Step interesesStep) {

        return new JobBuilder("interesesJob", jobRepository)
                .start(interesesStep)
                .build();
    }
}