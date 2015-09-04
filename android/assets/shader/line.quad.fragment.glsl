#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 v_col;
varying vec2 v_uv;

void main() {
    float alpha = 1.0 - 2.0 * abs(v_uv.y - 0.5);
//    if(v_uv.x < 0.1 || v_uv.x > 0.9 || v_uv.y < 0.1 || v_uv.y > 0.9){
//        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
//    }else{
    gl_FragColor = vec4(v_col.rgb, v_col.a * alpha);
//    }
}