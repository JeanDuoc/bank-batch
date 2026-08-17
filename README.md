# Bank Batch - Migración de Procesos Legacy

Proyecto desarrollado con Spring Batch para modernizar procesos batch legacy del Banco XYZ.

El sistema procesa archivos CSV con datos bancarios, aplica reglas de validación y transformación, almacena los resultados válidos en MySQL y genera reportes automáticos.

## Tecnologías utilizadas

- Java 17
- Spring Boot 4.1
- Spring Batch
- Spring JDBC
- MySQL
- Gradle

## Procesos Batch

El sistema contiene tres Jobs principales:

### 1. Reporte de Transacciones Diarias

Job: `transaccionesJob`

Lee el archivo:

`data/transacciones.csv`

Realiza:

- Validación de montos.
- Validación de fechas.
- Validación de tipos de transacción.
- Omisión de registros inválidos.
- Almacenamiento en MySQL.
- Generación de un resumen diario.

Reporte generado:

`reportes/resumen_transacciones.txt`

### 2. Cálculo de Intereses Mensuales

Job: `interesesJob`

Lee:

`data/intereses.csv`

Realiza:

- Validación de saldos.
- Validación de edades.
- Validación del tipo de cuenta.
- Manejo de registros duplicados.
- Cálculo de intereses.
- Actualización del saldo final.

Las tasas utilizadas son:

- Ahorro: 1%
- Préstamo: 2%

### 3. Estados de Cuenta Anuales

Job: `cuentasAnualesJob`

Lee:

`data/cuentas_anuales.csv`

Realiza:

- Validación de fechas.
- Validación de montos.
- Validación de campos vacíos.
- Manejo de registros duplicados.
- Almacenamiento de movimientos anuales.
- Generación de un informe para auditoría.

Reporte generado:

`reportes/informe_cuentas_anuales.txt`

## Manejo de errores

Los Steps utilizan tolerancia a fallos de Spring Batch mediante:

- `skip`
- `skipLimit`
- `retry`
- `retryLimit`

Los registros inválidos pueden ser omitidos sin detener completamente el Job.

Los errores temporales de acceso a la base de datos pueden ser reintentados automáticamente.

## Base de datos

El proyecto utiliza MySQL.

Base de datos:

`bank_batch`

Tablas principales:

- `transacciones_procesadas`
- `intereses_procesados`
- `cuentas_anuales_procesadas`

Spring Batch también genera sus tablas internas `BATCH_*` para almacenar información sobre las ejecuciones de Jobs y Steps.

## Configuración

La contraseña de MySQL se configura mediante una variable de entorno:

`DB_PASSWORD`

En IntelliJ se puede agregar desde:

`Run > Edit Configurations > Environment variables`

Ejemplo:

`DB_PASSWORD=contraseña_mysql`

## Seleccionar un Job

En `application.properties` se puede indicar qué Job ejecutar.

Ejemplo:

`spring.batch.job.name=transaccionesJob`

También se puede utilizar:

`spring.batch.job.name=interesesJob`

o:

`spring.batch.job.name=cuentasAnualesJob`

## Ejecución

Ejecutar la clase:

`BankBatchApplication`

Para realizar nuevas ejecuciones de un mismo Job se puede proporcionar un parámetro distinto, por ejemplo:

`run.id(long)=1`

Luego:

`run.id(long)=2`

## Estructura principal

```text
src/main/java/cl.duoc.bankbatch
├── config
├── dto
├── model
├── processor
├── tasklet
└── BankBatchApplication.java

src/main/resources
├── data
│   ├── transacciones.csv
│   ├── intereses.csv
│   └── cuentas_anuales.csv
└── application.properties

reportes
├── resumen_transacciones.txt
└── informe_cuentas_anuales.txt