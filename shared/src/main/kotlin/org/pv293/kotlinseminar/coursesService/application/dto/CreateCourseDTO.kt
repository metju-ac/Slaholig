package org.pv293.kotlinseminar.coursesService.application.dto

data class CreateCourseDTO(
    val title : String,
    val description : String,
    val capacity : Int,
)