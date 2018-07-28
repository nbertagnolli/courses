import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

import javax.media.opengl.GL2;
import java.nio.*;

public class VolumeRenderer
{
  class ShaderProgram
  {
    PApplet parent;
    
    int     vid;
    int     fid;
    int     pid;
    
    String  vsrc;
    String  fsrc;
    
    ShaderProgram( PApplet parent )
    {
      this.parent = parent; 
      this.pid = 0;
      this.vid = 0;
      this.fid = 0;
    }
    
    void setVertexShader( String src )
    {
      this.vsrc = src;
    }
    
    void setFragmentShader( String src )
    {
      this.fsrc = src;
    }
    
    void loadShaderSources( String vsrcfile, String fsrcfile )
    {
      if( vsrcfile != null )
        this.vsrc = parent.join( parent.loadStrings( vsrcfile ), '\n' );
        
      if( fsrcfile != null )
        this.fsrc = parent.join( parent.loadStrings( fsrcfile ), '\n' );
    }
      
    boolean compileShader( GL2 gl, int shader )
    {  
      // compile
      gl.glCompileShader( shader );
    
      // check whether compilation was successful
      int ival[] = { 0 };
      gl.glGetShaderiv( shader, gl.GL_COMPILE_STATUS, ival, 0 );
   
      boolean compiled = (ival[0] == gl.GL_TRUE);
  
      // if it failed, print error log
      if( !compiled )
      {
        gl.glGetShaderiv( shader, gl.GL_SHADER_TYPE, ival, 0 );
        int type = ival[0];
    
        String prefix = "unknown";
    
        if( type == gl.GL_FRAGMENT_SHADER )
          parent.print( "fragment" );
        else if( type == gl.GL_VERTEX_SHADER )
          parent.print( "vertex" );
      
        parent.println( " shader failed to compile --------" );
  
        // check for and print info log
        gl.glGetShaderiv( shader, gl.GL_INFO_LOG_LENGTH, ival, 0 );
        int infoLogLength = ival[0];
      
        if( ival[0] > 0 )
        {
          byte bInfoLog[] = new byte[ival[0]];
          gl.glGetShaderInfoLog( shader, ival[0], null, 0, bInfoLog, 0 ); 
  
          parent.println( new String(bInfoLog) );
        }
    
        return false;
      }
      
      return true;
    }
  
    void enable( GL2 gl )
    {
      if( pid == 0 )
      {
        pid = gl.glCreateProgram();
        
        vid = gl.glCreateShader( gl.GL_VERTEX_SHADER );
        gl.glAttachShader( pid, vid );
    
        fid = gl.glCreateShader( gl.GL_FRAGMENT_SHADER );
        gl.glAttachShader( pid, fid );
      }  
      
      boolean update = false;
      
      if( vsrc != null )
      {
        String stmp[] = { vsrc };
        gl.glShaderSource( vid, stmp.length, stmp, null, 0 );
        
        vsrc = null;
        update = true;
      }
      
      if( fsrc != null )
      {
        String stmp[] = { fsrc };
        gl.glShaderSource( fid, stmp.length, stmp, null, 0 );
        
        fsrc = null;
        update = true;
      }
        
      if( update )
      {
        if( !compileShader( gl, vid ) || !compileShader( gl, fid ) || !linkProgram( gl, pid ) )
        {
          setDefaultProgram();
  
          {
            String[] stmp = { vsrc };
            gl.glShaderSource( vid, stmp.length, stmp, null, 0 );
          }
          {
            String[] stmp = { fsrc };
            gl.glShaderSource( fid, stmp.length, stmp, null, 0 );
          }
          
          boolean ok = true;
          
          compileShader( gl, vid );
          compileShader( gl, fid );
          linkProgram( gl, pid );
                }
  
      }
      
      gl.glUseProgram( pid );
    }
  
    void disable( GL2 gl )
    {
      gl.glUseProgram( 0 );
    }
  
    void setDefaultProgram()
    {
      String stmp[] = { 
        "varying vec3 pos; void main() { pos = gl_Vertex.xyz; gl_Position = ftransform(); }",
        "varying vec3 pos; void main() { gl_FragColor = vec4( abs(cos(31.415926536*pos)), 1.0 ); }",
      };
          
      vsrc = stmp[0];
      fsrc = stmp[1];
    }    
  
