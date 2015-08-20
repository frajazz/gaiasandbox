package gaia.cu9.ari.gaiaorbit.util;

public abstract class DataFilesFactory {
    static DataFilesFactory instance;

    public static void initialize(DataFilesFactory inst) {
        instance = inst;

    }

    public static IDataFiles getDataFiles() {
        return instance.getDataFilesConcrete();
    }

    protected abstract IDataFiles getDataFilesConcrete();

}
