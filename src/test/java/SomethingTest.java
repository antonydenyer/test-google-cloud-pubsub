import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class SomethingTest {


    public static Node append(Node node, Integer value) {
        if (node == null) {
            return new Node(value);
        }

        if (value < node.value) {
            node.lhs = append(node.lhs, value);
        } else if (value > node.value) {
            node.rhs = append(node.rhs, value);
        }
        return node;
    }

    public static String calculatePath(Node root) {
        Stack<Node> memo = new Stack();
        List<Node> walkTree = new ArrayList<>();

        memo.push(root);

        while (!memo.empty()) {
            Node node = memo.pop();
            walkTree.add(node);

            if (node.rhs != null)
                memo.push(node.rhs);
            if (node.lhs != null)
                memo.push(node.lhs);
        }


        return walkTree.stream().map((n) -> n.value.toString()).collect(Collectors.joining(""));
    }

    public static boolean isValid(List<Integer> nodeValues) {

        String history = nodeValues.stream().map(String::valueOf)
                .collect(Collectors.joining(""));

        Node root = null;
        for (Integer i : nodeValues) {
            root = append(root, i);
        }

        return history.equals(calculatePath(root));
    }


    @Test
    public void canDoThing() {
        Integer[] x = new Integer[]{3,2,1,5,4,6};
        List<Integer> y = Arrays.asList(x);
        boolean result = isValid(y);

        assertThat(result, equalTo(true));
    }




}




class Node {
    Node lhs = null;
    Node rhs = null;
    Integer value;

    Node(Integer value) {
        this.value = value;
    }
}