    boolean linkProgram( GL2 gl, int program )
    {
      gl.glLinkProgram( program );
   
      int ival[] = { 0 };    
      gl.glGetProgramiv( program, gl.GL_LINK_STATUS, ival, 0 );
   
      boolean linked = (ival[0] == gl.GL_TRUE);
  
      if( !linked )
      {
        parent.println( "GLSL program failed to link --------" );
  
        // check for and print info log
        gl.glGetProgramiv( program, gl.GL_INFO_LOG_LENGTH, ival, 0 );
        int infoLogLength = ival[0];
      
        if( ival[0] > 0 )
        {
          byte bInfoLog[] = new byte[ival[0]];
          gl.glGetProgramInfoLog( program, ival[0], null, 0, bInfoLog, 0 ); 
  
          parent.println( new String(bInfoLog) );
        }
      }
      
      return linked;
    }
  }

  // ---
  
  PApplet          parent; 
  ViewController3D vctrl;

  String           glversion;

  byte[]           data;
  String           dataName;
  PImage           transferFunctionImage;

  FloatBuffer      vertexBuffer;
  IntBuffer        faceIndexBuffer;
  IntBuffer        edgeIndexBuffer;
  
  int              volumeTex;
  int              tfTex;

  ShaderProgram    program;

  float            volSpacing;
  
  boolean          lightEnabled;
  float            lightAmbient;
  float            lightDiffuse;
  float            lightSpecular;
  int              lightExponent;

  int              tfCenter;
  float            tfWidth;
  float            tfDensity;
  int              tfMode;
  int              tfColor1;              
  int              tfColor2;              

  float            sampleStep;
  int              compositeMode;
  
  // custom transfer function data
  int[]            customRed;
  int[]            customGreen;
  int[]            customBlue;
  int[]            customAlpha;

  VolumeRenderer( PApplet parent )
  {
    this.parent = parent;
    
    this.volumeTex = 0;
    this.tfTex     = 0;
    
    this.vctrl   = new ViewController3D( parent );
    
    this.program = new ShaderProgram( parent );
    loadShaders();
    
    lightEnabled = true;
    lightAmbient = 0.5f;
    lightDiffuse = 0.5f;
    lightSpecular = 0.6f;
    lightExponent = 10;   
   
    tfCenter  = 77;
    tfWidth   = 0.1f;
    tfDensity = 5.0f;
    tfMode    = 0;  
    
    tfColor1  = parent.color( 255, 0, 0 );
    tfColor2  = parent.color( 255, 255, 255 );
    
    sampleStep = 0.005f;
    compositeMode = 0;
    
    volSpacing = 1.0f;
    
    // initialize custom transfer function data with values
    customRed = new int[256];
    customGreen = new int[256];
    customBlue = new int[256];
    customAlpha = new int[256];
    for(int i = 0; i < 256; i++){
      customRed[i] = 255;
      customGreen[i] = 255;
      customBlue[i] = 255;
      customAlpha[i] = 255;
    }
  }
 
  void loadShaders()
  {
    program.loadShaderSources( "vr.glslv", "vr.glslf" );
  }
 
  void bindVolumeTexture( GL2 gl )
  {
    // create texture object
    if( volumeTex == 0 )
    {
      int tex[] = { 0 };
      gl.glGenTextures( 1, tex, 0 );
      
      volumeTex = tex[0];
    }
    
    if( data != null )
    {
      // determine the size of the volume data
      int vsize = 1;
  
      while( data.length > vsize*vsize*vsize )
        vsize *= 2;
    
      if( vsize*vsize*vsize != data.length )
        throw new RuntimeException( "cannot determine volume size" );
      
      volSpacing = 1.0f/vsize;
      
      // create a buffer to hold the data
      ByteBuffer buffer = ByteBuffer.allocateDirect( data.length );
      buffer.put( data );
      buffer.rewind();
  
      gl.glActiveTexture( gl.GL_TEXTURE0 );
      gl.glBindTexture( gl.GL_TEXTURE_3D, volumeTex );
      gl.glTexImage3D( gl.GL_TEXTURE_3D, 0, gl.GL_LUMINANCE, vsize, vsize, vsize, 0, gl.GL_LUMINANCE, gl.GL_UNSIGNED_BYTE, buffer );
    
      gl.glTexParameteri( gl.GL_TEXTURE_3D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR );
      gl.glTexParameteri( gl.GL_TEXTURE_3D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR );
      
      // print out data info
      print();
    }
  }
    
