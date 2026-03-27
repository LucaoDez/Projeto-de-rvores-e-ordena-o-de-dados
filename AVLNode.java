class AVLNode<T extends Comparable<T>> { // classe do nó da árvore AVL, com tipo genérico comparável

    private AVLNode<T> left; // referência para o filho da esquerda
    private AVLNode<T> right; // referência para o filho da direita
    private T info; // valor armazenado no nó
    private int fatBal; // fator de balanceamento do nó (-1, 0, 1)

    AVLNode(T info){ 
        this.info = info; // inicializa o nó com um valor
        // left e right começam como null automaticamente
        // fatBal começa como 0 automaticamente
    }

    void setInfo(T info){
        this.info = info; // altera o valor armazenado no nó
    }

    T getInfo(){
        return this.info; // retorna o valor armazenado
    }

    void setLeft(AVLNode<T> left){
        this.left = left; // define o filho da esquerda
    }
    
    AVLNode<T> getLeft(){
        return this.left; // retorna o filho da esquerda       
    }

    void setRight(AVLNode<T> right){
        this.right = right; // define o filho da direita
    }

    AVLNode<T> getRight(){
        return this.right; // retorna o filho da direita
    }

    void setFatBal(int fatBal){
        this.fatBal = fatBal; // define o fator de balanceamento
    }

    int getFatBal(){
        return this.fatBal; // retorna o fator de balanceamento
    }
} // fim da classe AVLNode