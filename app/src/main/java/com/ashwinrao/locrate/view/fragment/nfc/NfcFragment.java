package com.ashwinrao.locrate.view.fragment.nfc;

import android.nfc.tech.Ndef;

import androidx.annotation.NonNull;

public interface NfcFragment {

    void onTagDetected(@NonNull Ndef ndef);

}
