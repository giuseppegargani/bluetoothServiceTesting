package com.example.bluetoothtesting

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import android.util.Log

/* created by Giuseppe Gargani
 */

/* RIGUARDO ALLA ARCHITETTURA GENERALE
    in onCreate si inizializza il service bluechatService con mHandler
    chatService è iniziata a null (all'inizio del programma)
 */

class MainActivity : AppCompatActivity(), DevicesRecyclerViewAdapter.ItemClickListener,
    ChatFragment.CommunicationListener {

    private val REQUEST_ENABLE_BT = 123
    private val TAG = javaClass.simpleName
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewPaired: RecyclerView
    private val mDeviceList = arrayListOf<DeviceData>()
    private lateinit var devicesAdapter: DevicesRecyclerViewAdapter
    private var mBtAdapter: BluetoothAdapter? = null
    private val PERMISSION_REQUEST_LOCATION = 123
    private val PERMISSION_REQUEST_LOCATION_KEY = "PERMISSION_REQUEST_LOCATION"
    private var alreadyAskedForPermission = false
    private lateinit var headerLabel: TextView
    private lateinit var headerLabelPaired: TextView
    private lateinit var headerLabelContainer: LinearLayout
    private lateinit var status: TextView
    private lateinit var  mConnectedDeviceName: String
    private var connected: Boolean = false
    private lateinit var btImageView: ImageView
    @Volatile
    private var cambioDati = false


    private var mChatService: BluetoothChatService? = null
    private lateinit var chatFragment: ChatFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)

        val typeFace = Typeface.createFromAsset(assets, "fonts/product_sans.ttf")
        toolbarTitle.typeface = typeFace

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerViewPaired = findViewById(R.id.recyclerViewPaired)
        headerLabel = findViewById(R.id.headerLabel)
        headerLabelPaired = findViewById(R.id.headerLabelPaired)
        headerLabelContainer = findViewById(R.id.headerLabelContainer)
        status = findViewById(R.id.status)

        //giuseppe bluetooth Icon
        btImageView = findViewById(R.id.btImage)

        status.text = getString(R.string.bluetooth_not_enabled)

        headerLabelContainer.visibility = View.INVISIBLE

        if (savedInstanceState != null) {
            alreadyAskedForPermission = savedInstanceState.getBoolean(PERMISSION_REQUEST_LOCATION_KEY, false)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerViewPaired.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false
        recyclerViewPaired.isNestedScrollingEnabled = false

        //CLICK LISTENERS
        findViewById<Button>(R.id.search_devices).setOnClickListener {
            findDevices()
        }
        findViewById<Button>(R.id.make_visible).setOnClickListener {
            makeVisible()
        }

        devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mDeviceList)
        recyclerView.adapter = devicesAdapter
        devicesAdapter.setItemClickListener(this)

        // Register for broadcasts when a device is discovered.
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        /* Initialize the BluetoothChatService to perform bluetooth connections
        Inizializza il chatService con handler!!!
        Uno dei parametri di bluetoothChat Service è handler
         */
        mChatService = BluetoothChatService(this, mHandler)

        checkActivation()

        //showChatFragment()

    }

    private fun checkActivation() {
        if (mBtAdapter == null){
            showAlertAndExit()
            btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_disabled_24)
        }
        else {

            if (mBtAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_disabled_24)
            } else {
                status.text = getString(R.string.not_connected)
                btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_24)
            }

            // Get a set of currently paired devices
            val pairedDevices = mBtAdapter?.bondedDevices
            val mPairedDeviceList = arrayListOf<DeviceData>()

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices?.size ?: 0 > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices!!) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mPairedDeviceList.add(DeviceData(deviceName,deviceHardwareAddress))
                }

                val devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mPairedDeviceList)
                recyclerViewPaired.adapter = devicesAdapter
                devicesAdapter.setItemClickListener(this)
                headerLabelPaired.visibility = View.VISIBLE
            }
        }
    }

    private fun makeVisible() {

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
    }

    private fun checkPermissions() {

        if (alreadyAskedForPermission) {
            // don't check again because the dialog is still open
            return
        }

        // Android M Permission check 
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.need_loc_access))
            builder.setMessage(getString(R.string.please_grant_loc_access))
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                // the dialog will be opened so we have to save that
                alreadyAskedForPermission = true
                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), PERMISSION_REQUEST_LOCATION)
            }
            builder.show()

        } else {
            startDiscovery()
        }
    }

    private fun showAlertAndExit() {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.not_compatible))
            .setMessage(getString(R.string.no_support))
            .setPositiveButton("Exit", { _, _ -> System.exit(0) })
            .show()
    }

    private fun findDevices() {
        checkPermissions()
    }

    private fun startDiscovery() {

        headerLabelContainer.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        headerLabel.text = getString(R.string.searching)
        mDeviceList.clear()
        btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_searching_24)

        // If we're already discovering, stop it
        if (mBtAdapter?.isDiscovering ?: false)
            mBtAdapter?.cancelDiscovery()

        // Request discover from BluetoothAdapter
        mBtAdapter?.startDiscovery()
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device!!.name
                val deviceHardwareAddress = device.address // MAC address

                val deviceData = DeviceData(deviceName, deviceHardwareAddress)
                mDeviceList.add(deviceData)

                val setList = HashSet<DeviceData>(mDeviceList)
                mDeviceList.clear()
                mDeviceList.addAll(setList)

                devicesAdapter.notifyDataSetChanged()
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                progressBar.visibility = View.INVISIBLE
                headerLabel.text = getString(R.string.found)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        progressBar.visibility = View.INVISIBLE

        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            //Bluetooth is now connected.
            status.text = getString(R.string.not_connected)

            btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_24)

            // Get a set of currently paired devices
            val pairedDevices = mBtAdapter?.bondedDevices
            val mPairedDeviceList = arrayListOf<DeviceData>()

            mPairedDeviceList.clear()

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices?.size ?: 0 > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices!!) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mPairedDeviceList.add(DeviceData(deviceName,deviceHardwareAddress))
                }

                val devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mPairedDeviceList)
                recyclerViewPaired.adapter = devicesAdapter
                devicesAdapter.setItemClickListener(this)
                headerLabelPaired.visibility = View.VISIBLE

            }

        }
        //label.setText("Bluetooth is now enabled.")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PERMISSION_REQUEST_LOCATION_KEY, alreadyAskedForPermission)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            PERMISSION_REQUEST_LOCATION -> {
                // the request returned a result so the dialog is closed
                alreadyAskedForPermission = false
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "Coarse and fine location permissions granted")
                    startDiscovery()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.fun_limted))
                        builder.setMessage(getString(R.string.since_perm_not_granted))
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.show()
                    }
                }
            }
        }
    }

    //se un elemento di recyclerView viene cliccato lancia connectDevice
    override fun itemClicked(deviceData: DeviceData) {
        connectDevice(deviceData)
    }

    /*cancella discovery e lancia il servizio
    Se clicchi lancia connectDevice con deviceData corrispondente!!
    LANCIA IL METODO CONNECT DI mChatService!!! con la device corrispondente!!! che prende come parametro!!!
     */
    private fun connectDevice(deviceData: DeviceData) {

        // Cancel discovery because it's costly and we're about to connect
        mBtAdapter?.cancelDiscovery()
        val deviceAddress = deviceData.deviceHardwareAddress

        val device = mBtAdapter?.getRemoteDevice(deviceAddress)

        status.text = getString(R.string.connecting)

        // Attempt to connect to the device
        mChatService?.connect(device, true)

    }

    /*SE E' CONNECTED LANCIA IL METODO SHOWCHATFRAGMENT

     */
    override fun onResume() {
        super.onResume()
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        /* se il servizio di chat è diverso da null
            ma se connected naviga al frammento!!
         */
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService?.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService?.start()
            }
        }
        if(connected)
            showChatFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        //giuseppe
        mChatService?.stop()
        mHandler.removeCallbacksAndMessages(null)
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    /*HANDLER RICEVE UN MESSAGGIO INDIETRO DAL SERVIZIO!!!!!
        QUANDO SCRIVE UN MESSAGGIO INVOCA UN METODO DELLA CHAT DOPO AVER VERIFICATO CHE E' CONNESSO ALLA CHAT
        ma handler è uno dei concetti chiave di Android Kotlin
        vedi RayWenderlich. tutorial pratici da fare:
        cosa fà,a cosa serve e come si testa
        Un Handler permette di restituire indietro al main thread (UI) un altro thread che lavora in background!!!!
        e' un concetto importante!!!
        pag.389 Libro di android in italiano!!!!!
        https://medium.com/@ankit.sinhal/handler-in-android-d138c1f4980e
        articolo interessante!!! ma c'è qualche tutorial da fare pratico
        perchè utile? PERCHE' ANDROID NON PERMETTE CHE GLI ALTRI THREAD COMUNICHINO CON IL THREAD PRINCIPALE
        ma c'è ancora bisogno con gli ultimi aggiornamenti e coroutines?
        Se hai bisogno di aggiornare la UI thread da un altro thread hai bisogno di handler
        ma quali sono i metodi principali di Handler per la gestione del thread?
        ma si possono mettere handler con fragment?
        sembra che gli handler siano legati a services, è così?
        
     */
    /*Proviamo a mandare db quando riceve!! un comando

     */

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {

        //elementi relativi al DB
        var listaMin = mutableListOf<String>("elementoLista")
        var listaMax = mutableListOf<String>()

        fun calcoloChecksum(stringa: String ): String {
            //fai una lista di due caratteri
            var lista = stringa.chunked(2).map { it.toInt(16) }.sumBy{it} //aggiungo stringa 05

            return lista.toString(16)
        }
        fun ByteArray.toHex2(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }
        fun String.decodeHex(): ByteArray {
            check(length % 2 == 0) { "Must have an even length" }

            return chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }
        fun converti (stringa:String): String {
            return stringa
        }
        fun calcoloLunghezza(stringa: String): String{
            return stringa
        }
        fun calcoloValoriPressione (stringa: String):String{
            val payload = stringa.chunked(2)
            var convertito:String = "-"
            //Solo dei pacchetti f4 che sono di dimensione 9 (altrimenti aspetta)?? oppure unisci pezzetti? non conviene forse
            if(payload.size==9) {
                val dato=payload[4].toInt(16)
                convertito = (((dato * 2) - 330) / 10).toString()
            }
            return convertito
        }
        fun pulisciEdAggiungiStringa(stringa: String){
            //calcolo checkSum
            Log.d("calcolo", "stringa $stringa")
            var lista = stringa.chunked(2)
            var packetId:String = lista[0]+lista[1]
            var checksum:String = lista[lista.size-3]+lista[lista.size-2]
            var payload = lista.subList(4,lista.size-3)
            var convertita = payload.map { (((it.toLong(16)*2)-330)/10).toString() }

            var calcoloChecksum: String = calcoloChecksum(payload.joinToString(""))
            val stringaUnita = payload.joinToString("")
            Log.d("calcolo","packetId: $packetId checksum: $checksum e stringa: $convertita e calcolo $calcoloChecksum ")
        }

        override fun handleMessage(msg: Message) {

            when (msg.what) {

                Constants.MESSAGE_STATE_CHANGE -> {

                    when (msg.arg1) {

                        BluetoothChatService.STATE_CONNECTED -> {

                            status.text = getString(R.string.connected_to) + " "+ mConnectedDeviceName
                            btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_connected_24)
                            Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                            connected = true
                        }

                        BluetoothChatService.STATE_CONNECTING -> {
                            status.text = getString(R.string.connecting)
                            btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_searching_24)
                            connected = false
                        }

                        BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> {
                            status.text = getString(R.string.not_connected)
                            Snackbar.make(findViewById(R.id.mainScreen),getString(R.string.not_connected),Snackbar.LENGTH_SHORT).show()
                            btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_24)
                            connected = false
                        }
                    }
                }

                Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = writeBuf.toHex2()
                    //Toast.makeText(this@MainActivity,"Me: $writeMessage",Toast.LENGTH_SHORT).show()
                    //mConversationArrayAdapter.add("Me:  " + writeMessage)
                    val milliSecondsTime = System.currentTimeMillis()
                    chatFragment.communicate(com.example.bluetoothtesting.Message(writeMessage,milliSecondsTime,Constants.MESSAGE_TYPE_SENT))
                }

                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    //val readMessage = String(readBuf, 0, msg.arg1)
                    val readMessage = readBuf.toHex2().trimEnd('0')

                    Log.d("dati","dati: readBuf: ${readBuf} message: $readMessage")

                    val milliSecondsTime = System.currentTimeMillis()

                    //pulisce e riempie variabile lista
                    if(readMessage.startsWith("f005")){
                        pulisciEdAggiungiStringa(readMessage)
                    }

                    //manda un messaggio Toast quando arriva a fineDB con checksum ed altro
                    if(readMessage.endsWith("f00f0000000ff1")){
                        Toast.makeText(this@MainActivity,listaMin.toString(),Toast.LENGTH_SHORT).show()
                    }

                     if(readMessage.startsWith("f004")) {
                         val valore = calcoloValoriPressione(readMessage)
                        chatFragment.cambiaValore(valore)
                    }

                    //Toast.makeText(this@MainActivity,"$mConnectedDeviceName : $readMessage",Toast.LENGTH_SHORT).show()
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage)
                    chatFragment.communicate(com.example.bluetoothtesting.Message(readMessage,milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))

                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)!!
                    status.text = getString(R.string.connected_to) + " " +mConnectedDeviceName
                    btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_connected_24)
                    Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                    connected = true
                    showChatFragment()

                }
                Constants.MESSAGE_TOAST -> {
                    status.text = getString(R.string.not_connected)
                    btImageView.setBackgroundResource(R.drawable.ic_baseline_bluetooth_searching_24)
                    Snackbar.make(findViewById(R.id.mainScreen),
                        msg.data.getString(Constants.TOAST)!!,Snackbar.LENGTH_SHORT).show()
                    connected = false
                }
            }
        }

    }

