package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * The suite of all JUnit tests for the gitlet package.
 *
 * @author Ang Wang
 */
public class UnitTest {

    /**
     * Run the JUnit tests in the loa package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * Test conflicts.
     */
    @Test
    public void placeholderTest() {
        System.out.println(String.format(Utils.CONFLICT, "wug.txt"
              +  System.lineSeparator(), ""));
    }

    /**
     * Test path matcher.
     */
    @Test
    public void testPath() {
        String input = "../testing/otherdir/.gitlet";
        File res = new File(input);
        System.out.println(res.toString());
    }

}


