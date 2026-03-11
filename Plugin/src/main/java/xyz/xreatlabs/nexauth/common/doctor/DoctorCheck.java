package xyz.xreatlabs.nexauth.common.doctor;

@FunctionalInterface
public interface DoctorCheck {

    DoctorCheckResult run();
}
