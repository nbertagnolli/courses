// necessary imports to create control panel as separate window
import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;
import java.awt.Frame;
import controlP5.*;
import java.util.*;
import java.util.Comparator;


// control panel class
public class Controls extends PApplet{
  
  
  
  //
  //
  // CS6630 - START - you can start editing here
  //
  //
  
  
  
  // set the height & width of control panel
  int cWidth = 800;
  int cHeight = 500;
  
  //Plot Positions
  float plotX1, plotY1;
  float plotX2, plotY2;
  
  // data variables:
  
    // size of our uniform 3D grid
    int gridSize;
    
    // data[x][y][z]
    // stores 256 possible values
    int[][][] data;
    
    // min & max values for data (0 & 255, respectively)
    int dataMin, dataMax;
    
    //interaction variables
    int currentColor;  //The color to fill the box associated with the current editing graphic
    Function positions = new Function();//Initialize our arrays to use 
    FloatArrayComparator comp = new FloatArrayComparator();
    FloatArrayComparatorSize comp2 = new FloatArrayComparatorSize();
  
  
  // custom transfer function arrays - one for each RGBA channel
    
    // length 256; our array values are also mapped from 0 to 255
    // you must call updateTransferFunction() to update volume renderer!
    int[] red;
    int[] green;
    int[] blue;
    int[] alpha;
  
  
  
  // Processing setup method (runs once)
  public void setup(){
    currentColor = 1;
    // load the volume rendering data
    loadData();
    
    // create the control panel interface
    createControlPanel();
    
    // initalize our custom transfer function (all values as transparent white)
    initializeTransferFunction();
    
  //Corners of graph ploted
  plotX1 = 255;
  plotX2 = cWidth - plotX1/4;
  plotY1 = 60;
  plotY2 = cHeight - plotY1;
  
  //Initialize the data array with two points 0,0 and 0,plotWidth
  initialize();
  }
  
  
  
  // Processing draw method (runs repeatedly)
  public void draw(){
    background(255);
    
    // show the plot area as a grey box
    fill(255);
    rectMode(CORNERS);
    stroke(0);
    rect(plotX1,plotY1,plotX2,plotY2);
    
    
    //Draw the rectangle notifying the user of the current Transfer Function being edited
    drawCurrentColor(currentColor);

    mapColors(positions.red,1);
    mapColors(positions.green,2);
    mapColors(positions.blue,3);
    mapColors(positions.alpha,4);
    
    //draw the users lines
    drawLines(positions.red,1);
    drawLines(positions.green,2);
    drawLines(positions.blue,3);
    drawLines(positions.alpha,4);
    
    // update our custom transfer function
    // you may wish to only call this when you change the data
    updateTransferFunction();
  }
  
  public void initialize(){
    positions.red.add(new float[]{mapX(plotX1),mapY(plotY2),plotX1,plotY2,0});
    positions.red.add(new float[]{mapX(plotX2),mapY(plotY2),plotX2,plotY2,positions.red.size()});
    positions.green.add(new float[]{mapX(plotX1),mapY(plotY2),plotX1,plotY2,0});
    positions.green.add(new float[]{mapX(plotX2),mapY(plotY2),plotX2,plotY2,positions.red.size()});
    positions.blue.add(new float[]{mapX(plotX1),mapY(plotY2),plotX1,plotY2,0});
    positions.blue.add(new float[]{mapX(plotX2),mapY(plotY2),plotX2,plotY2,positions.red.size()});
    positions.alpha.add(new float[]{mapX(plotX1),mapY(plotY2),plotX1,plotY2,0});
    positions.alpha.add(new float[]{mapX(plotX2),mapY(plotY1),plotX2,plotY1,positions.red.size()});
  }
  
  
  // load in our data to variables
  public void loadData(){
    int s = volren.data.length;
    gridSize = (int) pow((float) s, (float) (1.0 / 3.0));
    data = new int[gridSize][gridSize][gridSize];
    int dx = 0;
    int dy = 0;
    int dz = 0;
    dataMin = 255;
    dataMax = 0;
    for(int i = 0; i < s; i++){
      
      // store data
      int d = volren.data[i] & 0xFF;
      data[dx][dy][dz] = d;
      
      
      // check min, max values
      if(d < dataMin)
        dataMin = d;
      if(d > dataMax)
        dataMax = d;
      
      // prep next data value
      dx++;
      if(dx == gridSize){
        dy++;
        dx = 0;
        if(dy == gridSize){
          dz++;
          dy = 0;
        }
      }
    }
  }
  
  
  
