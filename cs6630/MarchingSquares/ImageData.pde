//Right now this parser only works for data of 2 dimensions.
//This should be adjusted by calculating the dimension from the header later
//

class ImageData{
 int rowCount;
 int columnCount;
 int[][] data;
 String[] test;
 int max;
 int min;
 
 
 ImageData(String filename,int scale){
    String[] raw = loadStrings(filename);
    max = 0;
    min = 100000000;
   //Parse Header
   println(raw.length);
    String[] sizes = split(raw[4],' ');
    rowCount = int(sizes[1]);
    columnCount = int(sizes[2]);
    println("columnCount: " + columnCount);
    println("rowCount: " + rowCount);
    test = raw;
    data = new int[rowCount][columnCount];
    int rowCounter = 0;
    for(int i = 6; i < columnCount*rowCount + 6; i++){
       if((i-6)%(columnCount) == 0 && i != 6){
        rowCounter++; 
       }
       if(int(raw[i]) > max){max  = int(raw[i]);}
       
       if(int(raw[i]) < min){min = int(raw[i]);}
       println("index: " + i, ", row: " + rowCounter + ", column: " + (i-6)%columnCount + ", Element: " +raw[i]);
      data[rowCounter][(i-6)%(columnCount)] = int(raw[i]);
    }
    println(max);
    println(min);
    normalizeData(scale);
    
 } 
 
 void normalizeData(int scale){
    for(int i = 0; i < rowCount; i++){
      for(int j = 0; j< columnCount; j++){
        data[i][j] =(data[i][j]-min) *(scale - 0)/(max - min)+0;
        //print("("+ i +" "  + j + ")"+ ", ");
      }
      //println();
    }
 }
  

}
