#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP 
#endif

#define nop() {}

////////////////////////////////////////////////////////////////////////////////////
////////// POSITION ATTRIBUTE - VERTEX
////////////////////////////////////////////////////////////////////////////////////
#ifdef positionFlag
    attribute vec3 a_position;
#endif //positionFlag

varying vec4 v_position;
#define pushPositionValue(value) v_position = value
#if defined(positionFlag)
    vec4 g_position = vec4(a_position, 1.0);
    #define passPositionValue(value) pushPositionValue(value)
#else
    vec4 g_position = vec4(0.0, 0.0, 0.0, 1.0);
    #define passPositionValue(value) nop()
#endif
#define passPosition() passPositionValue(g_position)
#define pushPosition() pushPositionValue(g_position)

////////////////////////////////////////////////////////////////////////////////////
////////// COLOR ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#ifdef colorFlag
    attribute vec4 a_color;
#endif //colorFlag

varying vec4 v_color;
#define pushColor(value) v_color = value

#if defined(colorFlag)
    vec4 g_color = a_color;
#else
    vec4 g_color = vec4(1.0, 1.0, 1.0, 1.0);
#endif // colorFlag

////////////////////////////////////////////////////////////////////////////////////
////////// NORMAL ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#ifdef normalFlag
    attribute vec3 a_normal;
#endif //normalFlag

varying vec3 v_normal;
#define pushNormalValue(value) v_normal = (value)
#if defined(normalFlag)
    vec3 g_normal = a_normal;
    #define passNormalValue(value) pushNormalValue(value)
#else
    vec3 g_normal = vec3(0.0, 0.0, 1.0);
    #define passNormalValue(value) nop()
#endif
#define passNormal() passNormalValue(g_normal)
#define pushNormal() pushNormalValue(g_normal)

////////////////////////////////////////////////////////////////////////////////////
////////// BINORMAL ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#ifdef binormalFlag
    attribute vec3 a_binormal;
#endif //binormalFlag

varying vec3 v_binormal;
#define pushBinormalValue(value) v_binormal = (value)
#if defined(binormalFlag)
    vec3 g_binormal = a_binormal;
    #define passBinormalValue(value) pushBinormalValue(value)
#else
    vec3 g_binormal = vec3(0.0, 1.0, 0.0);
    #define passBinormalValue(value) nop()
#endif // binormalFlag
#define passBinormal() passBinormalValue(g_binormal)
#define pushBinormal() pushBinormalValue(g_binormal)

////////////////////////////////////////////////////////////////////////////////////
////////// TANGENT ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#ifdef tangentFlag
    attribute vec3 a_tangent;
#endif //tangentFlagvec3

varying vec3 v_tangent;
#define pushTangentValue(value) v_tangent = (value)
#if defined(tangentFlag)
    vec3 g_tangent = a_tangent;
    #define passTangentValue(value) pushTangentValue(value)
#else
    vec3 g_tangent = vec3(1.0, 0.0, 0.0);
    #define passTangentValue(value) nop()
#endif // tangentFlag
#define passTangent() passTangentValue(g_tangent)
#define pushTangent() pushTangentValue(g_tangent)

////////////////////////////////////////////////////////////////////////////////////
////////// TEXCOORD0 ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#ifdef texCoord0Flag
    #ifndef texCoordsFlag
	#define texCoordsFlag
    #endif
    attribute vec2 a_texCoord0;
#endif

varying vec2 v_texCoord0;
#define pushTexCoord0(value) v_texCoord0 = value

#if defined(texCoord0Flag)
    vec2 g_texCoord0 = a_texCoord0;
#else
    vec2 g_texCoord0 = vec2(0.0, 0.0);
#endif // texCoord0Flag



