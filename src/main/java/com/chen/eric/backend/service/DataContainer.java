package com.chen.eric.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;

import com.chen.eric.backend.Container;
import com.chen.eric.backend.Customer;
import com.chen.eric.backend.Employee;
import com.chen.eric.backend.Vessel;

public class DataContainer {
	private static DataContainer dbContainer; 
	Authentication authentication;
	
	private DBConnectionService dbService = DBConnectionService.getInstance();
	  
	public static DataContainer getInstance() { 
		if (dbContainer == null)
			dbContainer = new DataContainer(); 
		return dbContainer; 
	} 
	
	private DataContainer() {
	}
	
	private EntityService<Vessel> vesselService = new VesselService(dbService);
	public Map<String, Vessel> vesselRecords = new HashMap<>();
	
	public void getVesselRecords() {
		vesselRecords = vesselService.retriveRecords();
		dbService.closeConnection();
	}
	
	public void getVesselRecordsByParams(String filter, String value) {
		vesselRecords = vesselService.retriveRecordsByParameters(filter, value);
		dbService.closeConnection();
	}
	
	public int insertVesselRecords (Vessel vessel) {
		int code = vesselService.insertRecords(vessel);
		dbService.closeConnection();
		return code;
	}
	
	public int updateVesselRecords (Vessel vessel, int key) {
		int code = vesselService.updateRecords(vessel, key);
		dbService.closeConnection();
		return code;
	}
	
	public int deleteVesselRecords (int vesselID) {
		int code = vesselService.deleteRecords(vesselID);
		dbService.closeConnection();
		return code;
	}
	
	private EntityService<Employee> employeeService = new EmployeeService(dbService);
	public Map<String, Employee> employeeRecords = new HashMap<>();
	
	public void getEmployeeRecords() {
		employeeRecords = employeeService.retriveRecords();
		dbService.closeConnection();
	}
	
	public int updateEmployeeRecords (Employee employee, int key) {
		int code = employeeService.updateRecords(employee, key);
		dbService.closeConnection();
		return code;
	}
	
	public int deleteEmployeeRecords (int ssn) {
		int code = employeeService.deleteRecords(ssn);
		dbService.closeConnection();
		return code;
	}
	
	private EntityService<Container> containerService = new ContainerService(dbService);
	public Map<String, Container> containerRecords = new HashMap<>();
	
	
	public void getContainerRecords() {
		containerRecords = containerService.retriveRecords();
		dbService.closeConnection();
	}
	
	public void getContainerRecordsByParams(String filter, String value) {
		containerRecords = containerService.retriveRecordsByParameters(filter, value);
		dbService.closeConnection();
	}
	
	public int insertContainerRecords (Container contianer) {
		int code = containerService.insertRecords(contianer);
		dbService.closeConnection();
		return code;
	}
	
	public int updateContainerRecords (Container contianer, int key) {
		int code = containerService.updateRecords(contianer, key);
		dbService.closeConnection();
		return code;
	}
	
	public int deleteContainerRecords (int contianerID) {
		int code = containerService.deleteRecords(contianerID);
		dbService.closeConnection();
		return code;
	}
	
	public Map<String, Customer> customerRecords;
	private EntityService<Customer>customerService = new CustomerService(dbService);

	public int insertCustomerRecords(Customer customer) {
		int code = customerService.insertRecords(customer);
		dbService.closeConnection();
		return code;
	}

	public void getCustomerRecords() {
		customerRecords = customerService.retriveRecords();
		dbService.closeConnection();
	}
	
	public void getCustomerRecordsByParams(String filter, String value) {
		customerRecords = customerService.retriveRecordsByParameters(filter, value);
		dbService.closeConnection();
	}

	public int updateCustomerRecords(Customer cutomer, Integer customerID) {
		int code = customerService.updateRecords(cutomer, customerID);
		dbService.closeConnection();
		return code;
	}
	
	public int deleteCustomerRecords(Integer ID) {
		int code = customerService.deleteRecords(ID);
		dbService.closeConnection();
		return code;
	}
	
}
