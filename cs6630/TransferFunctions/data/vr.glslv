varying vec3 cameraDirection;

void main()
{
    gl_Position = ftransform();
    cameraDirection = gl_Vertex.xyz - gl_ModelViewMatrixInverse[3].xyz;
}
