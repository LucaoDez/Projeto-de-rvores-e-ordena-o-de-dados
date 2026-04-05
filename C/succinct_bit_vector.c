#include "succinct_bit_vector.h"
#include <stdlib.h>
#include <string.h>

/* Conta número de bits 1 em um uint64_t (popcount) */
static int popcount(uint64_t x) {
    uint64_t n = x;
    n = n - ((n >> 1) & 0x5555555555555555ULL);
    n = (n & 0x3333333333333333ULL) + ((n >> 2) & 0x3333333333333333ULL);
    n = (n + (n >> 4)) & 0x0f0f0f0f0f0f0f0fULL;
    n = n + (n >> 8);
    n = n + (n >> 16);
    n = n + (n >> 32);
    return n & 0x3f;
}

SuccinctBitVector* succinct_bit_vector_create(bool *bits, int length) {
    SuccinctBitVector *bv = (SuccinctBitVector*) malloc(sizeof(SuccinctBitVector));
    if (bv == NULL) return NULL;

    bv->length = length;
    int num_words = (length + 63) / 64;
    bv->words = (uint64_t*) calloc(num_words, sizeof(uint64_t));
    if (bv->words == NULL) {
        free(bv);
        return NULL;
    }

    /* Preenche as palavras */
    for (int i = 0; i < length; i++) {
        if (bits[i]) {
            bv->words[i / 64] |= (1ULL << (63 - (i % 64)));
        }
    }

    /* Pré-computa rank */
    int super_block_count = (length + SUPER_BLOCK_SIZE - 1) / SUPER_BLOCK_SIZE;
    int block_count = (length + BLOCK_SIZE - 1) / BLOCK_SIZE;

    bv->num_super_blocks = super_block_count;
    bv->num_blocks = block_count;

    bv->super_block_rank = (int*) malloc((super_block_count + 1) * sizeof(int));
    bv->block_rank = (int*) malloc((block_count + 1) * sizeof(int));
    if (bv->super_block_rank == NULL || bv->block_rank == NULL) {
        free(bv->super_block_rank);
        free(bv->block_rank);
        free(bv->words);
        free(bv);
        return NULL;
    }

    int cumulative = 0;
    int blocks_per_super = SUPER_BLOCK_SIZE / BLOCK_SIZE;

    for (int i = 0; i < block_count; i++) {
        if (i % blocks_per_super == 0) {
            bv->super_block_rank[i / blocks_per_super] = cumulative;
        }
        bv->block_rank[i] = cumulative - bv->super_block_rank[i / blocks_per_super];
        cumulative += popcount(bv->words[i]);
    }
    bv->super_block_rank[super_block_count] = cumulative;

    return bv;
}

void succinct_bit_vector_free(SuccinctBitVector *bv) {
    if (bv != NULL) {
        free(bv->words);
        free(bv->super_block_rank);
        free(bv->block_rank);
        free(bv);
    }
}

int succinct_bit_vector_rank1(SuccinctBitVector *bv, int i) {
    if (i <= 0) return 0;
    if (i > bv->length) i = bv->length;

    int block_idx = (i - 1) / BLOCK_SIZE;
    int super_block_idx = block_idx / (SUPER_BLOCK_SIZE / BLOCK_SIZE);
    int bit_in_block = i % BLOCK_SIZE;

    uint64_t mask;
    if (bit_in_block == 0) {
        mask = bv->words[block_idx];
    } else {
        /* Mascara apenas os bits até bit_in_block */
        mask = (bv->words[block_idx] >> (BLOCK_SIZE - bit_in_block)) << (BLOCK_SIZE - bit_in_block);
    }

    return bv->super_block_rank[super_block_idx]
         + bv->block_rank[block_idx]
         + popcount(mask);
}

int succinct_bit_vector_rank0(SuccinctBitVector *bv, int i) {
    return i - succinct_bit_vector_rank1(bv, i);
}

int succinct_bit_vector_select1(SuccinctBitVector *bv, int j) {
    int total_ones = succinct_bit_vector_total_ones(bv);
    if (j <= 0 || j > total_ones) return -1;

    /* Busca binária sobre superblocos */
    int lo = 0, hi = bv->num_super_blocks;
    while (lo < hi - 1) {
        int mid = (lo + hi) / 2;
        if (bv->super_block_rank[mid] < j) {
            lo = mid;
        } else {
            hi = mid;
        }
    }

    int super_start = lo * (SUPER_BLOCK_SIZE / BLOCK_SIZE);
    int remaining = j - bv->super_block_rank[lo];

    /* Scan linear nos blocos do superbloco */
    int blocks_per_super = SUPER_BLOCK_SIZE / BLOCK_SIZE;
    int block_idx = super_start;
    while (block_idx < super_start + blocks_per_super && block_idx < bv->num_blocks) {
        int cnt = popcount(bv->words[block_idx]);
        if (bv->block_rank[block_idx] + cnt >= remaining) break;
        block_idx++;
    }
    remaining -= bv->block_rank[block_idx];

    /* Scan bit a bit dentro da palavra */
    uint64_t w = bv->words[block_idx];
    for (int bit = 0; bit < BLOCK_SIZE; bit++) {
        if ((w & (1ULL << (63 - bit))) != 0) {
            remaining--;
            if (remaining == 0) return block_idx * BLOCK_SIZE + bit + 1;
        }
    }
    return -1;
}

bool succinct_bit_vector_get(SuccinctBitVector *bv, int i) {
    return (bv->words[i / 64] & (1ULL << (63 - (i % 64)))) != 0;
}

int succinct_bit_vector_length(SuccinctBitVector *bv) {
    return bv->length;
}

int succinct_bit_vector_total_ones(SuccinctBitVector *bv) {
    return succinct_bit_vector_rank1(bv, bv->length);
}

size_t succinct_bit_vector_memory_usage(SuccinctBitVector *bv) {
    if (bv == NULL) return 0;
    size_t words_size = ((bv->length + 63) / 64) * sizeof(uint64_t);
    size_t super_blocks_size = (bv->num_super_blocks + 1) * sizeof(int);
    size_t blocks_size = (bv->num_blocks + 1) * sizeof(int);
    return sizeof(SuccinctBitVector) + words_size + super_blocks_size + blocks_size;
}
