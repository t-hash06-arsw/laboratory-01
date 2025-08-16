## Punto I: Hilos de conteo

### Resumen
Se implementó un hilo que imprime números en un rango y se creó un `main` que lanza 3 hilos con intervalos específicos para correr en paralelo.

### Pasos realizados
1. Implementación de `CountThread`:
	- Extiende `Thread`.
	- Constructor recibe `start` y `end` (rango inclusivo).
	- `run()` imprime los números del rango. Si el rango viene invertido, cuenta en sentido contrario.
2. Actualización de `CountThreadsMain`:
	- Crea tres hilos con los intervalos: `[0..99]`, `[99..199]` y `[200..299]`.
	- Inicia los hilos con `start()` para ejecución concurrente.

### Cómo probar
- Ejecutar `com.lab01.app.threads.CountThreadsMain`.
- La salida muestra los números de los tres rangos intercalados por concurrencia.

![alt text](img/image.png)

### Preguntas

1) ¿Qué pasa al ejecutar con `start()`?

- Los tres hilos se ejecutan concurrentemente y la salida por consola se mezcla (las líneas de los distintos rangos aparecen intercaladas de forma no determinista).
- Esto ocurre porque `start()` crea hilos independientes que el sistema planifica y ejecuta en paralelo. `System.out.println` asegura líneas completas, pero no el orden entre hilos.

2) ¿Qué pasa si se usa `run()` en lugar de `start()`?

- Llamar a `run()` ejecuta el código en el hilo actual (main). La ejecución será secuencial: primero todo el primer rango, luego el segundo, luego el tercero.
- No se crean nuevas unidades de ejecución al usar `run()`.

## Punto II: Búsqueda en listas negras con N hilos

### Resumen
Se paralelizó la validación de IPs en listas negras dividiendo el espacio de servidores entre N hilos. Se creó un hilo que busca en un segmento, se sobrecargó `checkHost` para aceptar N, se agregaron los resultados de todos los hilos y se mantuvo un LOG verídico del número de listas revisadas. `Main` permite pasar N como argumento (por defecto 1).

### Pasos realizados
1. Implementación de `BlacklistSearchThread`:
	- Extiende `Thread`.
	- Constructor recibe fachada, IP, `inicio` y `fin` del rango [inicio, fin).
	- `run()` recorre su segmento, acumula IDs de listas donde aparece la IP y cuenta cuántas listas revisó.
	- Expone `getFound()` y `getChecked()`.
2. Actualización de `HostBlackListsValidator`:
	- Nuevo `checkHost(String ip, int nThreads)` que divide `getRegisteredServersCount()` en N partes balanceadas.
	- Lanza N hilos, hace `join()` a todos, agrega ocurrencias y decide si reporta como confiable/no confiable según el umbral.
	- Mantiene el LOG con la suma real de listas revisadas vs el total.
	- Se conserva `checkHost(String ip)` delegando a N=1 para compatibilidad.
3. Actualización de `Main`:
	- Acepta N como primer argumento. Si no se pasa, usa 1.

### Cómo probar
- Ejecutar `com.lab01.app.blacklistvalidator.Main` opcionalmente con N:
  - Ejemplo: N=1 (serial) o N=8 (paralelo). Se mostrará el LOG “Checked Black Lists:X of Y” y la lista de IDs encontrados.
- Para validar casos del enunciado, cambiar la IP en `Main` y ejecutar con distintos N:
  - `200.24.34.55`: reportado como NO confiable rápidamente.
  - `202.24.34.55`: reportado como NO confiable con ocurrencias dispersas.
  - `212.24.24.55`: reportado como confiable (lista vacía).

### Preguntas
1) ¿Se mantiene verídico el LOG de listas revisadas vs total?

- Sí. Se suma `getChecked()` de cada hilo y se reporta junto con el total de servidores registrados.

2) ¿Qué pasa con distintos valores de N?

- Si N ≤ 0 se normaliza a 1; si N > total de servidores se limita a ese total. La división del trabajo reparte uniformemente el residuo para balancear los segmentos.

3) ¿Se detiene la búsqueda global al alcanzar 5 ocurrencias entre todos los hilos?

- No. Esta optimización cruzada entre hilos corresponde al punto II.I para discutir. La implementación actual espera a que todos los hilos terminen y luego decide.

## Punto III: Evaluación de desempeño

### Resumen
Se instrumentó la aplicación para medir el tiempo de ejecución por corrida (en milisegundos), permitiendo variar la cantidad de hilos y la IP bajo prueba desde argumentos. Con esto se pueden ejecutar las 5 pruebas solicitadas y recolectar tiempos para graficar.

