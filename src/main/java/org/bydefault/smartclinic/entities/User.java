package org.bydefault.smartclinic.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String password;

    private Boolean isVerified = false;

    @Column(unique = true)
    private String code;

    @Column(name = "code_expires_at")
    private LocalDateTime codeExpiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @OneToMany(mappedBy = "patient")
    private Set<Appointment> appointments;

    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;


    public boolean activateUser(String code) {
       if (this.code.equals(code)) {
           this.isVerified = true;
           return true;
       }
       return false;
    }


    public String getFullName() {
        return firstName + " " + lastName;
    }


}
