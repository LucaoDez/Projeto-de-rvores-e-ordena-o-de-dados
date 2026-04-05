#ifndef RAM_MONITOR_H
#define RAM_MONITOR_H

#include <stddef.h>
#include <time.h>

/* Monitorador de RAM */
typedef struct {
    size_t initial_memory;      /* Memória inicial */
    size_t peak_memory;         /* Pico de memória */
    size_t current_memory;      /* Memória atual */
    clock_t start_time;         /* Tempo de início */
    clock_t end_time;           /* Tempo de fim */
} RAMMonitor;

/* Criar monitor */
RAMMonitor* ram_monitor_create(void);

/* Iniciar monitoramento */
void ram_monitor_start(RAMMonitor *monitor);

/* Atualizar memória (a cada checkpoint) */
void ram_monitor_update(RAMMonitor *monitor, size_t allocated);

/* Parar monitoramento */
void ram_monitor_stop(RAMMonitor *monitor);

/* Getters */
size_t ram_monitor_get_peak(RAMMonitor *monitor);
size_t ram_monitor_get_current(RAMMonitor *monitor);
double ram_monitor_get_time_elapsed(RAMMonitor *monitor);

/* Exibir relatório */
void ram_monitor_print_report(RAMMonitor *monitor, const char *name);

/* Liberar */
void ram_monitor_free(RAMMonitor *monitor);

/* Função utilitária para ler RSS (Resident Set Size) do sistema */
size_t get_current_rss(void);

#endif /* RAM_MONITOR_H */
