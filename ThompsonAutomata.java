package thompsonautomata;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author juansedo
 */
public class ThompsonAutomata {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Ingrese la expresión regular: ");
        String input = in.nextLine();
        try {
            Pattern.compile(input);
        } catch (PatternSyntaxException e) {
            System.out.println("La expresión regular " + input + " no es válida");
            System.err.println(e.getDescription());
            System.exit(1);
        }
        Node Tree = generateTree(input);
        Tree = treePruning(Tree);

        System.out.println("Árbol listo");

    }

    public static Node generateTree(String expr) {
        Node result = new Node(".");
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            Node n;
            switch (c) {
                case '*':
                    n = new Node("*");
                    n.addChild(result.child.removeLast());
                    result.addChild(n);
                    break;
                case '+':
                    n = new Node("+");
                    n.addChild(result.child.removeLast());
                    result.addChild(n);
                    break;
                case '|':
                    n = new Node(".");
                    for (Node child : result.child) {
                        n.addChild(child);
                    }
                    result.child.clear();
                    Node m = new Node("|");
                    m.addChild(n);
                    m.addChild(generateTree(expr.substring(i+1)));
                    result.addChild(m);
                    i = expr.length();
                    break;
                case '(':
                    String expr_parentheses = parentheses(expr, i);
                    n = generateTree(expr_parentheses);
                    result.addChild(n);
                    i += expr_parentheses.length() + 1;
                    break;
                case ' ':
                    break;
                default:
                    result.addChild(c + "");
                    break;
            }
        }

        return result;
    }

    public static Node treePruning(Node root) {
        if (root.child.size() == 1) {
            Node child = root.child.get(0);
            String parent_text = root.text;
            String child_text = child.text;

            if (parent_text.equals(child_text) || parent_text.equals(".")) {
                root.text = child.text;
                root.child = child.child;
            }
        }

        if (root.child.isEmpty()) {
            return root;
        }

        Node new_root = new Node(root.text);
        for (int i = 0; i < root.child.size(); i++) {
            new_root.addChild(treePruning(root.child.get(i)));
        }

        return new_root;
    }

    public static String parentheses(String str, int i) {
        int count = 1;
        int j = i+1;
        for ( ; j < str.length() && count > 0; j++) {
            char c = str.charAt(j);
            switch(c) {
                case '(': count++; break;
                case ')': count--; break;
            }
        }
        return str.substring(i+1, j-1);
    }
}

class Node {
    LinkedList<Node> child = new LinkedList<>();
    String text;
    Node(String text, Node child) {
        this.child.add(child);
        this.text = text;
    }
    Node(String text) {
        this.text = text;
    }

    void addChild(String expr) {
        this.child.add(new Node(expr));
    }

    void addChild(Node n) {
        this.child.add(n);
    }

    Node getLastChild() {
        return this.child.getLast();
    }
}
