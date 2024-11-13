package com.app.proyectosdr;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Main extends AppCompatActivity {
    public final String ACTION_USB_PERMISSION = "com.app.proyectosdr.USB_PERMISSION";
    static String defaultConfiguration = "{\"baudrate\":9600,\"config\":3}";

    WebView webView;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    SharedPreferences sharedPreferences;
    ListaEnlazada<Registro> registros;
    boolean alertaActiva = false;
    long cooldownFin = 0;
    int maxRitmo = 0;
    int minRitmo = Integer.MAX_VALUE;
    int sumaRitmos = 0;
    int conteoRitmos = 0;

    UsbSerialInterface.UsbReadCallback usbReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            int ritmoCardiaco = Integer.parseInt(new String(bytes));
            verificarRitmo(ritmoCardiaco);

            guardarDatos(ritmoCardiaco);
            webView.post(() -> webView.loadUrl("javascript:MonitorRitmo_onData('" + ritmoCardiaco + "')"));
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("USBReceiver", "Intent received: " + intent.getAction());
            Toast.makeText(context, "Intent received: " + intent.getAction(), Toast.LENGTH_SHORT).show();

            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (extras.containsKey(UsbManager.EXTRA_PERMISSION_GRANTED)) {
                        boolean granted = extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                        if (granted) {
                            createDevice();
                        } else {
                            webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`USB Permission not granted ...`)"));
                        }
                    } else {
                        webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`EXTRA_PERMISSION_GRANTED key is missing in intent ...`)"));
                    }
                } else {
                    webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`Received null bundle for USB permission ...`)"));
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Log.d("USBReceiver", "Device attached");
                webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`USB device attached ...`)"));
                createDevice();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                Log.d("USBReceiver", "Device detached");
                webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`USB device detached ...`)"));
                close();
            }
        }


    };



    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Filtro de intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        // Solicitud de permiso manual
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (UsbDevice usbDevice : usbDevices.values()) {
                device = usbDevice;  // Asigna el dispositivo
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                usbManager.requestPermission(usbDevice, pendingIntent);
                Log.d("USB_PERMISSION", "Solicitud de permiso enviada para el dispositivo USB.");
                break; // Salimos después de la primera solicitud para un dispositivo compatible
            }
        } else {
            Log.d("USB_PERMISSION", "No hay dispositivos USB conectados.");
        }



    sharedPreferences = getSharedPreferences("HeartRateData", MODE_PRIVATE);
        registros = new ListaEnlazada<>();

        this.usbManager = (UsbManager) getSystemService(USB_SERVICE);


        this.webView = findViewById(R.id.webview);
        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        this.webView.addJavascriptInterface(this, "AndroidSerial");
        this.webView.loadUrl("file:///android_asset/index.html");
        Button verRegistrosButton = findViewById(R.id.verRegistrosButton);
        verRegistrosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirRegistros();
            }
        });
    }

    public void createDevice() {
        connection = usbManager.openDevice(device);
        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if (serialPort != null) {
            if (serialPort.open()) {
                configure(defaultConfiguration);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(usbReadCallback);
                this.webView.post(() -> webView.loadUrl("javascript:AndroidSerial_onopen()"));
            } else {
                webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`Can't open port ...`)"));
            }
        } else {
            webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`Can't use port, it is NULL ...`)"));
        }
    }

    @JavascriptInterface
    public void configure(String configurationSTR) {
        try {
            JSONObject configuration = new JSONObject(configurationSTR);
            int baudrate = configuration.optInt("baudrate", 9600);
            serialPort.setBaudRate(baudrate);

            int config = configuration.optInt("config", 3);
            serialPort.setDataBits(config % 4 + UsbSerialInterface.DATA_BITS_5);
            serialPort.setStopBits((config / 8) % 2 + UsbSerialInterface.STOP_BITS_1);
            serialPort.setParity((config / 16) % 2 + UsbSerialInterface.PARITY_NONE);
        } catch(Exception e) {
            webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`Can't open port:" + e.toString() + " `)"));
        }
    }

    @JavascriptInterface
    public void connect(String configurationSTR) {
        defaultConfiguration = configurationSTR;
        boolean found = false;
        if (serialPort == null || !serialPort.isOpen()) {
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            if (!usbDevices.isEmpty()) {
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    device = entry.getValue();
                    int deviceVID = device.getVendorId();
                    if (deviceVID == 0x1B4F || deviceVID == 0x1A86 || deviceVID == 0x0403 || deviceVID == 0x2341) {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        usbManager.requestPermission(device, pendingIntent);

                        found = true;
                    }

                }
            }
        } else {
            found = true;
        }

        if (!found) {
            this.webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`No compatible USB device found ...`)"));
        }
    }

    @JavascriptInterface
    public void send(byte[] bytes) {
        try {
            serialPort.write(bytes);
        } catch(Exception e) {
            webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`" + e.toString() + "`)"));
        }
    }

    @JavascriptInterface
    public void close() {
        try {
            if (serialPort != null) {
                serialPort.close();
            }
        } catch(Exception e) {
            webView.post(() -> webView.loadUrl("javascript:AndroidSerial_log(`" + e.toString() + "`)"));
        }
        webView.post(() -> webView.loadUrl("javascript:AndroidSerial_onclose()"));
    }

    private void guardarDatos(int ritmo) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastHeartRate", ritmo);
        editor.apply();
    }

    @JavascriptInterface
    public void abrirRegistros() {
        Intent intent = new Intent(this, ListaRegistro.class);
        startActivity(intent);
    }

    private void verificarRitmo(int ritmo) {
        long ahora = System.currentTimeMillis();
        if (ritmo > 100 && !alertaActiva && ahora > cooldownFin) {
            alertaActiva = true;
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(500); // Vibración
            }
            mostrarVentanaEmergente(ritmo);

            maxRitmo = ritmo;
            minRitmo = ritmo;
            sumaRitmos = ritmo;
            conteoRitmos = 1;
            cooldownFin = ahora + 900000; // 15 minutos
        } else if (alertaActiva && ahora < cooldownFin) {
            maxRitmo = Math.max(maxRitmo, ritmo);
            minRitmo = Math.min(minRitmo, ritmo);
            sumaRitmos += ritmo;
            conteoRitmos++;
        } else if (alertaActiva && ahora >= cooldownFin) {
            int promedioRitmo = sumaRitmos / conteoRitmos;
            registrarResumen(minRitmo, maxRitmo, promedioRitmo);
            alertaActiva = false;
        }
    }

    @JavascriptInterface
    public void registrar(String pensamiento, String estadoAnimo, String reaccionesFisicas, String actividad) {
        Registro nuevoRegistro = new Registro(pensamiento, estadoAnimo, reaccionesFisicas, actividad);
        RegistrosSingleton.getInstance().agregarRegistro(nuevoRegistro); // Guardamos el registro en el Singleton
    }

    public ListaEnlazada<Registro> getRegistros() {
        return RegistrosSingleton.getInstance().getRegistros(); // Accedemos a los registros desde el Singleton
    }



    private void guardarRegistros() {
        // Código para serializar y guardar la lista de registros en SharedPreferences
    }

    private void mostrarVentanaEmergente(int ritmoActual) {
        Intent intent = new Intent(this, RegistroActivity.class);
        intent.putExtra("ritmoActual", ritmoActual);
        startActivity(intent);
    }

    private void registrarResumen(int min, int max, int promedio) {
        // Crea y guarda un registro resumen
    }

}