  //Helper Methods
  //Creates a box indicating the color selected.
  void drawCurrentColor(int currentColor){
    int rectSize = 25;
    switch(currentColor){
      case 1:
          fill(255,0,0);
          rect(plotX1,plotY1,plotX1+rectSize,plotY1-rectSize);
          break;
      case 2:
          fill(0,255,0);
          rect(plotX1,plotY1,plotX1+rectSize,plotY1-rectSize);
          break;
      case 3:
          fill(0,0,255);
          rect(plotX1,plotY1,plotX1+rectSize,plotY1-rectSize);
          break;
      case 4:
          fill(125);
          rect(plotX1,plotY1,plotX1+rectSize,plotY1-rectSize);
          break; 
    }
  }
  
  //Draw the lines from the positions function
  void drawLines(ArrayList<float[]> arr,int colour){
    strokeWeight(4);
    switch(colour){
      case 1:
          fill(255,0,0,100);
          stroke(255,0,0,100);
          break;
      case 2:
          fill(0,255,0,100);
          stroke(0,255,0,100);
          break;
      case 3:
          fill(0,0,255,100);
          stroke(0,0,255,100);
          break;
      case 4:
          fill(125,100);
          stroke(125,100);
          break;
    }
    
    for(int i = 0; i < arr.size()-1; i++){
      line(arr.get(i)[2],arr.get(i)[3],arr.get(i+1)[2],arr.get(i+1)[3]);
    }
    strokeWeight(1);
  }
   //<>//
  
  //maps the drawn function to the actual color map
  void mapColors(ArrayList<float[]> arr,int col){
    int prevIndex;
    int nextIndex;
    int x;
    int y;
    int m;
    int[] temp = new int[256];
    
    for(int i = 0; i < arr.size()-1; i++){
       prevIndex = round(arr.get(i)[0]);
       nextIndex = round(arr.get(i+1)[0]);
//       println(prevIndex);
//       println(nextIndex);
//       println(arr.get(i)[0]);
//       println(arr.get(i)[1]);
       x = round(arr.get(i+1)[0]);
       y = round(arr.get(i+1)[1]);
       if((x-round(arr.get(i)[0])) == 0){
        continue; 
       }
       m = (y - round(arr.get(i)[1]))/(x-round(arr.get(i)[0]));
       for(int j = prevIndex; j < nextIndex; j++){
         temp[j] = m*(j-x)+y;
       }
    }
    //checks which color we're on
    switch(col){
      case 1:
          for(int i = 0; i < 256; i++){
            red[i] = temp[i]; 
          }
          break;
      case 2:
          for(int i = 0; i < 256; i++){
            green[i] = temp[i]; 
          }
          break;
      case 3:
         for(int i = 0; i < 256; i++){
            blue[i] = temp[i]; 
          }
          break;
      case 4:
          for(int i = 0; i < 256; i++){
            alpha[i] = temp[i]; 
          }
          break;
    }
  }
  
  
  //map x val from figure to graph
  float mapX(float val){
    return map(val,plotX1,plotX2,0,255);
  }
  
  //map y val from figure to graph
  float mapY(float val){
    return map(val,plotY2,plotY1,0,255);
  }
  
  //Reversemaps
    float revMapX(float val){
    return map(val,0,255,plotX1,plotX2);
  }
  
