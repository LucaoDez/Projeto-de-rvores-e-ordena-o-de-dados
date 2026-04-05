#ifndef SUCCINCT_BIT_VECTOR_H
#define SUCCINCT_BIT_VECTOR_H

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

#define BLOCK_SIZE 64          /* bits por bloco (1 palavra de 64 bits) */
#define SUPER_BLOCK_SIZE 2048  /* bits por superbloco (32 blocos) */

/* Vetor de bits sucinto com suporte a rank/select em O(1) */
typedef struct {
    uint64_t *words;            /* Bits compactados em palavras de 64 bits */
    int length;                 /* Número de bits lógicos */

    /* Estruturas auxiliares de rank */
    int *super_block_rank;      /* rank acumulado a cada SUPER_BLOCK bits */
    int *block_rank;            /* rank incremental a cada BLOCK bits */

    int num_super_blocks;       /* Número de superblocos */
    int num_blocks;             /* Número de blocos */
} SuccinctBitVector;

/* Criar vetor sucinto a partir de array de booleanos */
SuccinctBitVector* succinct_bit_vector_create(bool *bits, int length);

/* Liberar memória do vetor */
void succinct_bit_vector_free(SuccinctBitVector *bv);

/* rank1(i) - número de 1s em bits[0..i-1] em O(1) */
int succinct_bit_vector_rank1(SuccinctBitVector *bv, int i);

/* rank0(i) - número de 0s em bits[0..i-1] em O(1) */
int succinct_bit_vector_rank0(SuccinctBitVector *bv, int i);

/* select1(j) - posição do j-ésimo 1 (1-indexada) */
int succinct_bit_vector_select1(SuccinctBitVector *bv, int j);

/* Retorna o bit na posição i */
bool succinct_bit_vector_get(SuccinctBitVector *bv, int i);

/* Número total de bits */
int succinct_bit_vector_length(SuccinctBitVector *bv);

/* Total de 1s no vetor */
int succinct_bit_vector_total_ones(SuccinctBitVector *bv);

/* Calcula memória usada pelo vetor */
size_t succinct_bit_vector_memory_usage(SuccinctBitVector *bv);

#endif /* SUCCINCT_BIT_VECTOR_H */
