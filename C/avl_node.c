#include "avl_node.h"
#include <stdlib.h>
#include <string.h>

AVLNode* avl_node_create(int value) {
    AVLNode *node = (AVLNode*) malloc(sizeof(AVLNode));
    if (node == NULL) {
        return NULL;
    }
    node->value = value;
    node->balance_factor = 0;
    node->left = NULL;
    node->right = NULL;
    return node;
}

void avl_node_free(AVLNode *node) {
    if (node != NULL) {
        free(node);
    }
}

int avl_node_get_value(AVLNode *node) {
    return node->value;
}

void avl_node_set_value(AVLNode *node, int value) {
    node->value = value;
}

int avl_node_get_balance_factor(AVLNode *node) {
    return node->balance_factor;
}

void avl_node_set_balance_factor(AVLNode *node, int bf) {
    node->balance_factor = bf;
}

AVLNode* avl_node_get_left(AVLNode *node) {
    return node->left;
}

void avl_node_set_left(AVLNode *node, AVLNode *left) {
    node->left = left;
}

AVLNode* avl_node_get_right(AVLNode *node) {
    return node->right;
}

void avl_node_set_right(AVLNode *node, AVLNode *right) {
    node->right = right;
}
