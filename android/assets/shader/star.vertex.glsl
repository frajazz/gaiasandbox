attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform vec4 u_quaternion;
uniform vec3 u_pos;
uniform float u_size;

varying vec4 v_color;
varying vec2 v_texCoords;

void main()
{
   v_color = a_color;
   v_texCoords = a_texCoord0;
   vec4 vertex = a_position;
   
   // Scale
   vertex.x *= u_size;
   vertex.y *= u_size;
   vertex.z *= u_size;
   
   // Translate
   vertex += vec4(u_pos, 0.0);
   
   // Position
   gl_Position =  u_projTrans * vertex;
}