/* SENDMESSAGE OF HANDLER
    A partire dallo stato, se bluetoothChatService è connected (altrimenti ritorna subito)
    SE IL MESSAGGIO NON E' VUOTO, LANCIA IL METODO WRITE DI MCHATSERVICE

 */
    private fun sendMessage(message: String) {

        // Check that we're actually connected before trying anything
        if (mChatService?.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return
        }

        // Check that there's actually something to send
        if (message.isNotEmpty()) {

            if(message==" "){
                if(cambioDati) {
                    var fakeSignal = "F0090001000009F1F0010001010002F1F002000201020005F1F003000200010004F1F0040003B7B7B70229F1F00700020097009EF1F008000200F90101F1F00B000100000BF1F00C000101000DF1"
                    var packet: ByteArray = fakeSignal.decodeHex()
                    mChatService?.write(packet)
                    cambioDati = false
                }
                else {
                    val fakeSignal = "F0090001000009F1F0010001020003F1F002000201030006F1F003000200040007F1F0040003BEBEBE023EF1F00700020096009DF1F008000200F80100F1F00B000100000BF1F00C000101000DF1"
                    val packet: ByteArray = fakeSignal.decodeHex()
                    mChatService?.write(packet)
                    cambioDati = true
                }
            }

            else{
                try {
                    val send = message.decodeHex()
                    mChatService?.write(send)
                }
                catch(e: Exception) {
                    Toast.makeText(this,"Rewrite: message must have an even length", Toast.LENGTH_SHORT).show()
                    val send = message.toByteArray()
                    mChatService?.write(send)
                }

                // Reset out string buffer to zero and clear the edit text field
                //mOutStringBuffer.setLength(0)
                //mOutEditText.setText(mOutStringBuffer)
            }
        }
    }

    private fun showChatFragment() {

        if(!isFinishing) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            chatFragment = ChatFragment.newInstance()
            chatFragment.setCommunicationListener(this)
            fragmentTransaction.replace(R.id.mainScreen, chatFragment, "ChatFragment")
            fragmentTransaction.addToBackStack("ChatFragment")
            fragmentTransaction.commit()
        }
    }

    override fun onCommunication(message: String) {
        sendMessage(message)
    }

    //se torna indietro ricontrolla attivazione bt
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0)
            super.onBackPressed()
        else
        {supportFragmentManager.popBackStack()
        Toast.makeText(this, "tornato indietro", Toast.LENGTH_SHORT).show()
            if((mBtAdapter==null)||(mBtAdapter?.isEnabled == false)) checkActivation() //if accidentally turned off bluetooth
        }
    }

    @ExperimentalUnsignedTypes
    fun ByteArray.toHex2(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun calcoloChecksum(stringa: String ): String {
        //fai una lista di due caratteri
        var lista = stringa.chunked(2).map { it.toInt(16) }.sumBy{it}

        return lista.toString(16)
    }
}