  //map y val from figure to graph
  float revMapY(float val){
    return map(val,0,255,plotY2,plotY1);
  }
  
  
  public void keyPressed(){
    switch(key){
      case 'r':
         currentColor = 1;
         break;
      case 'g':
          currentColor = 2;
          break;
      case 'b':
         currentColor = 3;
         break;
      case 'a':
         currentColor = 4;
         break;
      case 'u'://undoes the last data point of the specified color
          switch(currentColor){
            case 1:
                if(positions.red.size() > 2){
                  Collections.sort(positions.red,comp2);
                  positions.red.remove(positions.red.size()-1);
                  Collections.sort(positions.red,comp);
                }
                break;
            case 2:
                if(positions.green.size() > 2){
                  Collections.sort(positions.green,comp2);
                  positions.green.remove(positions.green.size()-1);
                  Collections.sort(positions.green,comp);
                }
                break;
            case 3:
                if(positions.blue.size() > 2){
                  Collections.sort(positions.blue,comp2);
                  positions.blue.remove(positions.blue.size()-1);
                  Collections.sort(positions.blue,comp);
                }
                break;
            case 4:
                if(positions.alpha.size() > 2){
                  Collections.sort(positions.alpha,comp2);
                  positions.alpha.remove(positions.alpha.size()-1);
                  Collections.sort(positions.alpha,comp);
                }
                break;
               
          }
          break;
      case 'c':
          positions.red.clear();
          positions.blue.clear();
          positions.green.clear();
          positions.alpha.clear();
          initialize();
          break;
    }
  }

  public void mousePressed(){
    if(mouseX >= plotX1 && mouseX < plotX2 && mouseY > plotY1 && mouseY < plotY2){ //<>//
      
      switch(currentColor){
        case 1:
           positions.red.add(new float[]{mapX(mouseX),mapY(mouseY),mouseX,mouseY,positions.red.size()});
           Collections.sort(positions.red,comp);
           break; 
        case 2:
            positions.green.add(new float[]{mapX(mouseX),mapY(mouseY),mouseX,mouseY,positions.green.size()});
            Collections.sort(positions.green,comp);
            break;
        case 3:
           positions.blue.add(new float[]{mapX(mouseX),mapY(mouseY),mouseX,mouseY,positions.blue.size()});
           Collections.sort(positions.blue,comp);
           break;
        case 4:
           positions.alpha.add(new float[]{mapX(mouseX),mapY(mouseY),mouseX,mouseY,positions.alpha.size()});
           Collections.sort(positions.alpha,comp);
           break;
      }
    }
    
  }
  
  public class Function{
 ArrayList<float[]> red; // [mapX,mapY,absX,absY,rank]
 ArrayList<float[]> green;
 ArrayList<float[]> blue;
 ArrayList<float[]> alpha;
 
 Function(){
   red = new ArrayList<float[]>();
   green = new ArrayList<float[]>();
   blue  = new ArrayList<float[]>();
   alpha = new ArrayList<float[]>();
 } 
 

}

//Comparator for float[] of colors
public class FloatArrayComparator implements Comparator<float[]> {
  @Override
  public int compare(float[] arg0, float[] arg1) {
    if(arg0[0]-arg1[0] > 0){
      return 1;
    }else if(arg0[0]-arg1[0]<0){
      return -1;
    }
    
    return 0;
  }

}

//Comparator for float[] based on index in array
public class FloatArrayComparatorSize implements Comparator<float[]> {
  @Override
  public int compare(float[] arg0, float[] arg1) {
    if(arg0[4]-arg1[4] > 0){
      return 1;
    }else if(arg0[4]-arg1[4]<0){
      return -1;
    }
    
    return 0;
  }

}
  
  
/////////////////////////////////////////////////////////////////////////////////////  
  //
  //
  // CS6630 - END - no need to edit below
  //
  //
  
  
  
  // variables for the control panel
  ColorPicker tfCPick1;
  ColorPicker tfCPick2;
  RadioButton tfMode;
  ControlP5   cp5;
  PApplet     parent;
  VolumeRenderer volren;
  
  // set initial transfer function data (all values to transparent white)
  public void initializeTransferFunction(){
    red = new int[256];
    green = new int[256];
    blue = new int[256];
    alpha = new int[256];
    for(int i = 0; i < 256; i++){
      red[i] = 255;
      green[i] = 255;
      blue[i] = 255;
      alpha[i] = 0;
    }
    
    // pass in our initial custom transfer function
    updateTransferFunction();
  }
  
