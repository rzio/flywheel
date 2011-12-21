package pro.reznick.flywheel.hashing;

import com.google.inject.Inject;

import java.util.Map;

/**
 * @author alex
 * @since 12/15/11 6:37 PM
 */

public class HashingStrategyFactory
{
    Map<String, HashingStrategy> strategies;

    @Inject
    public HashingStrategyFactory(Map<String, HashingStrategy> strategies)
    {
        this.strategies = strategies;
    }
    
    public HashingStrategy get(String name)
    {
        return strategies.get(name);
    }
}
