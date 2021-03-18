package com.example.bleadvertiser3

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.nio.charset.Charset
import java.util.*
import java.util.Objects.hash


class DashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    val advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser

    val advertisingCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE", "Advertising onStartFailure: $errorCode")
            super.onStartFailure(errorCode)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        textView.text = "User ID: " + currentUser?.uid
        textView2.text = "Name: " + currentUser?.displayName
        textView3.text = "E-mail: " + currentUser?.email

        Glide.with(this).load(currentUser?.photoUrl).into(profile_imageView)

        sign_out_button.setOnClickListener{
            stopAdvertising()
            //Firebase Sign out
            mAuth.signOut()

            //Google Sign out
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish() }
        }

        switch1.setOnClickListener(){
            if(switch1.isChecked){
                advertise()
            }
            else{
                stopAdvertising()
            }
        }
    }

    private fun advertise() {
        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(false)
                .build()

        //val pUuid = ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)))   //using string from strings.xml
        val pUuid = ParcelUuid(UUID.randomUUID())
        val id = mAuth.uid
        val idSubstring1 = id?.substring(0,5)  //substring: Returns the substring of this string starting at the startIndex and ending right before the endIndex.
        val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                //.addServiceUuid(pUuid)
                .addServiceData(pUuid, idSubstring1?.toByteArray(Charset.forName("UTF-8")))
                .build()

        advertiser.startAdvertising(settings, data, advertisingCallback)
    }

    private fun stopAdvertising() {
        //Log.d(TAG, "Service: Stopping Advertising")
        if (advertiser != null) {
            advertiser!!.stopAdvertising(advertisingCallback)
        }
    }
}