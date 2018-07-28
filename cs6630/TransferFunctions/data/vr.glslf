#version 120

varying vec3 cameraDirection;

uniform sampler3D volumeSampler;

struct VolumeParams
{
    float spacing;
};

uniform VolumeParams vol;

struct Params
{
    float step;
    int   composite;
};

uniform Params params;

struct TransferFunctionParams 
{
    float center;
    float width;
    float density;
    int   mode;
    vec3  color1;
    vec3  color2;
};

uniform TransferFunctionParams tf;

struct LightParams
{
    bool  enabled;
    float ambient;
    float diffuse;
    float specular;
    int   exponent;
};

uniform LightParams light;

struct Ray 
{
    vec3  position;
    vec3  direction;
    float length;
};

// load in data for transfer function
uniform vec4 customTF[256];


// -------------------------------------------------------------------------

// calculate scalar value at position
float sample( vec3 position )
{
    return texture3D( volumeSampler, position.xyz ).r;
}

// -------------------------------------------------------------------------

// calculate  gradient at position,
// using finite differences
vec3 gradient( vec3 position )
{
    vec3 gradient;

    gradient.x = texture3D( volumeSampler, position.xyz + vec3(vol.spacing,0,0) ).r -
                 texture3D( volumeSampler, position.xyz - vec3(vol.spacing,0,0) ).r;
    gradient.y = texture3D( volumeSampler, position.xyz + vec3(0,vol.spacing,0) ).r - 
                 texture3D( volumeSampler, position.xyz - vec3(0,vol.spacing,0) ).r;
    gradient.z = texture3D( volumeSampler, position.xyz + vec3(0,0,vol.spacing) ).r -
                 texture3D( volumeSampler, position.xyz - vec3(0,0,vol.spacing) ).r;

    return gradient * vol.spacing;
}

// -------------------------------------------------------------------------

// compute Blinn-Phong lighting
vec3 illuminate( vec3 color, vec3 normal, vec3 view )
{
    // do lighting if needed
    if( light.enabled )
    {
        vec3 light_direction = normalize( vec3( 1, 1, -1 ) );

        // transform normal to world coords
        normal = normalize( gl_NormalMatrix * normal );

        // diffuse
        float ndotl = max( dot( normal, light_direction ), 0.0 );

        if( ndotl < 0.0 )
        {
            normal = -normal;
            ndotl  = -ndotl;
        }

        color.rgb *= light.ambient + light.diffuse * abs( ndotl );

        // specular
        if( ndotl > 0.0 )
        {
            view = normalize( gl_NormalMatrix * view );

            // volume normal in eye space
            vec3 refl = reflect( -view, normal );

            float rdotl = dot( refl, light_direction );
    
            if( rdotl > 0.0 )
            {
                float spec = pow( max( rdotl, 0.0 ), float(light.exponent) ) * (float(light.exponent) + 2.0) / 6.28318;
                color.rgb += light.specular * spec;
            }
        }
    }
    
    // flip color for white background
    return 1.0 - color;
}


// -------------------------------------------------------------------------

// compute the intersection of the viewing
// ray through the current pixel and the volume
// (unit cube)
Ray calculateRay()
{    
    Ray r;   
    
    r.position    = gl_ModelViewMatrixInverse[3].xyz;
    r.direction = normalize( cameraDirection );

    // determine the ray length in volume coordinates
    // by cleverly clipping it against the volume bounds
    vec3 tbot = (vec3( 0, 0, 0 ) - r.position) / r.direction;
    vec3 ttop = (vec3( 1, 1, 1 ) - r.position) / r.direction;

    vec3 tmin = min( ttop, tbot );
    vec3 tmax = max( ttop, tbot );

    float largest_tmin  = max( max(tmin.x, tmin.y), max( tmin.z, 0.0 ) );
    float smallest_tmax = min( min(tmax.x, tmax.y), tmax.z );
    
    r.length = max( smallest_tmax - largest_tmin, 0.0 );
    r.position += largest_tmin * r.direction;
    
    return r;
}