////////////////////////////////////////////////////////////////////////////////////
////////// GROUND ATMOSPHERIC SCATTERING - VERTEX
////////////////////////////////////////////////////////////////////////////////////
varying vec3 v_atmosphereColor;
#ifdef atmosphereGround
    uniform vec3 v3PlanetPos; /* The position of the planet */
    uniform vec3 v3CameraPos; /* The camera's current position*/
    uniform vec3 v3LightPos; /* The direction vector to the light source*/
    uniform vec3 v3InvWavelength; /* 1 / pow(wavelength, 4) for the red, green, and blue channels*/
    
    uniform float fCameraHeight;
    uniform float fCameraHeight2; /* fCameraHeight^2*/
    uniform float fOuterRadius; /* The outer (atmosphere) radius*/
    uniform float fOuterRadius2; /* fOuterRadius^2*/
    uniform float fInnerRadius; /* The inner (planetary) radius*/
    uniform float fKrESun; /* Kr * ESun*/
    uniform float fKmESun; /* Km * ESun*/
    uniform float fKr4PI; /* Kr * 4 * PI*/
    uniform float fKm4PI; /* Km * 4 * PI*/
    uniform float fScale; /* 1 / (fOuterRadius - fInnerRadius)*/
    uniform float fScaleDepth; /* The scale depth (i.e. the altitude at which the atmosphere's average density is found)*/
    uniform float fScaleOverScaleDepth; /* fScale / fScaleDepth*/
    
    uniform int nSamples;
    uniform float fSamples;
    
    
    float scale(float fCos)
    {
    	float x = 1.0 - fCos;
    	return fScaleDepth * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
    }
    
    float getNearIntersection(vec3 pos, vec3 ray, float distance2, float radius2) {
        float B = 2.0 * dot (pos, ray);
        float C = distance2 - radius2;
        float fDet = max (0.0, B * B - 4.0 * C);
        return 0.5 * (-B - sqrt (fDet));
    }
    
    void calculateAtmosphereGroundColor() {
	// Get the ray from the camera to the vertex and its length (which is the far point of the ray passing through the atmosphere)
	vec3 v3Pos = (a_position) * fOuterRadius;
	vec3 v3Ray = v3Pos - v3CameraPos;
	float fFar = length(v3Ray);
	v3Ray /= fFar;

	// Calculate the closest intersection of the ray with the outer atmosphere (which is the near point of the ray passing through the atmosphere)
	float fNear = getNearIntersection (v3CameraPos, v3Ray, fCameraHeight2, fOuterRadius2);

	// Calculate the ray's starting position, then calculate its scattering offset
	vec3 v3Start = v3CameraPos + v3Ray * fNear;
	fFar -= fNear;
	float fDepth = exp((fInnerRadius - fOuterRadius) / fScaleDepth);
	float poslen = length(a_position);
	float fCameraAngle = dot(-v3Ray, a_position) / poslen;
	float fLightAngle = dot(v3LightPos, a_position) / poslen;
	float fCameraScale = scale(fCameraAngle);
	float fLightScale = scale(fLightAngle);
	float fCameraOffset = fDepth * fCameraScale;
	float fTemp = (fLightScale + fCameraScale);

	/* Initialize the scattering loop variables*/
	float fSampleLength = fFar / fSamples;
	float fScaledLength = fSampleLength * fScale;
	vec3 v3SampleRay = v3Ray * fSampleLength;
	vec3 v3SamplePoint = v3Start + v3SampleRay * 0.5;

	// Now loop through the sample rays
	vec3 v3FrontColor = vec3(0.0, 0.0, 0.0);
	vec3 v3Attenuate;
	for (int i = 0; i < nSamples; i++) {
	    float fHeight = length (v3SamplePoint);
	    float fDepth = exp (fScaleOverScaleDepth * (fInnerRadius - fHeight));
	    float fScatter = fDepth * fTemp - fCameraOffset;
	    
	    v3Attenuate = exp(-fScatter * (v3InvWavelength * fKr4PI + fKm4PI));
	    
	    v3FrontColor += v3Attenuate * (fDepth * fScaledLength);
	    v3SamplePoint += v3SampleRay;
	}
	v_atmosphereColor = vec3(v3FrontColor * (v3InvWavelength * fKrESun + fKmESun)) * 0.4;
    }
#else
    void calculateAtmosphereGroundColor() {
	v_atmosphereColor = vec3(0.0);
    }
#endif // atmosphereGround


// Uniforms which are always available
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform vec4 u_cameraPosition;
uniform mat3 u_normalMatrix;

// Other uniforms
varying float v_opacity;
#ifdef blendedFlag
    uniform float u_opacity;
#else
    const float u_opacity = 1.0;
#endif

varying float v_alphaTest;
#ifdef alphaTestFlag
    uniform float u_alphaTest;
#else
    const float u_alphaTest = 0.0;
#endif

#ifdef shininessFlag
    uniform float u_shininess;
#else
    const float u_shininess = 20.0;
#endif

#ifdef diffuseColorFlag
    uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
    uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
    uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
    uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
    uniform sampler2D u_normalTexture;
#endif

#ifdef bumpTextureFlag
    uniform sampler2D u_bumpTexture;
