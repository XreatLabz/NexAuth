package xyz.xreatlabs.nexauth.common.doctor.checks;

import org.junit.jupiter.api.Test;
import xyz.xreatlabs.nexauth.api.database.ReadWriteDatabaseProvider;
import xyz.xreatlabs.nexauth.api.database.User;
import xyz.xreatlabs.nexauth.api.database.connector.DatabaseConnector;
import xyz.xreatlabs.nexauth.common.doctor.DoctorSeverity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseDoctorCheckTest {

    @Test
    void failsWhenDatabaseProviderIsMissing() {
        var result = new DatabaseDoctorCheck(null, null).run();

        assertEquals(DoctorSeverity.FAIL, result.severity());
        assertEquals("Database provider unavailable", result.message());
    }

    @Test
    void warnsWhenConnectorIsUnavailable() {
        var result = new DatabaseDoctorCheck(new TestDatabaseProvider(), null).run();

        assertEquals(DoctorSeverity.WARN, result.severity());
        assertEquals("Database connector unavailable", result.message());
    }

    @Test
    void failsWhenConnectorIsDisconnected() {
        var result = new DatabaseDoctorCheck(new TestDatabaseProvider(), new TestDatabaseConnector(false)).run();

        assertEquals(DoctorSeverity.FAIL, result.severity());
        assertEquals("Database connector is disconnected", result.message());
    }

    @Test
    void reportsOkWhenProviderAndConnectorAreReady() {
        var result = new DatabaseDoctorCheck(new TestDatabaseProvider(), new TestDatabaseConnector(true)).run();

        assertEquals(DoctorSeverity.OK, result.severity());
        assertEquals("Database provider and connector are ready", result.message());
    }

    private static final class TestDatabaseProvider implements ReadWriteDatabaseProvider {
        @Override public User getByName(String name) { return null; }
        @Override public User getByUUID(UUID uuid) { return null; }
        @Override public User getByPremiumUUID(UUID uuid) { return null; }
        @Override public Collection<User> getAllUsers() { return List.of(); }
        @Override public Collection<User> getByIP(String ip) { return List.of(); }
        @Override public void insertUser(User user) { }
        @Override public void insertUsers(Collection<User> users) { }
        @Override public void updateUser(User user) { }
        @Override public void deleteUser(User user) { }
    }

    private record TestDatabaseConnector(boolean connected) implements DatabaseConnector<RuntimeException, Object> {
        @Override public void connect() { }
        @Override public boolean connected() { return connected; }
        @Override public void disconnect() { }
        @Override public Object obtainInterface() { return new Object(); }
        @Override public <V> V runQuery(xyz.xreatlabs.nexauth.api.util.ThrowableFunction<Object, V, RuntimeException> function) { return null; }
    }
}
