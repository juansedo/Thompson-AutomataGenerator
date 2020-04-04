/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thompsonautomata;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import javafx.util.Pair;

/**
 *
 * @author juans
 */
public class ThompsonAutomata {
    
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Ingrese la expresión regular: ");
        String input = in.nextLine();
        Node Tree = generateTree(input);
        //System.out.println("Árbol listo");
        //int tree_length = Node.getTreeLength(Tree);
        Node.setLevels(Tree, 0);
        Node.setArcs(Tree);
        //Tree.setRest();
        ArrayList<String> s = getTreeGraph(Tree);
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
        Node actual;
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
    
    public static ArrayList<String> printArc(int rest_left, int rest_right, int length, boolean left, boolean right) {
        ArrayList<String> t = new ArrayList<>();
        for (int i = length; i > 0; i--) {
            String s = "";
            for (int j = 0; j < rest_left - length; j++)  s += " ";
            for (int j = 0; j < i - 1; j++) s += " ";
            s += left? "/ ": "  ";
            for (int j = i; j < length; j++) s += " ";
            for (int j = i; j < length; j++) s += " ";
            s += right? "\\": " ";
            for (int j = 0; j < i - 1; j++) s += " ";
            for (int j = 0; j < rest_right - length; j++) s += " ";
            t.add(s);
        }
        return t;
    }
    
    public static ArrayList<String> getTreeGraph(Node root) {
        Hashtable<Pair<Integer,Integer>, String> map = new Hashtable<>();
        
        int rest = root.getLongLeftPath();
        int total_width = rest + 1 + root.getLongRightPath();
        
        putIntoMap(map, root, rest, 0);
        
        ArrayList<String> s = new ArrayList<>();
        for (int i = 0; !map.isEmpty(); i++) {
            s.add(i, "");
            for (int j = 0; j < total_width; j++) {
                Pair<Integer, Integer> p = new Pair<>(j,i);
                if(map.containsKey(p)) {
                    s.set(i, s.get(i) + map.get(p));
                    map.remove(p);
                } else {
                    s.set(i, s.get(i) + " ");
                }
            }
            s.set(i, s.get(i) + "\n");
        }
        return s;
    }
    
    private static void putIntoMap(Hashtable<Pair<Integer,Integer>, String> map, Node n, int x, int y) {
        map.put(new Pair<>(x, y), n.me);
        if(n.hasLeft()) {
            int i;
            for (i = 1; i <= n.arc_length; i++) {
                map.put(new Pair<>(x-i, y+i), "/");
            }
            putIntoMap(map, n.left, x-i, y+i);
        }
        
        if (n.hasRight()) {
            int i;
            for (i = 1; i <= n.arc_length; i++) {
                map.put(new Pair<>(x+i, y+i), "\\");
            }
            putIntoMap(map, n.right, x+i, y+i);
        }
    }
    
//    public static TreeGraph getTreeGraph(Node root) {
//        TreeGraph t = new TreeGraph();
//        TreeGraph children = new TreeGraph();
//        int arc = root.arc_length;
//        
//        int arcs_left = root.total_LeftArcSons(); 
//        int arcs_right = root.total_RightArcSons();
//        
//        int left_width;
//        int right_width;
//        
//        t.lines.add(
//                whiteSpaces(arcs_left) +
//                root.me +
//                whiteSpaces(arcs_right)
//        );
//        
//        t.lines.addAll(printArc(arcs_left, arcs_right, arc, root.hasLeft(), root.hasRight()));
//        
//        TreeGraph t_left = new TreeGraph(), t_right = new TreeGraph();
//        if (root.left != null && root.right != null) {
//            t_left = getTreeGraph(root.left);
//            t_right = getTreeGraph(root.right);
//            
//            left_width = t_left.lines.get(0).length();
//            right_width = t_right.lines.get(0).length();
//            
//            children = TreeGraph.concat(t_left, t_right, arcs_left + 1 + arcs_right - left_width - right_width);
//        }
//        else if (root.left != null) {
//            leading = (arc != root.left.arc_length)? 3: 0;
//            t_left = getTreeGraph(root.left);
//            t_right.lines.add(whiteSpaces(arc + 1));
//            children = TreeGraph.concat(t_left, t_right, leading);
//        }
//        else if (root.right != null) {
//            if (arc != root.left.arc_length) {
//                leading = 3;
//                t_left.lines.add(whiteSpaces(rest)+whiteSpaces(arc-1));
//            } else {
//                leading = 0;
//                t_left.lines.add(whiteSpaces(rest)+whiteSpaces(arc));
//            }
//            t_right = getTreeGraph(root.right);
//            children = TreeGraph.concat(t_left, t_right, leading);
//        }
//        t.lines.addAll(children.lines);
//        return t;
//    }
    
    
//    public static TreeGraph getTreeGraph(Node root) {
//        TreeGraph t = new TreeGraph();
//        TreeGraph children = new TreeGraph();
//        int rest = root.rest;
//        int arc = root.arc_length;
//        int leading = 0;
//        
//        int arcs_left = root.total_LeftArcSons(); 
//        int arcs_right = root.total_RightArcSons();
//        
//        t.lines.add(
//                whiteSpaces(arcs_left) +
//                root.me +
//                whiteSpaces(arcs_right)
//        );
//        TreeGraph t_left = new TreeGraph(), t_right = new TreeGraph();
//        if (root.left != null && root.right != null) {
//            leading = (arc != root.left.arc_length)? 3: 0;
//            t_left = getTreeGraph(root.left);
//            if (root.left.right == null) ;
//            t_right = getTreeGraph(root.right);
//            children = TreeGraph.concat(t_left, t_right, leading);
//        }
//        else if (root.left != null) {
//            leading = (arc != root.left.arc_length)? 3: 0;
//            t_left = getTreeGraph(root.left);
//            t_right.lines.add(whiteSpaces(arc + 1));
//            children = TreeGraph.concat(t_left, t_right, leading);
//        }
//        else if (root.right != null) {
//            if (arc != root.left.arc_length) {
//                leading = 3;
//                t_left.lines.add(whiteSpaces(rest)+whiteSpaces(arc-1));
//            } else {
//                leading = 0;
//                t_left.lines.add(whiteSpaces(rest)+whiteSpaces(arc));
//            }
//            t_right = getTreeGraph(root.right);
//            children = TreeGraph.concat(t_left, t_right, leading);
//        }
//        t.lines.addAll(printArc(arcs_left, arcs_right, arc, root.left != null, root.right != null));
//        t.lines.addAll(children.lines);
//        return t;
//    }
    
