#include <Adafruit_NeoPixel.h>
#include <Wire.h>

#define PIN 5
#define NUM_LEDS 30 
// Parameter 1 = number of pixels in strip
// Parameter 2 = pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
  // NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
  // NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
 Adafruit_NeoPixel strip = Adafruit_NeoPixel(30, 5, NEO_GRB + NEO_KHZ800); 
/*
int r = 1;
int g = 2;
int b = 3;
void receiveEvent(int bytes){
  // Read the last number sent (0-30)
  // int signalStatus = Wire.read();
  int signalStatus = 16;
  // Scale signalStatus to 0 - 29
  if (signalStatus <= 29){
    for(int i = 0; i >= 29-signalStatus; i++){
          strip.setPixelColor(i, strip.Color(215, 40, 50));
          strip.show();
    }
    for(int i = 0; i <= signalStatus; i++){
          strip.setPixelColor(i, strip.Color(40, 215, 160));
          strip.show();
    }   
  }
  Serial.println(signalStatus); 
}
*/
void setup() {
  // Start I2C as slave
  Wire.begin(4);
 //  Wire.onReceive(receiveEvent);
  double arduinoElevatorPosition = 0;
  Serial.begin(9600);
  //Initialize LED Strip
  strip.begin();
  strip.setBrightness(255);
  strip.show();
  }
int n = 1;  

void loop() {

double arduinoElevatorPosition = Wire.read();
 if(arduinoElevatorPosition >= n){
 for(int i = 0; i < n; i++){

    strip.setPixelColor(i, 0, 255, 0);
    strip.show();
  //Sets pixel color 
  }
  //Sets pixel color 
  }

  if(arduinoElevatorPosition >= n + 1){
  n++;
  }

}
