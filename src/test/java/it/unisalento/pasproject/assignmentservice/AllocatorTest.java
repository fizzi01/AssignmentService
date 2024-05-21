package it.unisalento.pasproject.assignmentservice;

import it.unisalento.pasproject.assignmentservice.business.assignment.AllocationAlgorithm;
import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllocatorTest {
    private List<Resource> resources;

    @BeforeEach
    void setUp() {
        AllocationAlgorithm allocationAlgorithm = new AllocationAlgorithm();

        Task task1 = new Task();
        task1.setId("T001");
        task1.setIdTask("Task1");
        task1.setEmailUtente("utente@example.com");
        task1.setMaxComputingPower(94000.0);
        task1.setMaxCudaPower(0.0);
        task1.setMinCudaPower(0.0);
        task1.setTaskDuration(3600.0); // 1 ora
        task1.setMaxEnergyConsumption(150.0);
        task1.setMinComputingPower(35000.0);
        task1.setMinEnergyConsumption(60.0);
        task1.setMinWorkingTime(1800.0); // 30 minuti
        task1.setRunning(false);
        task1.setEnabled(true);

        Task task2 = new Task();
        task2.setId("T003");
        task2.setIdTask("Task3");
        task2.setEmailUtente("user3@example.com");
        task2.setMaxComputingPower(550.0);
        task2.setMaxCudaPower(0.0); // Nessun limite massimo di CUDA
        task2.setMinCudaPower(0.0);
        task2.setTaskDuration(10800.0); // 3 ore
        task2.setMaxEnergyConsumption(30.0);
        task2.setMinComputingPower(400.0);
        task2.setMinEnergyConsumption(15.0);
        task2.setMinWorkingTime(5400.0); // 1.5 ore
        task2.setRunning(false);
        task2.setEnabled(false);


        Resource resource = new Resource();

        resource.setIdResource("44");
        //resource.setAvailableHours(2);
        resource.setKWh(10);
        resource.setMemberEmail("mirko@gmail.com");
        resource.setIsAvailable(true);
        resource.setAssignedUser(null);
        resource.setSingleCoreScore(345);
        resource.setMulticoreScore(345);
        resource.setOpenclScore(0.0);
        resource.setVulkanScore(0.0);
        resource.setCudaScore(0.0);

        Resource resource2 = new Resource();
        resource2.setIdResource("11");
        //resource2.setAvailableHours(3);
        resource2.setKWh(20);
        resource2.setMemberEmail("mirko@gmail.com");
        resource2.setIsAvailable(true);
        resource2.setAssignedUser(null);
        resource2.setSingleCoreScore(314);
        resource2.setMulticoreScore(504);
        resource2.setOpenclScore(0.0);
        resource2.setVulkanScore(0.0);
        resource2.setCudaScore(0.0);

        Resource resource3 = new Resource();
        resource3.setIdResource("22");
        //resource3.setAvailableHours(4);
        resource3.setKWh(130);
        resource3.setMemberEmail("mirko@gmail.com");
        resource3.setIsAvailable(true);
        resource3.setAssignedUser(null);
        resource3.setSingleCoreScore(0.0);
        resource3.setMulticoreScore(0.0);
        resource3.setOpenclScore(40936);
        resource3.setVulkanScore(47313);
        resource3.setCudaScore(39049);

        Resource resource4 = new Resource();
        resource4.setIdResource("33");
        //resource4.setAvailableHours(5);
        resource4.setKWh(100);
        resource4.setMemberEmail("mirko@gmail.com");
        resource4.setIsAvailable(true);
        resource4.setAssignedUser(null);
        resource4.setSingleCoreScore(0.0);
        resource4.setMulticoreScore(0.0);
        resource4.setOpenclScore(39650);
        resource4.setVulkanScore(37402);
        resource4.setCudaScore(49049);

        List<Task> tasks = Arrays.asList(task1, task2);
        resources = Arrays.asList(resource, resource2, resource3, resource4).reversed();
        allocationAlgorithm.assignResources(tasks,resources);

        System.out.println("Tasks: " + tasks);
    }

    @Test
    void testFirstIsSuitableResource() {
       assertEquals("utente@example.com", resources.getFirst().getAssignedUser());
    }

    @Test
    void testSecondIsSuitableResource() {
        //assertEquals("utente@example.com", resources.get(1).getAssignedUser());
        assertEquals("utente@example.com", resources.get(1).getAssignedUser());
    }

    @Test
    void testThirdIsSuitableResource() {
        assertEquals("user3@example.com", resources.get(2).getAssignedUser());
    }

    @Test
    void testFourthIsSuitableResource() {
        assertTrue(resources.get(3).getIsAvailable());
    }

}
