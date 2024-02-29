package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

data class FieldsListSlot(
    val timeslot:Timeslot,
    val fields: MutableList<SportField>,
    val booked: Boolean,
    val bookedOtherSlot: Boolean
)

class FreeFieldsForSlot(private val db: AppDatabase) {
    private val _tmp = MutableLiveData<List<FieldsListSlot>>()
    val tmp: LiveData<List<FieldsListSlot>> get() = _tmp
    private val observer = Observer<List<FreeSportTimeSlot>> {
        val map = mutableMapOf<Int,FieldsListSlot>()
        for(slot in it){
            if(map.containsKey(slot.t.id)){
                if(slot.flagBooked || map[slot.t.id]!!.booked)
                    map[slot.t.id] = FieldsListSlot(slot.t, mutableListOf(), true, slot.bookedOtherSlot)
                else
                    map[slot.t.id]!!.fields.add(slot.f)
            }else{
                if(slot.flagBooked)
                    map[slot.t.id] = FieldsListSlot(slot.t, mutableListOf(), true, slot.bookedOtherSlot)
                else
                    map[slot.t.id] = FieldsListSlot(slot.t, mutableListOf(slot.f), false, slot.bookedOtherSlot)
            }
        }
        this._tmp.value = map.values.toList()
    }

    fun getFreeFields(sport:String, userEmail: String){
        val result = db.freeSportTimeSlotDao().getFreeSlot(sport,userEmail)

        result.observeForever(observer)
    }
}