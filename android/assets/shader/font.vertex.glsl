attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform float a_viewAngle;
uniform float a_thOverFactor;
uniform float a_componentAlpha;
uniform float a_labelAlpha;

varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_opacity;

void main()
{
   v_opacity = max(0.0, min(0.95, (a_viewAngle - a_thOverFactor)/(a_thOverFactor * 2.5))) * a_componentAlpha * a_labelAlpha;
   v_color = a_color;
   v_texCoords = a_texCoord0;
   gl_Position =  u_projTrans * a_position;
}
