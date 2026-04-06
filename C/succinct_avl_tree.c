#include "succinct_avl_tree.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

static int count_nodes(AVLNode *node);
static void encode_node(AVLNode *node, bool *bp, bool *balance,
                        int *bp_idx, int *val_idx);
static void print_inorder_rec(AVLNode *node);
static int left_child_bp(SuccinctAVLTree *tree, int p);
static int right_child_bp(SuccinctAVLTree *tree, int p);
static int left_subtree_size(SuccinctAVLTree *tree, int bp_pos);
static int search_node(SuccinctAVLTree *tree, int bp_pos, int node_idx, int value);
static void fill_values_preorder(AVLNode *node, int *values, int *idx);

SuccinctAVLTree* succinct_avl_tree_create(void) {
    SuccinctAVLTree *tree = (SuccinctAVLTree*) malloc(sizeof(SuccinctAVLTree));
    if (tree == NULL) return NULL;

    tree->avl_tree = avl_tree_create();
    if (tree->avl_tree == NULL) {
        free(tree);
        return NULL;
    }

    tree->bp_bits = NULL;
    tree->balance_bits = NULL;
    tree->node_values = NULL;
    tree->node_count = 0;
    tree->encoded = false;

    return tree;
}

void succinct_avl_tree_destroy(SuccinctAVLTree *tree) {
    if (tree != NULL) {
        avl_tree_destroy(tree->avl_tree);
        if (tree->bp_bits != NULL) {
            succinct_bit_vector_free(tree->bp_bits);
        }
        if (tree->balance_bits != NULL) {
            succinct_bit_vector_free(tree->balance_bits);
        }
        if (tree->node_values != NULL) {
            free(tree->node_values);
        }
        free(tree);
    }
}

void succinct_avl_tree_insert(SuccinctAVLTree *tree, int value) {
    avl_tree_insert(tree->avl_tree, value);
    tree->encoded = false;  /* Invalida codificação anterior */
}

/* Preenche array de valores em pré-ordem */
static void fill_values_preorder(AVLNode *node, int *values, int *idx) {
    if (node == NULL) return;
    values[(*idx)++] = node->value;
    fill_values_preorder(node->left, values, idx);
    fill_values_preorder(node->right, values, idx);
}

static int count_nodes(AVLNode *node) {
    if (node == NULL) return 0;
    return 1 + count_nodes(node->left) + count_nodes(node->right);
}

static void encode_node(AVLNode *node, bool *bp, bool *balance,
                        int *bp_idx, int *val_idx) {
    if (node == NULL) return;

    /* '(' ao entrar */
    bp[(*bp_idx)++] = true;

    /* Fator de balanceamento (2 bits em pré-ordem) */
    int node_pos = (*val_idx);
    int fb = node->balance_factor;
    balance[2 * node_pos] = (fb == 1);           /* bit alto: 1 se fatBal = +1 */
    balance[2 * node_pos + 1] = (fb == 0 || fb == 1); /* bit baixo: 1 se fatBal = 0 ou +1 */

    /* Valor do nó */
    // Nota: Em C usamos array de ints, não genéricos como Java
    (*val_idx)++;

    /* Filhos em pré-ordem */
    encode_node(node->left, bp, balance, bp_idx, val_idx);
    encode_node(node->right, bp, balance, bp_idx, val_idx);

    /* ')' ao sair */
    bp[(*bp_idx)++] = false;
}

void succinct_avl_tree_encode(SuccinctAVLTree *tree) {
    tree->node_count = count_nodes(tree->avl_tree->root);

    if (tree->node_count == 0) {
        bool *empty_bits = (bool*) malloc(1 * sizeof(bool));
        tree->bp_bits = succinct_bit_vector_create(empty_bits, 0);
        tree->balance_bits = succinct_bit_vector_create(empty_bits, 0);
        tree->node_values = (int*) malloc(sizeof(int));
        tree->encoded = true;
        free(empty_bits);
        return;
    }

    int bp_size = 2 * tree->node_count;
    int balance_size = 2 * tree->node_count;

    bool *bp = (bool*) calloc(bp_size, sizeof(bool));
    bool *balance = (bool*) calloc(balance_size, sizeof(bool));
    tree->node_values = (int*) malloc(tree->node_count * sizeof(int));

    if (bp == NULL || balance == NULL || tree->node_values == NULL) {
        free(bp);
        free(balance);
        free(tree->node_values);
        tree->encoded = false;
        return;
    }

    int bp_idx = 0;
    int val_idx = 0;

    /* Traversal pré-ordem */
    encode_node(tree->avl_tree->root, bp, balance, &bp_idx, &val_idx);

    /* Armazena valores em pré-ordem da árvore */
    int node_idx = 0;
    fill_values_preorder(tree->avl_tree->root, tree->node_values, &node_idx);

    tree->bp_bits = succinct_bit_vector_create(bp, bp_size);
    tree->balance_bits = succinct_bit_vector_create(balance, balance_size);

    free(bp);
    free(balance);

    tree->encoded = true;
}

