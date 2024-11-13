#include "USBRFMApp.h"

void USBRFMApp::App::sendECGData(int ecgData) {
  // Crear un buffer de datos para el objeto de ECG y definir el nombre
  CDS::DataBuffer* ecgBuffer = CDS::Object::newObject(this->rootIT(nullptr), K("ECG_Data"));
  
  // Añadir el valor del dato de ECG
  CDS::DataBuffer* ecgDataBuffer = CDS::Object::newNumber(ecgBuffer, K("ecgValue"));
  CDS::Number::set(ecgDataBuffer, ecgData);

  // Enviar el buffer de datos del ECG
  this->send(ecgBuffer, nullptr);

  // Liberar memoria del buffer
  delete ecgBuffer;
}



USBRFMApp::USBRFMApp() : RootNode(NULL, S("root")), serial(this, S("serial")), rfm(this, S("rfm")), app(this, S("app")) {
	this->nodes->set(this->serial.name, &this->serial);
	this->nodes->set(this->rfm.name, &this->rfm);
	this->nodes->set(this->app.name, &this->app);
	this->app.addDataChannel(&this->rfm.channel);
	this->addDataChannel(&this->serial.channel);
}

USBRFMApp::~USBRFMApp() {
	delete this->storageIterator.buffer;
}

void USBRFMApp::setup() {
	RootNode::setup();
	
	size_t eepromSize = sizeof(DEFAULTS);
	#ifdef ESP8266
		EEPROM.begin(eepromSize);
	#else
		EEPROM.begin();
	#endif
	CDS::EEPROMBuffer* eepromBuffer = new CDS::EEPROMBuffer(0);
	this->storageIterator = CDS::Iterator(eepromBuffer, eepromSize);
	bool validDefaults = CDS::Element::valid(&storageIterator);
	if (!validDefaults) {
		for (size_t i = (size_t) 0; i < eepromSize; i++) {
			uint8_t value =  pgm_read_byte(DEFAULTS + i);
			EEPROM.write(i, value);
		}
		#ifdef ESP8266
			EEPROM.commit();
		#endif
	}
	// TODO:: check version or structure
	
	this->serial.setup();
	this->rfm.setup();
	this->app.setup();
}

void USBRFMApp::loop() {
	RootNode::loop();
	this->serial.loop();
	this->rfm.loop();
	this->app.loop();
}



