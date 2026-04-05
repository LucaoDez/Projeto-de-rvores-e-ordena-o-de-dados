#ifndef AVL_TREE_H
#define AVL_TREE_H

#include "avl_node.h"
#include <stdbool.h>
#include <stddef.h>

/* Árvore AVL clássica com ponteiros */
typedef struct {
    AVLNode *root;          /* Raiz da árvore */
    bool status;            /* Indica se altura mudou após inserção */
    int node_count;         /* Número de nós */
} AVLTree;

/* Criar árvore vazia */
AVLTree* avl_tree_create(void);

/* Liberar toda a árvore */
void avl_tree_destroy(AVLTree *tree);

/* Isere um valor na árvore */
void avl_tree_insert(AVLTree *tree, int value);

/* Busca um valor */
bool avl_tree_search(AVLTree *tree, int value);

/* Retorna número de nós */
int avl_tree_get_node_count(AVLTree *tree);

/* Retorna a raiz */
AVLNode* avl_tree_get_root(AVLTree *tree);

/* Calcula altura da árvore */
int avl_tree_get_height(AVLTree *tree);

/* Calcula tamanho total de memória usada */
size_t avl_tree_memory_usage(AVLTree *tree);

/* Imprime árvore em in-ordem */
void avl_tree_print_inorder(AVLTree *tree);

#endif /* AVL_TREE_H */