#endif

////////////////////////////////////////////////////////////////////////////////////
////////// SKINNING
///////////////////////////////////////////////////////////////////////////////////
#ifdef boneWeight0Flag
    #define boneWeightsFlag
    attribute vec2 a_boneWeight0;
#endif //boneWeight0Flag

#ifdef boneWeight1Flag
    #ifndef boneWeightsFlag
	#define boneWeightsFlag
    #endif
    attribute vec2 a_boneWeight1;
#endif //boneWeight1Flag

#ifdef boneWeight2Flag
    #ifndef boneWeightsFlag
	#define boneWeightsFlag
    #endif
    attribute vec2 a_boneWeight2;
#endif //boneWeight2Flag

#ifdef boneWeight3Flag
    #ifndef boneWeightsFlag
	#define boneWeightsFlag
    #endif
    attribute vec2 a_boneWeight3;
#endif //boneWeight3Flag

#ifdef boneWeight4Flag
    #ifndef boneWeightsFlag
	#define boneWeightsFlag
    #endif
    attribute vec2 a_boneWeight4;
#endif //boneWeight4Flag

#ifdef boneWeight5Flag
    #ifndef boneWeightsFlag
	#define boneWeightsFlag
    #endif
    attribute vec2 a_boneWeight5;
#endif //boneWeight5Flag

#ifdef boneWeight6Flag
    #ifndef boneWeightsFlag
	#define boneWeightsFlag
    #endif
    attribute vec2 a_boneWeight6;
#endif //boneWeight6Flag

#ifdef boneWeight7Flag
    #ifndef boneWeightsFlag
	#define boneWeightsFlag
    #endif
    attribute vec2 a_boneWeight7;
#endif //boneWeight7Flag

#if defined(numBones) && defined(boneWeightsFlag)
    #if (numBones > 0) 
	#define skinningFlag
    #endif
#endif

#if defined(numBones)
    #if numBones > 0
	uniform mat4 u_bones[numBones];
    #endif //numBones
#endif

#ifdef skinningFlag
    mat4 skinning = mat4(0.0);
    #ifdef boneWeight0Flag
	skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];
    #endif //boneWeight0Flag
    #ifdef boneWeight1Flag				
	skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];
    #endif //boneWeight1Flag
    #ifdef boneWeight2Flag		
	skinning += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];
    #endif //boneWeight2Flag
    #ifdef boneWeight3Flag
	skinning += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];
    #endif //boneWeight3Flag
    #ifdef boneWeight4Flag
	skinning += (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)];
    #endif //boneWeight4Flag
    #ifdef boneWeight5Flag
	skinning += (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)];
    #endif //boneWeight5Flag
    #ifdef boneWeight6Flag
	skinning += (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)];
    #endif //boneWeight6Flag
    #ifdef boneWeight7Flag
	skinning += (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)];
    #endif //boneWeight7Flag
#endif //skinningFlag

#ifdef skinningFlag
    vec3 applySkinning(const in vec3 x)
    {   return (skinning * vec4(x, 0.0)).xyz;}
    vec4 applySkinning(const in vec4 x)
    {   return (skinning * x);}
#else
    #define applySkinning(x) x
#endif //skinningFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
    #define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
    #define specularFlag
#endif

#if defined(specularFlag) || defined(fogFlag)
    #define cameraPositionFlag
#endif

#ifdef shadowMapFlag
    uniform mat4 u_shadowMapProjViewTrans;
    varying vec3 v_shadowMapUv;
    #define separateAmbientFlag
#endif //shadowMapFlag

#if defined(normalFlag) && defined(binormalFlag) && defined(tangentFlag)
    #define calculateTangentVectors() nop()
#elif defined(normalFlag) && defined(binormalFlag)
    #define calculateTangentVectors() g_tangent = cross(g_normal, g_binormal)
#elif defined(normalFlag) && defined(tangentFlag)
    #define calculateTangentVectors() g_binormal = cross(g_normal, g_tangent)
#elif defined(binormalFlag) && defined(tangentFlag)
    #define calculateTangentVectors() g_normal = cross(g_binormal, g_tangent)
#elif defined(normalFlag) || defined(binormalFlag) || defined(tangentFlag)
#if defined(normalFlag)
    void calculateTangentVectors()
    {
	g_binormal = vec3(0, g_normal.z, -g_normal.y);
	//g_binormal = (cross(g_normal, biggestAngle(g_normal, vec3(1.0, 0.0, 0.0), vec3(0.0, 0.0, 1.0))));
	g_tangent = cross(g_normal, g_binormal);
    }
