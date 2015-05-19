attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_uv;

uniform mat4 u_projModelView;
uniform vec2 u_viewport;

varying vec4 v_col;
varying vec2 v_uv;

void main() {
   gl_Position = u_projModelView * a_position;
   v_col = a_color;
   
   v_uv = a_uv;
}