    public static String whiteSpaces(int n) {
        String s = "";
        for (int i = 0; i < n; i++) s += " ";
        return s;
    }
}

class NodeTable {
    ArrayList<Node>[] table;
    
    public NodeTable(Node n, int levels) {
        table = new ArrayList[levels];
        for (int i = 0; i < levels; i++) {
            table[i] = new ArrayList<>();
        }
        initTable(n);
    }
    
    void initTable(Node n) {
        put(n.level, n);
        if (n.hasLeft()) initTable(n.left);
        if (n.hasRight()) initTable(n.right);
    }
    
    ArrayList<Node> get (int pos) {
        return table[pos];
    }
    
    void put (int pos, Node n) {
        if (pos >= 0 && pos < table.length) table[pos].add(n);
    }
}

class Node {
    Node left, right;
    String me;
    int arc_length;
    int rest;
    int level;
    
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
    
    int setRest() {
        rest = (hasLeft())? 1 + left.setRest() + left.arc_length: 0;
        if (hasRight()) right.setRest();
        return rest;
    }
    
    public static void setLevels(Node n, int level) {
        n.level = level;
        if (n.hasLeft()) setLevels(n.left, level + 1);
        if (n.hasRight()) setLevels(n.right, level + 1);
    }
    
    public static void setArcs(Node root) {
        int [] arcs = new int [getTreeLength(root) + 1];
        arcs[arcs.length - 1] = 0;
        arcs[arcs.length - 2] = 1;
        NodeTable h = new NodeTable(root, arcs.length);
        defineArcs(h, arcs, arcs.length - 3);
        applyArcs(h, arcs);
    }
    
    private static void defineArcs(NodeTable h, int [] arcs, int pos) {
        if (pos < 0) return;
        
        for (int i = pos; i >= 0; i--) {
            for (Node n: h.get(i)) {
                if (arcs[i] < arcs[i+1]) {
                    arcs[i] = arcs[i+1];
                }
                
                if(hasCollisions(n,0,0,new Coordinates())) {
                    int a, b;
                    a = n.left.getLongRightPath();
                    b = (n.hasRight())? n.right.getLongLeftPath(): 0;
                    arcs[i] = Math.max(a, b) + 1;
                }
            }
        }
    }
    
