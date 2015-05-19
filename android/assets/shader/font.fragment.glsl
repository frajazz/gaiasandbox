#version 120
varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_opacity;

uniform sampler2D u_texture;
uniform float scale;
uniform float u_opacity;

void main(void){
    // Smoothing is adapted arbitrarily to produce crisp borders at all sizes
    float smoothing = 0.25 / (4.0 * scale);
    float dist = texture2D(u_texture, v_texCoords).a;
    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, dist);
    float aa = alpha * v_opacity * u_opacity;
    if (aa < 0.001)
	    discard;

    gl_FragColor = vec4(v_color.rgb, aa);
}
