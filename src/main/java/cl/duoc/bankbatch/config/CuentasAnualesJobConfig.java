package cl.duoc.bankbatch.config;

import cl.duoc.bankbatch.dto.CuentaAnualCsv;
import cl.duoc.bankbatch.model.CuentaAnual;
import cl.duoc.bankbatch.processor.CuentaAnualProcessor;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.dao.TransientDataAccessException;

import cl.duoc.bankbatch.tasklet.ReporteAnualTasklet;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CuentasAnualesJobConfig {

    @Bean
    public FlatFileItemReader<CuentaAnualCsv> cuentaAnualReader() {

        return new FlatFileItemReaderBuilder<CuentaAnualCsv>()
                .name("cuentaAnualReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                        "cuentaId",
                        "fecha",
                        "transaccion",
                        "monto",
                        "descripcion"
                )
                .targetType(CuentaAnualCsv.class)
                .build();
    }

    @Bean
    public CuentaAnualProcessor cuentaAnualProcessor() {
        return new CuentaAnualProcessor();
    }
    @Bean
    public ReporteAnualTasklet reporteAnualTasklet(JdbcTemplate jdbcTemplate) {
        return new ReporteAnualTasklet(jdbcTemplate);
    }
    @Bean
    public Step reporteAnualStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ReporteAnualTasklet reporteAnualTasklet) {

        return new StepBuilder("reporteAnualStep", jobRepository)
                .tasklet(reporteAnualTasklet, transactionManager)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter(
            DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<CuentaAnual>()
                .dataSource(dataSource)
                .sql("""
                    INSERT IGNORE INTO cuentas_anuales_procesadas
                    (cuenta_id, fecha, transaccion, monto, descripcion)
                    VALUES
                    (:cuentaId, :fecha, :transaccion, :monto, :descripcion)
                    """)
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step cuentasAnualesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<CuentaAnualCsv> cuentaAnualReader,
            CuentaAnualProcessor cuentaAnualProcessor,
            JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter) {

        return new StepBuilder("cuentasAnualesStep", jobRepository)
                .<CuentaAnualCsv, CuentaAnual>chunk(10)
                .transactionManager(transactionManager)
                .reader(cuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)

                .faultTolerant()

                .skip(FlatFileParseException.class)
                .skipLimit(10)

                .retry(TransientDataAccessException.class)
                .retryLimit(3)

                .build();
    }

    @Bean
    public Job cuentasAnualesJob(
            JobRepository jobRepository,
            Step cuentasAnualesStep,
            Step reporteAnualStep) {

        return new JobBuilder("cuentasAnualesJob", jobRepository)
                .start(cuentasAnualesStep)
                .next(reporteAnualStep)
                .build();
    }
}