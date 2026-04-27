package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final ShiftRepository shiftRepository;
    private final CafeRepository cafeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initCafes();
        initUsers();
        initEmployees();
        initMenu();
        initShifts();
        log.info("=== Khoi tao du lieu mau hoan tat ===");
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ROLE_ADMIN"));
            roleRepository.save(new Role("ROLE_MANAGER"));
            roleRepository.save(new Role("ROLE_STAFF"));
        }
    }

    private void initCafes() {
        if (cafeRepository.count() == 0) {
            Cafe cafe1 = new Cafe();
            cafe1.setName("Cafe Sai Gon");
            cafe1.setAddress("123 Nguyen Hue, Quan 1, TP.HCM");
            cafe1.setPhone("028-1234-5678");
            cafe1.setDescription("Quan ca phe phong cach Sai Gon co dien");
            cafeRepository.save(cafe1);

            Cafe cafe2 = new Cafe();
            cafe2.setName("Cafe Ha Noi");
            cafe2.setAddress("45 Hang Gai, Hoan Kiem, Ha Noi");
            cafe2.setPhone("024-8765-4321");
            cafe2.setDescription("Quan ca phe phong cach Ha Noi truyen thong");
            cafeRepository.save(cafe2);
        }
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            Role managerRole = roleRepository.findByName("ROLE_MANAGER").orElseThrow();
            Role staffRole = roleRepository.findByName("ROLE_STAFF").orElseThrow();

            java.util.List<Cafe> cafes = cafeRepository.findAll();
            Cafe cafe1 = cafes.get(0);
            Cafe cafe2 = cafes.get(1);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@cafe.vn");
            admin.setRoles(Set.of(adminRole));
            admin.setCafe(cafe1);
            userRepository.save(admin);

            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setEmail("manager@cafe.vn");
            manager.setRoles(Set.of(managerRole));
            manager.setCafe(cafe1);
            userRepository.save(manager);

            User staff = new User();
            staff.setUsername("staff01");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setEmail("staff01@cafe.vn");
            staff.setRoles(Set.of(staffRole));
            staff.setCafe(cafe1);
            userRepository.save(staff);

            User admin2 = new User();
            admin2.setUsername("admin2");
            admin2.setPassword(passwordEncoder.encode("admin123"));
            admin2.setEmail("admin2@cafe.vn");
            admin2.setRoles(Set.of(adminRole));
            admin2.setCafe(cafe2);
            userRepository.save(admin2);

            log.info("Da tao 4 tai khoan: admin/admin123 (cafe1), manager, staff01, admin2/admin123 (cafe2)");
        }
    }

    private void initEmployees() {
        if (employeeRepository.count() == 0) {
            User managerUser = userRepository.findByUsername("manager").orElseThrow();
            User staffUser = userRepository.findByUsername("staff01").orElseThrow();
            java.util.List<Cafe> cafes = cafeRepository.findAll();
            Cafe cafe1 = cafes.get(0);
            Cafe cafe2 = cafes.get(1);

            Employee emp1 = new Employee();
            emp1.setEmployeeCode("NV0001");
            emp1.setFullName("Nguyen Van Quan Ly");
            emp1.setPhone("0901234567");
            emp1.setEmail("manager@cafe.vn");
            emp1.setPosition("Quan ly");
            emp1.setDepartment("Ban Giam Doc");
            emp1.setHireDate(LocalDate.of(2022, 1, 1));
            emp1.setSalary(new BigDecimal("15000000"));
            emp1.setUser(managerUser);
            emp1.setCafe(cafe1);
            employeeRepository.save(emp1);

            Employee emp2 = new Employee();
            emp2.setEmployeeCode("NV0002");
            emp2.setFullName("Tran Thi Nhan Vien");
            emp2.setPhone("0912345678");
            emp2.setEmail("staff01@cafe.vn");
            emp2.setPosition("Barista");
            emp2.setDepartment("Pha Che");
            emp2.setHireDate(LocalDate.of(2023, 3, 15));
            emp2.setSalary(new BigDecimal("8000000"));
            emp2.setUser(staffUser);
            emp2.setCafe(cafe1);
            employeeRepository.save(emp2);

            Employee emp3 = new Employee();
            emp3.setEmployeeCode("NV0003");
            emp3.setFullName("Le Thi Thu Huong");
            emp3.setPhone("0923456789");
            emp3.setEmail("huong@cafe.vn");
            emp3.setPosition("Phuc vu");
            emp3.setDepartment("Phuc Vu");
            emp3.setHireDate(LocalDate.of(2023, 6, 1));
            emp3.setSalary(new BigDecimal("7500000"));
            emp3.setCafe(cafe1);
            employeeRepository.save(emp3);

            Employee emp4 = new Employee();
            emp4.setEmployeeCode("NV0004");
            emp4.setFullName("Pham Minh Tuan");
            emp4.setPhone("0934567890");
            emp4.setEmail("tuan@hncafe.vn");
            emp4.setPosition("Quan ly");
            emp4.setDepartment("Ban Giam Doc");
            emp4.setHireDate(LocalDate.of(2022, 5, 1));
            emp4.setSalary(new BigDecimal("14000000"));
            emp4.setCafe(cafe2);
            employeeRepository.save(emp4);

            Employee emp5 = new Employee();
            emp5.setEmployeeCode("NV0005");
            emp5.setFullName("Vu Thi Lan Huong");
            emp5.setPhone("0945678901");
            emp5.setEmail("lan@hncafe.vn");
            emp5.setPosition("Barista");
            emp5.setDepartment("Pha Che");
            emp5.setHireDate(LocalDate.of(2023, 8, 1));
            emp5.setSalary(new BigDecimal("7800000"));
            emp5.setCafe(cafe2);
            employeeRepository.save(emp5);
        }
    }

    private void initMenu() {
        if (categoryRepository.count() == 0) {
            java.util.List<Cafe> cafes = cafeRepository.findAll();
            Cafe cafe1 = cafes.get(0);
            Cafe cafe2 = cafes.get(1);

            Category coffee = new Category();
            coffee.setName("Ca Phe");
            coffee.setDescription("Cac loai ca phe dac trung");
            coffee.setCafe(cafe1);
            categoryRepository.save(coffee);

            Category tea = new Category();
            tea.setName("Tra & Nuoc Ep");
            tea.setDescription("Cac loai tra va nuoc ep trai cay tuoi");
            tea.setCafe(cafe1);
            categoryRepository.save(tea);

            MenuItem caphe = new MenuItem();
            caphe.setName("Ca Phe Den");
            caphe.setDescription("Ca phe den nguyen chat, dam da");
            caphe.setPrice(new BigDecimal("25000"));
            caphe.setCategory(coffee);
            menuItemRepository.save(caphe);

            MenuItem traxanh = new MenuItem();
            traxanh.setName("Tra Xanh Matcha");
            traxanh.setDescription("Tra xanh matcha Nhat Ban");
            traxanh.setPrice(new BigDecimal("45000"));
            traxanh.setCategory(tea);
            menuItemRepository.save(traxanh);

            Category coffeeHN = new Category();
            coffeeHN.setName("Ca Phe");
            coffeeHN.setDescription("Ca phe dac san Ha Noi");
            coffeeHN.setCafe(cafe2);
            categoryRepository.save(coffeeHN);

            MenuItem egCoffee = new MenuItem();
            egCoffee.setName("Ca Phe Trung");
            egCoffee.setDescription("Dac san ca phe trung Ha Noi");
            egCoffee.setPrice(new BigDecimal("40000"));
            egCoffee.setCategory(coffeeHN);
            menuItemRepository.save(egCoffee);
        }
    }

    private void initShifts() {
        if (shiftRepository.count() == 0) {
            java.util.List<Cafe> cafes = cafeRepository.findAll();
            for (Cafe cafe : cafes) {
                Shift morning = new Shift();
                morning.setName("Ca Sang");
                morning.setStartTime(LocalTime.of(6, 0));
                morning.setEndTime(LocalTime.of(14, 0));
                morning.setDescription("Ca sang 6:00 - 14:00");
                morning.setActive(true);
                morning.setCafe(cafe);
                shiftRepository.save(morning);

                Shift afternoon = new Shift();
                afternoon.setName("Ca Chieu");
                afternoon.setStartTime(LocalTime.of(14, 0));
                afternoon.setEndTime(LocalTime.of(22, 0));
                afternoon.setDescription("Ca chieu 14:00 - 22:00");
                afternoon.setActive(true);
                afternoon.setCafe(cafe);
                shiftRepository.save(afternoon);
            }
        }
    }
}
