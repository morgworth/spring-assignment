package com.assignment.dao;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.Oid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.assignment.domain.Employee;
import com.google.common.cache.LoadingCache;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/*
 * For reading in cache before DB, used code snippets from this video:
 * https://www.youtube.com/watch?v=diJr2t4KmVw
 */


@Repository
public class DAO {
	DB db= new MongoClient().getDB("SpringAssignmentDB");
	Jongo jongo = new Jongo(db);
	
	@Autowired
	MongoCollection employees;
	@Autowired
	Cache cache1;
	@Autowired
	Cache cache2;
	@Autowired
	Logger logger;
	@Autowired
	LoadingCache<String,Object> guavaCache;

	public List<Employee> getEmployeesByFirstName(String firstName) {
		List<Employee> list= new LinkedList<>();
		Element cacheElement=cache1.get("getEmployeesbyFirstName:"+firstName);
		if(cacheElement==null) {
			logger.debug("Cache Level 1 miss");
			cacheElement=cache2.get("getEmployeesbyFirstName:"+firstName);
			if(cacheElement==null) {
				logger.debug("Cache Level 2 miss");
				MongoCursor<Employee> emps=employees.find("{name:',"+firstName+"'}").as(Employee.class);
				while(emps.hasNext()) {
					list.add(emps.next());
				}
				cache1.put(new Element("getEmployeesbyFirstName:"+firstName,list));
				cache2.put(new Element("getEmployeesbyFirstName:"+firstName,list));
				guavaCache.put("getEmployeesbyFirstName:"+firstName,list);
			} else {
				logger.debug("Cache Level 2 hit");
				cache1.put(cacheElement);
				list=(List<Employee>) cacheElement.getObjectValue();
			}
		} else {
			logger.debug("Cache Level 1 hit");
			list=(List<Employee>) cacheElement.getObjectValue();
		}
		return list;
	}

	public List<Employee> getAll(){
		
		List<Employee> list = new LinkedList<>();
		Element cacheElement=cache1.get("all-employees");
		if(cacheElement==null) {
			logger.debug("Cache Level 1 miss");
			cacheElement=cache2.get("all-employees");
			if(cacheElement==null) {
				logger.debug("Cache Level 2 miss");
				MongoCursor<Employee> emps=employees.find("{}").as(Employee.class);
				while(emps.hasNext()) {
					Employee emp=emps.next();
					list.add(emp);
					cache1.put(new Element(emp.getId(),emp));
					cache2.put(new Element(emp.getId(),emp));
					guavaCache.put(emp.getId(),emp);
				}
				cache1.put(new Element("all-employees",list));
				cache2.put(new Element("all-employees",list));
				guavaCache.put("all-employees",list);
			} else {
				logger.debug("Cache Level 2 hit");
				cache1.put(cacheElement);
				list = (List<Employee>) cacheElement.getObjectValue();
			}
		} else {
			logger.debug("Cache Level 1 hit");
			list=(List<Employee>) cacheElement.getObjectValue();
		}
		
		return list;
	}

	public void deleteEmployeeById(String id) {
		cache1.remove(id);
		cache2.remove(id);
	}

	public Employee getEmployeeById(String id) {

		Employee emp;
		
		Element cacheElement=cache1.get(id);
		if(cacheElement==null) {
			logger.debug("Cache Level 1 miss");
			cacheElement=cache2.get(id);
			if(cacheElement==null) {
				logger.debug("Cache Level 2 miss");

				emp=employees.findOne(Oid.withOid(id)).as(Employee.class);
				cache1.put(new Element(emp.getId(),emp));
				cache2.put(new Element(emp.getId(),emp));
				guavaCache.put(emp.getId(),emp);
			} else {
				logger.debug("Cache Level 2 hit");
				emp=(Employee) cacheElement.getObjectValue();
				cache1.put(new Element(emp.getId(),emp));
			}	
		} else {
			logger.debug("Cache Level 1 hit");
			emp=(Employee) cacheElement.getObjectValue();
		}
		return emp;
	}

	public void addEmployee(Employee employee) {
		//		Employee e = new Employee();
		//		Address a= new Address();
		//		a.setCity("San Jose");
		//		a.setState("California");
		//		a.setStreet("Somewhere");
		//		a.setZipcode("95110");
		//		e.getAddresses().add(a);
		//		a=new Address();
		//		a.setCity("Columbus");
		//		a.setState("Ohio");
		//		a.setStreet("200 W Norwich Ave");
		//		a.setZipcode("43201");
		//		e.getAddresses().add(a);
		//		e.setFirstName("Morgan");
		//		e.setLastName("Worthington");
		//		e.setAge(28);
		//		e.setDept("Engineering");
		//		employees.insert(e);

		//		employees.insert(employee);

		Element cacheElement=new Element("addEmployee:"+employee.toString(),employee);
		cache1.put(cacheElement);
		cache2.put(cacheElement);
		guavaCache.put("addEmployee:"+employee.toString(),employee);
	}

	public Employee updateEmployee(Employee updated) {
		//		employees.update(Oid.withOid(updated.getId())).with("{$set: {"
		//				+ "firstName:'"+updated.getFirstName()+"',"
		//				+ "lastName:'"+updated.getLastName()+"',"
		//				+ "age:"+updated.getAge()+","
		//				+ "dept:'"+updated.getDept()+"',"
		//				+ "addresses:'"+updated.getAddresses()+"'}}"
		//				);

		Element cacheElement=new Element("updateEmployee:"+updated.getId(),updated);
		cache1.put(cacheElement);
		cache2.put(cacheElement);
		guavaCache.put("updateEmployee:"+updated.getId(),updated);
		
		return updated;
	}
}