    private static void applyArcs(NodeTable h, int [] arcs) {
        for (int i = 0; i < h.table.length; i++) {
            for (Node n: h.get(i)) {
                n.arc_length = arcs[i];
            }
        }
    }
    
    private static boolean hasCollisions(Node n, int x, int y, Coordinates p) {
        if (!(n.hasLeft() || n.hasRight())) {
            p.add(x, y);
            return false;
        }
        
        int new_x, new_y;
        
        if (n.hasLeft()) {
            new_x = (x-1) - n.arc_length;
            new_y = (y+1) + n.arc_length;
            p.add(new_x, new_y);
            if (hasCollisions(n.left, new_x, new_y, p)) {
                return true;
            }
        }
        
        if (n.hasRight()) {
            new_x = (x+1) - n.arc_length;
            new_y = (y+1) + n.arc_length;
            p.add(new_x, new_y);
            if (hasCollisions(n.right, new_x, new_y, p)) {
                return true;
            }
        }
        
        for (int i = 0; i < p.size(); i++) {
            for (int j = i+1; j < p.size(); j++) {
                if (p.get(i).equals(p.get(j))) return true;
            }
        }
        return false;
    }
    
    public static int getTreeLength(Node n) {
        int a = Integer.MIN_VALUE, b = Integer.MIN_VALUE;
        if(n.left == null && n.right == null) return 0;
        if(n.left != null) {
            a = 1 + getTreeLength(n.left);
        }
        if(n.right != null) {
            b = 1 + getTreeLength(n.right);
        }
        return Math.max(a, b);
    }
    
    int total_LeftArcSons() {
        if(left != null) return 1 + arc_length + left.total_LeftArcSons();
        else return 0;
    }
    
    int total_RightArcSons() {
        return hasRight()? 1 + arc_length + right.total_RightArcSons(): 0;
    }
    
    int getLongRightPath() {
        return Math.max(hasLeft()? left.getLongRightPath() - arc_length - 1: 0,
                hasRight()? right.getLongRightPath() + arc_length + 1: 0);
    }
    
    int getLongLeftPath() {
        return Math.max(hasLeft()? left.getLongLeftPath() + arc_length + 1: 0,
                hasRight()? right.getLongLeftPath() - arc_length - 1: 0);
    }
}

class TreeGraph {
    ArrayList<String> lines = new ArrayList<>();
    
    public static TreeGraph concat(TreeGraph t1, TreeGraph t2, int leading) {
        TreeGraph t = new TreeGraph();
        int t1_size = t1.lines.size();
        int t2_size = t2.lines.size();
        int t1_line_length = t1.lines.get(0).length();
        String s = "";
        for (int i = 0; i < leading; i++) {
            s += " ";
        }
        if (t1_size > t2_size) {
            for (int i = 0; i < t2_size; i++) {
                t.lines.add(t1.lines.get(i) + s + t2.lines.get(i));
            }
            for (int i = t2_size; i < t1_size; i++) {
                t.lines.add(t1.lines.get(i));
            }
        }
        else if (t1_size < t2_size) {
            for (int i = 0; i < t1_size; i++) {
                t.lines.add(t1.lines.get(i) + s + t2.lines.get(i));
            }
            for (int i = t1_size; i < t2_size; i++) {
                String whitespaces = s;
                for (int j = 0; j < t1_line_length; j++) whitespaces += " ";
                t.lines.add(whitespaces + t2.lines.get(i));
            }
        }
        else {
            for (int i = 0; i < t1_size; i++) {
                t.lines.add(t1.lines.get(i) + s + t2.lines.get(i));
            }
        }
        return t;
    }
    
    public void print() {
        for(String s: lines) {
            System.out.println(s);
        }
    }
    
    public static TreeGraph addSpaces(TreeGraph t, boolean inLeft, int amount) {
        if (t.lines.isEmpty()) return t;
        for (int i = 0; i < t.lines.size(); i++) {
            String s = "";
            for (int j = 0; j < amount; j++) {
                s += " ";
            }
            String left = (inLeft)? s : "";
            String right = (!inLeft)? s : "";
            t.lines.set(i, left + t.lines.get(i) + right);
        }
        return t;
    }
}

class Coordinates {
    ArrayList< Pair<Integer,Integer> > points = new ArrayList<>();
    
    void add (int x, int y) {
        points.add(new Pair<>(x, y));
    }
    
    Pair<Integer,Integer> get (int i) {
        return points.get(i);
    } 
    
    int size() {
        return points.size();
    }
}