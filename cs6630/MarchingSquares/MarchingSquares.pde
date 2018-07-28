
float plotX1, plotY1;
float plotX2, plotY2;

ImageData2 data;
Cell[][] grid;
int[][] threshGrid;
int[][] squares;

int dataMax;

float scaling;
boolean enlarged = false;
boolean interpolate = false; //To interpolate or not interpolate
boolean key2 = false;//has the #2 key been pressed?
boolean contour = false; //should we contour?
boolean keyc = false; //has the c key been pressed
int rows;
int cols;

void setup(){
  scaling = 1;//120 for full screen on test grid,1.5888 for brain
  dataMax = 255;
  data = new ImageData2("brain.nrrd",dataMax);
  println(data.rowCount);
  println(data.columnCount);
  grid = new Cell[data.columnCount][data.rowCount];
  for (int i = 0; i < data.rowCount; i++) {
    for (int j = 0; j < data.columnCount; j++) {
      // Initialize each object
      grid[j][i] = new Cell(data.data[i][j],i*scaling,j*scaling,scaling,scaling);
    }
  }
  rows = data.columnCount;
  cols = data.rowCount;
  threshGrid = threshold(130); 
  squares = assignSquares(threshGrid);

  
  
  //Adjust these based on the size of our loaded in data
  size(cols*800/rows,800);
  textAlign(LEFT);
  textSize(20);
  
  //plot positioning
  plotX1 = 50;
  plotX2 = width - plotX1;
  plotY1 = 60;
  plotY2 = height - plotY1;
  smooth();
}

void draw(){
  background(220);
  //draw plot area
  fill(255);
  noStroke();
  //rect(plotX1,plotY1,plotX2,plotY2);
  displayGrid(grid);
  if(interpolate){
   billinearInterpolation(); 
  }
  if(contour){
    contour(); 
  }
  //applyColorMap();
}

//This method displays the current grid
void displayGrid(Cell[][] grid){
  for (int i = 0; i < rows; i++) {
    for (int j = 0; j < cols; j++) {
      // Initialize each object
      grid[i][j].display();
    }
  }
}

//This method enlarges the current grid to 800 height
void enlargeGrid(){
  float scaling = 800.0/rows;
  grid = new Cell[data.columnCount][data.rowCount];
  for (int i = 0; i < data.rowCount; i++) {
    for (int j = 0; j < data.columnCount; j++) {
      // Initialize each object
      grid[j][i] = new Cell(data.data[i][j],i*scaling,j*scaling,scaling,scaling);
    }
  }
}


//this method shrinks the image back to normal size
void shrinkGrid(){
  float scaling = 1;
  grid = new Cell[data.columnCount][data.rowCount];
  for (int i = 0; i < data.rowCount; i++) {
    for (int j = 0; j < data.columnCount; j++) {
      // Initialize each object
      grid[j][i] = new Cell(data.data[i][j],i*scaling,j*scaling,scaling,scaling);
    }
  }
}

//applys the colormap
void applyColorMap(){
     
    //The Color map implementation
//    float R = map(value,0,255,117,239);
//    float G = map(value,0,255,107,237);
//    float B = map(value,0,255,177,245);
//    float T = map(value,0,255,24.5,98.8);
//    //color c = new color(R,G,B);
//    //brightness(c);
//    stroke(R,G,B);
//    fill(R,G,B);
//    rect(x,y,w,h); 
    ///////////////////////////// 
    
    
}

void billinearInterpolation(){
  loadPixels();
  int gridW = ceil(grid[1][2].w)/2;
  int gridH = ceil(grid[1][2].h)/2;
  //The Q's take the corners of the texture element and strore the RGB value at that location
  int[] Q1 = new int[3];//bottome left of grid
  int[] Q2 = new int[3];//top left of grid
  int[] Q3 = new int[3];//bottom right of grid
  int[] Q4 = new int[3];//top right of grid
  int Q11;//bottome left of grid
  int Q12;//top left of grid
  int Q21;//bottom right of grid
  int Q22;//top right of grid
  int x1;//left x
  int x2;//right x
  int y1;//lower y
  int y2;//upper y
  int[] val = new int[3];
  color c;
  for (int i = 1; i < height; i++) {//Steps through the rows aka y values
    for (int j = 1; j < width; j++) {//steps through the columns aka x values
    //finds the corners
      x1 = j-gridW;
      x2 = j+gridW;
      y2 = i-gridH;
      y1 = i+gridH;
      //gets the pixel values
      Q1 = pixelTORGB(get(x1,y1));
      Q2 = pixelTORGB(get(x1,y2));
      Q3 = pixelTORGB(get(x2,y1));
      Q4 = pixelTORGB(get(x2,y2));
      
      ///Mapping back to original value
      Q11 = int(map(Q1[0],117,239,0,255));
      Q12 = int(map(Q2[0],117,239,0,255));
      Q21 = int(map(Q3[0],117,239,0,255));
      Q22 = int(map(Q4[0],117,239,0,255));
      //
      //interpolates 
      //lerpColor(Q11, Q12, 
      val[0] = 1/((x2-x1)*(y2-y1))*(Q11*(x2-j)*(y2-i)+Q21*(j-x1)*(y2-i)+Q12*(x2-j)*(i-y1)+Q22*(j-x1)*(i-y1));  //<>//
      //val[1] = 1/((x2-x1)*(y2-y1))*(Q11[1]*(x2-j)*(y2-i)+Q21[1]*(j-x1)*(y2-i)+Q12[1]*(x2-j)*(i-y1)+Q22[1]*(j-x1)*(i-y1)); 
      //val[2] = 1/((x2-x1)*(y2-y1))*(Q11[2]*(x2-j)*(y2-i)+Q21[2]*(j-x1)*(y2-i)+Q12[2]*(x2-j)*(i-y1)+Q22[2]*(j-x1)*(i-y1)); 
      
      //val[0] = (Q11[0] + Q12[0]+Q21[0]+Q22[0])/4;
      
      //c = color(val[0],val[0],val[0]);
      c = color(map(val[0],0,255,117,239),map(val[0],0,255,107,237),map(val[0],0,255,177,245));
      //set(i,j,c);
      //println(pixels[i*width+j]);
      pixels[(i*width)+j] = c;
      //set(i,j,c);
    }
  }
  updatePixels();
}

