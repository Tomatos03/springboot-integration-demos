package org.demo.springjpademo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 课程实体类 (多对多关系示例的被维护端)
 *
 * @author : Tomatos
 */
@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 课程名称
     */
    @Column(nullable = false, length = 128)
    private String title;

    /**
     * @ManyToMany(mappedBy = "courses"): 
     *   表示当前实体是关系的“被维护端”。
     *   "courses" 指的是 Student 实体类中维护这层关系的属性名称。
     *   这告诉 JPA 不要为 Course 创建中间表，中间表的外键维护工作交由 Student 端的 @JoinTable 负责。
     */
    @ManyToMany(mappedBy = "courses")
    @Builder.Default
    private Set<Student> students = new HashSet<>();
}