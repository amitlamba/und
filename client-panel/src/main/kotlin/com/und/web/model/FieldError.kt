package com.und.model.api


class FieldError(val field: String, val message: String){
    override fun toString(): String {
        return "field $field  message $message"
    }
}
