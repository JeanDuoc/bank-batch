package cl.duoc.bankbatch.config;

import cl.duoc.bankbatch.dto.TransaccionCsv;
import cl.duoc.bankbatch.model.Transaccion;
import cl.duoc.bankbatch.policy.BankSkipPolicy;
import cl.duoc.bankbatch.processor.TransaccionProcessor;
import cl.duoc.bankbatch.tasklet.ReporteTransaccionesTasklet;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.core.task.AsyncTaskExecutor;

import javax.sql.DataSource;

@Configuration
public class TransaccionesJobConfig {

    @Bean
    public FlatFileItemReader<TransaccionCsv> transaccionReader() {

        return new FlatFileItemReaderBuilder<TransaccionCsv>()
                .name("transaccionReader")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("id", "fecha", "monto", "tipo")
                .targetType(TransaccionCsv.class)
                .build();
    }

    @Bean
    public TransaccionProcessor transaccionProcessor() {
        return new TransaccionProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> transaccionWriter(
            DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .dataSource(dataSource)
                .sql("""
                    INSERT IGNORE INTO transacciones_procesadas
                    (id, fecha, monto, tipo)
                    VALUES (:id, :fecha, :monto, :tipo)
                    """)
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step transaccionesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<TransaccionCsv> transaccionReader,
            TransaccionProcessor transaccionProcessor,
            JdbcBatchItemWriter<Transaccion> transaccionWriter,
            BankSkipPolicy bankSkipPolicy,
            AsyncTaskExecutor batchTaskExecutor) {

        return new StepBuilder("transaccionesStep", jobRepository)
                .<TransaccionCsv, Transaccion>chunk(10)
                .transactionManager(transactionManager)
                .reader(transaccionReader)
                .processor(transaccionProcessor)
                .writer(transaccionWriter)
                .taskExecutor(batchTaskExecutor)

                .faultTolerant()

                .skipPolicy(bankSkipPolicy)

                .retry(TransientDataAccessException.class)
                .retryLimit(3)

                .build();
    }

    @Bean
    public ReporteTransaccionesTasklet reporteTransaccionesTasklet(
            JdbcTemplate jdbcTemplate) {

        return new ReporteTransaccionesTasklet(jdbcTemplate);
    }

    @Bean
    public Step reporteTransaccionesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ReporteTransaccionesTasklet reporteTransaccionesTasklet) {

        return new StepBuilder("reporteTransaccionesStep", jobRepository)
                .tasklet(
                        reporteTransaccionesTasklet,
                        transactionManager
                )
                .build();
    }

    @Bean
    public Job transaccionesJob(
            JobRepository jobRepository,
            Step transaccionesStep,
            Step reporteTransaccionesStep) {

        return new JobBuilder(
                "transaccionesJob",
                jobRepository
        )
                .start(transaccionesStep)
                .next(reporteTransaccionesStep)
                .build();
    }
}