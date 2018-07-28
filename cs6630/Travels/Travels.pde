//Import map files
import de.fhpotsdam.unfolding.*;
import de.fhpotsdam.unfolding.geo.*;
import de.fhpotsdam.unfolding.utils.*;
import de.fhpotsdam.unfolding.marker.*;

//Construct the map object
UnfoldingMap map;
/*
*NOTE: for latitude and longitude they should be given a negative value if they are
*either west or south
*/

//Map Objects
Location[] cities; //Array of locations for the cities in our text file
SimplePointMarker[] markers; //Array of markers based on cities

//Data variables
FloatTable data;

int row_count;
int column_count;

void setup(){
 size(800,600, OPENGL);
 map = new UnfoldingMap(this);
 data = new FloatTable("Countries.txt");
 row_count = data.getRowCount();
 column_count = data.getColumnCount();
 cities = new Location[column_count];
 MapUtils.createDefaultEventDispatcher(this,map);
 
 //Find the locations
 cities = makeLocationArray(data.data,3,4);
 markers = makeSimplePointMarkerArray(cities);
 
 
}

void draw(){
  map.draw(); 
  //displaySimpleMarkers(markers);
}

//creates an array of city locations based on latitude and longitude data
Location[] makeLocationArray(float[][] cities,int lat_pos,int long_pos){
  Location[] answer = new Location[row_count];
   for(int i = 0; i < cities.length; i++){
      answer[i] = new Location(cities[i][lat_pos],cities[i][long_pos]); //<>//
   }
   return answer;
}

//Creates an array of simple markers based on an array of locations
SimplePointMarker[] makeSimplePointMarkerArray(Location[] loc){
  SimplePointMarker[] answer = new SimplePointMarker[loc.length];
  for(int i = 0; i < loc.length; i++){
   answer[i] = new SimplePointMarker(loc[i]);
  }
  return answer;
}

//Displays the simple markers in an array of simple markers
void displaySimpleMarkers(SimplePointMarker[] markers){
  for(int i = 0; i < markers.length; i++){
   map.addMarker(markers[i]); 
  }
}
