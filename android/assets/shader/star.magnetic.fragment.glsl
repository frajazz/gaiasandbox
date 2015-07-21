#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec4 u_color;
uniform float u_inner_rad;
uniform float u_time;
// Distance in km to the star
uniform float u_distance;

#ifdef GL_ES
vec4 draw_star() {
    // Distance from the center of the image to the border, in [0, 1]
    float dist = distance(vec2(0.5, 0.5), v_texCoords.xy) * 2.0;
    float fac = 1.0 - sqrt(dist);
    return vec4(u_color.rgb, u_color.a * fac);
}
#else
uniform float u_th_dist_up;
#define max_opacity 0.6
#define time u_time * 0.2

float
snoise(vec3 uv, float res)	// by trisomie21
{
    const vec3 s = vec3 (1e0, 1e2, 1e4);

    uv *= res;

    vec3 uv0 = floor (mod (uv, res)) * s;
    vec3 uv1 = floor (mod (uv + vec3 (1.), res)) * s;

    vec3 f = fract (uv);
    f = f * f * (3.0 - 2.0 * f);

    vec4 v = vec4 (uv0.x + uv0.y + uv0.z, uv1.x + uv0.y + uv0.z,
		   uv0.x + uv1.y + uv0.z,
		   uv1.x + uv1.y + uv0.z);

    vec4 r = fract (sin (v * 1e-3) * 1e5);
    float r0 = mix (mix (r.x, r.y, f.x), mix (r.z, r.w, f.x), f.y);

    r = fract (sin ((v + uv1.z - uv0.z) * 1e-3) * 1e5);
    float r1 = mix (mix (r.x, r.y, f.x), mix (r.z, r.w, f.x), f.y);

    return mix (r0, r1, f.z) - 1.0;
}

// Level is between 0 and 1
vec4
draw_complex_star() {
    float scale = 0.1;
    float brightness = 0.1;

    vec3 orange = vec3 (0.8, 0.65, 0.3) * 0.3 + u_color.rgb * 0.7;
    vec2 uv = v_texCoords.xy;
    vec2 p = -0.5 + uv;

    float fade = pow (length (2.0 * p), 0.5);
    float fVal1 = 1.0 - fade;
    float fVal2 = 1.0 - fade;

    float angle = atan (p.x, p.y) / 6.28318530718;
    float dist = length (p);
    vec3 coord = vec3 (angle, dist, time * 0.1);

    float newTime1 = abs (snoise (coord + vec3 (0.0, -time * (0.35 + brightness * 0.001), time * 0.015), 15.0));
    float newTime2 = abs (snoise (coord + vec3 (0.0, -time * (0.15 + brightness * 0.001), time * 0.015), 45.0));
    for (int i = 1; i <= 1; i++) {
	float power = pow (4.0, float (i + 1));
	fVal1 += (0.5 / power) * snoise (coord + vec3 (0.0, -time, time * 0.2), (power * (10.0) * (newTime1 + 1.0)));
	fVal2 += (0.5 / power) * snoise (coord + vec3 (0.0, -time, time * 0.2), (power * (25.0) * (newTime2 + 1.0)));
    }

    float corona = pow (fVal1 * max (1.1 - fade, 0.0), 2.0) * 50.0 * scale;
    corona += pow (fVal2 * max (1.1 - fade, 0.0), 2.0) * 50.0 * scale;
    corona *= 1.2 - newTime1;

    vec3 col = vec3 (corona * orange);
    return vec4 (col, u_color.a * (col.r + col.g + col.b) / 3.0);
}


vec4
draw_simple_star(vec2 texCoords) {
    // Distance from the center of the image to the border, in [0, 1]
    float dist = distance (vec2 (0.5, 0.5), texCoords.xy) * 2.0;
    float fac = 1.0 - pow (dist, 0.08);
    float core = smoothstep (u_inner_rad, 0.0, dist);

    return vec4 (u_color.rgb + core, u_color.a * (fac + core));
}


vec4
draw_star() {
	if(u_distance < u_th_dist_up){
	    // Level is 0 when dist <= dist_down and 1 when dist >= dist_up 
	    float level = min((u_distance) / u_th_dist_up, 1.0);
	    vec4 c = draw_complex_star();
	    vec4 s = draw_simple_star(v_texCoords);
	    return c * (max_opacity - level * max_opacity) + s * level;
	}else{
	    return draw_simple_star (v_texCoords);
	}
//    if (u_distance < u_th_dist_up) {
//	return crepuscular_rays (v_texCoords);
//    } else {
//	return draw_simple_star (v_texCoords);
//    }
}
#endif // GL_ES

void
main() {
    gl_FragColor = draw_star ();
}