int succinct_avl_tree_get_node_count(SuccinctAVLTree *tree) {
    return tree->node_count;
}

int succinct_avl_tree_get_balance_factor(SuccinctAVLTree *tree, int node_idx) {
    if (!tree->encoded || node_idx < 0 || node_idx >= tree->node_count) {
        return 0;  /* Inválido */
    }

    bool hi = succinct_bit_vector_get(tree->balance_bits, 2 * node_idx);
    bool lo = succinct_bit_vector_get(tree->balance_bits, 2 * node_idx + 1);

    if (!hi && !lo) return -1;
    if (!hi && lo) return 0;
    if (hi && lo) return 1;
    return 0;  /* Inválido (11 não usado) */
}

int succinct_avl_tree_get_value(SuccinctAVLTree *tree, int node_idx) {
    if (!tree->encoded || node_idx < 0 || node_idx >= tree->node_count) {
        return -1;  /* Inválido */
    }
    return tree->node_values[node_idx];
}

/* Iteração sobre filho esquerdo na representação BP */
static int left_child_bp(SuccinctAVLTree *tree, int p) {
    if (p < 1 || p > tree->bp_bits->length) return -1;
    int next = p + 1;
    if (next > tree->bp_bits->length || !succinct_bit_vector_get(tree->bp_bits, next - 1)) {
        return -1;
    }
    return next;
}

/* Iteração sobre filho direito na representação BP */
static int right_child_bp(SuccinctAVLTree *tree, int p) {
    int lc = left_child_bp(tree, p);
    if (lc == -1) return -1;
    int close_lc = succinct_avl_tree_find_close(tree, lc);
    if (close_lc == -1 || close_lc + 1 > tree->bp_bits->length) return -1;
    if (!succinct_bit_vector_get(tree->bp_bits, close_lc)) return -1;
    return close_lc + 1;
}

/* Tamanho da subárvore esquerda */
static int left_subtree_size(SuccinctAVLTree *tree, int bp_pos) {
    int lc = left_child_bp(tree, bp_pos);
    if (lc == -1) return 0;
    int close_lc = succinct_avl_tree_find_close(tree, lc);
    if (close_lc == -1) return 0;
    return succinct_bit_vector_rank1(tree->bp_bits, close_lc) -
           succinct_bit_vector_rank1(tree->bp_bits, lc - 1);
}

/* Busca recursiva na representação sucinta */
static int search_node(SuccinctAVLTree *tree, int bp_pos, int node_idx, int value) {
    if (bp_pos < 1 || bp_pos > tree->bp_bits->length) return -1;
    if (!succinct_bit_vector_get(tree->bp_bits, bp_pos - 1)) return -1;

    int node_val = tree->node_values[node_idx];

    if (node_val == value) {
        return node_idx;
    }

    if (node_val > value) {
        /* Vai para filho esquerdo */
        int lc = left_child_bp(tree, bp_pos);
        return (lc == -1) ? -1 : search_node(tree, lc, node_idx + 1, value);
    } else {
        /* Vai para filho direito */
        int rc = right_child_bp(tree, bp_pos);
        if (rc == -1) return -1;
        int lc_size = left_subtree_size(tree, bp_pos);
        return search_node(tree, rc, node_idx + lc_size + 1, value);
    }
}

int succinct_avl_tree_search(SuccinctAVLTree *tree, int value) {
    if (!tree->encoded || tree->node_count == 0) return -1;
    return search_node(tree, 1, 0, value);
}

