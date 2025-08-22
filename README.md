## ARSW Lab 01 — Hilos y Búsqueda en Listas Negras

Laboratorio introductorio sobre concurrencia en Java. Primero se construyen hilos simples de conteo, luego se paraleliza una búsqueda en listas negras a través de muchos servidores, y finalmente se corren pequeños experimentos de desempeño y se reflexiona con la Ley de Amdahl.

## Qué pide la guía (resumen rápido)

- Parte I — Fundamentos de hilos
	- Implementar un `Thread` que imprime números en un rango.
	- Lanzar tres hilos con rangos distintos usando `start()`; comparar el comportamiento frente a llamar `run()` directamente.

- Parte II — Búsqueda en listas negras (vergonzosamente paralela)
	- Dada una IP, consultarla en N servidores de listas negras usando la fachada `HostBlacklistsDataSourceFacade`.
	- Reportar el host como NO confiable si aparece en al menos 5 listas; de lo contrario, confiable.
	- Agregar una clase de hilo para escanear un segmento de servidores y un método `checkHost(ip, N)` que divide el trabajo en N hilos, espera a todos (`join`), agrega resultados y registra el número de listas revisadas vs el total.
	- Casos conocidos para probar: 200.24.34.55 (aciertos tempranos), 202.24.34.55 (dispersos), 212.24.24.55 (ninguno).

- Parte II.I — Discusión (no implementada): considerar parada temprana global cuando se alcance el umbral.

- Parte III — Desempeño
	- Medir tiempo extremo a extremo para: 1 hilo, núcleos, 2×núcleos, 50 y 100 hilos.
	- Observar CPU/memoria en jVisualVM y graficar tiempo vs hilos.

- Parte IV — Ley de Amdahl
	- Analizar rendimientos decrecientes con muchos hilos y comparar configuraciones.

## Qué está implementado (resuelto)

- Hilos de conteo
	- `com.lab01.app.threads.CountThread`: imprime números en un rango inclusivo (soporta rangos invertidos).
	- `com.lab01.app.threads.CountThreadsMain`: lanza tres hilos con rangos [0..99], [99..199], [200..299] usando `start()`.

- Búsqueda paralela en listas negras
	- `com.lab01.app.blacklistvalidator.BlacklistSearchThread`: recorre un subrango de servidores; lleva los IDs de listas donde aparece la IP y cuántos servidores revisó.
	- `com.lab01.app.blacklistvalidator.HostBlackListsValidator`:
		- Sobrecarga `checkHost(String ip, int nThreads)`: divide `getRegisteredServersCount()` entre N hilos (balanceado, manejando residuos), `start()` + `join()`, agrega coincidencias, registra “revisadas vs total” y reporta confiabilidad con el umbral de 5 listas.
		- Mantiene `checkHost(String ip)` delegando a N=1 para compatibilidad.
	- `com.lab01.app.blacklistvalidator.Main`: acepta N como primer argumento (por defecto 1) y una IP opcional. Mide e imprime el tiempo y los IDs de listas donde apareció la IP.

- Notas
	- La fachada `com.lab01.app.spamkeywordsdatasource.HostBlacklistsDataSourceFacade` es thread-safe y no se modifica.
	- La parada temprana global entre hilos no está implementada a propósito (tema de la Parte II.I).
	- El log conserva un “Checked Black Lists: X of Y” verídico sumando el conteo revisado por cada hilo.
	- Manejo defensivo: N ≤ 0 → 1; N > servidores totales → se limita; el particionamiento balanceado reparte el residuo.

## Mapa del código

- `com.lab01.app.threads`
	- `CountThread`: imprime números en un rango.
	- `CountThreadsMain`: lanza tres hilos de conteo.

- `com.lab01.app.blacklistvalidator`
	- `BlacklistSearchThread`: trabajador que escanea un tramo [inicio, fin) de servidores.
	- `HostBlackListsValidator`: ofrece `checkHost(ip)` y `checkHost(ip, nThreads)`; agrega resultados y registra conteos.
	- `Main`: punto de entrada CLI; parsea argumentos, llama al validador, toma tiempos y muestra resultados.

- `com.lab01.app.spamkeywordsdatasource`
	- `HostBlacklistsDataSourceFacade`: fachada thread-safe para consultar N servidores de listas negras y reportar confiabilidad (sin cambios).

## Cómo funciona (recorrido breve)

1) Se obtiene el total de servidores vía la fachada y se dividen en N segmentos contiguos.
2) Se inician N instancias de `BlacklistSearchThread`, cada una escanea su segmento y recuerda los IDs de listas donde aparece la IP.
3) Se hace `join()` a todos, se fusionan los IDs encontrados y se suman los “revisados” por hilo para el log.
4) Si los aciertos totales ≥ 5, se marca el host como NO confiable; en caso contrario, confiable. Siempre se registra “revisadas vs total” antes de retornar la lista de IDs de listas negras coincidentes.

## Experimentos para ejecutar (de la guía)

Para una IP con aciertos dispersos (p. ej., 202.24.34.55), medir el tiempo para:
1. 1 hilo
2. número de núcleos
3. 2× número de núcleos
4. 50 hilos
5. 100 hilos

Mientras corre, observar CPU y memoria en jVisualVM. Graficar tiempo vs hilos. Esperar rendimientos decrecientes más allá de cierto punto por sobrecosto de planificación/cambios de contexto y por la parte no paralelizable del algoritmo (Ley de Amdahl).

## Limitaciones y próximos pasos

- Sin parada temprana global: los hilos no se cancelan cuando la suma de aciertos llega a 5; terminan sus tramos. Es la próxima optimización a considerar (requiere estado compartido y coordinación).
- Considerar pruebas unitarias para particionamiento y agregación.
- Para estudios de desempeño, promediar varias corridas por configuración para reducir ruido.