  // update custom transfer function data & pass to volume renderer
  public void updateTransferFunction(){
    volren.customRed = red;
    volren.customGreen = green;
    volren.customBlue = blue;
    volren.customAlpha = alpha;
    parent.redraw();
  }
  
  // create control panel interface
  public void createControlPanel(){
    
    // setup P5 library
    cp5 = new ControlP5(this);
    frameRate(25);
    
    // variables for creating control panel
    int y = -10, height = 15, spacing = 20;
    
    // dataset selector
    float status3[] = { 0, 0, 0, 1, 0, 0 };
    cp5.addTextlabel("label5", "Data Set")
      .setPosition( 10, y+=spacing )
      .setHeight(48)
      .setColor( parent.color(17, 17, 17) );
    cp5.addRadio( "dataset", 10, y+=spacing )
      .setSize( 20, height )
      .setColorForeground(color(180, 180, 180))
      .setColorBackground(color(180, 180, 180))
      .setItemsPerRow(3)
      .setSpacingColumn( 50 )
      .addItem( "aneurism", 0 )
      .addItem( "bonsai", 1 )
      .addItem( "bucky", 2 )
      .addItem( "foot", 3 )
      .addItem( "fuel", 4 )
      .addItem( "skull", 5 )
      .setColorLabel( parent.color(75, 75, 75) )
      .setArrayValue( status3 )
      .plugTo( this, "setData" );
    
    // lighting settings
    y += spacing + height;
    cp5.addTextlabel( "label1", "Light Settings" )
      .setPosition( 10, y+=spacing )
      .setColor( parent.color(17, 17, 17) );
    cp5.addToggle("enabled")
      .setPosition( 190, y )
      .setSize( 20, height-5 )
      .setColorForeground(color(180, 180, 180))
      .setColorBackground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setValue( volren.lightEnabled )
      .plugTo( volren, "lightEnabled" )
      .getCaptionLabel()
        .setPaddingX( 5 )
        .align( ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER );
    cp5.addSlider("ambient")
      .setPosition( 10, y+=spacing )
      .setSize( 200, height )
      .setColorBackground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setRange( 0, 1 ) 
      .setValue( volren.lightAmbient )
      .plugTo( volren, "lightAmbient" );
    cp5.addSlider("diffuse")
      .setPosition( 10, y+=spacing )
      .setSize( 200, height )
      .setColorBackground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setRange( 0, 1 )
      .setValue( volren.lightDiffuse )
      .plugTo( volren, "lightDiffuse" );
    cp5.addSlider("specular")
      .setPosition( 10, y+=spacing )
      .setSize( 200, height )
      .setColorBackground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setRange( 0, 1 )
      .setValue( volren.lightSpecular )
      .plugTo( volren, "lightSpecular" );
    cp5.addSlider("exponent")
      .setPosition( 10, y+=spacing )
      .setSize( 200, height )
      .setColorBackground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setRange( 1, 50 )
      .setValue( volren.lightExponent )
      .plugTo( volren, "lightExponent" );
    
    // sampling settings (removed)
    // y += 10;
    // cp5.addTextlabel( "label3", "Sampling Settings" )
    //   .setPosition( 10, y+=spacing )
    //   .setColor( parent.color(255,200,0) );
    // cp5.addSlider( "Step", 0.001f, 0.01f, 10, y+=spacing, 200, height )
    //   .setDecimalPrecision( 5 )
    //   .setValue( 0.005f )
    //   .plugTo( volren, "sampleStep" );
    
    // composite settings (removed)
    // y += 10;
    // float[] status1 = { 1, 0 };
    // cp5.addTextlabel( "label4", "Compositing" )
    //   .setPosition( 10, y+=spacing )
    //   .setColor( parent.color(255,200,0) );
    // cp5.addRadio( "compositeMode", 10, y+=spacing )
    //   .setSize( 30, height )
    //   .setItemsPerRow(3)
    //   .setSpacingColumn( 40 )
    //   .addItem( "LEVOY", 0 )
    //   .addItem( "MIP", 1 )
    //   .setArrayValue( status1 )
    //   .plugTo( this, "setCompositeMode" );
    
    // transfer function settings
    y += 10;
    cp5.addTextlabel( "label2", "Transfer Function Settings" )
      .setPosition( 10, y+=spacing )
      .setColor( parent.color(17, 17, 17) );
    cp5.addSlider( "Center", 0, 255, 10, y+=spacing, 200, height )
      .setColorBackground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setValue( 77 )
      .plugTo( volren, "tfCenter" );
    // cp5.addSlider( "Width", 0, 1, 10, y+=spacing, 200, height )
    //   .setValue( 0.1f )
    //   .plugTo( volren, "tfWidth" );
    cp5.addSlider( "Density", 0, 40, 10, y+=spacing, 200, height )
      .setColorBackground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setValue( 5.0f )
      .plugTo( volren, "tfDensity" );
    
    // transfer function mode
    y += 10;
    float[] status2 = {1, 0};
    cp5.addRadio( "tfMode", 10, y+=spacing )
      .setSize( 20, height )
      .setItemsPerRow(4)
      .setSpacingColumn( 40 )
      .addItem( "STEP", 0 )
      // .addItem( "RECT", 1 )
      // .addItem( "HAT", 2 )
      // .addItem( "BUMP", 3 )
      .addItem( "CUSTOM TRANSFER FUNCTION", 4 )
      // .addItem( "CUSTOM2", 5 )
      // .addItem( "CUSTOM3", 6 )
      // .addItem( "CUSTOM4", 7 )
      .setColorBackground(color(180, 180, 180))
      .setColorForeground(color(180, 180, 180))
      .setColorLabel( parent.color(75, 75, 75) )
      .setArrayValue( status2 )
      .plugTo( this, "setTFMode" );
    
    // transfer function color picker
    y += 15 + spacing;
    cp5.addColorPicker( "tfColor1", 10, y+=height, 200, 10 )
      .setColorLabel( parent.color(75, 75, 75) )
      .setColorValue( volren.tfColor1 )
      .plugTo( this, "setTFColor1" );
    // cp5.addColorPicker( "tfColor2", 10, y+=70, 200, 10 )
    //   .setColorValue( volren.tfColor2 )
    //   .plugTo( this, "setTFColor2" );
    
    // update control panel
    cp5.addCallback(new RedrawListener(parent));
  }
  
