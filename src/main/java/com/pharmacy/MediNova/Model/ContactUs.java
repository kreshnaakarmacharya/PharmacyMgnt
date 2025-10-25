package com.pharmacy.MediNova.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="contact_us")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContactUs {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @Column(name="name")
    private String name;

    @Column(name="email")
    private String email;

    @Column(name="phone")
    private String phone;

    @Column(name="message")
    private String message;
}