//Helper method to convert pixel values into R,G,B.  I found this fix at
//https://processing.org/discourse/beta/num_1159135995.html
  int[] pixelTORGB(int c){
    int[] ans = new int[3];
    ans[0]=(c&0x00FF0000)>>16; // red part
    ans[1]=(c&0x0000FF00)>>8; // green part
    ans[2]=(c&0x000000FF); // blue part
    return ans;
  
}

//Thresholds the current grid on the value that we are given.
int[][] threshold(int val){
  int[][] ans = new int[rows][cols];
  for(int i = 0; i < rows; i++){
    for(int j = 0; j < cols; j++){
       if(grid[i][j].value >= val){
         ans[i][j] = 1;
       }else{
         ans[i][j] = 0;
       } 
    }
  } 
  return ans; 
}

//Assigns the square value
int[][] assignSquares(int[][] thresholds){
  int[][] ans = new int[rows-1][cols-1];
  String temp = "";
    for(int i = 0; i < rows-1; i++){
      for(int j = 0; j < cols-1; j++){
        temp += thresholds[i][j];
        temp += thresholds[i][j+1]; 
        temp += thresholds[i+1][j+1];
        temp += thresholds[i+1][j];
        ans[i][j] = unbinary(temp);
        temp = "";
      }
  } 
  return ans; 
  
}




void marchingTable(int val,int i,int j){
  float[] pos = new float[4]; // stores the line coordinates x1,y1,x2,y2
  stroke(250,0,0);
  switch(val){
   case 0:
      break;
   case 1:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (grid[i][j].h*1.5);
      line(pos[0],pos[1],pos[2],pos[3]);
      break;
   case 2:
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h*1.5);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
      break;
   case 3:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
      break;
   case 4:
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (.5*grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
      break;
   case 5:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (.5*grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h*1.5);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 6:
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (.5*grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (1.5*grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 7:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (.5*grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 8:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (.5*grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 9:
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (.5*grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (1.5*grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 10:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (grid[i][j].h*1.5);
      line(pos[0],pos[1],pos[2],pos[3]);
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (.5*grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 11:
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (.5*grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 12:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 13:
      pos[0] = grid[i][j].x + grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h*1.5);
      pos[2] = grid[i][j].x + (grid[i][j].w*1.5);
      pos[3] = grid[i][j].y + (grid[i][j].h);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 14:
      pos[0] = grid[i][j].x + .5*grid[i][j].w;
      pos[1] = grid[i][j].y + (grid[i][j].h);
      pos[2] = grid[i][j].x + (grid[i][j].w);
      pos[3] = grid[i][j].y + (grid[i][j].h*1.5);
      line(pos[0],pos[1],pos[2],pos[3]);
       break;
   case 15:
       break;
  }
  
}

void contour(){
 for(int i = 0; i < rows-1; i++){
   for(int j = 0; j < cols -1; j++){
     //println("rows: " + i + " cols: "+ j);
     marchingTable(squares[i][j],i,j); 
   }
 } 
}



void keyPressed(){
  if(key == '1'){//resizes image
    if(!enlarged){
      enlarged = true;
      enlargeGrid();
    }else{
     enlarged = false; 
     shrinkGrid();
    }
    
  } else if(key == '2'){//applies bilinear interpolation
    if(enlarged & !key2){
       key2 = true;
       interpolate = true;
    }else{
     if(key2){
       key2 = false;
       interpolate = false;
     } 
    }
    
  }
  if(key == 'c' & !keyc){
    contour = true; 
    keyc = true;
  }else{
    keyc = false;
    contour = false;
  }
}

void mousePressed(){
  int[] temp = pixelTORGB(get(mouseX,mouseY));
  int threshVal = int(map(temp[0],117,239,0,255)+map(temp[1],107,237,0,255)+map(temp[2],177,245,0,255))/3; 
  println(threshVal);
  threshGrid = threshold(threshVal); 
  squares = assignSquares(threshGrid);
  println(temp[0] + ", " + temp[1] + ", " + temp[2]);
}
