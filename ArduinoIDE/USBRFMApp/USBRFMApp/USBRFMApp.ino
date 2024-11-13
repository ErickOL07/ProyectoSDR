#include "USBRFMApp.h"

USBRFMApp usbRFMApp;

// Pines del AD8232
const int LO_PLUS_PIN = 10;
const int LO_MINUS_PIN = 11;
const int ECG_PIN = A0;

void setup() {
  usbRFMApp.setup();
  
  // Configuración para el módulo AD8232
  pinMode(LO_PLUS_PIN, INPUT);
  pinMode(LO_MINUS_PIN, INPUT);
  Serial.begin(9600);
}

void loop() {
  usbRFMApp.loop();

  // Lectura del AD8232 y transmisión de datos
  if ((digitalRead(LO_PLUS_PIN) == 1) || (digitalRead(LO_MINUS_PIN) == 1)) {
    Serial.println('!');
  } else {
    int ecgData = analogRead(ECG_PIN);
    Serial.println(ecgData);

    // Enviar datos de ECG
    usbRFMApp.app.sendECGData(ecgData);
  }

  delay(1); // Evitar saturación de datos en el puerto serie
}