#elif defined(binormalFlag)
    void calculateTangentVectors()
    {
	g_tangent = vec3(-g_binormal.z, 0, g_binormal.x);
	//g_tangent = (cross(g_binormal, biggestAngle(g_binormal, vec3(0.0, 1.0, 0.0), vec3(0.0, 0.0, 1.0))));
	g_normal = cross(g_binormal, g_tangent);
    }
#elif defined(tangentFlag)
    void calculateTangentVectors()
    {
	g_binormal = vec3(-g_tangent.z, 0, g_tangent.x);
	//g_binormal = (cross(g_tangent, biggestAngle(g_binormal, vec3(1.0, 0.0, 0.0), vec3(0.0, 0.0, 1.0))));
	g_normal = cross(g_tangent, g_binormal);
    }
#endif
#endif

//////////////////////////////////////////////////////
////// AMBIENT LIGHT
//////////////////////////////////////////////////////
varying vec3 v_ambientLight;

#ifdef ambientLightFlag
    #ifndef ambientFlag
	#define ambientFlag
    #endif
    uniform vec3 u_ambientLight;
#endif

#ifdef ambientCubemapFlag
    uniform vec3 u_ambientCubemap[6];
#endif // ambientCubemapFlag 

//////////////////////////////////////////////////////
////// POINTS LIGHTS
//////////////////////////////////////////////////////
#ifdef lightingFlag
    #if defined(numPointLights) && (numPointLights > 0)
	#define pointLightsFlag
    #endif // numPointLights
#endif //lightingFlag

#ifdef pointLightsFlag
    struct PointLight
    {
	vec3 color;
	vec3 position;
	float intensity;
    };
    uniform PointLight u_pointLights[numPointLights];
#endif

//////////////////////////////////////////////////////
////// DIRECTIONAL LIGHTS
//////////////////////////////////////////////////////
#ifdef lightingFlag
#if defined(numDirectionalLights) && (numDirectionalLights > 0)
#define directionalLightsFlag
#endif // numDirectionalLights
#endif //lightingFlag

#ifdef directionalLightsFlag
struct DirectionalLight
{
    vec3 color;
    vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif

varying vec3 v_lightDir;
varying vec3 v_lightCol;
varying vec3 v_viewDir;

#ifdef environmentCubemapFlag
varying vec3 v_reflect;
#endif

void main() {
    calculateAtmosphereGroundColor();
    v_opacity = u_opacity;
    v_alphaTest = u_alphaTest;

    calculateTangentVectors();

    g_position = applySkinning(g_position);
    g_normal = normalize(u_normalMatrix * applySkinning(g_normal));
    g_binormal = normalize(u_normalMatrix * applySkinning(g_binormal));
    g_tangent = normalize(u_normalMatrix * applySkinning(g_tangent));

    g_position = u_worldTrans * g_position;
    gl_Position = u_projViewTrans * g_position;

    #ifdef shadowMapFlag
	vec4 spos = u_shadowMapProjViewTrans * g_position;
	v_shadowMapUv.xy = (spos.xy / spos.w) * 0.5 + 0.5;
	v_shadowMapUv.z = min(spos.z * 0.5 + 0.5, 0.998);
    #endif //shadowMapFlag

    mat3 worldToTangent;
    worldToTangent[0] = g_tangent;
    worldToTangent[1] = g_binormal;
    worldToTangent[2] = g_normal;

    #ifdef ambientLightFlag
	v_ambientLight = u_ambientLight;
    #else
	v_ambientLight = vec3(0.0);
    #endif // ambientLightFlag
    
    #ifdef ambientCubemapFlag 		
	vec3 squaredNormal = g_normal * g_normal;
	vec3 isPositive = step(0.0, g_normal);
	v_ambientLight += squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +
	squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +
	squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);
    #endif // ambientCubemapFlag

    v_lightDir = -u_dirLights[0].direction * worldToTangent;
    v_lightCol = u_dirLights[0].color;
    vec3 viewDir = (u_cameraPosition.xyz - g_position.xyz);
    v_viewDir = viewDir * worldToTangent;
    #ifdef environmentCubemapFlag
	v_reflect = reflect(-viewDir, g_normal);
    #endif // environmentCubemapFlag

    pushColor(g_color);
    pushTexCoord0(g_texCoord0);
}
