#include "avl_tree.h"
#include <stdlib.h>
#include <stdio.h>

static int get_height(AVLNode *node);
static AVLNode* rotate_right(AVLNode *a);
static AVLNode* rotate_left(AVLNode *a);
static AVLNode* insert_node(AVLNode *node, int value, bool *status);
static void print_inorder_rec(AVLNode *node);
static void free_tree(AVLNode *node);
static int count_nodes(AVLNode *node);

AVLTree* avl_tree_create(void) {
    AVLTree *tree = (AVLTree*) malloc(sizeof(AVLTree));
    if (tree == NULL) {
        return NULL;
    }
    tree->root = NULL;
    tree->status = false;
    tree->node_count = 0;
    return tree;
}

void avl_tree_destroy(AVLTree *tree) {
    if (tree != NULL) {
        free_tree(tree->root);
        free(tree);
    }
}

static void free_tree(AVLNode *node) {
    if (node == NULL) return;
    free_tree(node->left);
    free_tree(node->right);
    avl_node_free(node);
}

void avl_tree_insert(AVLTree *tree, int value) {
    if (tree->root == NULL) {
        tree->root = avl_node_create(value);
        tree->node_count = 1;
    } else {
        tree->status = false;
        tree->root = insert_node(tree->root, value, &tree->status);
        tree->node_count = count_nodes(tree->root);
    }
}

static AVLNode* insert_node(AVLNode *node, int value, bool *status) {
    if (node == NULL) {
        node = avl_node_create(value);
        *status = true;  /* Altura aumentou */
        return node;
    }

    if (value < node->value) {  /* Insere à esquerda */
        node->left = insert_node(node->left, value, status);

        if (*status) {
            int bf = node->balance_factor;
            switch (bf) {
                case 1:   /* Estava pesado à direita */
                    node->balance_factor = 0;
                    *status = false;
                    break;
                case 0:   /* Estava equilibrado */
                    node->balance_factor = -1;
                    break;
                case -1:  /* Já pesado à esquerda */
                    node = rotate_right(node);
                    break;
            }
        }
    } else if (value > node->value) {  /* Insere à direita */
        node->right = insert_node(node->right, value, status);

        if (*status) {
            int bf = node->balance_factor;
            switch (bf) {
                case -1:  /* Estava pesado à esquerda */
                    node->balance_factor = 0;
                    *status = false;
                    break;
                case 0:   /* Equilibrado */
                    node->balance_factor = 1;
                    break;
                case 1:   /* Já pesado à direita */
                    node = rotate_left(node);
                    break;
            }
        }
    }

    return node;
}

static AVLNode* rotate_right(AVLNode *a) {
    AVLNode *b = a->left;
    AVLNode *c = NULL;

    if (b->balance_factor == -1) {  /* Rotação simples */
        a->left = b->right;
        b->right = a;
        a->balance_factor = 0;
        a = b;
    } else {  /* Rotação dupla */
        c = b->right;
        b->right = c->left;
        c->left = b;
        a->left = c->right;
        c->right = a;

        if (c->balance_factor == -1) {
            a->balance_factor = 1;
        } else {
            a->balance_factor = 0;
        }

        if (c->balance_factor == 1) {
            b->balance_factor = -1;
        } else {
            b->balance_factor = 0;
        }

        a = c;
    }

    a->balance_factor = 0;
    return a;
}

static AVLNode* rotate_left(AVLNode *a) {
    AVLNode *b = a->right;
    AVLNode *c = NULL;

    if (b->balance_factor == 1) {  /* Rotação simples */
        a->right = b->left;
        b->left = a;
        a->balance_factor = 0;
        a = b;
    } else {  /* Rotação dupla */
        c = b->left;
        b->left = c->right;
        c->right = b;
        a->right = c->left;
        c->left = a;

        if (c->balance_factor == 1) {
            a->balance_factor = -1;
        } else {
            a->balance_factor = 0;
        }

        if (c->balance_factor == -1) {
            b->balance_factor = 1;
        } else {
            b->balance_factor = 0;
        }

        a = c;
    }

    a->balance_factor = 0;
    return a;
}

bool avl_tree_search(AVLTree *tree, int value) {
    AVLNode *current = tree->root;
    while (current != NULL) {
        if (value == current->value) {
            return true;
        } else if (value < current->value) {
            current = current->left;
        } else {
            current = current->right;
        }
    }
    return false;
}

int avl_tree_get_node_count(AVLTree *tree) {
    return tree->node_count;
}

AVLNode* avl_tree_get_root(AVLTree *tree) {
    return tree->root;
}

static int get_height(AVLNode *node) {
    if (node == NULL) return 0;
    int lh = get_height(node->left);
    int rh = get_height(node->right);
    return 1 + (lh > rh ? lh : rh);
}

int avl_tree_get_height(AVLTree *tree) {
    return get_height(tree->root);
}

static int count_nodes(AVLNode *node) {
    if (node == NULL) return 0;
    return 1 + count_nodes(node->left) + count_nodes(node->right);
}

size_t avl_tree_memory_usage(AVLTree *tree) {
    /* Tamanho da árvore = sizeof(AVLTree) + n * sizeof(AVLNode) */
    if (tree == NULL) return 0;
    return sizeof(AVLTree) + (tree->node_count * sizeof(AVLNode));
}

void avl_tree_print_inorder(AVLTree *tree) {
    printf("In-ordem: ");
    print_inorder_rec(tree->root);
    printf("\n");
}

static void print_inorder_rec(AVLNode *node) {
    if (node == NULL) return;
    print_inorder_rec(node->left);
    printf("%d(bf=%+d) ", node->value, node->balance_factor);
    print_inorder_rec(node->right);
}