// -------------------------------------------------------------------------

vec4 classify( float value )
{
    vec4 color;

    if( tf.mode == 0 ) // STEP
    {
        // opacity is a step function
        color.a   = tf.density * step( tf.center, value );
        
        // color is always tf.color1
        color.rgb = tf.color1;
    }
    else if( tf.mode == 1 ) // RECT
    {
        float min = tf.center - tf.width;
        float max = tf.center + tf.width;

        value = 0.5 * (value - min) / tf.width;
        
        // opacity is non-zero only on [min,max]
        color.a   = (step( 0.0, value ) - step( 1.0, value )) / tf.width;
        
        // color blends linearly between tf.color1 and tf.color2
        color.rgb = mix( tf.color1, tf.color2, value );
    }          
    else if( tf.mode == 2 ) // HAT
    {
        if( value < tf.center - tf.width )
            color.a = 0.0;
        else if( value < tf.center )
            color.a = (value - tf.center - tf.width) / tf.width;
        else if( value < tf.center + tf.width )
            color.a = 1.0 - (value - tf.center) / tf.width;
        else
            color.a = 0.0;
                 
        color.rgb = mix( tf.color1, tf.color2, value );
    }
    else if( tf.mode == 3 ) // BUMP 
    {
        color.a = (smoothstep( tf.center-tf.width, tf.center, value ) -
                   smoothstep( tf.center, tf.center+tf.width, value )) / tf.width;
        
        color.rgb = mix( tf.color1, tf.color2, value );
    }
    else if( tf.mode == 4 ) // CUSTOM1
    {
        
        // custom transfer function using color array, from Processing
        color.r = customTF[int(value * 255.0)].r;
        color.g = customTF[int(value * 255.0)].g;
        color.b = customTF[int(value * 255.0)].b;
        color.a = customTF[int(value * 255.0)].a;
    }
    else if( tf.mode == 5 ) // CUSTOM2
    {
    }
    else if( tf.mode == 6 ) // CUSTOM3
    {
    }
    else if( tf.mode == 7 ) // CUSTOM4
    {
    }
    
    // allow manual scaling of opacity
    color.a *= tf.density;
    
    return color;
}

// -------------------------------------------------------------------------

vec4 composite( vec4 final, vec4 sample )
{
    // subtract RGB for white background
    final.rgb -= sample.rgb * (1.0-final.a) * params.step * sample.a;
    final.a   += sample.a   * (1.0-final.a) * params.step;
    
    return final;
}

// -------------------------------------------------------------------------

void main()
{
    // calculate viewing ray and determine
    // start position at volume boundary and
    // length of volume intersection
    Ray r = calculateRay();

    // initialize set accumulated color
    // set it to white for a white background
    vec4 finalColor = vec4( 1.0, 1.0, 1.0, 0.0 );
    
    float maxValue = 0.0;
    
    // begin tracing the ray through the volume;
    // stop once we have left the volume
    while( r.length >= 0.0 )
    {        
        // --- Levoy step 1: sampling
        float sampleValue = sample( r.position );
        
        // --- Levoy step 2a: classification
        vec4 sampleColor = classify( sampleValue );

        // ----Levoy step 2b: lighting
        
        // compute volume gradient as local surface normal
        vec3 sampleNormal = gradient( r.position );
        
        // --- Levoy step 3: composite
        if( params.composite == 1 && sampleValue <= maxValue )
        {
            finalColor.rgb = mix( tf.color1, tf.color2, sampleValue );
            finalColor.a   = 1.0;
            maxValue   = sampleValue;
        }
        else
        {
            // apply the local light model; only changes color, opacity 
            // (sampleColor.a) remains unchanged
            sampleColor.rgb = illuminate( sampleColor.rgb, sampleNormal, r.direction );
            finalColor = composite( finalColor, sampleColor );
        }
        
        // move to the next sample position
        r.position += params.step * r.direction;
        r.length   -= params.step;
    }

    
    gl_FragColor = finalColor;
}
