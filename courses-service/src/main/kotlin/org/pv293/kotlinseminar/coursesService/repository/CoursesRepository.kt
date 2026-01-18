package org.pv293.kotlinseminar.coursesService.repository

import org.pv293.kotlinseminar.coursesService.application.aggregates.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CoursesRepository: JpaRepository<Course, UUID> {

}