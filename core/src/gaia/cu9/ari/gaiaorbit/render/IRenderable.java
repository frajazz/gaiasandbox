package gaia.cu9.ari.gaiaorbit.render;


/**
 * A top-level renderable interface that all renderable objects must extend.
 * @author Toni Sagrista
 *
 */
public interface IRenderable {

    /**
     * Renders the entity. It should provide the right parameters so that
     * the right render sub-method is called.
     * @param params The render parameters.
     */
    public void render(Object... params);

    /**
     * Gets the component type of this entity.
     * @return The component type
     */
    public ComponentType getComponentType();

    /**
     * Gets the last distance to the camera calculated for this entity.
     * @return The distance.
     */
    public float getDistToCamera();

}
