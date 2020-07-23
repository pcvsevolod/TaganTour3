package vshapovalov.arproject.tagantour3.tours

import vshapovalov.arproject.tagantour3.Tour

class ToursItem {
    var tour : Tour? = null

    constructor() {}
    constructor(t : Tour) {
        this.tour = t;
    }
}