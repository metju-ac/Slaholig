package org.pv293.kotlinseminar.exceptions

class NotFoundException: RuntimeException() {
    override val message: String
        get() = "Entity not found"
}