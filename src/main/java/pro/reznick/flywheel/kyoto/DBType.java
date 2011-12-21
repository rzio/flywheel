package pro.reznick.flywheel.kyoto;

import org.apache.commons.lang.StringUtils;

/**
 * @author alex
 * @since 12/15/11 5:02 PM
 */

public enum DBType
{
    ProtoHashDB("-", false),
    ProtoTreeDB("+", false),
    StashDB(":", false),
    CacheDB("*", false),
    GrassDB("%", false),
    HashDB(".kch", true),
    TreeDB(".kct", true),
    DirDB(".kcd", true),
    ForestDB(".kct", true);


    private boolean hasPath;
    private String suffix;

    private DBType(String suffix, boolean hasPath)
    {

        this.suffix = suffix;
        this.hasPath = hasPath;
    }

    public boolean hasPath()
    {
        return hasPath;
    }

    public void setHasPath(boolean hasPath)
    {
        this.hasPath = hasPath;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public static DBType valueOfIgnoreCase(String name)
    {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException(String.format("Failed resolving value %s to enum type DBType", name));

        if (name.compareToIgnoreCase("ProtoHashDB") == 0)
            return DBType.ProtoHashDB;
        else if (name.compareToIgnoreCase("ProtoTreeDB") == 0)
            return DBType.ProtoTreeDB;
        else if (name.compareToIgnoreCase("StashDB") == 0)
            return DBType.StashDB;
        else if (name.compareToIgnoreCase("CacheDB") == 0)
            return DBType.CacheDB;
        else if (name.compareToIgnoreCase("GrassDB") == 0)
            return DBType.GrassDB;
        if (name.compareToIgnoreCase("HashDB") == 0)
            return DBType.HashDB;
        else if (name.compareToIgnoreCase("TreeDB") == 0)
            return DBType.TreeDB;
        else if (name.compareToIgnoreCase("DirDB") == 0)
            return DBType.DirDB;
        else if (name.compareToIgnoreCase("ForestDB") == 0)
            return DBType.ForestDB;
        else if (name.compareToIgnoreCase("TextDB") == 0)
            return DBType.HashDB;
        throw new IllegalArgumentException(String.format("Failed resolving value %s to enum type DBType", name));

    }


}
