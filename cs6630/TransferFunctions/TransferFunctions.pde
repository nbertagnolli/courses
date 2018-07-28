// control panel & volume renderer windows
Controls         controls;
VolumeRenderer   volren;

// load our data and create both our windows
void setup(){
  size(800, 600, P2D);
  noLoop();
  
  // create our volume renderer, load data
  volren = new VolumeRenderer(this);
  volren.dataName = "foot";
  volren.data = loadBytes(volren.dataName + ".raw");
  
  // create control panel
  controls = new Controls(this, volren);
}

// update our volume renderer
void draw(){
  background(255);
  volren.draw();
}

// update our shaders when you press 'r' (not needed)
void keyPressed(){
  if(key == 'r'){
    volren.loadShaders();
    redraw();
  }
  
}
