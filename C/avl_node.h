#ifndef AVL_NODE_H
#define AVL_NODE_H

#include <stdint.h>

/* Nó da árvore AVL genérica */
typedef struct AVLNode {
    int value;                    /* Valor armazenado */
    int balance_factor;           /* Fator de balanceamento (-1, 0, 1) */
    struct AVLNode *left;         /* Ponteiro para filho esquerdo */
    struct AVLNode *right;        /* Ponteiro para filho direito */
} AVLNode;

/* Cria um novo nó com valor */
AVLNode* avl_node_create(int value);

/* Libera memória do nó */
void avl_node_free(AVLNode *node);

/* Getters e setters */
int avl_node_get_value(AVLNode *node);
void avl_node_set_value(AVLNode *node, int value);

int avl_node_get_balance_factor(AVLNode *node);
void avl_node_set_balance_factor(AVLNode *node, int bf);

AVLNode* avl_node_get_left(AVLNode *node);
void avl_node_set_left(AVLNode *node, AVLNode *left);

AVLNode* avl_node_get_right(AVLNode *node);
void avl_node_set_right(AVLNode *node, AVLNode *right);

#endif /* AVL_NODE_H */
