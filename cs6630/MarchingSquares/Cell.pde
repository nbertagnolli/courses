
/**
*This code is adapted directly from processing.org/tutorials/2darray.  I've made minor modifications
*to suit my needs but the rasterization is almost verbatim.
*/
// A Cell object
class Cell {
  // A cell object knows about its location in the grid as well as its size with the variables x,y,w,h.
  float x,y;   // x,y location
  float w,h;   // width and height
  float value; // value of data at this point

  // Cell Constructor
  Cell(float value_, float tempX, float tempY, float tempW, float tempH) {
    value = value_;
    x = tempX;
    y = tempY;
    w = tempW;
    h = tempH;
  } 

  void display() {
    //stroke(value);
    //fill(value);
    //rect(x,y,w,h); 
    float R = map(value,0,255,117,239);
    float G = map(value,0,255,107,237);
    float B = map(value,0,255,177,245);
    float T = map(value,0,255,24.5,98.8);
    //color c = new color(R,G,B);
    //brightness(c);
    stroke(R,G,B);
    fill(R,G,B);
    rect(x,y,w,h); 
  }
}
