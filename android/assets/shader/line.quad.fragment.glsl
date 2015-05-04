#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_col;
varying vec2 v_uv;

void main() {
    float alpha = 1.0 - 2.0 * abs(v_uv.y - 0.5);
    gl_FragColor = vec4(v_col.rgb + step(0.8, alpha), v_col.a * alpha);
}