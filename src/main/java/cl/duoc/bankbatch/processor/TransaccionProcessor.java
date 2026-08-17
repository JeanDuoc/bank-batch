package cl.duoc.bankbatch.processor;

import cl.duoc.bankbatch.dto.TransaccionCsv;
import cl.duoc.bankbatch.model.Transaccion;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TransaccionProcessor implements ItemProcessor<TransaccionCsv, Transaccion> {

    @Override
    public Transaccion process(TransaccionCsv item) {

        try {
            Integer id = Integer.parseInt(item.getId());
            LocalDate fecha = LocalDate.parse(item.getFecha());
            BigDecimal monto = new BigDecimal(item.getMonto());
            String tipo = item.getTipo();

            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Transacción omitida por monto inválido: " + item.getMonto());
                return null;
            }

            if (!tipo.equalsIgnoreCase("debito")
                    && !tipo.equalsIgnoreCase("credito")) {

                System.out.println("Transacción omitida por tipo inválido: " + tipo);
                return null;
            }

            return new Transaccion(id, fecha, monto, tipo.toLowerCase());

        } catch (NumberFormatException | DateTimeParseException e) {

            System.out.println("Transacción omitida por formato inválido: "
                    + item.getId() + ", "
                    + item.getFecha() + ", "
                    + item.getMonto());

            return null;
        }
    }
}