package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceRepository : JpaRepository<Service, UUID>