  // set transfer function mode
  public void setTFMode(int c){
    volren.tfMode = c;
    parent.redraw();
  }
  
  // set transfer function color
  public void setTFColor1(int c){
    volren.tfColor1 = c;
    parent.redraw();
  }
  
  // select our dataset
  public void setData(int c){
    
    // set data to load
    if(c == 0)
      volren.dataName = "aneurism";
    else if(c == 1)
      volren.dataName = "bonsai";
    else if(c == 2)
      volren.dataName = "bucky";
    else if(c == 3)
      volren.dataName = "foot";
    else if(c == 4)
      volren.dataName = "fuel";
    else if(c == 5)
      volren.dataName = "skull";
    
    // load data
    volren.data = parent.loadBytes(volren.dataName + ".raw");
    loadData();
    
    // update volume renderer
    parent.redraw();
  }
  
  // grab our controlp5 object
  public ControlP5 control(){
    return cp5;
  }
  
  // creates our window
  Controls(PApplet parent, VolumeRenderer volren){
    this.parent = parent;
    this.volren = volren;
    
    Frame f = new Frame("Controls");
    f.add(this);
    
    init();
    
    f.setTitle("Control Panel");
    f.setSize(cWidth, cHeight);
    f.setLocation(100, 100);
    f.setResizable(false);
    f.setVisible(true);
  }
  
  // only update screen when necessary
  class RedrawListener implements CallbackListener{
    PApplet target;
    
    RedrawListener(PApplet target){
      this.target = target;
    }
    
    public void controlEvent(CallbackEvent event){
      if(event.getAction() == ControlP5.ACTION_BROADCAST)
        target.redraw();
    }
  }
  
  // set composite mode (removed)
  // public void setCompositeMode(int c){
  //   volren.compositeMode = c;
  //   parent.redraw();
  // }
  
  // set second transfer function color (removed)
  // public void setTFColor2(int c){
  //   volren.tfColor2 = c;
  //   parent.redraw();
  // }
}