  void renderCube( GL2 gl, int mode )
  {
    if( vertexBuffer == null )
    {
      float v[] = {
        0, 0, 0,  0, 0, 1,  0, 1, 1,  0, 1, 0,
        1, 0, 0,  1, 0, 1,  1, 1, 1,  1, 1, 0,
      };
      
      vertexBuffer = ByteBuffer.allocateDirect( 4 * v.length ).order( ByteOrder.nativeOrder() ).asFloatBuffer();
      vertexBuffer.put( v );
      vertexBuffer.rewind();
    }
    
    if( faceIndexBuffer == null )
    {
      int faces[] = {
        0, 1, 2, 3,  3, 2, 6, 7,  7, 6, 5, 4,  
        4, 5, 1, 0,  5, 6, 2, 1,  7, 4, 0, 3,
      };
          
      faceIndexBuffer = ByteBuffer.allocateDirect( 4 * faces.length ).order( ByteOrder.nativeOrder() ).asIntBuffer();
      faceIndexBuffer.put( faces );
      faceIndexBuffer.rewind();
    }
    
    if( edgeIndexBuffer == null )
    {
      int edges[] = {
        0, 1,  1, 2,  2, 3,  3, 0, 
        4, 5,  5, 6,  6, 7,  7, 4,
        0, 4,  1, 5,  2, 6,  3, 7,
      };
     
      edgeIndexBuffer = ByteBuffer.allocateDirect( 4 * edges.length ).order( ByteOrder.nativeOrder() ).asIntBuffer();
      edgeIndexBuffer.put( edges );
      edgeIndexBuffer.rewind();
    } 
    
    gl.glEnableClientState( gl.GL_VERTEX_ARRAY );
    gl.glVertexPointer( 3, gl.GL_FLOAT, 0, vertexBuffer );

    if( mode == gl.GL_LINE )
    {
      gl.glPolygonMode( gl.GL_FRONT_AND_BACK, gl.GL_LINE );
      
      // gl.glDrawElements( gl.GL_LINES, 24, gl.GL_UNSIGNED_INT, edgeIndexBuffer );      
      gl.glDrawElements( gl.GL_QUADS, 24, gl.GL_UNSIGNED_INT, faceIndexBuffer );

      gl.glPolygonMode( gl.GL_FRONT_AND_BACK, gl.GL_FILL );
    }
    else
    {
      gl.glEnableClientState( gl.GL_COLOR_ARRAY );
      gl.glColorPointer( 3, gl.GL_FLOAT, 0, vertexBuffer );
  
      gl.glDrawElements( gl.GL_QUADS, 24, gl.GL_UNSIGNED_INT, faceIndexBuffer );

      gl.glDisableClientState( gl.GL_COLOR_ARRAY );
    }  
    
    gl.glDisableClientState( gl.GL_VERTEX_ARRAY );
  }    
  
//  void setUniform( GL2 gl, String name, boolean value )
//  {
//    int loc = gl.glGetUniformLocation( program.pid, name );
//
//    if( loc >= 0 )
//      gl.glUniform1i( loc, value ? 1 : 0 );
//  }    

  void setUniform( GL2 gl, String name, float value )
  {
    int loc = gl.glGetUniformLocation( program.pid, name );
    
    if( loc >= 0 )
      gl.glUniform1f( loc, value );
  }
  
  void setUniform( GL2 gl, String name, int value )
  {
    int loc = gl.glGetUniformLocation( program.pid, name );
    
    if( loc >= 0 )
      gl.glUniform1i( loc, value );
  }

