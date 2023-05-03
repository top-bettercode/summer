package top.bettercode.summer.data.jpa.repository

import top.bettercode.summer.data.jpa.JpaExtRepository
import top.bettercode.summer.data.jpa.domain.Employee
import top.bettercode.summer.data.jpa.domain.EmployeeKey

/**
 * @author Peter Wu
 */
interface EmployeeRepository : JpaExtRepository<Employee?, EmployeeKey?>
