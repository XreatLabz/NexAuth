package xyz.xreatlabs.nexauth.common.doctor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoctorServiceTest {

    @Test
    void runsChecksIntoOrderedReport() {
        var service = new DoctorService(List.<DoctorCheck>of(
                () -> new DoctorCheckResult("email", DoctorSeverity.OK, "Email ready"),
                () -> new DoctorCheckResult("database", DoctorSeverity.FAIL, "Database unreachable"),
                () -> new DoctorCheckResult("config", DoctorSeverity.WARN, "Configuration fallback active")
        ));

        var report = service.run();

        assertEquals(DoctorSeverity.FAIL, report.overallSeverity());
        assertEquals(List.of("database", "config", "email"), report.checks().stream().map(DoctorCheckResult::checkId).toList());
    }
}