### Pasos realizados
1. Actualización de `Main` para medición:
	- Acepta `N` como primer argumento y una IP como segundo argumento (opcional).
	- Mide el tiempo con `System.nanoTime()` alrededor de la llamada a `checkHost`.
	- Imprime: `Threads=N, IP=..., Elapsed(ms)=...` y los IDs de listas encontradas.
2. Preparación de escenarios:
	- IP con ocurrencias tempranas: `200.24.34.55`.
	- IP con ocurrencias dispersas: `202.24.34.55`.
	- IP sin ocurrencias: `212.24.24.55`.

### Cómo probar
- Ejecutar variando N e IP para recolectar tiempos (correr varias veces y promediar):
	1) Un solo hilo: `N=1`.
	2) Tantos hilos como núcleos: `N=Runtime.getRuntime().availableProcessors()`.
	3) El doble de núcleos: `N=2*Runtime.getRuntime().availableProcessors()`.
	4) 50 hilos: `N=50`.
	5) 100 hilos: `N=100`.
- Anotar el consumo de CPU y memoria con jVisualVM durante cada prueba.
- Graficar tiempo vs número de hilos y analizar.

### Preguntas
1) ¿Por qué no siempre mejora con muchos hilos (p.ej., 500) y cómo se compara con 200?

- Por sobrecosto de creación/planificación de hilos, cambio de contexto, contención y saturación de CPU/memoria. Con 200 puede haber mejor relación trabajo/sobrecosto que con 500.

2) ¿Tantos hilos como núcleos vs el doble?

- Usar el doble puede aumentar sobrecosto y colas en CPU; según el caso, mejora poco o empeora por contención. Igualar núcleos suele ser un buen punto de partida.

3) ¿1 hilo en 100 máquinas o c hilos en 100/c máquinas?

- Distribuir reduce contención local y puede escalar mejor si el costo de coordinación/red es bajo. Con c hilos en 100/c máquinas se aprovechan núcleos locales manteniendo concurrencia total, pero la ganancia real depende de latencias y costos de coordinación (aplica la ley de Amdahl considerando fracción no paralelizable y overhead de distribución).

## Punto IV: Análisis con la ley de Amdahl

### Resumen
Se interpretaron los resultados de la parte III usando la ley de Amdahl, identificando el impacto de la fracción no paralelizable y del overhead de concurrencia sobre el escalamiento al aumentar N hilos y al distribuir en varias máquinas.

### Pasos realizados
1. Cálculo de speedup observado: `S(N) = T(1) / T(N)` con los tiempos medidos.
2. Estimación cualitativa de la fracción paralelizable `P` a partir de la tendencia de `S(N)`.
3. Análisis del punto de rendimientos decrecientes por overhead (hilos en exceso vs núcleos disponibles).

### Cómo probar
- A partir de los tiempos del Punto III, calcular `S(N)` para cada N y compararlo con `S(N)=1/((1-P)+P/N)` para una `P` aproximada. Ver dónde el overhead práctico (planificación, memoria, sincronización) desvía la curva teórica.

### Preguntas
1) ¿Por qué no se logra el mejor desempeño con 500 hilos y cómo se compara con 200?

- Porque el problema tiene una parte no paralelizable y además existe overhead de concurrencia (creación/cambio de contexto/colas/GC). Con 500 hilos se sobrecarga el planificador y se contiende más por CPU y memoria, degradando el rendimiento. Con 200 suele haber menor overhead por hilo y mejor utilización efectiva, por lo que puede rendir mejor que 500.

2) ¿Tantos hilos como núcleos vs el doble?

- Con hilos ≈ núcleos se minimiza cambio de contexto y se aprovecha al máximo la CPU. Con el doble, la contención y el cambio de contexto suben; a veces el rendimiento es similar o peor. Solo ayuda si hay latencias I/O o espera significativa; en cómputo CPU-bound, no suele mejorar.

3) ¿1 hilo en 100 máquinas o c hilos en 100/c máquinas (siendo c núcleos)?

- Amdahl mejora en escenarios distribuidos cuando el overhead de red/coord es pequeño comparado con el trabajo. 1 hilo en 100 máquinas elimina contención local pero añade latencia/red. c hilos en 100/c máquinas balancea el uso de núcleos locales manteniendo concurrencia total y puede rendir mejor si la partición es embarrassingly parallel y el overhead de distribución es bajo. El límite de Amdahl permanece por la porción no paralelizable y el overhead añadido.