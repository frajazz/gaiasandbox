varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float scale;
uniform float avalue;

void main(void){
    // Smoothing is adapted arbitrarily to produce crisp borders at all sizes
    float smoothing = 0.25 / (4.0 * scale);
    float dist = texture2D(u_texture, v_texCoords).a;
    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, dist);
    float aa = alpha * avalue;
    if (aa < 0.02)
	discard;

    gl_FragColor = vec4(v_color.rgb, aa);
}
