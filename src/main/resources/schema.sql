CREATE TABLE IF NOT EXISTS transacciones_procesadas (
                                                        id INT PRIMARY KEY,
                                                        fecha DATE NOT NULL,
                                                        monto DECIMAL(15,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL
    );

CREATE TABLE IF NOT EXISTS intereses_procesados (
                                                    cuenta_id INT PRIMARY KEY,
                                                    nombre VARCHAR(150) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    edad INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    interes_calculado DECIMAL(15,2) NOT NULL,
    saldo_final DECIMAL(15,2) NOT NULL
    );

CREATE TABLE IF NOT EXISTS cuentas_anuales_procesadas (
                                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                          cuenta_id INT NOT NULL,
                                                          fecha DATE NOT NULL,
                                                          transaccion VARCHAR(50) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,

    UNIQUE KEY uk_movimiento_anual (
                                       cuenta_id,
                                       fecha,
                                       transaccion,
                                       monto,
                                       descripcion
                                   )
    );