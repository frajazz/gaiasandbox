package gaia.cu9.ari.gaiaorbit.event;

public interface IObserver {

    public void notify(Events event, Object... data);

}
