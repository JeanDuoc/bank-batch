package cl.duoc.bankbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchThreadConfig {

    @Bean
    public AsyncTaskExecutor batchTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Cantidad de hilos base disponibles
        executor.setCorePoolSize(4);

        // Máximo de hilos simultáneos
        executor.setMaxPoolSize(4);

        // Tareas que pueden esperar mientras los hilos están ocupados
        executor.setQueueCapacity(20);

        // Nombre para reconocer los hilos en consola
        executor.setThreadNamePrefix("bank-batch-");

        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.initialize();

        return executor;
    }
}