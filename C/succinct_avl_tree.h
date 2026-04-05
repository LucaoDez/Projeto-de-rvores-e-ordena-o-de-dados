#ifndef SUCCINCT_AVL_TREE_H
#define SUCCINCT_AVL_TREE_H

#include "avl_tree.h"
#include "succinct_bit_vector.h"
#include <stdbool.h>

/* Árvore AVL com codificação sucinta */
typedef struct {
    AVLTree *avl_tree;                  /* Árvore AVL base (para inserção balanceada) */
    SuccinctBitVector *bp_bits;         /* Sequência de balanced parentheses (2n bits) */
    SuccinctBitVector *balance_bits;    /* Fatores de balanceamento (2 bits/nó = 2n bits) */
    int *node_values;                   /* Valores dos nós em pré-ordem */
    int node_count;                     /* Número de nós */
    bool encoded;                       /* Flag de estado */
} SuccinctAVLTree;

/* Criar árvore AVL sucinta vazia */
SuccinctAVLTree* succinct_avl_tree_create(void);

/* Liberar toda a árvore */
void succinct_avl_tree_destroy(SuccinctAVLTree *tree);

/* Inserir valor */
void succinct_avl_tree_insert(SuccinctAVLTree *tree, int value);

/* Codificar a árvore sucintamente */
void succinct_avl_tree_encode(SuccinctAVLTree *tree);

/* Buscar na representação sucinta */
int succinct_avl_tree_search(SuccinctAVLTree *tree, int value);

/* Getters */
int succinct_avl_tree_get_node_count(SuccinctAVLTree *tree);
int succinct_avl_tree_get_balance_factor(SuccinctAVLTree *tree, int node_idx);
int succinct_avl_tree_get_value(SuccinctAVLTree *tree, int node_idx);

/* Navegação na representação sucinta */
int succinct_avl_tree_find_close(SuccinctAVLTree *tree, int p);
int succinct_avl_tree_depth(SuccinctAVLTree *tree, int p);

/* Calcula memória usada */
size_t succinct_avl_tree_memory_usage(SuccinctAVLTree *tree);

/* Exibe estatísticas */
void succinct_avl_tree_print_stats(SuccinctAVLTree *tree);
void succinct_avl_tree_print_inorder(SuccinctAVLTree *tree);

#endif /* SUCCINCT_AVL_TREE_H */
