package hello.dao.single.impl;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import hello.BaseTest;
import hello.dao.single.EmployeeDao;
import hello.entity.single.Employee;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@DatabaseSetup({"/employee.xml"})
public class EmployeeDaoImplTest extends BaseTest {

    @Autowired
    private EmployeeDao employeeDao;

    @Test
    public void persist() {
        Employee employee = new Employee("NewJohn", "User");
        employeeDao.persist(employee);
        flushAndClean();

        assertThat(employee.getId(), is(notNullValue()));
    }

    @Test
    public void getAll() {
        List<Employee> employees = employeeDao.getAll();

        assertThat(employees.size(), equalTo(2));
        assertThat(employees, containsInAnyOrder(
                hasProperty("id", is(1L)),
                hasProperty("id", is(2L))
        ));
    }

    @Test
    public void getById_WhenExist() {
        Optional<Employee> optionalEmployee = employeeDao.getById(1L);
        Employee expectedEmployee = new Employee("John", "admin");
        expectedEmployee.setId(1L);

        assertThat(optionalEmployee, equalTo(Optional.of(expectedEmployee)));
    }

    @Test
    public void getById_WhenNotExist() {
        Optional<Employee> optionalEmployee = employeeDao.getById(-1L);

        assertThat(optionalEmployee, equalTo(Optional.empty()));
    }

    @Test
    public void delete() {
        Employee employee = employeeDao.getById(1L)
                .orElseThrow(() -> new RuntimeException("Try delete not exist Employee"));
        employeeDao.delete(employee);
        flushAndClean();

        List<Employee> employees = employeeDao.getAll();
        assertThat(employees.size(), equalTo(1));
        assertThat(employees, containsInAnyOrder(
                hasProperty("id", is(2L))
        ));
    }

    @Test
    public void findByName_WhenExistName() {
        List<Employee> employees = employeeDao.findByName("John");

        assertThat(employees.size(), equalTo(1));
        assertThat(employees, containsInAnyOrder(
                hasProperty("name", is("John"))
        ));
    }

    @Test(expected = NullPointerException.class)
    public void getByProps_WhenPassedNull() {
        List<Employee> employees = employeeDao.getByProps(null);
    }

    @Test
    public void getByProps_WhenEmptyPassedFields_ShouldReturnAll() {
        Map<String, List<?>> props = new HashMap<>();
        List<Employee> employees = employeeDao.getByProps(props);

        assertThat(employees.size(), equalTo(2));
    }

    @Test
    public void getByProps_ById_WhenNotExistId() {
        Map<String, List<?>> props = new HashMap<>();
        props.put("id", Collections.singletonList(-1L));
        List<Employee> employees = employeeDao.getByProps(props);

        assertThat(employees.size(), equalTo(0));
    }

    @Test
    public void getByProps_ById() {
        Map<String, List<?>> props = new HashMap<>();
        props.put("id", Collections.singletonList(1L));
        List<Employee> employees = employeeDao.getByProps(props);

        assertThat(employees.size(), equalTo(1));
        assertThat(employees, containsInAnyOrder(
                hasProperty("id", is(1L))
        ));
    }

    @Test
    public void getByProps_ById_WhenIdIsString() {
        Map<String, List<?>> props = new HashMap<>();
        props.put("id", Collections.singletonList("1"));
        List<Employee> employees = employeeDao.getByProps(props);

        assertThat(employees.size(), equalTo(1));
        assertThat(employees, containsInAnyOrder(
                hasProperty("id", is(1L))
        ));
    }

    @Test
    public void getByProps_ById_WhenIdIsDouble() {
        Map<String, List<?>> props = new HashMap<>();
        props.put("id", Collections.singletonList(1.));
        List<Employee> employees = employeeDao.getByProps(props);

        assertThat(employees.size(), equalTo(1));
        assertThat(employees, containsInAnyOrder(
                hasProperty("id", is(1L))
        ));
    }

    @Test
    public void getByProps_ByName() {
        Map<String, List<?>> props = new HashMap<>();
        props.put("name", Arrays.asList("John", "NotExistName"));
        List<Employee> employees = employeeDao.getByProps(props);

        assertThat(employees.size(), equalTo(1));
        assertThat(employees, containsInAnyOrder(
                hasProperty("name", is("John"))
        ));
    }
}