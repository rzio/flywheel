package pro.reznick.flywheel.http;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static pro.reznick.flywheel.testtools.IterableMatcher.hasItemsInOrder;

/**
 * @author alex
 * @since 12/10/11 12:21 PM
 */

public class PathTokenizerTests
{

    @Test
    public void test()
    {
        assertThat(PathTokenizer.tokenize("a/b/c"),hasItemsInOrder("a", "b", "c"));
        assertThat(PathTokenizer.tokenize("/a/b/c/"),hasItemsInOrder("a", "b", "c"));
        assertThat(PathTokenizer.tokenize("a//b//c"),hasItemsInOrder("a", "b", "c"));

        assertThat(PathTokenizer.tokenize("abc//bcd//cde"),hasItemsInOrder("abc", "bcd", "cde"));
        assertThat(PathTokenizer.tokenize("abc//bcd//cde"),hasItemsInOrder("abc", "bcd", "cde"));
        assertThat(PathTokenizer.tokenize("abc//bcd//cde"),hasItemsInOrder("abc", "bcd", "cde"));
    }

    

}
