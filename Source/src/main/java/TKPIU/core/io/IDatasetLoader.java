package TKPIU.core.io;

import TKPIU.core.database.Database;
import TKPIU.core.database.UncertainItem;

public interface IDatasetLoader {
    public Database loadDatabase(String path);
    public UncertainItem getUncertainItem(String input);
}
