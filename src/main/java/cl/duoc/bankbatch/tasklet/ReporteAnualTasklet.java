package cl.duoc.bankbatch.tasklet;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ReporteAnualTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public ReporteAnualTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        List<Map<String, Object>> cuentas = jdbcTemplate.queryForList("""
                SELECT DISTINCT cuenta_id
                FROM cuentas_anuales_procesadas
                ORDER BY cuenta_id
                """);

        StringBuilder reporte = new StringBuilder();

        reporte.append("INFORME ANUAL DE CUENTAS - AUDITORIA\n");
        reporte.append("===================================\n\n");

        for (Map<String, Object> cuenta : cuentas) {

            Integer cuentaId = ((Number) cuenta.get("cuenta_id")).intValue();

            reporte.append("CUENTA: ")
                    .append(cuentaId)
                    .append("\n");

            reporte.append("-----------------------------------\n");

            List<Map<String, Object>> movimientos = jdbcTemplate.queryForList("""
                    SELECT fecha, transaccion, monto, descripcion
                    FROM cuentas_anuales_procesadas
                    WHERE cuenta_id = ?
                    ORDER BY fecha
                    """, cuentaId);

            BigDecimal total = BigDecimal.ZERO;

            for (Map<String, Object> movimiento : movimientos) {

                BigDecimal monto =
                        (BigDecimal) movimiento.get("monto");

                total = total.add(monto);

                reporte.append("Fecha: ")
                        .append(movimiento.get("fecha"))
                        .append("\n");

                reporte.append("Transacción: ")
                        .append(movimiento.get("transaccion"))
                        .append("\n");

                reporte.append("Monto: $")
                        .append(monto)
                        .append("\n");

                reporte.append("Descripción: ")
                        .append(movimiento.get("descripcion"))
                        .append("\n");

                reporte.append("\n");
            }

            reporte.append("Cantidad de movimientos: ")
                    .append(movimientos.size())
                    .append("\n");

            reporte.append("Total anual: $")
                    .append(total)
                    .append("\n");

            reporte.append("===================================\n\n");
        }

        Path carpeta = Path.of("reportes");
        Files.createDirectories(carpeta);

        Path archivo =
                carpeta.resolve("informe_cuentas_anuales.txt");

        Files.writeString(
                archivo,
                reporte.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "Informe anual generado en: "
                        + archivo.toAbsolutePath()
        );

        return RepeatStatus.FINISHED;
    }
}