package ru.netology.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertServiceImpl;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class MedicalServiceTest {

    @Test
    @DisplayName("out message to check blood pressure")
    void outMessageToCheckTest() {
        PatientInfoFileRepository patientInfoFileRepository =
                spy(new PatientInfoFileRepository(new File("patient.txt"), new ObjectMapper()));

        BloodPressure bloodPressureNormal = new BloodPressure(120, 70);
        BloodPressure bloodPressure = new BloodPressure(120, 120);


        HealthInfo healthInfo = spy(new HealthInfo(new BigDecimal("2"), bloodPressureNormal));
        when(patientInfoFileRepository.getById(anyString()))
                .thenReturn(new PatientInfo(
                      "1",
                      "1",
                      "1",
                      LocalDate.now(), healthInfo
                ));
        SendAlertServiceImpl sendAlertServiceSpy = spy(SendAlertServiceImpl.class);

        MedicalServiceImpl medicalService =
                spy(new MedicalServiceImpl(patientInfoFileRepository, sendAlertServiceSpy));
        medicalService.checkBloodPressure("1", bloodPressure);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(sendAlertServiceSpy, times(1)).send(argumentCaptor.capture());
        String expected = "Warning, patient with id: 1, need help";
        String actual = argumentCaptor.getValue();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("out message to check temperature")
    void outMessageToCheckTemperatureTest() {
        PatientInfoFileRepository patientInfoFileRepositorySpy = spy(
                new PatientInfoFileRepository(new File("patient.txt"), new ObjectMapper()));
        HealthInfo healthInfo = spy(HealthInfo.class);

        when(healthInfo.getNormalTemperature()).thenReturn(new BigDecimal("36.6"));

        when(patientInfoFileRepositorySpy.getById(anyString()))
                .thenReturn(new PatientInfo(
                        "1",
                        "2",
                        "@",
                        LocalDate.now(),
                        healthInfo));

        SendAlertServiceImpl sendAlertServiceSpy = spy(SendAlertServiceImpl.class);

        MedicalServiceImpl medicalService =
                spy(new MedicalServiceImpl(patientInfoFileRepositorySpy, sendAlertServiceSpy));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        medicalService.checkTemperature("2", new BigDecimal("12"));

        verify(sendAlertServiceSpy, times(1))
                .send(stringArgumentCaptor.capture());

        String expectedResult = "Warning, patient with id: 1, need help";
        String actualResult = stringArgumentCaptor.getValue();

        assertEquals(expectedResult, actualResult);

    }

    @Test
    @DisplayName("alert doesn't send")
    void messageDoesNotSendAlertTest() {
        PatientInfoFileRepository patientInfoFileRepositorySpy = spy(
                new PatientInfoFileRepository(new File("patient.txt"), new ObjectMapper()));
        HealthInfo healthInfo = spy(HealthInfo.class);

        when(healthInfo.getNormalTemperature()).thenReturn(new BigDecimal("36.6"));

        when(patientInfoFileRepositorySpy.getById(anyString()))
                .thenReturn(new PatientInfo(
                        "1",
                        "2",
                        "@",
                        LocalDate.now(),
                        healthInfo));

        SendAlertServiceImpl sendAlertServiceSpy = spy(SendAlertServiceImpl.class);

        MedicalServiceImpl medicalService =
                spy(new MedicalServiceImpl(patientInfoFileRepositorySpy, sendAlertServiceSpy));
        medicalService.checkTemperature("2", new BigDecimal("36.6"));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(sendAlertServiceSpy, never())
                .send(argumentCaptor.capture());
    }

}
