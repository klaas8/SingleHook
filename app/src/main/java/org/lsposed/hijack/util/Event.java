package org.lsposed.hijack.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Event extends ViewModel {

    private final MutableLiveData<Boolean> callShowNavigation = new MutableLiveData<>();
    private final MutableLiveData<Void> callUpdate = new MutableLiveData<>();
    
    public void callUpdate() {
        callUpdate.setValue(null);
    }

    public LiveData<Void> observeCallUpdate() {
        return callUpdate;
    }
    
    public void callShowNavigation(boolean value) {
        callShowNavigation.setValue(value);
    }

    public LiveData<Boolean> observeCallShowNavigation() {
        return callShowNavigation;
    }
}
