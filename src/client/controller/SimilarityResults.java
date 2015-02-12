package client.controller;

import client.model.Product;

import javax.swing.*;

/**
 * Class:
 * Author: Martin Veselovsky
 * Date:   14.12.2014
 * Info:   Keep info about similarity results. This is item of list in Database class.
 */
public class SimilarityResults extends JLabel {

    public double similarity;
    public int picture_id;
    public Product product;

    public SimilarityResults(double similarity, int picture_id, Product product) {
        this.similarity = similarity;
        this.picture_id = picture_id;
        this.product = product;
        setToolTipText("Similarity: " + similarity);
    }

    @Override
    public String toString() {
        return "picture " + picture_id + " of " + product.getName();
    }
}
