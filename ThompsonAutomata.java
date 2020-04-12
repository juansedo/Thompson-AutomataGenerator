/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thompsonautomata;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import javafx.util.Pair;

/**
 *
 * @author juansedo, smaring1
 */
public class ThompsonAutomata {
    
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Ingrese la expresión regular: ");
        String input = in.nextLine();
        try {
            Pattern.compile(input);
        } catch (PatternSyntaxException e) {
            System.out.println("La expresión regular " + input + " no es válida");
            System.err.println(e.getDescription());
            System.exit(1);
        }
        Node Tree = generateTree(input);
        
        NodeGraph ng = new NodeGraph(Tree);
        ng.initTree();
        ArrayList<String> s = getTreeGraph(ng);
        for (int i = 0; i < s.size(); i++) {
            System.out.print(s.get(i));
        }
    }
    
    public static Node generateTree(String expr) {
        /**
         * Implementing:
         * E -> R '|' E | R
         * R -> SR | S
         * S -> T* | T+ | T
         * T -> (a..z) | 'e' | (E)
         */
        return E(expr);
    }
    
    public static Node E(String expr) {
        Node actual;
        int pos_pipe = search(expr,'|');
        if (pos_pipe >= 0) {
            actual = new Node("|");
            actual.left = R(expr.substring(0, pos_pipe));
            actual.right = E(expr.substring(pos_pipe+1));
            return actual;
        }
        return R(expr);
    }
    
    public static Node R(String expr) {
        Node actual;
        int increment;
        char c, d;
        if (!expr.isEmpty()) c = expr.charAt(0);
        else return S(expr);
        
        if (c == '(') {
            int pos_bracket = closedBracket(expr, 0);
            if(pos_bracket < expr.length() - 2) {
                actual = new Node(".");
                d = expr.charAt(pos_bracket+1);
                if(d == '*' || d == '+') pos_bracket++;
                actual.left = S(expr.substring(0, ++pos_bracket));
                actual.right = R(expr.substring(pos_bracket));
            }
            else return S(expr);
        }
        else {
            if(expr.length() > 1) {
                d = expr.charAt(1);
                if (expr.length() > 2) {
                    actual = new Node(".");
                    
                    increment = (d != '*' && d != '+')? 1: 2;
                    
                    actual.left = S(expr.substring(0, increment));
                    actual.right = R(expr.substring(increment));
                }
                else {
                    actual = new Node(d);
                    if (d != '*' && d != '+') {
                        actual = new Node(".");
                        actual.right = S(d + "");
                    }
                    actual.left = S(c + "");
                }
            }
            else return S(expr);
        }
        return actual;
    }
    
    public static Node S(String expr) {
        Node actual;
        char c;
        if (!expr.isEmpty()) c = expr.charAt(expr.length() - 1);
        else return T(expr);
        
        switch(c) {
            case '*': case '+':
                actual = new Node(c);
                actual.left = T(expr.substring(0, expr.length() - 1));
                break;
            default:
                actual = T(expr);
                break;
        }
        return actual;
    }
    
    public static Node T(String expr) {
        char c;
        if (!expr.isEmpty()) c = expr.charAt(0);
        else return null;
        
        if (c == '(') {
            int pos_bracket = closedBracket(expr, 0);
            if (expr.length() > 2) return E(expr.substring(1, pos_bracket));
            else return null;
        }
        return new Node(expr);
    }
    
    public static int search(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') count++;
            else if (s.charAt(i) == ')') count--;
            else if (s.charAt(i) == c && count == 0) return i;
        }
        return -1;
    }
    
    public static int closedBracket(String s, int opening) {
        int count = 1;
        for (int i = opening+1; i < s.length(); i++) {
            if (s.charAt(i) == '(') count++;
            else if (s.charAt(i) == ')') count--;
            
            if (count == 0) return i;
        }
        return -1;
    }
    
    public static ArrayList<String> getTreeGraph(NodeGraph root) {
        Hashtable<Pair<Integer,Integer>, String> map = new Hashtable<>();
        
        int total_width = root.max_arc_left + 1 + root.max_arc_right;
        putIntoMap(map, root, root.max_arc_left, 0);
        
        ArrayList<String> s = new ArrayList<>();
        int i = 0;
        while (!map.isEmpty()) {
            s.add(i, "");
            for (int j = 0; j < total_width; j++) {
                Pair<Integer, Integer> p = new Pair<>(j,i);
                s.set(i, s.get(i) + (map.containsKey(p)? map.remove(p): " "));
            }
            s.set(i, s.get(i) + "\n");
            i++;
        }
        return s;
    }
    
    private static void putIntoMap(Hashtable<Pair<Integer,Integer>, String> map, NodeGraph n, int x, int y) {
        int i;
        map.put(new Pair<>(x, y), n.me);
        if(n.hasLeft()) {
            for (i = 1; i <= n.arc_length; i++) map.put(new Pair<>(x-i, y+i), "/");
            putIntoMap(map, n.left, x-i, y+i);
        }
        if (n.hasRight()) {
            for (i = 1; i <= n.arc_length; i++) map.put(new Pair<>(x+i, y+i), "\\");
            putIntoMap(map, n.right, x+i, y+i);
        }
    }
}

