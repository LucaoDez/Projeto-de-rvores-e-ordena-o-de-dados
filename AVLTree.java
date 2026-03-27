public class AVLTree<T extends Comparable<T>> { // árvore AVL genérica com elementos comparáveis

    private AVLNode<T> root; // raiz da árvore
    private boolean status; // indica se a altura da árvore mudou após inserção

    void setRoot(AVLNode<T> root){
        this.root = root; // define a raiz
    }

    AVLNode<T> getRoot(){
        return this.root; // retorna a raiz
    }

    void setStatus(boolean status){
        this.status = status; // define o status
    }

    boolean getStatus(){
        return this.status; // retorna o status
    }

    private boolean isEmpty(){
        return root == null; // verifica se a árvore está vazia
    }

    public void insert(T value){
        if(this.isEmpty()){ // se a árvore estiver vazia
            this.root = new AVLNode<T>(value); // cria o primeiro nó (raiz)
        }
        else{
            this.root = insertNode(this.root, value); // insere recursivamente
            this.status = false; // reseta o status
        }
    }

    private AVLNode<T> insertNode(AVLNode<T> r, T value){
        if(r == null){ // encontrou posição vazia
            r = new AVLNode<T>(value); // cria novo nó
            this.status = true; // altura aumentou
        }
        else if(r.getInfo().compareTo(value) > 0){ // se valor atual > valor inserido
            r.setLeft(insertNode(r.getLeft(), value)); // insere à esquerda

            if(this.status){ // se altura mudou
                switch(r.getFatBal()){ // verifica fator de balanceamento
                    case 1: // estava pesado à direita
                        r.setFatBal(0); // agora equilibrado
                        this.status = false; // para propagação
                        break;

                    case 0: // estava equilibrado
                        r.setFatBal(-1); // agora pesado à esquerda
                        break;

                    case -1: // já estava pesado à esquerda
                        r = this.rotateRight(r); // precisa rotacionar
                        break;
                }
            }
        }
        else{ // inserção à direita
            r.setRight(insertNode(r.getRight(), value)); // insere à direita

            if(this.status){ // se altura mudou
                switch(r.getFatBal()){
                    case -1: // estava pesado à esquerda
                        r.setFatBal(0); // agora equilibrado
                        this.status = false;
                        break;

                    case 0: // equilibrado
                        r.setFatBal(1); // agora pesado à direita
                        break;

                    case 1: // já estava pesado à direita
                        r = this.rotateLeft(r); // rotaciona
                        break;
                }
            }
        }
        return r; // retorna o nó atualizado
    }

    private AVLNode<T> rotateRight(AVLNode<T> a){
        AVLNode<T> b, c;
        b = a.getLeft(); // pega filho esquerdo

        if(b.getFatBal() == -1){ // rotação simples
            a.setLeft(b.getRight()); // ajusta ponteiros
            b.setRight(a);
            a.setFatBal(0); // atualiza fator
            a = b; // nova raiz
        }
        else{ // rotação dupla
            c = b.getRight(); // nó intermediário

            b.setRight(c.getLeft());
            c.setLeft(b);
            a.setLeft(c.getRight());
            c.setRight(a);

            if(c.getFatBal() == -1){
                a.setFatBal(1);
            } else {
                a.setFatBal(0);
            }

            if(c.getFatBal() == 1){
                b.setFatBal(-1);
            } else {
                b.setFatBal(0);
            }

            a = c; // nova raiz
        }

        a.setFatBal(0); // ajusta fator final
        this.status = false; // altura estabilizou
        return a; // retorna nova raiz
    }

    private AVLNode<T> rotateLeft(AVLNode<T> a){
        AVLNode<T> b, c;
        b = a.getRight(); // pega filho direito

        if(b.getFatBal() == 1){ // rotação simples
            a.setRight(b.getLeft());
            b.setLeft(a);
            a.setFatBal(0);
            a = b;
        }
        else{ // rotação dupla
            c = b.getLeft();

            b.setLeft(c.getRight());
            c.setRight(b);
            a.setRight(c.getLeft());
            c.setLeft(a);

            if(c.getFatBal() == 1){
                a.setFatBal(-1);
            } else {
                a.setFatBal(0);
            }

            if(c.getFatBal() == -1){
                b.setFatBal(1);
            } else {
                b.setFatBal(0);
            }

            a = c;
        }

        a.setFatBal(0); // ajusta fator final
        this.status = false; // altura estabilizou
        return a; // retorna nova raiz
    }
}