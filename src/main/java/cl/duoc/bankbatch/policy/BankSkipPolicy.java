package cl.duoc.bankbatch.policy;

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

@Component
public class BankSkipPolicy implements SkipPolicy {

    private static final long MAX_SKIP_COUNT = 10;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) {

        // Spring Batch puede consultar la política con un contador negativo
        // para saber si una excepción es potencialmente omisible.
        if (skipCount < 0) {
            return t instanceof FlatFileParseException;
        }

        // Solo permitimos omitir errores producidos al leer/formatear el CSV.
        if (t instanceof FlatFileParseException) {

            if (skipCount < MAX_SKIP_COUNT) {
                System.out.println(
                        "Registro CSV omitido por formato inválido. "
                                + "Cantidad de errores: " + (skipCount + 1)
                );

                return true;
            }

            System.out.println(
                    "Se alcanzó el límite máximo de "
                            + MAX_SKIP_COUNT
                            + " registros inválidos."
            );
        }

        return false;
    }
}