int succinct_avl_tree_find_close(SuccinctAVLTree *tree, int p) {
    if (!tree->encoded || p < 1 || p > tree->bp_bits->length) return -1;

    int depth = 1;
    for (int i = p - 1; i < tree->bp_bits->length; i++) {
        if (succinct_bit_vector_get(tree->bp_bits, i)) {
            depth++;
        } else {
            depth--;
        }
        if (depth == 0) return i + 1;
    }
    return -1;
}

int succinct_avl_tree_depth(SuccinctAVLTree *tree, int p) {
    if (!tree->encoded || p < 1 || p > tree->bp_bits->length) return 0;
    return 2 * succinct_bit_vector_rank1(tree->bp_bits, p) - p;
}

size_t succinct_avl_tree_memory_usage(SuccinctAVLTree *tree) {
    if (tree == NULL) return 0;
    size_t bp_mem = (tree->bp_bits != NULL) ?
                    succinct_bit_vector_memory_usage(tree->bp_bits) : 0;
    size_t balance_mem = (tree->balance_bits != NULL) ?
                         succinct_bit_vector_memory_usage(tree->balance_bits) : 0;
    size_t values_mem = (tree->node_values != NULL) ?
                        (tree->node_count * sizeof(int)) : 0;
    return sizeof(SuccinctAVLTree) + bp_mem + balance_mem + values_mem;
}

void succinct_avl_tree_print_stats(SuccinctAVLTree *tree) {
    if (!tree->encoded) {
        printf("Erro: Árvore não codificada. Chame succinct_avl_tree_encode() primeiro.\n");
        return;
    }

    int bp_bits_used = 2 * tree->node_count;
    int balance_bits_used = 2 * tree->node_count;
    int total_bits = bp_bits_used + balance_bits_used;
    double bits_per_node = (tree->node_count > 0) ? (double)total_bits / tree->node_count : 0;

    double theoretical_limit = 0.938;
    double naive_pointers_bits = tree->node_count * 3.0 * 64;  /* 3 ponteiros de 64 bits */

    printf("\n╔══════════════════════════════════════════════════════╗\n");
    printf("║   ESTATÍSTICAS DE CODIFICAÇÃO SUCINTA (AVL TREE)     ║\n");
    printf("╠══════════════════════════════════════════════════════╣\n");
    printf("║  Nós na árvore              : %6d                 ║\n", tree->node_count);
    printf("║  Bits BP (estrutura)        : %6d  (2n bits)       ║\n", bp_bits_used);
    printf("║  Bits balance (fatores AVL) : %6d  (2 bits/nó)     ║\n", balance_bits_used);
    printf("║  Total bits                 : %6d                 ║\n", total_bits);
    printf("║  Bits por nó (estrutura)    : %8.3f               ║\n", (double)bp_bits_used / tree->node_count);
    printf("║  Bits por nó (total)        : %8.3f               ║\n", bits_per_node);
    printf("╠══════════════════════════════════════════════════════╣\n");
    printf("║  Limite teórico (artigo)    : %8.3f bits/nó        ║\n", theoretical_limit);
    printf("║  Espaço estrutura naive     : %6d bits (3×64×n)  ║\n", (int)naive_pointers_bits);
    printf("║  Redução vs. naive          : %6.1f%%               ║\n",
           (1.0 - (double)bp_bits_used / naive_pointers_bits) * 100);
    printf("╠══════════════════════════════════════════════════════╣\n");
    printf("║  TEORIA APLICADA:                                    ║\n");
    printf("║  O artigo prova que AVL trees requerem ~0.938        ║\n");
    printf("║  bits/nó para codificar a ESTRUTURA (sem valores).  ║\n");
    printf("║  A rep. BP usa 2 bits/nó + o(n) bits auxiliares.    ║\n");
    printf("║  Isso representa uma compressão de ~53%% em relação  ║\n");
    printf("║  à representação por ponteiros (192 bits/nó).       ║\n");
    printf("╚══════════════════════════════════════════════════════╝\n");
}

void succinct_avl_tree_print_inorder(SuccinctAVLTree *tree) {
    printf("In-ordem (sorted): ");
    print_inorder_rec(tree->avl_tree->root);
    printf("\n");
}

static void print_inorder_rec(AVLNode *node) {
    if (node == NULL) return;
    print_inorder_rec(node->left);
    printf("%d(fb=%+d) ", node->value, node->balance_factor);
    print_inorder_rec(node->right);
}
