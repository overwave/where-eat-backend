package dev.overwave.whereeat.core.util;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@UtilityClass
public class MediaUtils {
    public static List<Pair<Integer, Integer>> justify(List<Pair<Integer, Integer>> sizes, int targetWidth, int targetHeight, int gap) {
        List<Box> boxes = sizes.stream()
                .map(pair -> new Box(pair.getFirst(), pair.getSecond(), 1))
                .toList();
        Node rootNode = new Node(List.of(), 0);

        traverse(rootNode, boxes, 0, targetWidth, targetHeight, gap);

        rootNode.countScore();
        Node bestChild = rootNode.getBestChild();
        return bestChild.getBoxesFromRoot().stream()
                .map(Box::toPair)
                .toList();
    }

    private static void traverse(Node parentNode, List<Box> boxes, int from, int targetWidth, int targetHeight, int gap) {
        int cursor = from;
        double occupiedWidth = gap;

        List<Box> row = new ArrayList<>();
        for (; cursor < boxes.size() && occupiedWidth < targetWidth; cursor++) {
            Box box = boxes.get(cursor);
            double ratio = targetHeight / box.y();
            row.add(box.mul(ratio));
            occupiedWidth += box.x() * ratio + gap;
        }

        if (cursor > from) {
            Node node = createRow(row, targetWidth, gap);
            parentNode.add(node);
            traverse(node, boxes, cursor,  targetWidth,  targetHeight, gap);
        }
        if (cursor - 1 > from) {
            Node node = createRow(row.subList(0, row.size() - 1), targetWidth, gap);
            parentNode.add(node);
            traverse(node, boxes, cursor - 1,targetWidth,  targetHeight, gap);
        }
    }

    private static Node createRow(List<Box> row, int targetWidth, int gap) {
        double width = row.stream().mapToDouble(Box::x).sum() ;
        double ratio = (targetWidth - (row.size() + 1) * gap) / width;

        row = row.stream()
                .map(box -> box.mul(ratio))
                .toList();
        double score = row.stream()
                .mapToDouble(Box::scale)
                .map(s -> (1 - s) * 100)
                .map(s -> s * s)
                .sum();

        return new Node(row, score);
    }

    private record Box(double x, double y, double scale) {
        public Box mul(double ratio) {
            return new Box(x * ratio, y * ratio, scale * ratio);
        }

        public Pair<Integer, Integer> toPair() {
            double ratio = Math.ceil(y) / y;

            return Pair.of((int) (x / ratio), (int) (y / ratio));
        }
    }

    private static class Node {
        private Node parent;
        private final List<Node> children;

        private final List<Box> boxes;
        private double score;


        public Node(List<Box> boxes, double score) {
            this.children = new ArrayList<>();
            this.boxes = boxes;
            this.score = score;
        }

        public void add(Node child) {
            child.parent = this;
            children.add(child);
        }

        public void countScore() {
            if (parent != null) {
                score += parent.score;
            }
            children.forEach(Node::countScore);
        }

        public Node getBestChild() {
            return children.stream()
                    .map(Node::getBestChild)
                    .min(Comparator.comparingDouble(node -> node.score))
                    .orElse(this);
        }

        public List<Box> getBoxesFromRoot() {
            List<Box> result = new ArrayList<>();

            Node node = this;
            while (node != null) {
                result.addAll(Lists.reverse(node.boxes));
                node = node.parent;
            }

            return Lists.reverse(result);
        }
    }
}
