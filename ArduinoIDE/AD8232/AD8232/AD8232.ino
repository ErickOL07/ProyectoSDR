#include <SPI.h>
#include <LoRa.h>

const int sensorPin = A0; // Pin del sensor

void setup() {
  Serial.begin(9600);
  while (!Serial);

  if (!LoRa.begin(915E6)) { // 915MHz
    Serial.println("Error al iniciar el módulo LoRa.");
    while (1);
  }
  Serial.println("Módulo LoRa inicializado.");

  pinMode(9, OUTPUT);
  digitalWrite(9, HIGH);
}

void loop() {
  int sensorValue = analogRead(sensorPin);
  Serial.print("Valor del sensor: ");
  Serial.println(sensorValue);

  // con esto se envía lo que obtiene el sensor a través de LoRa
  LoRa.beginPacket();
  LoRa.print(sensorValue);
  LoRa.endPacket();

  // Un pequeño delay para que descanse y no explote 🐒
  delay(1000);
}

