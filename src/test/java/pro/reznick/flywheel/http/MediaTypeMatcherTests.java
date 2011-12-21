package pro.reznick.flywheel.http;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author alex
 * @since 12/17/11 2:18 PM
 */

public class MediaTypeMatcherTests
{
    @Test
    public void testSingleValues()
    {
        assertThat(MediaTypeMatcher.matchAcceptHeader("text/plain","text/plain" ), is(true));
        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html","text/html; charset=ISO-8859-4" ), is(false));
        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html; charset=ISO-8859-4","text/html; charset=ISO-8859-4" ), is(true));
        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html; charset=ISO-8859-4","text/*" ), is(true));
        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html; charset=ISO-8859-4","*/*" ), is(true));
    }

    @Test
    public void testMultiValues()
    {
        assertThat(MediaTypeMatcher.matchAcceptHeader("text/plain","audio/mp3; q=0.5, application/json; q=0.2, text/plain; charset=ISO-8859-4; q=0.2, text/*" ), is(true));
//        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html","text/html; charset=ISO-8859-4" ), is(false));
//        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html; charset=ISO-8859-4","text/html; charset=ISO-8859-4" ), is(true));
//        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html; charset=ISO-8859-4","text/*" ), is(true));
//        assertThat(MediaTypeMatcher.matchAcceptHeader("text/html; charset=ISO-8859-4","*/*" ), is(true));
    }

}
