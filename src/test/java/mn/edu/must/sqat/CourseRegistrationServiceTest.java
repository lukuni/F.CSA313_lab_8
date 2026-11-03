package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {
    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() { service = new CourseRegistrationService(repository, emailSender); }

    private Student student(boolean active, List<String> taken) {
        return new Student("S1", "s1@must.edu.mn", active, taken);
    }

    @Test
    void register_activeStudentWithPrereqs_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals("OK", service.register("S1", "CS201"));

        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), anyString());
    }

    @Test
    void register_unknownStudent_returnsNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent("S9")).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register("S9", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void register_inactiveStudent_returnsInactiveAndNeverTouchesEmail() {
        when(repository.findStudent("S1")).thenReturn(student(false, List.of()));

        assertEquals("ERROR_INACTIVE_STUDENT", service.register("S1", "CS201"));

        verifyNoInteractions(emailSender);
    }

    @Test
    void register_missingPrerequisites_returnsPrerequisitesAndDoesNotSave() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of()));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals("ERROR_PREREQUISITES", service.register("S1", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaved() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of()));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of());
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        assertEquals("OK", service.register("S1", "CS201"));

        verify(repository).saveRegistration("S1", "CS201");
    }

    @Test
    void register_nullStudentId_throwsIllegalArgumentAndTouchesNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));

        verifyNoInteractions(repository, emailSender);
    }

    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of()));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of());
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

        service.register("S1", "CS201");

        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), body.capture());
        assertTrue(body.getValue().contains("CS201"));
    }

    @ParameterizedTest
    @CsvSource({
        "false, true,  true,  ERROR_NO_STUDENT",
        "true,  false, true,  ERROR_INACTIVE_STUDENT",
        "true,  true,  false, ERROR_PREREQUISITES"
    })
    void register_errorCases_returnExpectedCode(boolean exists, boolean active,
                                                boolean hasPrereq, String expected) {
        lenient().when(repository.findStudent("S1"))
                .thenReturn(exists ? student(active, hasPrereq ? List.of("CS101") : List.of()) : null);
        lenient().when(repository.courseExists("CS201")).thenReturn(true);
        lenient().when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals(expected, service.register("S1", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
    }
}