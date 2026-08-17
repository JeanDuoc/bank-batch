package cl.duoc.bankbatch.processor;

import cl.duoc.bankbatch.dto.CuentaAnualCsv;
import cl.duoc.bankbatch.model.CuentaAnual;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualCsv, CuentaAnual> {

    @Override
    public CuentaAnual process(CuentaAnualCsv item) {

        try {
            Integer cuentaId = Integer.parseInt(item.getCuentaId());
            LocalDate fecha = LocalDate.parse(item.getFecha());
            String transaccion = item.getTransaccion();
            BigDecimal monto = new BigDecimal(item.getMonto());
            String descripcion = item.getDescripcion();

            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println(
                        "Registro anual omitido por monto inválido: "
                                + cuentaId + " - " + item.getMonto()
                );
                return null;
            }

            if (transaccion == null || transaccion.isBlank()) {
                System.out.println(
                        "Registro anual omitido por transacción vacía: "
                                + cuentaId
                );
                return null;
            }

            if (descripcion == null || descripcion.isBlank()) {
                System.out.println(
                        "Registro anual omitido por descripción vacía: "
                                + cuentaId
                );
                return null;
            }

            CuentaAnual cuentaAnual = new CuentaAnual();

            cuentaAnual.setCuentaId(cuentaId);
            cuentaAnual.setFecha(fecha);
            cuentaAnual.setTransaccion(transaccion.toLowerCase());
            cuentaAnual.setMonto(monto);
            cuentaAnual.setDescripcion(descripcion);

            return cuentaAnual;

        } catch (NumberFormatException e) {

            System.out.println(
                    "Registro anual omitido por formato numérico inválido: "
                            + item.getCuentaId()
            );

            return null;

        } catch (DateTimeParseException e) {

            System.out.println(
                    "Registro anual omitido por fecha inválida: "
                            + item.getFecha()
            );

            return null;
        }
    }
}