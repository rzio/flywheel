package pro.reznick.flywheel.testtools;


import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.is;

/**
 * @author alex
 * @since 12/10/11 12:32 PM
 */

public class IterableMatcher<T> extends TypeSafeMatcher<Iterable<T>>
{
    private Iterable<T> expected;


    public IterableMatcher(Iterable<T> expected)
    {

        this.expected = expected;

    }

    @Override
    public boolean matchesSafely(Iterable<T> actual)
    {
        Iterator<T> eIterator = expected.iterator();
        Iterator<T> aIterator = actual.iterator();

        while (eIterator.hasNext() && eIterator.hasNext() == aIterator.hasNext() )
        {

            if (!is(eIterator.next()).matches(aIterator.next()))
                return false;
        }

        return true;
    }

    @Override
    public void describeTo(Description description)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public static <T> org.hamcrest.Matcher<java.lang.Iterable<T>> hasItemsInOrder(T... elements)
    {
        List<T> l = new ArrayList<T>();
        Collections.addAll(l, elements);
        return new IterableMatcher<T>(l);
    }

}


