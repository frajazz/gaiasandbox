package gaia.cu9.ari.gaiaorbit.util.override;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

public class AtmosphereGroundShaderProvider extends DefaultShaderProvider {
    public final AtmosphereGroundShader.Config config;

    public AtmosphereGroundShaderProvider(final AtmosphereGroundShader.Config config) {
	this.config = (config == null) ? new AtmosphereGroundShader.Config() : config;
    }

    public AtmosphereGroundShaderProvider(final String vertexShader, final String fragmentShader) {
	this(new AtmosphereGroundShader.Config(vertexShader, fragmentShader));
    }

    public AtmosphereGroundShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader) {
	this(vertexShader.readString(), fragmentShader.readString());
    }

    public AtmosphereGroundShaderProvider() {
	this(null);
    }

    @Override
    protected Shader createShader(final Renderable renderable) {
	return new AtmosphereGroundShader(renderable, config);
    }
}
