FloatTable data;
float dataMin, dataMax;

float plotX1, plotY1;
float plotX2, plotY2;

int row_count;
int column_count;


Axis[] axes;//array of parallel axis objects
float[] column_positions;//array with the final positions of the axis objects
int[] order; //the order of the elements
float[] col_maxs;//columnwise maximums
float[] col_mins;//columnwise minimums 
String[] names;
int[] available_data; //the indecies of available data after filtering

//interactivity
int num_selected = 0;
int selected; //The previously selected column
int num_filters = 0;//number of filters in place
int count = 0; //the number of data points in the available data array

void setup(){
  //basic graphical setup
  size(1000,445); 
  rectMode(CORNERS);
  noStroke();
  textSize(20);
  textAlign(LEFT);
  
  //load in data
  data = new FloatTable("cars_cleaned.txt");
  //data = new FloatTable("cameras.tsv");
  row_count = data.getRowCount();
  column_count = data.getColumnCount();
  println(row_count);
  println(column_count);
  println(data.getColumnName(0));
  println(data.getColumnName(6));
  
  //plot positioning
  plotX1 = 50;
  plotX2 = width - plotX1;
  plotY1 = 60;
  plotY2 = height - plotY1;
  
  //define axis objects
  axes = new Axis[column_count];
  column_positions = new float[column_count];
  col_mins = new float[column_count];
  col_maxs = new float[column_count];
  names = new String[column_count];
  order = new int[column_count];
  available_data = new int[row_count];
  
  for(int i = 0; i < column_count; i++){
    column_positions[i] = (i) * (width) / column_count+plotX1+7.5; 
    order[i] = i; 
    col_mins[i] = data.getColumnMin(i);
    col_maxs[i] = data.getColumnMax(i);
    names[i] = data.getColumnName(i);
    axes[i] = new Axis(column_positions[i],col_mins[i],col_maxs[i],names[i],false,data.getColumn(i)); 
  }

  for(int i = 0; i < row_count;i++){
    available_data[i] = i;
  } 
  
  
  
  smooth();
  
  
}

void draw(){
  background(220);
  //draw plot area
  fill(255);
  rectMode(CORNERS);
  noStroke();
  rect(plotX1,plotY1,plotX2,plotY2);
  drawLines();
  for(int i = 0; i < column_count;i++){
     axes[i].display();
  }
  
  //plot axes, labels, and ticks
  
}

void drawLines(){
  strokeWeight(1);
  stroke(86,121,193,80);
  fill(86,121,193,80);
  int k;
  for(int j = 0; j < column_count-1; j++){
    for(int i  = 0; i < available_data.length; i++){
      float y1 = map(axes[order[j]].data[available_data[i]],axes[order[j]].min,axes[order[j]].max,plotY2,plotY1);
      float y2 = map(axes[order[j+1]].data[available_data[i]],axes[order[j+1]].min,axes[order[j+1]].max,plotY2,plotY1);
      line(axes[order[j]].position,y1,axes[order[j+1]].position,y2);
    }
  }
}


/////////////////////////////Interactivity///////////////////////////

void mousePressed(){
  int nearest = findNearestTitle(); //The position in order of the nearest column value.
  int temp;
  //If the distance between the mouse and the name is sufficiently small then select it on click
  if(dist(mouseX,mouseY,column_positions[nearest],plotY1-15) <= 20){
    num_selected++;
    if(num_selected > 1){
      temp = order[nearest];
      order[nearest] = order[selected];
      order[selected] = temp;
      num_selected = 0;
      updateOrdering();
    }else{
      axes[order[nearest]].selected = true; 
      selected = nearest;
    }
  }
  ///////////////Filtering////////////////////
  int nearest_axis = findNearestAxis(); //finds the nearest axis to the mouse when clicked
  //If the distance between the cursor and the axis is sufficiently small then begin filtering
  if((dist(mouseX,mouseY,axes[nearest_axis].position,mouseY) < 5) && !(mouseY < plotY1)){
    //check to see if we have already chosen a first filter point
    if(axes[nearest_axis].filter[1] && axes[nearest_axis].filter[0]){//if already two filter points get rid of them
        axes[nearest_axis].filter[0] = false;
        axes[nearest_axis].filter[1] = false;
        num_filters--;
          available_data = new int[row_count];
          for(int i = 0; i < row_count;i++){
           available_data[i] = i;
         } 
        int[] temporary;
        temporary = findFilteredData();
        available_data = new int[temporary.length];
        available_data = temporary;
    }else{
      if(axes[nearest_axis].filter[0]){//if one is selected choose the next filter point
        axes[nearest_axis].filter[1] = true;
        axes[nearest_axis].filter_2 = mouseY;
        num_filters++;
        int[] temporary;
        temporary = findFilteredData();
        available_data = new int[temporary.length];
        available_data = temporary;
      }else{
      axes[nearest_axis].filter[0] = true;
      axes[nearest_axis].filter_1 = mouseY;
      rect(axes[nearest_axis].position-2,mouseY,axes[nearest_axis].position+2,mouseY);
      count = 0;
      }
      
    }
  }
  

}


//What to do when key is pressed
void keyPressed(){
  //Invert if I and selected.
  if(key == 'i'){
    int sel = findSelected();
    if(sel != -1){
      axes[sel].selected = false;
      axes[sel].data = flip(axes[sel].data);
      float temp = axes[sel].min;
      axes[sel].min = -1*axes[sel].max;
      axes[sel].max = -1*temp;
      num_selected = 0;
    }
  }
}



