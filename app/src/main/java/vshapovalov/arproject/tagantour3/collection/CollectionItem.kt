package vshapovalov.arproject.tagantour3.collection

import vshapovalov.arproject.tagantour3.Place

class CollectionItem {
    //var desc: String? = null
    //var id = 0
    var place : Place? = null

    constructor() {}
    //constructor(desc: String?, id: Int, p : Place) {
    constructor(p : Place) {
        //this.desc = desc
        //this.id = id
        this.place = p;
    }

}