class Node {
    Node left, right;
    String me;
    
    Node(String me) {
        this.me = me;
    }
    Node(char c) {
        this.me = String.valueOf(c);
    }
    
    boolean hasLeft() {
        return left != null;
    }
    
    boolean hasRight() {
        return right != null;
    }
    
    void setLeft(Node n) {
        this.left = n;
    }
}

class NodeGraph {
    NodeGraph left, right;
    String me;
    int arc_length;
    int level;
    int max_arc_left;
    int max_arc_right;
    
    NodeGraph(Node root) {
        this.me = root.me;
        if(root.hasLeft()) this.left = new NodeGraph(root.left);
        if(root.hasRight()) this.right = new NodeGraph(root.right);
    }
    
    void initTree() {
        setLevels(this, 0);
        setArcs(this);
    }
    
    boolean hasLeft() {
        return left != null;
    }
    
    boolean hasRight() {
        return right != null;
    }
    
    public static void setLevels(NodeGraph n, int level) {
        n.level = level;
        if (n.hasLeft()) setLevels(n.left, level + 1);
        if (n.hasRight()) setLevels(n.right, level + 1);
    }
    
    public static void setArcs(NodeGraph root) {
        int [] arcs = new int [getTreeLength(root) + 1];
        arcs[arcs.length - 1] = 0;
        arcs[arcs.length - 2] = 1;
        NodeTable h = new NodeTable(root, arcs.length);
        defineArcs(h, arcs, arcs.length - 3);
    }
    
    private static void defineArcs(NodeTable h, int [] arcs, int pos) {
        if (pos < 0) return;
        
        for (int i = pos; i >= 0; i--) {
            for (NodeGraph n: h.get(i)) {
                if (arcs[i] < arcs[i+1]) {
                    arcs[i] = arcs[i+1];
                }
                if(hasCollisions(n)) {
                    int a, b;
                    a = n.left.getLongRightPath();
                    b = n.hasRight()? n.right.getLongLeftPath(): 0;
                    arcs[i] = Math.max(a, b) + 1;
                }
            }
            
            for (NodeGraph n: h.get(i)) {
                n.arc_length = arcs[i];
                n.max_arc_left = n.getLongLeftPath();
                n.max_arc_right = n.getLongRightPath();
            }
        }
    }
    
    private static boolean hasCollisions(NodeGraph n) {
        if (!(n.hasLeft() && n.hasRight())) return false;
        if (n.left.hasRight() && n.right.hasLeft()) return true;
        else return false;
    }
    
    public static int getTreeLength(NodeGraph n) {
        int a = n.hasLeft()? 1 + getTreeLength(n.left) : 1;
        int b = n.hasRight()? 1 + getTreeLength(n.right) : 1;
        return Math.max(a, b);
    }
    
    int getLongLeftPath() {
        return Math.max(hasLeft()? left.getLongLeftPath() + arc_length + 1: 0,
                hasRight()? right.getLongLeftPath() - arc_length - 1: 0);
    }
    
    int getLongRightPath() {
        return Math.max(hasLeft()? left.getLongRightPath() - arc_length - 1: 0,
                hasRight()? right.getLongRightPath() + arc_length + 1: 0);
    }
}

class NodeTable {
    ArrayList<NodeGraph>[] table;
    
    public NodeTable(NodeGraph n, int levels) {
        table = new ArrayList[levels];
        for (int i = 0; i < levels; i++) {
            table[i] = new ArrayList<>();
        }
        initTable(n);
    }
    
    void initTable(NodeGraph n) {
        put(n.level, n);
        if (n.hasLeft()) initTable(n.left);
        if (n.hasRight()) initTable(n.right);
    }
    
    ArrayList<NodeGraph> get (int pos) {
        return table[pos];
    }
    
    void put (int pos, NodeGraph n) {
        if (pos >= 0 && pos < table.length) table[pos].add(n);
    }
}