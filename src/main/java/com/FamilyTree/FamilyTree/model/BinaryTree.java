package com.FamilyTree.FamilyTree.model;


import java.util.Collections;
import java.util.List;

public class BinaryTree {

    Node root;

    public BinaryTree() {
        root = null;
    }

    public Node getRoot() {
        return root;
    }

    public void insert(List<List<String>> data) {
        for (List<String> item : data) {
            root = insertRec(root, Collections.singletonList(item));
        }
    }

    private Node insertRec(Node root, List<List<String>> data) {
        if (root == null) {
            return new Node(data);
        }

        String relationship = data.get(0).get(2);
        String[] tokens = relationship.split(" ");
        Node current = root;

        for (String token : tokens) {
            if (token.contains("Anne")) {
                if (current.left == null) {
                    current.left = new Node(null);
                }
                current = current.left;
            } else if (token.contains("Baba")) {
                if (current.right == null) {
                    current.right = new Node(null);
                }
                current = current.right;
            }
        }

        current.data = data;
        return root;
    }

}
