package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL)
    private List<ShiftAssignment> assignments;

    /** Số giờ làm việc của ca */
    public double getWorkHours() {
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (minutes < 0) minutes += 24 * 60; // qua ngày hôm sau
        return Math.round(minutes / 60.0 * 10) / 10.0;
    }
}
