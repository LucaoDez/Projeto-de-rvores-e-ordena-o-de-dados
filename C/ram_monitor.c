#include "ram_monitor.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

/* Lê RSS do sistema (Linux) */
size_t get_current_rss(void) {
    FILE *fp;
    size_t rss = 0;
    char line[128];

    fp = fopen("/proc/self/status", "r");
    if (fp != NULL) {
        while (fgets(line, 128, fp) != NULL) {
            if (strncmp(line, "VmRSS:", 6) == 0) {
                /* Formato: VmRSS:      1234 kB */
                sscanf(line, "VmRSS:%lu kB", &rss);
                rss *= 1024;  /* Converte de KB para bytes */
                break;
            }
        }
        fclose(fp);
    }
    return rss;
}

RAMMonitor* ram_monitor_create(void) {
    RAMMonitor *monitor = (RAMMonitor*) malloc(sizeof(RAMMonitor));
    if (monitor == NULL) return NULL;

    monitor->initial_memory = 0;
    monitor->peak_memory = 0;
    monitor->current_memory = 0;
    monitor->start_time = 0;
    monitor->end_time = 0;

    return monitor;
}

void ram_monitor_start(RAMMonitor *monitor) {
    if (monitor != NULL) {
        monitor->initial_memory = get_current_rss();
        monitor->peak_memory = monitor->initial_memory;
        monitor->current_memory = monitor->initial_memory;
        monitor->start_time = clock();
    }
}

void ram_monitor_update(RAMMonitor *monitor, size_t allocated) {
    if (monitor != NULL) {
        monitor->current_memory = monitor->initial_memory + allocated;
        if (monitor->current_memory > monitor->peak_memory) {
            monitor->peak_memory = monitor->current_memory;
        }
    }
}

void ram_monitor_stop(RAMMonitor *monitor) {
    if (monitor != NULL) {
        /* Tenta ler RSS do sistema se disponível */
        size_t final_rss = get_current_rss();
        if (final_rss > 0) {
            monitor->current_memory = final_rss;
            if (monitor->current_memory > monitor->peak_memory) {
                monitor->peak_memory = monitor->current_memory;
            }
        }
        monitor->end_time = clock();
    }
}

size_t ram_monitor_get_peak(RAMMonitor *monitor) {
    return (monitor != NULL) ? monitor->peak_memory : 0;
}

size_t ram_monitor_get_current(RAMMonitor *monitor) {
    return (monitor != NULL) ? monitor->current_memory : 0;
}

double ram_monitor_get_time_elapsed(RAMMonitor *monitor) {
    if (monitor == NULL) return 0.0;
    return (double)(monitor->end_time - monitor->start_time) / CLOCKS_PER_SEC;
}

void ram_monitor_print_report(RAMMonitor *monitor, const char *name) {
    if (monitor == NULL) return;

    size_t diff = monitor->peak_memory - monitor->initial_memory;
    double time_elapsed = ram_monitor_get_time_elapsed(monitor);

    printf("\n╔════════════════════════════════════════════════════╗\n");
    printf("║         RELATÓRIO DE MONITORAMENTO: %-20s║\n", name);
    printf("╠════════════════════════════════════════════════════╣\n");
    printf("║  Memória inicial          : %12.2f MB          ║\n",
           monitor->initial_memory / (1024.0 * 1024.0));
    printf("║  Memória pico             : %12.2f MB          ║\n",
           monitor->peak_memory / (1024.0 * 1024.0));
    printf("║  Diferença (Δ)            : %12.2f MB          ║\n",
           diff / (1024.0 * 1024.0));
    printf("║  Tempo decorrido          : %12.3f segundos    ║\n", time_elapsed);
    printf("╚════════════════════════════════════════════════════╝\n");
}

void ram_monitor_free(RAMMonitor *monitor) {
    if (monitor != NULL) {
        free(monitor);
    }
}
