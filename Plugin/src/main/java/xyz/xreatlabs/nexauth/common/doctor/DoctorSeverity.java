package xyz.xreatlabs.nexauth.common.doctor;

public enum DoctorSeverity {
    OK,
    WARN,
    FAIL;

    public static DoctorSeverity max(DoctorSeverity left, DoctorSeverity right) {
        return left.ordinal() >= right.ordinal() ? left : right;
    }
}
