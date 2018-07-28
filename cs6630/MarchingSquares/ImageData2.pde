//Same as image data but goes through columns first
//It assumes that the image was vectorized and unfolded along the second dimension
class ImageData2{
 int rowCount;
 int columnCount;
 float[][] data;
 String[] test;
 int max;
 int min;
 
 
 ImageData2(String filename,int scale){
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
    data = new float[rowCount][columnCount];
    int columnCounter = 0;
    for(int i = 6; i < columnCount*rowCount + 6; i++){
       if((i-6)%(rowCount) == 0 && i != 6){
        columnCounter++; 
       }
       if(int(raw[i]) > max){max  = int(raw[i]);}
       
       if(int(raw[i]) < min){min = int(raw[i]);}
       //println("index: " + i, ", column: " + columnCounter + ", row: " + (i-6)%rowCount + ", Element: " +raw[i]);
      data[(i-6)%(rowCount)][columnCounter] = int(raw[i]);
    }
    println(max);
    println(min);
    normalizeData(scale);

//   int[][] ans = new int[data[0].length][data.length];
//   for(int rows = 0; rows < data.length; rows++){
//     for(int cols = 0; cols < data[0].length; cols++){
//        ans[cols][rows] = data[rows][cols];
//     }
//   }
//   data = ans;
//   columnCount = data.length;
//   rowCount = data[0].length;

 } 
 
 void normalizeData(int scale){
    for(int i = 0; i < rowCount; i++){
      for(int j = 0; j< columnCount; j++){
        data[i][j] =(data[i][j]-min) *(scale - 0)/(max - min)+0;
        //print("("+ i +" "  + j + ")"+ ", ");
      }
      println((32700-min) *(scale - 0)/(max - min)+0);
    }
 }
 

}