/*
*This class is an axis object. Each axis has a position that it is drawn at, labels, and ticks
*associated with it.  
*/

class Axis{
 float position; //position in x coordinates for line to be drawn
 float[] data;
 float min;  //minimum data value
 float max;//maximum data value
 float interval;
 String title; //axis title
 boolean selected;
 float filter_1;
 float filter_2;
 boolean[] filter;
 
 
 
 Axis(float position_,float min_, float max_, String title_,boolean selected_,float[] data_){
   position = position_;
   min = min_;
   max = max_;
   interval = ceil((max-min)/10);
   title = title_;
   selected = selected_;
   data = data_;
   filter = new boolean[2];
   filter[0] = false;
   filter[1] = false;
   filter_1 = min_;
   filter_2 = max_;
 }

 void display(){
   fill(0);
   stroke(0);
  //draw the axis line 
  line(position,plotY1,position,plotY2);
  //draw the axis label
  if(selected){
    fill(255,0,0);
    text(title,position, plotY1-15);
  }else{
    text(title,position, plotY1-15);
  }
  axisLabels();
  displayFilter();
 }

//labels the axes with numbers related to the data
 void axisLabels(){
   fill(0);
   textSize(10);
   strokeWeight(1);
   textAlign(CENTER,CENTER);
   //NOTE: I should fix the fact that the labels do not include negative numbers
   //The abs() applied to floor was added to deal with column flipping I should store
   //The real value of an element in a separate array and use that...  Will work on later
   //if there is time
   for(float v = floor(min); v <  max; v += interval){
     float y = map(v,min,max,plotY2,plotY1);
      textAlign(CENTER,CENTER); 
      if(v >= 1000){
        text(abs(floor(v)),position-18,y);
        line(position-4,y,position,y);
      }else if(v >= 100 && v < 1000){
        text(abs(floor(v)),position-14,y);
        line(position-4,y,position,y);
      }else if(v >= 10 && v < 100){
        text(abs(floor(v)),position-10,y);
        line(position-4,y,position,y);
      }else{
        text(abs(floor(v)),position-8,y);
        line(position-4,y,position,y);
      }
  }
    
}

  void displayFilter(){
    if(filter[0]){
     fill(190);
     rect(position-2,filter_1,position+2,filter_1);
    }if(filter[0] && filter[1]){
     rect(position-2,filter_1,position+2,filter_2);
    } 
  }

  
}





/////////////////Helper Methods//////////////////////////
/*
*This method looks to see which title is closest to the cursor and returns it's position in the
*column_positions array
*/
int findNearestTitle(){
 int index = 0;//The actual value of the closest column from the original array
 float temp = 100000;
 float distance = 0;
 for(int i = 0; i < column_positions.length;i++){
   distance = dist(mouseX,mouseY,column_positions[i],plotY1-15);
   if(distance < temp){
     index = i; 
     temp = distance;
   }
 }
 return index; 
}

//Finds the nearest axis for filtering
int findNearestAxis(){
 int index = 0;//The actual value of the closest column from the original array
 float temp = 100000;
 float distance = 0;
 for(int i = 0; i < column_positions.length;i++){
   distance = dist(mouseX,mouseY,column_positions[i],plotY1-15);
   if(distance < temp){
     index = i; 
     temp = distance;
   }
 }
 
 for(int i = 0; i < column_count; i++){
  if(column_positions[index] == axes[i].position){
   return i;
  } 
 }
 
 println(index);
 return index; 
}

void updateOrdering(){
  for(int i = 0; i < column_count; i++){
   axes[i].selected = false;
   axes[order[i]].position = column_positions[i];
  }
}

//This helper method finds the selected column.
int findSelected(){
  for(int i = 0; i < column_count; i++){
    if(axes[i].selected){
      return i;
    }
  }
  return -1;
}

//Flips the data around the origin makes positive negative and negative positive
float[] flip(float[] arr){
 for(int i = 0; i < arr.length; i++){
   arr[i] = -1*arr[i];
 } 
 return arr;
}



//checks to see if element is in list
boolean contains(int[]arr, int element){
  for(int i = 0; i < arr.length; i++){
   if(arr[i] == element){
    return true;
   } 
  }
  return false;
}

//creates the list of available data points
int[] findFilteredData(){
  int[] dat = new int[row_count];
  for(int i = 0; i < row_count; i++){
   dat[i] = i; 
  }
  int k;
  int[] temp_available = new int[row_count];
  int temp_counter = 0;
  int counter = 0;
  for(int j = 0; j < column_count; j++){
    for(int i  = 0; i < row_count; i++){
      float y1 = map(axes[order[j]].data[i],axes[order[j]].min,axes[order[j]].max,plotY2,plotY1);
      if(axes[order[j]].filter[1] && axes[order[j]].filter[0]){
        if((y1 <= axes[order[j]].filter_1 && y1 >= axes[order[j]].filter_2) || (y1 >= axes[order[j]].filter_1 && y1 <= axes[order[j]].filter_2)){
          if(contains(dat,i)){
            temp_available[temp_counter] = i;
            temp_counter++; 
          }
        }
      }
    }
    if(temp_counter != 0){
      dat = new int[temp_counter];
      for(int i = 0; i < temp_counter; i++){
       dat[i] = temp_available[i]; 
      }
      temp_available = new int[row_count];
      temp_counter = 0;
    }
    //dat = temp_available;
    //temp_available = new int[column_count];
    //temp_counter = 0;
  }//end row for 
//    int[] answer = new int[counter];
//    for(int i = 0; i < counter; i++){
//     answer[i] = dat[i]; 
//    }
  return dat;
}
