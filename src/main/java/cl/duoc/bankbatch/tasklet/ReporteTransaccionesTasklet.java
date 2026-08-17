package cl.duoc.bankbatch.tasklet;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ReporteTransaccionesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public ReporteTransaccionesTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        List<Map<String, Object>> resultados = jdbcTemplate.queryForList("""
                SELECT
                    fecha,
                    COUNT(*) AS cantidad,
                    SUM(CASE WHEN tipo = 'credito' THEN monto ELSE 0 END) AS total_creditos,
                    SUM(CASE WHEN tipo = 'debito' THEN monto ELSE 0 END) AS total_debitos,
                    SUM(monto) AS total_movimientos
                FROM transacciones_procesadas
                GROUP BY fecha
                ORDER BY fecha
                """);

        StringBuilder reporte = new StringBuilder();

        reporte.append("REPORTE DE TRANSACCIONES DIARIAS\n");
        reporte.append("================================\n\n");

        for (Map<String, Object> fila : resultados) {

            reporte.append("Fecha: ")
                    .append(fila.get("fecha"))
                    .append("\n");

            reporte.append("Cantidad de transacciones: ")
                    .append(fila.get("cantidad"))
                    .append("\n");

            reporte.append("Total créditos: $")
                    .append(fila.get("total_creditos"))
                    .append("\n");

            reporte.append("Total débitos: $")
                    .append(fila.get("total_debitos"))
                    .append("\n");

            reporte.append("Total movimientos: $")
                    .append(fila.get("total_movimientos"))
                    .append("\n");

            reporte.append("--------------------------------\n");
        }

        Path carpeta = Path.of("reportes");
        Files.createDirectories(carpeta);

        Path archivo = carpeta.resolve("resumen_transacciones.txt");

        Files.writeString(
                archivo,
                reporte.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "Reporte generado en: " + archivo.toAbsolutePath()
        );

        return RepeatStatus.FINISHED;
    }
}