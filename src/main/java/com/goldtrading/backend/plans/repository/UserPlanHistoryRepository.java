package com.goldtrading.backend.plans.repository; import com.goldtrading.backend.plans.domain.entity.UserPlanHistory; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID; public interface UserPlanHistoryRepository extends JpaRepository<UserPlanHistory, UUID> {}

