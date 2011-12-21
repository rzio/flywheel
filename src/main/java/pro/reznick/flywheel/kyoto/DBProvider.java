package pro.reznick.flywheel.kyoto;

import com.google.inject.Inject;
import kyotocabinet.DB;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import pro.reznick.flywheel.configuration.InvalidConfigurationException;

import javax.inject.Provider;
import java.io.File;

/**
 * @author alex
 * @since 12/15/11 5:02 PM
 */
public class DBProvider implements Provider<DB>
{
    DB db;

    private final static String dbFilename = "flywheel";

    @Inject
    public DBProvider(Configuration topology)
    {
        // create the object
        db = new DB();

        String path = topology.getString("topology.kyoto[@pathToFile]");
        String dbType = topology.getString("topology.kyoto[@dbType]");


        // open the database
        if (!db.open(resolveDBFileName(path,dbType), DB.OWRITER | DB.OCREATE))
        {
            throw db.error();
        }

    }


    private String resolveDBFileName(String path, String dbType)
    {
        DBType type;
        if (StringUtils.isBlank(path) && StringUtils.isBlank(dbType))
            type = DBType.ProtoHashDB;
        else if (StringUtils.isBlank(dbType))
            type = DBType.HashDB;
        else
            type = DBType.valueOfIgnoreCase(dbType);

        if (!type.hasPath())
            return type.getSuffix();
        else
        {
            if (StringUtils.isBlank(path))
                throw new InvalidConfigurationException("you're trying to configure flywheel with kyoto cabinet, but you haven't provided the necessary configuration" +
                        "<flywheel><topology><kyoto pathToFile=\"directory\" dbType=\"type\"</topology></flywheel>/>");
            if (!path.endsWith(File.separator))
                path = path + File.separator;
            return path + dbFilename + type.getSuffix();

        }
    }


    @Override
    public DB get()
    {
        return db;
    }
}
