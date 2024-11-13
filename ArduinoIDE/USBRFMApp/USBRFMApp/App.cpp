#include "USBRFMApp.h"

void App_send(Node* cdNode, CDS::DataBuffer* params, CDS::DataBuffer* response) {
    USBRFMApp::App* app = (USBRFMApp::App*)cdNode;
    app->send(params, response);
}

USBRFMApp::App::App(Node* parent, const char* name) : Node(parent, name) {
    Method* send = new Node::Method(this, App_send);
    this->methods->set((char*)String(F("send")).c_str(), send);
}

USBRFMApp::App::~App() {}

// Implementación de setup (si es necesario)
void USBRFMApp::App::setup() {
    // Inicialización específica de la aplicación
}

// Implementación de loop (si es necesario)
void USBRFMApp::App::loop() {
    // Bucle de procesamiento
}

// Implementación de la función state
void USBRFMApp::App::state(CDS::DataBuffer* params, CDS::DataBuffer* response) {
    // Lógica para procesar el estado, si es necesario
    CDS::DataBuffer* object = this->rootIT(response);
    CDS::DataBuffer* state = CDS::Object::newObject(object, K("state"));
}

// Implementación de la función onData
void USBRFMApp::App::onData(DataChannel* dataChannel, CDS::DataBuffer* data) {
    // Procesar los datos entrantes de LoRa
    if (CDS::Element::isNumber(data)) {
        CDS::DataBuffer* command = CDS::Element::newObject();
        CDS::DataBuffer* object = this->rootIT(command);
        CDS::DataBuffer* ondata = CDS::Object::newObject(object, K("ondata"));
        CDS::DataBuffer* rssi = CDS::Object::newNumber(ondata, K("rssi"));
        CDS::Number::set(rssi, LoRa.packetRssi()); // Indicador de fuerza de señal
        CDS::DataBuffer* snr = CDS::Object::newNumber(ondata, K("snr"));
        CDS::Number::set(snr, LoRa.packetSnr()); // Relación señal/ruido
        CDS::DataBuffer* pfe = CDS::Object::newNumber(ondata, K("pfe"));
        CDS::Number::set(pfe, LoRa.packetFrequencyError()); // Error de frecuencia de paquete
        CDS::Object::set(ondata, K("data"), data);
        this->command(command);
        delete command;
    }
}

// Implementación de la función send
void USBRFMApp::App::send(CDS::DataBuffer* params, CDS::DataBuffer* response) {
    int sent = 0;
    CDS::DataBuffer* data = CDS::Object::get(params, K("data"));
    if (CDS::Element::isNumber(data)) {
        for (int i = 0; i < this->channels->length; i++) {
            DataChannel* channel = this->channels->get(i);
            sent += channel->send(data);
            yield();
        }
        CDS::DataBuffer* object = this->rootIT(response);
        CDS::DataBuffer* sentObject = CDS::Object::newObject(object, K("sent"));
        CDS::DataBuffer* total = CDS::Object::newNumber(sentObject, K("total"));
        CDS::Number::set(total, sent);
    }
}
