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

### Nota `start()` vs `run()`
- `start()`: crea un hilo del SO y ejecuta en paralelo.
- `run()`: ejecuta el método en el hilo actual, de forma secuencial.
