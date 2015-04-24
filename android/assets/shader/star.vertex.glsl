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
   
   mat4 transform = u_projTrans;
   
   // Scale
   mat4 translate = mat4(1.0);
   
   // Translate
   translate[3][0] = u_pos.x;
   translate[3][1] = u_pos.y;
   translate[3][2] = u_pos.z;
   translate[3][3] = 1.0;
   transform *= translate;
   
   // Rotate
   mat4 rotation = mat4(0.0);
   float xx = u_quaternion.x * u_quaternion.x;
   float xy = u_quaternion.x * u_quaternion.y;
   float xz = u_quaternion.x * u_quaternion.z;
   float xw = u_quaternion.x * u_quaternion.w;
   float yy = u_quaternion.y * u_quaternion.y;
   float yz = u_quaternion.y * u_quaternion.z;
   float yw = u_quaternion.y * u_quaternion.w;
   float zz = u_quaternion.z * u_quaternion.z;
   float zw = u_quaternion.z * u_quaternion.w;
   
   rotation[0][0] = 1.0 - 2.0 * (yy + zz);
   rotation[1][0] = 2.0 * (xy - zw);
   rotation[2][0] = 2.0 * (xz + yw);
   rotation[0][1] = 2.0 * (xy + zw);
   rotation[1][1] = 1.0 - 2.0 * (xx + zz);
   rotation[2][1] = 2.0 * (yz - xw);
   rotation[3][1] = 0.0;
   rotation[0][2] = 2.0 * (xz - yw);
   rotation[1][2] = 2.0 * (yz + xw);
   rotation[2][2] = 1.0 - 2.0 * (xx + yy);
   rotation[3][3] = 1.0;
   transform *= rotation;
   
   // Scale
   transform[0][0] *= u_size;
   transform[1][1] *= u_size;
   transform[2][2] *= u_size;
   
   // Position
   gl_Position =  transform * a_position;
}
