package com.FamilyTree.FamilyTree.model;

import java.util.List;

public class Node {
    public List<List<String>> data;
    public Node left;
    public Node right;

    public Node(List<List<String>> data) {
        this.data = data;
        this.left = null;
        this.right = null;
    }



}
