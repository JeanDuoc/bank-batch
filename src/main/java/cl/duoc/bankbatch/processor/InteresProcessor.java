package cl.duoc.bankbatch.processor;

import cl.duoc.bankbatch.dto.InteresCsv;
import cl.duoc.bankbatch.model.Interes;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InteresProcessor implements ItemProcessor<InteresCsv, Interes> {

    @Override
    public Interes process(InteresCsv item) {

        try {
            Integer cuentaId = Integer.parseInt(item.getCuentaId());

            if (item.getNombre() == null || item.getNombre().isBlank()) {
                System.out.println("Cuenta omitida por nombre vacío: " + cuentaId);
                return null;
            }

            if (item.getSaldo() == null || item.getSaldo().isBlank()) {
                System.out.println("Cuenta omitida por saldo vacío: " + cuentaId);
                return null;
            }

            BigDecimal saldo = new BigDecimal(item.getSaldo());
            Integer edad = Integer.parseInt(item.getEdad());
            String tipo = item.getTipo();

            if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Cuenta omitida por saldo inválido: " + cuentaId);
                return null;
            }

            if (edad < 18 || edad > 100) {
                System.out.println("Cuenta omitida por edad inválida: " + cuentaId);
                return null;
            }

            if (tipo == null ||
                    (!tipo.equalsIgnoreCase("ahorro")
                            && !tipo.equalsIgnoreCase("prestamo"))) {

                System.out.println("Cuenta omitida por tipo inválido: " + cuentaId);
                return null;
            }

            BigDecimal tasa;

            if (tipo.equalsIgnoreCase("ahorro")) {
                tasa = new BigDecimal("0.01");
            } else {
                tasa = new BigDecimal("0.02");
            }

            BigDecimal interesCalculado = saldo
                    .multiply(tasa)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal saldoFinal = saldo
                    .add(interesCalculado)
                    .setScale(2, RoundingMode.HALF_UP);

            Interes interes = new Interes();

            interes.setCuentaId(cuentaId);
            interes.setNombre(item.getNombre());
            interes.setSaldo(saldo);
            interes.setEdad(edad);
            interes.setTipo(tipo.toLowerCase());
            interes.setInteresCalculado(interesCalculado);
            interes.setSaldoFinal(saldoFinal);

            return interes;

        } catch (NumberFormatException e) {

            System.out.println(
                    "Cuenta omitida por formato numérico inválido: "
                            + item.getCuentaId()
            );

            return null;
        }
    }
}