  void loadUniforms( GL2 gl )
  {
    if( sampleStep < 0.0001f ) 
      sampleStep = 0.0001f;
     
    setUniform( gl, "vol.spacing", volSpacing );
    
    setUniform( gl, "params.step", sampleStep );
    setUniform( gl, "params.composite", compositeMode );
    
    setUniform( gl, "light.enabled",  lightEnabled ? 1 : 0 );
    setUniform( gl, "light.ambient",  lightAmbient );
    setUniform( gl, "light.diffuse",  lightDiffuse );
    setUniform( gl, "light.specular", lightSpecular );
    setUniform( gl, "light.exponent", lightExponent );
    
    setUniform( gl, "tf.center",  ((float) tfCenter) / 255.0f );
    setUniform( gl, "tf.width",   tfWidth );
    setUniform( gl, "tf.density", tfDensity );
    setUniform( gl, "tf.mode",    tfMode );
    
    // pass in our custom transfer function values to shader
    float[] customTF = new float[256 * 4];
    for(int i = 0; i < 256; i++){
      customTF[i * 4] = (float) customRed[i] / 255.0f;
      customTF[i * 4 + 1] = (float) customGreen[i] / 255.0f;
      customTF[i * 4 + 2] = (float) customBlue[i] / 255.0f;
      customTF[i * 4 + 3] = (float) customAlpha[i] / 255.0f;
    }
    int loc = gl.glGetUniformLocation(program.pid, "customTF");
    if(loc >= 0)
      gl.glUniform4fv(loc, 256 * 4, customTF, 0);
    
    loc = gl.glGetUniformLocation( program.pid, "tf.color1" );
    
    if( loc >= 0 )
      gl.glUniform3f( loc, parent.red(tfColor1)/255.0f, 
                           parent.green(tfColor1)/255.0f, 
                           parent.blue(tfColor1)/255.0f );

    loc = gl.glGetUniformLocation( program.pid, "tf.color2" );
    
    if( loc >= 0 )
      gl.glUniform3f( loc, parent.red(tfColor2)/255.0f, 
                           parent.green(tfColor2)/255.0f, 
                           parent.blue(tfColor2)/255.0f );
  }
  
  void draw()
  {
    GL2 gl = ( (PJOGL) ((PGraphicsOpenGL)parent.g).beginPGL()).gl.getGL2();
  
    if( glversion == null )
    {
      glversion =  "OpenGL version: " + gl.glGetString( gl.GL_VERSION ) + '\n';
      glversion += "GLSL version:   " + gl.glGetString( gl.GL_SHADING_LANGUAGE_VERSION ) + '\n';
      
      parent.println( glversion );
    }
  
    // save relevant OpenGL state
    gl.glPushAttrib( gl.GL_COLOR_BUFFER_BIT | 
                     gl.GL_ENABLE_BIT |
                     gl.GL_POLYGON_BIT );
    
    // set projection and view matrices
    gl.glMatrixMode( gl.GL_PROJECTION );
    gl.glPushMatrix();
    gl.glLoadMatrixd( vctrl.prMatrix, 0 );
    
    gl.glMatrixMode( gl.GL_MODELVIEW );
    gl.glPushMatrix();
    gl.glLoadMatrixd( vctrl.mvMatrix, 0 );

    // draw only back faces
    gl.glCullFace( gl.GL_BACK );
    gl.glEnable( gl.GL_CULL_FACE );

    // draw the bounding box
    gl.glDisable( gl.GL_LINE_SMOOTH );
    
    gl.glColor4f( 0.85f, 0.85f, 0.85f, 1.0f );
    renderCube( gl, gl.GL_LINE );

    // bind (and possibly update) the volume texture
    bindVolumeTexture( gl );
    
    // enable volume rendering shader program
    program.enable( gl );

    loadUniforms( gl );
    
    // enable alpha blending
    gl.glEnable( gl.GL_BLEND );
    gl.glBlendFunc( gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA );

    // draw only back faces
    gl.glCullFace( gl.GL_BACK );
    gl.glEnable( gl.GL_CULL_FACE );

    renderCube( gl, gl.GL_FILL );

    // disable program
    program.disable( gl );

    // reset view to previous settings
    gl.glMatrixMode( gl.GL_PROJECTION );
    gl.glPopMatrix();
    
    gl.glMatrixMode( gl.GL_MODELVIEW );
    gl.glPopMatrix();
    
    // restore OpenGL state
    gl.glPopAttrib();
    
    ((PGraphicsOpenGL)parent.g).endPGL();
    
    // clear data
    clearData();
  }
  
  // print out data info
  void print(){
    System.out.println("data: " + dataName);
    int s = this.data.length;
    int g = (int) Math.pow(s, (float) (1.0 / 3.0));
    System.out.println("size: " + s);
    System.out.println("grid: " + g + " by " + g + " by " + g);
    System.out.println();
  }
  
  // clear data, it has been uploaded
  void clearData(){
    data = null;
  }
}

