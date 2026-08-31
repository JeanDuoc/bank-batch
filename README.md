## Cumplimiento de requisitos

### Procesamiento Batch

El proyecto utiliza Spring Batch para procesar archivos CSV provenientes de un
sistema bancario legado.

Se implementaron tres Jobs independientes:

- `transaccionesJob`: procesa transacciones diarias y genera un reporte resumen.
- `interesesJob`: calcula intereses mensuales para las cuentas procesadas.
- `cuentasAnualesJob`: procesa movimientos anuales y genera un informe para auditoría.

Cada Job utiliza un `FlatFileItemReader` para leer archivos CSV, un
`ItemProcessor` para validar y transformar los datos, y un
`JdbcBatchItemWriter` para almacenar los resultados en MySQL.

### Validación de datos

Los ItemProcessor validan los registros antes de almacenarlos.

Entre las validaciones realizadas se encuentran:

- Montos inválidos.
- Fechas con formato incorrecto.
- Tipos de transacción incorrectos.
- Datos numéricos inválidos.
- Registros inconsistentes.

Los registros que no cumplen las reglas de negocio son filtrados y no son
enviados al Writer.

### Tolerancia a fallos

Se implementó una política personalizada llamada `BankSkipPolicy`.

Esta política permite omitir registros CSV que presentan errores de formato,
estableciendo un límite máximo de registros inválidos antes de detener el
procesamiento.

También se configuró una política de reintentos para errores transitorios de
acceso a la base de datos:

- Máximo de registros omitidos: 10.
- Máximo de reintentos de base de datos: 3.

### Escalabilidad

Para mejorar el rendimiento del procesamiento Batch se implementó
multithreading mediante `ThreadPoolTaskExecutor`.

Configuración utilizada:

- Core Pool Size: 4
- Max Pool Size: 4
- Queue Capacity: 20

Los Steps encargados de procesar los archivos CSV utilizan este TaskExecutor,
permitiendo procesamiento concurrente.

### Base de datos

El proyecto utiliza MySQL como base de datos relacional.

Base de datos utilizada:

`bank_batch`

Las tablas de resultados son:

- `transacciones_procesadas`
- `intereses_procesados`
- `cuentas_anuales_procesadas`

Spring Batch utiliza además sus propias tablas internas para almacenar la
información de ejecución de Jobs y Steps.

El archivo `schema.sql` crea automáticamente las tablas necesarias si estas no
existen.

### Configuración

La contraseña de MySQL no se almacena directamente en el repositorio.

Se utiliza la variable de entorno:

`DB_PASSWORD`

Ejemplo:

`DB_PASSWORD=contraseña_mysql`

### Ejecución de Jobs

El Job que se ejecutará se configura en `application.properties`.

Ejemplo:

`spring.batch.job.name=transaccionesJob`

Jobs disponibles:

`transaccionesJob`

`interesesJob`

`cuentasAnualesJob`

Para realizar una nueva ejecución se puede utilizar un parámetro `run.id`
diferente.

Ejemplo:

`run.id(long)=15`

## Pruebas de escalabilidad

Para evaluar el procesamiento paralelo se realizaron pruebas sobre el
`transaccionesJob`, utilizando diferentes cantidades de hilos en el
`ThreadPoolTaskExecutor`.

| Cantidad de hilos | Tiempo de ejecución |
|-------------------|---------------------|
| 1 hilo            | 621 ms              |
| 2 hilos           | 587 ms              |
| 4 hilos           | 594 ms              |
| 8 hilos           | 578 ms              |

La configuración de 8 hilos obtuvo el menor tiempo de ejecución durante las
pruebas, alcanzando 578 ms.

Por este motivo se seleccionó una configuración final de 8 hilos para el
procesamiento Batch.

Las diferencias entre las configuraciones son reducidas debido al tamaño del
dataset utilizado. Sin embargo, las pruebas permiten comprobar el
funcionamiento del procesamiento paralelo y comparar el comportamiento del
sistema bajo diferentes niveles de concurrencia.