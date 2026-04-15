package org.demo.springjpademo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 学生实体类 (多对多关系示例)
 *
 * @author : Tomatos
 */
@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    /**
     * @ManyToMany: 定义多对多关系。
     *   对于多对多关系，通常由其中一方作为“关系维护端”（即主导生成中间表的一方），
     *   另一方作为“被维护端”（使用 mappedBy 属性）。
     *   这里我们让 Student 作为关系的维护端。
     *
     * @JoinTable: 用于配置多对多关系中的关联表（中间表）。
     *   name = "student_course": 指定中间表的表名为 student_course。
     *   joinColumns: 指定中间表中关联当前实体（Student）的外键列名。
     *   inverseJoinColumns: 指定中间表中关联另一端实体（Course）的外键列名。
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private Set<Course> courses = new HashSet<>();

    /**
     * 便捷方法：添加课程，并同时维护双向关系
     */
    public void addCourse(Course course) {
        if (course != null) {
            courses.add(course);
            course.getStudents().add(this);
        }
    }

    /**
     * 便捷方法：移除课程，并同时维护双向关系
     */
    public void removeCourse(Course course) {
        if (course != null) {
            courses.remove(course);
            course.getStudents().remove(this);
        }
    }
}