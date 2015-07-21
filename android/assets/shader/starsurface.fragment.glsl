#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// UNIFORMS

// Diffuse base texture
uniform sampler2D u_diffuseTexture;
// Grayscale lookup table
uniform sampler2D u_normalTexture;

// VARYINGS

// Time in seconds
varying float v_time;
// Ambient color (star color in this case)
varying vec3 v_lightDiffuse;
// The normal
varying vec3 v_normal;
// Coordinate of the texture
varying vec2 v_texCoords0;
// Opacity
varying float v_opacity;
// View vector
varying vec3 v_viewVec;


#define time v_time * 0.001

void main() {
    // Perimeter is 1 when normal faces camera, 0 when normal is 90 degrees from view.
    float perimeter = dot(normalize(v_normal), vec3(v_viewVec));
    vec3 percolor = v_lightDiffuse * min(1.0, perimeter + 0.35);
    
    vec3 diffuse = texture2D(u_diffuseTexture, v_texCoords0).rgb;
    float grayscale = (diffuse.r + diffuse.g + diffuse.b) / 3.0;
    grayscale *= 0.5;
    float offset = (sin(time) + 1.0) * 0.25;
    vec3 color = texture2D(u_normalTexture, vec2(grayscale + offset, 0.0)).rgb;
    gl_FragColor = vec4(color * 1.7 * v_lightDiffuse * percolor, v_opacity);
}