package pro.reznick.flywheel.configuration;

import pro.reznick.flywheel.hashing.HashingStrategy;

/**
 * @author alex
 * @since 12/10/11 9:36 AM
 */

public class CollectionConfiguration
{
    private int id;
    private String name;
    private HashingStrategy hashingStrategy;


    public static CollectionConfiguration create(String name, HashingStrategy hashingStrategy)
    {
        return new CollectionConfiguration(name.hashCode(), name, hashingStrategy);
    }

    public static CollectionConfiguration create(int id, String name, HashingStrategy hashingStrategy)
    {
        return new CollectionConfiguration(id, name, hashingStrategy);
    }

    private CollectionConfiguration(int id, String name, HashingStrategy hashingStrategy)
    {
        this.id = id;
        this.name = name;
        this.hashingStrategy = hashingStrategy;
    }


    public int getId()
    {
        return id;
    }

    public HashingStrategy getHashingStrategy()
    {
        return hashingStrategy;
    }

    public String getName()
    {
        return name;
    }
}
