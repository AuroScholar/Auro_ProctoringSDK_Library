package com.example.mytoolbox

import androidx.lifecycle.LiveData

class ListLiveData<T> : LiveData<MutableList<T>?>() {
    fun addAll(items: List<T>?) {
        if (value != null && items != null) {
            value!!.addAll(items)
            value = value
        }
    }

    fun clear() {
        if (value != null) {
            value!!.clear()
            value = value
        }
    }

    public override fun setValue(value: MutableList<T>?) {
        super.setValue(value)
    }

    override fun getValue(): MutableList<T>? {
        return super.getValue()
    }
}